package com.ernesto.playout.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ernesto.playout.data.model.InstalacionCustom
import com.ernesto.playout.data.repository.InstalacionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: InstalacionRepository
) : ViewModel() {

    val customInstalaciones: StateFlow<List<InstalacionCustom>> =
        repository.getAllCustomInstalaciones()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun delete(instalacion: InstalacionCustom) {
        viewModelScope.launch {
            repository.deleteCustom(instalacion)
        }
    }
}
