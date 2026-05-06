package com.ernesto.playout.ui.detail

import android.location.Location
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ernesto.playout.data.location.LocationDataSource
import com.ernesto.playout.data.model.Instalacion
import com.ernesto.playout.data.repository.InstalacionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: InstalacionRepository,
    private val locationDataSource: LocationDataSource
) : ViewModel() {

    private val fid: Int = checkNotNull(savedStateHandle["fid"])

    private val _instalacion = MutableStateFlow<Instalacion?>(null)
    val instalacion: StateFlow<Instalacion?> = _instalacion.asStateFlow()

    private val _userLocation = MutableStateFlow<Location?>(null)
    val userLocation: StateFlow<Location?> = _userLocation.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getInstalacionById(fid).collect { _instalacion.value = it }
        }
        viewModelScope.launch {
            locationDataSource.getCurrentLocation().collect { _userLocation.value = it }
        }
    }
}
