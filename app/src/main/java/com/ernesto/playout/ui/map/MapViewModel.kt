package com.ernesto.playout.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ernesto.playout.data.location.LocationDataSource
import com.ernesto.playout.data.model.Facility
import com.ernesto.playout.data.repository.FacilityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val repository: FacilityRepository,
    private val locationDataSource: LocationDataSource
) : ViewModel() {

    private val facilities: StateFlow<List<Facility>> =
        repository.getAllFacilities()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    val userLocation = MutableStateFlow<android.location.Location?>(null)

    init {
        viewModelScope.launch {
            locationDataSource.getCurrentLocation().collect { loc ->
                if (loc != null) userLocation.value = loc
            }
        }
    }

    fun setCategory(cat: String?) {
        _selectedCategory.value = cat
    }

    val filteredInstalaciones: StateFlow<List<Facility>> =
        combine(facilities, _selectedCategory) { list, cat ->
            if (cat == null) list else list.filter { it.sport == cat }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
