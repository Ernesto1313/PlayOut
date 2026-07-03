package com.ernesto.playout.ui.add

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ernesto.playout.data.location.LocationDataSource
import com.ernesto.playout.data.model.CustomFacility
import com.ernesto.playout.data.model.Proposal
import com.ernesto.playout.data.remote.FirebaseStorageDataSource
import com.ernesto.playout.data.remote.FirestoreDataSource
import com.ernesto.playout.data.repository.FacilityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddFacilityViewModel @Inject constructor(
    private val repository: FacilityRepository,
    private val locationDataSource: LocationDataSource,
    private val firestoreDataSource: FirestoreDataSource,
    private val storageDataSource: FirebaseStorageDataSource,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val sport = MutableStateFlow<String?>(null)
    val description = MutableStateFlow("")
    val condition = MutableStateFlow<Int?>(null)
    val water = MutableStateFlow(false)
    val seats = MutableStateFlow(false)
    val experience = MutableStateFlow(0)
    val photoPaths = MutableStateFlow<List<String?>>(listOf(null, null, null, null))
    val location = MutableStateFlow<android.location.Location?>(null)
    val pinLatLng = MutableStateFlow<com.google.android.gms.maps.model.LatLng?>(null)

    val isSaving = MutableStateFlow(false)
    val saveSuccess = MutableStateFlow(false)
    val validationError = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            locationDataSource.getCurrentLocation().collect { loc ->
                if (loc != null && location.value == null) {
                    location.value = loc
                    pinLatLng.value = com.google.android.gms.maps.model.LatLng(
                        loc.latitude, loc.longitude
                    )
                }
            }
        }
    }

    fun savePhoto(index: Int, uri: Uri) {
        viewModelScope.launch {
            val suffix = when (index) {
                0 -> "main"; 1 -> "extra1"; 2 -> "extra2"; else -> "extra3"
            }
            val dir = java.io.File(context.filesDir, "photos")
            dir.mkdirs()
            val tempName = "custom_temp_${System.currentTimeMillis()}_$suffix.jpg"
            val file = java.io.File(dir, tempName)
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            val current = photoPaths.value.toMutableList()
            current[index] = file.absolutePath
            photoPaths.value = current
        }
    }

    fun save() {
        when {
            sport.value == null -> {
                validationError.value = "Please select a category"
                return
            }
            condition.value == null -> {
                validationError.value = "Please select a condition"
                return
            }
            experience.value == 0 -> {
                validationError.value = "Please add a rating"
                return
            }
            photoPaths.value.any { it == null } -> {
                validationError.value = "Please add all 4 photos"
                return
            }
            pinLatLng.value == null -> {
                validationError.value = "Please select a location"
                return
            }
        }

        val lat = pinLatLng.value!!.latitude
        val lng = pinLatLng.value!!.longitude

        viewModelScope.launch {
            isSaving.value = true
            val tempFacility = CustomFacility(
                sport = sport.value,
                description = description.value.ifBlank { null },
                condition = condition.value,
                water = if (water.value) 1 else 0,
                seats = if (seats.value) 1 else 0,
                experience = experience.value,
                longitude = lng,
                latitude = lat,
                photo = null
            )
            val newFid = repository.insertCustomFacility(tempFacility).toInt()

            val dir = java.io.File(context.filesDir, "photos")
            val suffixes = listOf("main", "extra1", "extra2", "extra3")
            val finalPaths = photoPaths.value.mapIndexed { index, path ->
                if (path != null) {
                    val newFile = java.io.File(dir, "${newFid}_${suffixes[index]}.jpg")
                    java.io.File(path).renameTo(newFile)
                    newFile.absolutePath
                } else null
            }

            val finalMainPath = finalPaths[0]
            if (finalMainPath != null) {
                val updated = CustomFacility(
                    fid = newFid,
                    sport = sport.value,
                    description = description.value.ifBlank { null },
                    condition = condition.value,
                    water = if (water.value) 1 else 0,
                    seats = if (seats.value) 1 else 0,
                    experience = experience.value,
                    longitude = lng,
                    latitude = lat,
                    photo = finalMainPath
                )
                repository.updateCustomFacility(updated)
            }

            viewModelScope.launch {
                try {
                    // Upload photos to Storage
                    val uploadSuffixes = listOf("main", "extra1", "extra2", "extra3")
                    val uploadedUrls = mutableListOf<String>()
                    val tempId = "temp_$newFid"

                    uploadSuffixes.forEachIndexed { index, suffix ->
                        val localPath = finalPaths.getOrNull(index)
                        if (localPath != null) {
                            val url = storageDataSource.uploadPhoto(localPath, tempId, suffix)
                            if (url.isNotEmpty()) uploadedUrls.add(url)
                        }
                    }

                    // Submit to Firestore
                    val proposal = Proposal(
                        sport = sport.value ?: "",
                        description = description.value,
                        condition = condition.value ?: 0,
                        water = if (water.value) 1 else 0,
                        seats = if (seats.value) 1 else 0,
                        experience = experience.value,
                        longitude = pinLatLng.value?.longitude ?: 0.0,
                        latitude = pinLatLng.value?.latitude ?: 0.0,
                        photoUrls = uploadedUrls,
                        status = "pending",
                        localFid = newFid
                    )
                    firestoreDataSource.submitProposal(proposal)
                    Log.d("PlayOut_Firebase", "Proposal submitted for fid $newFid")
                } catch (e: Exception) {
                    Log.e("PlayOut_Firebase", "Failed to submit proposal: ${e.message}")
                }
            }

            isSaving.value = false
            saveSuccess.value = true
        }
    }
}
