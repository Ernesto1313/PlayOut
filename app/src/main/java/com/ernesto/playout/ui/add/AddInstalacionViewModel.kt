package com.ernesto.playout.ui.add

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ernesto.playout.data.location.LocationDataSource
import com.ernesto.playout.data.model.InstalacionCustom
import com.ernesto.playout.data.repository.InstalacionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddInstalacionViewModel @Inject constructor(
    private val repository: InstalacionRepository,
    private val locationDataSource: LocationDataSource,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val categoria = MutableStateFlow<String?>(null)
    val descripcion = MutableStateFlow("")
    val estado = MutableStateFlow<Int?>(null)
    val agua = MutableStateFlow(false)
    val asientos = MutableStateFlow(false)
    val experiencia = MutableStateFlow(0)
    val photoPaths = MutableStateFlow<List<String?>>(listOf(null, null, null, null))
    val location = MutableStateFlow<android.location.Location?>(null)
    val pinLatLng = MutableStateFlow<com.google.android.gms.maps.model.LatLng?>(null)

    val isSaving = MutableStateFlow(false)
    val saveSuccess = MutableStateFlow(false)
    val validationError = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            locationDataSource.getCurrentLocation().collect { loc ->
                if (loc != null && location.value == null) {
                    location.value = loc
                    pinLatLng.value = com.google.android.gms.maps.model.LatLng(
                        loc.latitude, loc.longitude
                    )
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
            val tempName = "custom_temp_${System.currentTimeMillis()}_$suffix.jpg"
            val file = java.io.File(dir, tempName)
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            val current = photoPaths.value.toMutableList()
            current[index] = file.absolutePath
            photoPaths.value = current
        }
    }

    fun save() {
        when {
            categoria.value == null -> {
                validationError.value = "Selecciona una categoría"
                return
            }
            estado.value == null -> {
                validationError.value = "Selecciona el estado"
                return
            }
            experiencia.value == 0 -> {
                validationError.value = "Añade una valoración"
                return
            }
            photoPaths.value[0] == null -> {
                validationError.value = "Añade al menos la foto principal"
                return
            }
            pinLatLng.value == null -> {
                validationError.value = "Selecciona una ubicación"
                return
            }
        }

        val lat = pinLatLng.value!!.latitude
        val lng = pinLatLng.value!!.longitude

        viewModelScope.launch {
            isSaving.value = true
            val tempInst = InstalacionCustom(
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
            val newFid = repository.insertCustom(tempInst)

            val dir = java.io.File(context.filesDir, "photos")
            val suffixes = listOf("main", "extra1", "extra2", "extra3")
            photoPaths.value.forEachIndexed { index, path ->
                if (path != null) {
                    val newFile = java.io.File(dir, "${newFid}_${suffixes[index]}.jpg")
                    java.io.File(path).renameTo(newFile)
                }
            }

            isSaving.value = false
            saveSuccess.value = true
        }
    }
}
