package com.ernesto.playout.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ernesto.playout.data.model.Instalacion
import com.ernesto.playout.data.repository.InstalacionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val repository: InstalacionRepository
) : ViewModel() {
    val instalaciones: StateFlow<List<Instalacion>> =
        repository.getAllInstalaciones()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
