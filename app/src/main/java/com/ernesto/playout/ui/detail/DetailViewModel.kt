package com.ernesto.playout.ui.detail

import android.content.Context
import android.location.Location
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ernesto.playout.data.location.LocationDataSource
import com.ernesto.playout.data.model.Facility
import com.ernesto.playout.data.model.Review
import com.ernesto.playout.data.remote.AuthDataSource
import com.ernesto.playout.data.remote.FirebaseStorageDataSource
import com.ernesto.playout.data.remote.FirestoreDataSource
import com.ernesto.playout.data.remote.ReviewsDataSource
import com.ernesto.playout.data.repository.FacilityRepository
import com.ernesto.playout.data.util.RatingCalculator
import com.ernesto.playout.data.util.RatingCalculator.RatingResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: FacilityRepository,
    private val locationDataSource: LocationDataSource,
    private val reviewsDataSource: ReviewsDataSource,
    private val authDataSource: AuthDataSource,
    private val firestoreDataSource: FirestoreDataSource,
    private val storageDataSource: FirebaseStorageDataSource,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val fid: Int = checkNotNull(savedStateHandle["fid"])

    private val _facility = MutableStateFlow<Facility?>(null)
    val facility: StateFlow<Facility?> = _facility.asStateFlow()

    private val _userLocation = MutableStateFlow<Location?>(null)
    val userLocation: StateFlow<Location?> = _userLocation.asStateFlow()

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    private val _rating = MutableStateFlow(RatingResult(0.0, 0, 0))
    val rating: StateFlow<RatingResult> = _rating.asStateFlow()

    private val _canReview = MutableStateFlow(false)
    val canReview: StateFlow<Boolean> = _canReview.asStateFlow()

    private val _userExistingReview = MutableStateFlow<Review?>(null)
    val userExistingReview: StateFlow<Review?> = _userExistingReview.asStateFlow()

    val isLoggedIn: Boolean get() = authDataSource.isLoggedIn
    val isEmailVerified: Boolean get() = authDataSource.isEmailVerified
    val currentUserId: String? get() = authDataSource.currentUserId

    private var reviewsLoaded = false

    init {
        viewModelScope.launch {
            repository.getFacilityById(fid).collect {
                _facility.value = it
                if (it != null && !reviewsLoaded) {
                    reviewsLoaded = true
                    loadReviews()
                }
            }
        }
        viewModelScope.launch {
            locationDataSource.getCurrentLocation().collect { _userLocation.value = it }
        }
    }

    fun loadReviews() {
        viewModelScope.launch {
            val list = reviewsDataSource.getReviewsForFacility(fid)
            val userId = authDataSource.currentUserId
            _reviews.value = list.sortedWith(
                compareByDescending<Review> { it.userId == userId }
                    .thenByDescending { it.timestamp }
            )
            _rating.value = RatingCalculator.calculate(list)

            if (userId != null) {
                val existing = list.firstOrNull { it.userId == userId }
                _userExistingReview.value = existing
                // Can review if logged in, verified, not the proposer,
                // and hasn't reviewed yet (or can edit existing)
                val isFacilityProposedByCurrentUser = firestoreDataSource.isProposalOwner(fid, userId)
                _canReview.value = authDataSource.isEmailVerified &&
                    existing == null &&
                    !isFacilityProposedByCurrentUser
            } else {
                _canReview.value = false
            }
        }
    }

    fun submitReview(stars: Int, condition: Int, comment: String,
                      photoUrls: List<String>, onDone: () -> Unit) {
        viewModelScope.launch {
            val userId = authDataSource.currentUserId ?: return@launch
            val profile = authDataSource.getUserProfile() ?: return@launch
            val review = Review(
                facilityFid = fid,
                userId = userId,
                username = profile.username,
                stars = stars,
                condition = condition,
                comment = comment,
                photoUrls = photoUrls,
                isInitial = false
            )
            reviewsDataSource.submitReview(review)
            loadReviews()
            onDone()
        }
    }

    fun updateReview(reviewId: String, stars: Int, condition: Int,
                      comment: String, photoUrls: List<String>, onDone: () -> Unit) {
        viewModelScope.launch {
            reviewsDataSource.updateReview(reviewId, stars, condition, comment, photoUrls)
            loadReviews()
            onDone()
        }
    }

    fun deleteReview(reviewId: String, onDone: () -> Unit) {
        viewModelScope.launch {
            reviewsDataSource.deleteReview(reviewId)
            loadReviews()
            onDone()
        }
    }

    fun submitReviewWithPhotos(stars: Int, condition: Int, comment: String,
                                photoUris: List<Uri>, onDone: () -> Unit) {
        viewModelScope.launch {
            val userId = authDataSource.currentUserId ?: return@launch
            val urls = uploadPhotos(photoUris, userId)
            submitReview(stars, condition, comment, urls, onDone)
        }
    }

    fun updateReviewWithPhotos(reviewId: String, stars: Int, condition: Int, comment: String,
                                existingPhotoUrls: List<String>, newPhotoUris: List<Uri>,
                                onDone: () -> Unit) {
        viewModelScope.launch {
            val userId = authDataSource.currentUserId ?: return@launch
            val newUrls = uploadPhotos(newPhotoUris, userId)
            updateReview(reviewId, stars, condition, comment, existingPhotoUrls + newUrls, onDone)
        }
    }

    private suspend fun uploadPhotos(uris: List<Uri>, userId: String): List<String> {
        val urls = mutableListOf<String>()
        val dir = java.io.File(context.filesDir, "review_photos")
        dir.mkdirs()
        uris.forEachIndexed { index, uri ->
            val tempFile = java.io.File(dir, "temp_${System.currentTimeMillis()}_$index.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output -> input.copyTo(output) }
            }
            val url = storageDataSource.uploadReviewPhoto(
                tempFile.absolutePath, "${userId}_$fid", "photo${index + 1}"
            )
            if (url.isNotEmpty()) urls.add(url)
            tempFile.delete()
        }
        return urls
    }
}
