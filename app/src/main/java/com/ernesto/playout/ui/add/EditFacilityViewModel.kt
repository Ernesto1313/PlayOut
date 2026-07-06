package com.ernesto.playout.ui.add

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ernesto.playout.data.model.CustomFacility
import com.ernesto.playout.data.remote.FirestoreDataSource
import com.ernesto.playout.data.repository.FacilityRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class EditFacilityViewModel @Inject constructor(
    private val repository: FacilityRepository,
    private val firestoreDataSource: FirestoreDataSource,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val fid: Int = checkNotNull(savedStateHandle["fid"])

    val sport = MutableStateFlow<String?>(null)
    val description = MutableStateFlow("")
    val condition = MutableStateFlow<Int?>(null)
    val water = MutableStateFlow(false)
    val seats = MutableStateFlow(false)
    val experience = MutableStateFlow(0)
    val photoPaths = MutableStateFlow<List<String?>>(listOf(null, null, null, null))
    val pinLatLng = MutableStateFlow<com.google.android.gms.maps.model.LatLng?>(null)
    val isSaving = MutableStateFlow(false)
    val saveSuccess = MutableStateFlow(false)
    val validationError = MutableStateFlow<String?>(null)

    private var loaded = false

    init {
        viewModelScope.launch {
            repository.getCustomFacilityById(fid).collect { inst ->
                if (inst != null && !loaded) {
                    loaded = true
                    sport.value = inst.sport
                    description.value = inst.description ?: ""
                    condition.value = inst.condition
                    water.value = inst.water == 1
                    seats.value = inst.seats == 1
                    experience.value = inst.experience ?: 0
                    pinLatLng.value = if (inst.latitude != null && inst.longitude != null)
                        com.google.android.gms.maps.model.LatLng(inst.latitude, inst.longitude)
                    else null
                    val dir = java.io.File(context.filesDir, "photos")
                    val suffixes = listOf("main", "extra1", "extra2", "extra3")
                    photoPaths.value = suffixes.map { suffix ->
                        val file = java.io.File(dir, "${fid}_$suffix.jpg")
                        if (file.exists()) file.absolutePath else null
                    }
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
            val file = java.io.File(dir, "${fid}_$suffix.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            val current = photoPaths.value.toMutableList()
            current[index] = file.absolutePath
            photoPaths.value = current
        }
    }

    fun save() {
        val lat = pinLatLng.value?.latitude
        val lng = pinLatLng.value?.longitude
        if (sport.value == null) { validationError.value = "Please select a category"; return }
        if (condition.value == null) { validationError.value = "Please select a condition"; return }
        if (experience.value == 0) { validationError.value = "Please add a rating"; return }
        if (lat == null || lng == null) { validationError.value = "Please select a location"; return }

        viewModelScope.launch {
            isSaving.value = true
            val updated = CustomFacility(
                fid = fid,
                sport = sport.value,
                description = description.value.ifBlank { null },
                condition = condition.value,
                water = if (water.value) 1 else 0,
                seats = if (seats.value) 1 else 0,
                experience = experience.value,
                longitude = lng,
                latitude = lat,
                photo = photoPaths.value[0]
            )
            repository.updateCustomFacility(updated)

            viewModelScope.launch {
                try {
                    // Find the proposal in Firestore by localFid and update it
                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    val snapshot = db.collection("proposals")
                        .whereEqualTo("localFid", fid)
                        .get()
                        .await()

                    snapshot.documents.firstOrNull()?.let { doc ->
                        doc.reference.update(mapOf(
                            "sport" to (sport.value ?: ""),
                            "description" to description.value,
                            "condition" to (condition.value ?: 1),
                            "water" to if (water.value) 1 else 0,
                            "seats" to if (seats.value) 1 else 0,
                            "experience" to experience.value,
                            "status" to "pending"
                        )).await()
                        Log.d("PlayOut_Firebase", "Proposal updated for fid $fid")
                    }
                } catch (e: Exception) {
                    Log.e("PlayOut_Firebase", "Failed to update proposal: ${e.message}")
                }
            }

            isSaving.value = false
            saveSuccess.value = true
        }
    }
}
