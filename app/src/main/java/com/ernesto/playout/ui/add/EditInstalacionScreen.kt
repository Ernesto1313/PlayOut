package com.ernesto.playout.ui.add

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.ernesto.playout.R
import com.google.android.gms.maps.model.LatLng

private val editCategories = listOf(
    "Football", "Basketball", "Ping-Pong", "Volleyball",
    "Chess", "Fencing", "Skating", "Calisthenics",
    "Athletics", "Mini Golf", "Pétanque", "Other"
)

@Composable
fun EditInstalacionScreen(
    fid: Int,
    onBack: () -> Unit,
    viewModel: EditInstalacionViewModel = hiltViewModel()
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

    val context = LocalContext.current
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

    var activeSlotIndex by remember { mutableStateOf(0) }
    var showPhotoSourceDialog by remember { mutableStateOf(false) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { viewModel.savePhoto(activeSlotIndex, it) } }
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraUri?.let { viewModel.savePhoto(activeSlotIndex, it) }
        }
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createCameraUri(context)
            cameraUri = uri
            cameraLauncher.launch(uri)
        }
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
                    "Edit Location",
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
                    "Category",
                    color = Color(0xFFF5F5F5),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(editCategories) { name ->
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
                Text("Photos", color = Color(0xFFF5F5F5), fontSize = 14.sp)
                Text(
                    "Add up to 4 photos. The first one is the main photo.",
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
                                    activeSlotIndex = index
                                    showPhotoSourceDialog = true
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
                                        contentDescription = "Remove photo",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            } else {
                                Icon(
                                    Icons.Default.AddAPhoto,
                                    contentDescription = "Add photo",
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }

                // Description
                Text("Description", color = Color(0xFFF5F5F5), fontSize = 14.sp)
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
                        Text("Describe the facility...", color = Color(0xFF8B949E))
                    }
                )

                // Condition
                Text("Condition", color = Color(0xFFF5F5F5), fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        Triple(1, "Good", Color(0xFF4CAF50)),
                        Triple(2, "Fair", Color(0xFFFFC107)),
                        Triple(3, "Broken", Color(0xFFF44336))
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

                // Experience
                Text("Experience", color = Color(0xFFF5F5F5), fontSize = 14.sp)
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

                // Amenities
                Text("Amenities", color = Color(0xFFF5F5F5), fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { viewModel.agua.value = !agua }
                    ) {
                        Image(
                            painter = painterResource(R.drawable.drop),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            colorFilter = ColorFilter.tint(
                                if (agua) Color(0xFF1E88E5) else Color(0xFF8B949E)
                            )
                        )
                        Text(
                            "Water",
                            color = if (agua) Color(0xFF1E88E5) else Color(0xFF8B949E),
                            fontSize = 12.sp
                        )
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { viewModel.asientos.value = !asientos }
                    ) {
                        Image(
                            painter = painterResource(R.drawable.bench),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            colorFilter = ColorFilter.tint(
                                if (asientos) Color(0xFF8D6E63) else Color(0xFF8B949E)
                            )
                        )
                        Text(
                            "Seats",
                            color = if (asientos) Color(0xFF8D6E63) else Color(0xFF8B949E),
                            fontSize = 12.sp
                        )
                    }
                }

                // Location
                Text("Location", color = Color(0xFFF5F5F5), fontSize = 14.sp)
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
                        if (pinLatLng != null) "Location selected ✓" else "Select location",
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
                            "Save location",
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

        if (showPhotoSourceDialog) {
            AlertDialog(
                onDismissRequest = { showPhotoSourceDialog = false },
                containerColor = Color(0xFF2C332D),
                title = { Text("Add photo", color = Color(0xFFF5F5F5)) },
                text = {
                    Column {
                        TextButton(onClick = {
                            showPhotoSourceDialog = false
                            val hasCameraPermission = ContextCompat.checkSelfPermission(
                                context, Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasCameraPermission) {
                                val uri = createCameraUri(context)
                                cameraUri = uri
                                cameraLauncher.launch(uri)
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }) {
                            Icon(Icons.Default.CameraAlt, null, tint = Color(0xFF4CAF50))
                            Spacer(Modifier.width(8.dp))
                            Text("Camera", color = Color(0xFFF5F5F5))
                        }
                        TextButton(onClick = {
                            showPhotoSourceDialog = false
                            galleryLauncher.launch("image/*")
                        }) {
                            Icon(Icons.Default.PhotoLibrary, null, tint = Color(0xFF4CAF50))
                            Spacer(Modifier.width(8.dp))
                            Text("Gallery", color = Color(0xFFF5F5F5))
                        }
                    }
                },
                confirmButton = {}
            )
        }

        if (showMapPicker) {
            MapPickerScreen(
                initialPosition = pinLatLng ?: LatLng(50.7753, 6.0839),
                currentLocation = null,
                onConfirm = { latLng ->
                    viewModel.pinLatLng.value = latLng
                    showMapPicker = false
                },
                onDismiss = { showMapPicker = false }
            )
        }
    }
}
