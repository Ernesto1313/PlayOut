package com.ernesto.playout.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ernesto.playout.data.model.Instalacion
import com.ernesto.playout.data.repository.InstalacionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val repository: InstalacionRepository
) : ViewModel() {

    private val instalaciones: StateFlow<List<Instalacion>> =
        repository.getAllInstalaciones()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    fun setCategory(cat: String?) {
        _selectedCategory.value = cat
    }

    val filteredInstalaciones: StateFlow<List<Instalacion>> =
        combine(instalaciones, _selectedCategory) { list, cat ->
            if (cat == null) list else list.filter { it.categoria == cat }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
