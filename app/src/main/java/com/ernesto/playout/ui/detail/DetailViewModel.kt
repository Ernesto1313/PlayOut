package com.ernesto.playout.ui.detail

import android.location.Location
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ernesto.playout.data.location.LocationDataSource
import com.ernesto.playout.data.model.Facility
import com.ernesto.playout.data.repository.FacilityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: FacilityRepository,
    private val locationDataSource: LocationDataSource
) : ViewModel() {

    private val fid: Int = checkNotNull(savedStateHandle["fid"])

    private val _facility = MutableStateFlow<Facility?>(null)
    val instalacion: StateFlow<Facility?> = _facility.asStateFlow()

    private val _userLocation = MutableStateFlow<Location?>(null)
    val userLocation: StateFlow<Location?> = _userLocation.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getFacilityById(fid).collect { _facility.value = it }
        }
        viewModelScope.launch {
            locationDataSource.getCurrentLocation().collect { _userLocation.value = it }
        }
    }
}
