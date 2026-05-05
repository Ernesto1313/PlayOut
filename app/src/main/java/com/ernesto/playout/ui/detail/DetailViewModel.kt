package com.ernesto.playout.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val repository: InstalacionRepository
) : ViewModel() {

    private val fid: Int = checkNotNull(savedStateHandle["fid"])

    private val _instalacion = MutableStateFlow<Instalacion?>(null)
    val instalacion: StateFlow<Instalacion?> = _instalacion.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getInstalacionById(fid).collect { _instalacion.value = it }
        }
    }
}
