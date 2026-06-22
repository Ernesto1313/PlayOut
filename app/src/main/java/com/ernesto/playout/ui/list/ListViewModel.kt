package com.ernesto.playout.ui.list

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ernesto.playout.data.location.LocationDataSource
import com.ernesto.playout.data.model.Instalacion
import com.ernesto.playout.data.repository.InstalacionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ListUiState {
    object Loading : ListUiState()
    data class Success(val instalaciones: List<Instalacion>) : ListUiState()
    data class Error(val message: String) : ListUiState()
}

@HiltViewModel
class ListViewModel @Inject constructor(
    private val repository: InstalacionRepository,
    private val locationDataSource: LocationDataSource
) : ViewModel() {

    val uiState: StateFlow<ListUiState> = repository.getAllInstalaciones()
        .map<List<Instalacion>, ListUiState> { ListUiState.Success(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ListUiState.Loading)

    val userLocation = MutableStateFlow<Location?>(null)

    init {
        viewModelScope.launch {
            locationDataSource.getCurrentLocation().collect { loc ->
                if (loc != null) userLocation.value = loc
            }
        }
    }

    fun distanceTo(instalacion: Instalacion): Float? {
        val loc = userLocation.value ?: return null
        val lat = instalacion.latitud ?: return null
        val lon = instalacion.longitud ?: return null
        val result = FloatArray(1)
        Location.distanceBetween(loc.latitude, loc.longitude, lat, lon, result)
        return result[0]
    }
}
