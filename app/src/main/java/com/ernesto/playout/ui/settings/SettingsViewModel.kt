package com.ernesto.playout.ui.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ernesto.playout.data.model.CustomFacility
import com.ernesto.playout.data.remote.FirestoreDataSource
import com.ernesto.playout.data.repository.FacilityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: FacilityRepository,
    private val firestoreDataSource: FirestoreDataSource
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
}
