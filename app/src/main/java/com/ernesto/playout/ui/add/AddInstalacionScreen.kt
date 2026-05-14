package com.ernesto.playout.ui.add

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.ernesto.playout.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@DrawableRes
private fun categoryDrawable(name: String): Int = when (name) {
    "Futbol" -> R.drawable.futbol
    "Baloncesto" -> R.drawable.baloncesto
    "Pingpong" -> R.drawable.pingpong
    "Volleyball" -> R.drawable.volleyball
    "Ajedrez" -> R.drawable.ajedrez
    "Esgrima" -> R.drawable.esgrima
    "Patinaje" -> R.drawable.skate
    "Calistenia" -> R.drawable.calistenia
    "Atletismo" -> R.drawable.atletismo
    "Minigolf" -> R.drawable.minigolf
    "Petanca" -> R.drawable.petanca
    else -> R.drawable.otro
}

private val categories = listOf(
    "Futbol", "Baloncesto", "Pingpong", "Volleyball",
    "Ajedrez", "Esgrima", "Patinaje", "Calistenia",
    "Atletismo", "Minigolf", "Petanca", "Otro"
)

@Composable
fun AddInstalacionScreen(
    onBack: () -> Unit,
    viewModel: AddInstalacionViewModel = hiltViewModel()
) {
    val categoria by viewModel.categoria.collectAsStateWithLifecycle()
    val descripcion by viewModel.descripcion.collectAsStateWithLifecycle()
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val agua by viewModel.agua.collectAsStateWithLifecycle()
    val asientos by viewModel.asientos.collectAsStateWithLifecycle()
    val experiencia by viewModel.experiencia.collectAsStateWithLifecycle()
    val photoPaths by viewModel.photoPaths.collectAsStateWithLifecycle()
    val pinLatLng by viewModel.pinLatLng.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val saveSuccess by viewModel.saveSuccess.collectAsStateWithLifecycle()
    val validationError by viewModel.validationError.collectAsStateWithLifecycle()
    val currentLocation by viewModel.location.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) onBack()
    }

    LaunchedEffect(validationError) {
        validationError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.validationError.value = null
        }
    }

    var currentPhotoIndex by remember { mutableStateOf(0) }
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.savePhoto(currentPhotoIndex, it) }
    }

    var showMapPicker by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2C332D))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFFF5F5F5)
                    )
                }
                Text(
                    "Nueva Instalación",
                    color = Color(0xFFF5F5F5),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Category
                Text(
                    "Categoría",
                    color = Color(0xFFF5F5F5),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { name ->
                        val selected = categoria == name
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.clickable { viewModel.categoria.value = name }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (selected) Color(0xFF4CAF50) else Color(0xFF1C2230)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(categoryDrawable(name)),
                                    contentDescription = name,
                                    modifier = Modifier.size(36.dp),
                                    colorFilter = ColorFilter.tint(Color.White)
                                )
                            }
                            Text(name, color = Color(0xFFF5F5F5), fontSize = 10.sp)
                        }
                    }
                }

                // Photos
                Text("Fotos", color = Color(0xFFF5F5F5), fontSize = 14.sp)
                Text(
                    "Añade hasta 4 fotos. La primera es la principal.",
                    color = Color(0xFF8B949E),
                    fontSize = 12.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    photoPaths.forEachIndexed { index, path ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(80.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1C2230))
                                .clickable {
                                    currentPhotoIndex = index
                                    photoLauncher.launch("image/*")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (path != null) {
                                AsyncImage(
                                    model = java.io.File(path),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .clickable {
                                            val current = photoPaths.toMutableList()
                                            current[index] = null
                                            viewModel.photoPaths.value = current
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Eliminar foto",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            } else {
                                Icon(
                                    Icons.Default.AddAPhoto,
                                    contentDescription = "Añadir foto",
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }

                // Description
                Text("Descripción", color = Color(0xFFF5F5F5), fontSize = 14.sp)
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { viewModel.descripcion.value = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4CAF50),
                        unfocusedBorderColor = Color(0xFF8B949E),
                        focusedTextColor = Color(0xFFF5F5F5),
                        unfocusedTextColor = Color(0xFFF5F5F5),
                        cursorColor = Color(0xFF4CAF50)
                    ),
                    placeholder = {
                        Text("Describe la instalación...", color = Color(0xFF8B949E))
                    }
                )

                // Estado
                Text("Estado", color = Color(0xFFF5F5F5), fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        Triple(1, "Bueno", Color(0xFF4CAF50)),
                        Triple(2, "Desgastado", Color(0xFFFFC107)),
                        Triple(3, "Roto", Color(0xFFF44336))
                    ).forEach { (value, label, color) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (estado == value) color else Color(0xFF1C2230))
                                .clickable { viewModel.estado.value = value }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(label, color = Color.White)
                        }
                    }
                }

                // Experiencia
                Text("Experiencia de uso", color = Color(0xFFF5F5F5), fontSize = 14.sp)
                Row {
                    repeat(5) { index ->
                        Text(
                            text = if (index < experiencia) "★" else "☆",
                            color = Color(0xFFFFC107),
                            fontSize = 32.sp,
                            modifier = Modifier.clickable {
                                viewModel.experiencia.value = index + 1
                            }
                        )
                    }
                }

                // Amenidades
                Text("Amenidades", color = Color(0xFFF5F5F5), fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { viewModel.agua.value = !agua }
                    ) {
                        Image(
                            painter = painterResource(R.drawable.gota),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            colorFilter = ColorFilter.tint(
                                if (agua) Color(0xFF1E88E5) else Color(0xFF8B949E)
                            )
                        )
                        Text(
                            "Agua",
                            color = if (agua) Color(0xFF1E88E5) else Color(0xFF8B949E),
                            fontSize = 12.sp
                        )
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { viewModel.asientos.value = !asientos }
                    ) {
                        Image(
                            painter = painterResource(R.drawable.banco),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            colorFilter = ColorFilter.tint(
                                if (asientos) Color(0xFF8D6E63) else Color(0xFF8B949E)
                            )
                        )
                        Text(
                            "Asientos",
                            color = if (asientos) Color(0xFF8D6E63) else Color(0xFF8B949E),
                            fontSize = 12.sp
                        )
                    }
                }

                // Location
                Text("Ubicación", color = Color(0xFFF5F5F5), fontSize = 14.sp)
                Button(
                    onClick = { showMapPicker = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (pinLatLng != null) Color(0xFF4CAF50) else Color(0xFF1C2230)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Place, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        if (pinLatLng != null) "Ubicación seleccionada ✓" else "Seleccionar ubicación",
                        color = Color.White
                    )
                }
                if (pinLatLng != null) {
                    Text(
                        "${pinLatLng!!.latitude}, ${pinLatLng!!.longitude}",
                        color = Color(0xFF8B949E),
                        fontSize = 12.sp
                    )
                }

                // Save button
                Button(
                    onClick = { viewModel.save() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            "Guardar instalación",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        if (showMapPicker) {
            MapPickerScreen(
                initialPosition = pinLatLng ?: LatLng(50.7753, 6.0839),
                currentLocation = currentLocation,
                onConfirm = { latLng ->
                    viewModel.pinLatLng.value = latLng
                    showMapPicker = false
                },
                onDismiss = { showMapPicker = false }
            )
        }
    }
}

@Composable
private fun MapPickerScreen(
    initialPosition: LatLng,
    currentLocation: android.location.Location?,
    onConfirm: (LatLng) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val scope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(initialPosition, 15f)
        }

        val locationPermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                hasLocationPermission = true
                currentLocation?.let { loc ->
                    scope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(loc.latitude, loc.longitude), 16f
                            )
                        )
                    }
                }
            }
        }

        Box(Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
                uiSettings = MapUiSettings(myLocationButtonEnabled = false)
            )

            Icon(
                Icons.Default.Place,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.Center)
                    .offset(y = (-24).dp)
            )

            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2C332D))
                    .statusBarsPadding()
                    .padding(16.dp)
            ) {
                Text(
                    "Mueve el mapa para posicionar el pin",
                    color = Color(0xFFF5F5F5)
                )
            }

            FloatingActionButton(
                onClick = {
                    if (hasLocationPermission) {
                        currentLocation?.let { loc ->
                            scope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(loc.latitude, loc.longitude), 16f
                                    )
                                )
                            }
                        }
                    } else {
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 96.dp),
                containerColor = Color(0xFFF5F5F5),
                contentColor = Color(0xFF2C332D)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Mi ubicación")
            }

            Button(
                onClick = {
                    val center = cameraPositionState.position.target
                    onConfirm(center)
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("Confirmar ubicación", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
