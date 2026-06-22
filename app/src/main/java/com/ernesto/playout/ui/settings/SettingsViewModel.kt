package com.ernesto.playout.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ernesto.playout.data.model.CustomFacility
import com.ernesto.playout.data.repository.FacilityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: FacilityRepository
) : ViewModel() {

    val customInstalaciones: StateFlow<List<CustomFacility>> =
        repository.getAllCustomFacilities()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun delete(facility: CustomFacility) {
        viewModelScope.launch {
            repository.deleteCustomFacility(facility)
        }
    }
}
