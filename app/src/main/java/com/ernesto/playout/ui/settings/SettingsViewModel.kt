package com.ernesto.playout.ui.settings

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ernesto.playout.data.model.CustomFacility
import com.ernesto.playout.data.model.Proposal
import com.ernesto.playout.data.remote.FirebaseStorageDataSource
import com.ernesto.playout.data.remote.FirestoreDataSource
import com.ernesto.playout.data.remote.UploadStatusTracker
import com.ernesto.playout.data.repository.FacilityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: FacilityRepository,
    private val firestoreDataSource: FirestoreDataSource,
    private val storageDataSource: FirebaseStorageDataSource,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val customInstalaciones: StateFlow<List<CustomFacility>> =
        repository.getAllCustomFacilities()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _proposalStatuses = MutableStateFlow<Map<Int, String>>(emptyMap())
    val proposalStatuses: StateFlow<Map<Int, String>> = _proposalStatuses

    fun fetchProposalStatuses(localFids: List<Int>) {
        viewModelScope.launch {
            try {
                val proposals = firestoreDataSource.getUserProposals(localFids)
                _proposalStatuses.value = proposals.associate { it.localFid to it.status }
            } catch (e: Exception) {
                Log.e("PlayOut_Settings", "Failed to fetch proposal statuses: ${e.message}")
            }
        }
    }

    fun delete(facility: CustomFacility) {
        viewModelScope.launch {
            repository.deleteCustomFacility(facility)
        }
    }

    fun retryUpload(fid: Int) {
        viewModelScope.launch {
            UploadStatusTracker.setStatus(fid, "uploading")
            // Re-trigger the upload for this fid
            // Get the custom facility from Room
            val facility = repository.getCustomFacilityById(fid).first()
            if (facility == null) {
                UploadStatusTracker.setStatus(fid, "error")
                return@launch
            }
            try {
                val dir = java.io.File(context.filesDir, "photos")
                val suffixes = listOf("main", "extra1", "extra2", "extra3")
                val uploadedUrls = mutableListOf<String>()
                suffixes.forEach { suffix ->
                    val file = java.io.File(dir, "${fid}_$suffix.jpg")
                    if (file.exists()) {
                        val url = storageDataSource.uploadPhoto(
                            file.absolutePath, "temp_$fid", suffix)
                        if (url.isNotEmpty()) uploadedUrls.add(url)
                    }
                }
                val proposal = Proposal(
                    sport = facility.sport ?: "",
                    description = facility.description ?: "",
                    condition = facility.condition ?: 0,
                    water = facility.water ?: 0,
                    seats = facility.seats ?: 0,
                    experience = facility.experience ?: 0,
                    longitude = facility.longitude ?: 0.0,
                    latitude = facility.latitude ?: 0.0,
                    photoUrls = uploadedUrls,
                    status = "pending",
                    localFid = fid
                )
                firestoreDataSource.submitProposal(proposal)
                UploadStatusTracker.setStatus(fid, "pending")
            } catch (e: Exception) {
                Log.e("PlayOut_Firebase", "Retry failed: ${e.message}")
                UploadStatusTracker.setStatus(fid, "error")
            }
        }
    }
}
