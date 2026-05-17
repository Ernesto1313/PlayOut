package com.ernesto.playout.ui.add

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ernesto.playout.data.model.InstalacionCustom
import com.ernesto.playout.data.repository.InstalacionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditInstalacionViewModel @Inject constructor(
    private val repository: InstalacionRepository,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val fid: Int = checkNotNull(savedStateHandle["fid"])

    val categoria = MutableStateFlow<String?>(null)
    val descripcion = MutableStateFlow("")
    val estado = MutableStateFlow<Int?>(null)
    val agua = MutableStateFlow(false)
    val asientos = MutableStateFlow(false)
    val experiencia = MutableStateFlow(0)
    val photoPaths = MutableStateFlow<List<String?>>(listOf(null, null, null, null))
    val pinLatLng = MutableStateFlow<com.google.android.gms.maps.model.LatLng?>(null)
    val isSaving = MutableStateFlow(false)
    val saveSuccess = MutableStateFlow(false)
    val validationError = MutableStateFlow<String?>(null)

    private var loaded = false

    init {
        viewModelScope.launch {
            repository.getCustomById(fid).collect { inst ->
                if (inst != null && !loaded) {
                    loaded = true
                    categoria.value = inst.categoria
                    descripcion.value = inst.descripcion ?: ""
                    estado.value = inst.estado
                    agua.value = inst.agua == 1
                    asientos.value = inst.asientos == 1
                    experiencia.value = inst.experiencia_uso ?: 0
                    pinLatLng.value = if (inst.latitud != null && inst.longitud != null)
                        com.google.android.gms.maps.model.LatLng(inst.latitud, inst.longitud)
                    else null
                    val dir = java.io.File(context.filesDir, "photos")
                    val suffixes = listOf("main", "extra1", "extra2", "extra3")
                    photoPaths.value = suffixes.map { suffix ->
                        val file = java.io.File(dir, "${fid}_$suffix.jpg")
                        if (file.exists()) file.absolutePath else null
                    }
                }
            }
        }
    }

    fun savePhoto(index: Int, uri: Uri) {
        viewModelScope.launch {
            val suffix = when (index) {
                0 -> "main"; 1 -> "extra1"; 2 -> "extra2"; else -> "extra3"
            }
            val dir = java.io.File(context.filesDir, "photos")
            dir.mkdirs()
            val file = java.io.File(dir, "${fid}_$suffix.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            val current = photoPaths.value.toMutableList()
            current[index] = file.absolutePath
            photoPaths.value = current
        }
    }

    fun save() {
        val lat = pinLatLng.value?.latitude
        val lng = pinLatLng.value?.longitude
        if (categoria.value == null) { validationError.value = "Please select a category"; return }
        if (estado.value == null) { validationError.value = "Please select a condition"; return }
        if (experiencia.value == 0) { validationError.value = "Please add a rating"; return }
        if (lat == null || lng == null) { validationError.value = "Please select a location"; return }

        viewModelScope.launch {
            isSaving.value = true
            val updated = InstalacionCustom(
                fid = fid,
                categoria = categoria.value,
                descripcion = descripcion.value.ifBlank { null },
                estado = estado.value,
                agua = if (agua.value) 1 else 0,
                asientos = if (asientos.value) 1 else 0,
                experiencia_uso = experiencia.value,
                longitud = lng,
                latitud = lat,
                foto = photoPaths.value[0]
            )
            repository.updateCustom(updated)
            isSaving.value = false
            saveSuccess.value = true
        }
    }
}
