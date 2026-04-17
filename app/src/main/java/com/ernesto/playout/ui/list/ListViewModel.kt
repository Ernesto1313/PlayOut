package com.ernesto.playout.ui.list

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

sealed class ListUiState {
    object Loading : ListUiState()
    data class Success(val instalaciones: List<Instalacion>) : ListUiState()
    data class Error(val message: String) : ListUiState()
}

@HiltViewModel
class ListViewModel @Inject constructor(
    private val repository: InstalacionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ListUiState>(ListUiState.Loading)
    val uiState: StateFlow<ListUiState> = _uiState.asStateFlow()

    private var allInstalaciones: List<Instalacion> = emptyList()

    init {
        viewModelScope.launch {
            try {
                repository.getAllInstalaciones().collect { list ->
                    allInstalaciones = list
                    _uiState.value = ListUiState.Success(list)
                }
            } catch (e: Exception) {
                _uiState.value = ListUiState.Error(e.message ?: "Unknown error")
            }
        }
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
