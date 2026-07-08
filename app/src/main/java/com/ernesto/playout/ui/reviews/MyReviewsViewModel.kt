package com.ernesto.playout.ui.reviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ernesto.playout.data.model.Facility
import com.ernesto.playout.data.model.Review
import com.ernesto.playout.data.remote.AuthDataSource
import com.ernesto.playout.data.remote.ReviewsDataSource
import com.ernesto.playout.data.repository.FacilityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// Combined review + facility info for display
data class ReviewWithFacility(
    val review: Review,
    val facility: Facility?
)

@HiltViewModel
class MyReviewsViewModel @Inject constructor(
    private val reviewsDataSource: ReviewsDataSource,
    private val authDataSource: AuthDataSource,
    private val repository: FacilityRepository
) : ViewModel() {

    private val _allReviews = MutableStateFlow<List<ReviewWithFacility>>(emptyList())

    private val _sportFilter = MutableStateFlow<String?>(null)
    val sportFilter: StateFlow<String?> = _sportFilter

    private val _sortMode = MutableStateFlow("date_desc")
    val sortMode: StateFlow<String> = _sortMode

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    val reviews: StateFlow<List<ReviewWithFacility>> = combine(
        _allReviews, _sportFilter, _sortMode
    ) { all, sport, sort ->
        var filtered = all
        if (sport != null) {
            filtered = filtered.filter { it.facility?.sport == sport }
        }
        filtered = when (sort) {
            "date_desc" -> filtered.sortedByDescending { it.review.timestamp }
            "date_asc" -> filtered.sortedBy { it.review.timestamp }
            "rating_desc" -> filtered.sortedByDescending { it.review.stars }
            "rating_asc" -> filtered.sortedBy { it.review.stars }
            else -> filtered
        }
        filtered
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Available sports for filter chips (only sports the user has reviewed)
    val availableSports: StateFlow<List<String>> = _allReviews.map { list ->
        list.mapNotNull { it.facility?.sport }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init { loadReviews() }

    fun loadReviews() {
        viewModelScope.launch {
            _isLoading.value = true
            val userId = authDataSource.currentUserId
            if (userId == null) {
                _allReviews.value = emptyList()
                _isLoading.value = false
                return@launch
            }
            val userReviews = reviewsDataSource.getReviewsByUser(userId)
            val allFacilities = repository.getAllFacilities().first()
            val combined = userReviews.map { review ->
                ReviewWithFacility(
                    review = review,
                    facility = allFacilities.firstOrNull { it.fid == review.facilityFid }
                )
            }
            _allReviews.value = combined
            _isLoading.value = false
        }
    }

    fun setSportFilter(sport: String?) { _sportFilter.value = sport }
    fun setSortMode(mode: String) { _sortMode.value = mode }

    fun deleteReview(reviewId: String) {
        viewModelScope.launch {
            reviewsDataSource.deleteReview(reviewId)
            loadReviews()
        }
    }
}
