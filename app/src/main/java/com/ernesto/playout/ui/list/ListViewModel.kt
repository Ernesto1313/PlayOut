package com.ernesto.playout.ui.list

import android.location.Location
import android.util.Log
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

sealed class SortOrder {
    object ByDistance : SortOrder()
    object ByRating : SortOrder()
    object ByCategory : SortOrder()
}

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

    private val _uiState = MutableStateFlow<ListUiState>(ListUiState.Loading)
    val uiState: StateFlow<ListUiState> = _uiState.asStateFlow()

    private var allInstalaciones: List<Instalacion> = emptyList()

    private val _userLocation = MutableStateFlow<Location?>(null)
    val userLocation: StateFlow<Location?> = _userLocation.asStateFlow()

    private val _sortOrder = MutableStateFlow<SortOrder>(SortOrder.ByDistance)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.getAllInstalaciones().collect { list ->
                    allInstalaciones = list
                    updateSortedList()
                }
            } catch (e: Exception) {
                _uiState.value = ListUiState.Error(e.message ?: "Unknown error")
            }
        }
        viewModelScope.launch {
            locationDataSource.getCurrentLocation().collect { location ->
                _userLocation.value = location
                updateSortedList()
            }
        }
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
        updateSortedList()
    }

    fun distanceTo(instalacion: Instalacion): Float? {
        val loc = _userLocation.value ?: return null
        val lat = instalacion.latitud?.toDouble() ?: return null
        val lon = instalacion.longitud?.toDouble() ?: return null
        Log.d("PlayOut_Distance", "Calculating distance to: lat=${instalacion.latitud} lng=${instalacion.longitud} userLat=${_userLocation.value?.latitude} userLng=${_userLocation.value?.longitude}")
        val results = FloatArray(1)
        Location.distanceBetween(loc.latitude, loc.longitude, lat, lon, results)
        return results[0]
    }

    private fun updateSortedList() {
        val sorted = when (_sortOrder.value) {
            is SortOrder.ByDistance -> allInstalaciones.sortedWith { a, b ->
                val da = distanceTo(a); val db = distanceTo(b)
                if (da == null && db == null) 0
                else if (da == null) 1 else if (db == null) -1
                else da.compareTo(db)
            }
            is SortOrder.ByRating -> allInstalaciones.sortedWith { a, b ->
                val ra = a.experiencia_uso; val rb = b.experiencia_uso
                if (ra == null && rb == null) 0
                else if (ra == null) 1 else if (rb == null) -1
                else rb.compareTo(ra)
            }
            is SortOrder.ByCategory -> allInstalaciones.sortedWith { a, b ->
                (a.categoria ?: "").compareTo(b.categoria ?: "")
            }
        }
        _uiState.value = ListUiState.Success(sorted)
    }

    fun filterByCategoria(categoria: String?) {
        val current = allInstalaciones
        _uiState.value = if (categoria.isNullOrEmpty()) {
            ListUiState.Success(current)
        } else {
            ListUiState.Success(current.filter { it.categoria == categoria })
        }
    }
}
