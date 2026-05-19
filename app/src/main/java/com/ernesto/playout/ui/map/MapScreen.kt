package com.ernesto.playout.ui.map

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Paint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.ernesto.playout.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@DrawableRes
private fun categoryDrawable(categoria: String?): Int = when (categoria) {
    "Ajedrez", "Chess" -> R.drawable.chess
    "Pingpong", "Ping-Pong" -> R.drawable.pingpong
    "Fútbol", "Futbol", "Football" -> R.drawable.football
    "Esgrima", "Fencing" -> R.drawable.fencing
    "Patinaje", "Skating" -> R.drawable.skate
    "Volleyball" -> R.drawable.volleyball
    "Baloncesto", "Basketball" -> R.drawable.basketball
    "Calistenia", "Calisthenics" -> R.drawable.calisthenics
    "Atletismo", "Athletics" -> R.drawable.atletism
    "Minigolf", "Mini Golf" -> R.drawable.minigolf
    "Petanca", "Pétanque" -> R.drawable.petanque
    else -> R.drawable.other
}

@DrawableRes
private fun categoryDrawableWhite(categoria: String?): Int = when (categoria) {
    "Ajedrez", "Chess" -> R.drawable.chess_white
    "Pingpong", "Ping-Pong" -> R.drawable.pingpong_white
    "Fútbol", "Futbol", "Football" -> R.drawable.football_white
    "Esgrima", "Fencing" -> R.drawable.fencing_white
    "Patinaje", "Skating" -> R.drawable.skate_white
    "Volleyball" -> R.drawable.volleyball_white
    "Baloncesto", "Basketball" -> R.drawable.basketball_white
    "Calistenia", "Calisthenics" -> R.drawable.calisthenics_white
    "Atletismo", "Athletics" -> R.drawable.atletism_white
    "Minigolf", "Mini Golf" -> R.drawable.minigolf_white
    "Petanca", "Pétanque" -> R.drawable.petanque_white
    else -> R.drawable.other_white
}

@Composable
private fun rememberCategoryMarkerBitmap(context: Context, @DrawableRes iconRes: Int): BitmapDescriptor {
    return remember(iconRes) {
        val size = 120
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val paint = Paint().apply { isAntiAlias = true }

        paint.color = android.graphics.Color.parseColor("#00AEFF")
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 2f, paint)

        paint.style = Paint.Style.STROKE
        paint.color = android.graphics.Color.parseColor("#F5F5F5")
        paint.strokeWidth = 4f
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 2f, paint)

        val drawable = ContextCompat.getDrawable(context, iconRes)!!
        val iconSize = size / 2
        val offset = (size - iconSize) / 2
        drawable.setBounds(offset, offset, offset + iconSize, offset + iconSize)
        drawable.draw(canvas)

        BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}

// Display name → ViewModel category name (used for filtering)
private val categoryDisplayToKey = mapOf(
    "Football" to "Football",
    "Basketball" to "Basketball",
    "Ping-Pong" to "Ping-Pong",
    "Volleyball" to "Volleyball",
    "Chess" to "Chess",
    "Fencing" to "Fencing",
    "Skating" to "Skating",
    "Calisthenics" to "Calisthenics",
    "Athletics" to "Athletics",
    "Mini Golf" to "Mini Golf",
    "Pétanque" to "Pétanque",
    "Other" to "Other"
)

private val categories = listOf(
    "Football", "Basketball", "Ping-Pong", "Volleyball",
    "Chess", "Fencing", "Skating", "Calisthenics",
    "Athletics", "Mini Golf", "Pétanque", "Other"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onInstalacionClick: (Int) -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: MapViewModel = hiltViewModel()
) {
    val filteredInstalaciones by viewModel.filteredInstalaciones.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var showLocationSettingsDialog by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(50.7753, 6.0839), 13f)
    }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasLocationPermission = granted
        if (granted) {
            userLocation?.let { loc ->
                scope.launch {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(loc.latitude, loc.longitude), 15f
                        )
                    )
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
            uiSettings = MapUiSettings(myLocationButtonEnabled = false)
        ) {
            filteredInstalaciones.forEach { instalacion ->
                val lat = instalacion.latitud
                val lng = instalacion.longitud
                if (lat != null && lng != null) {
                    val iconRes = categoryDrawableWhite(
                        instalacion.categoria?.replaceFirstChar { it.uppercase() }
                    )
                    val markerIcon = rememberCategoryMarkerBitmap(context, iconRes)
                    Marker(
                        state = MarkerState(position = LatLng(lat, lng)),
                        icon = markerIcon,
                        onClick = { _ ->
                            onInstalacionClick(instalacion.fid)
                            true
                        }
                    )
                }
            }
        }

        // Locate-me FAB — bottom-start
        FloatingActionButton(
            onClick = {
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE)
                    as android.location.LocationManager
                val isLocationEnabled = locationManager.isProviderEnabled(
                    android.location.LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(
                    android.location.LocationManager.NETWORK_PROVIDER)
                if (!isLocationEnabled) {
                    showLocationSettingsDialog = true
                } else if (hasLocationPermission) {
                    if (userLocation != null) {
                        scope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(userLocation!!.latitude, userLocation!!.longitude), 15f
                                )
                            )
                        }
                    } else {
                        try {
                            val fusedClient = com.google.android.gms.location
                                .LocationServices.getFusedLocationProviderClient(context)
                            fusedClient.lastLocation.addOnSuccessListener { location ->
                                location?.let {
                                    scope.launch {
                                        cameraPositionState.animate(
                                            CameraUpdateFactory.newLatLngZoom(
                                                LatLng(it.latitude, it.longitude), 15f
                                            )
                                        )
                                    }
                                }
                            }
                        } catch (e: SecurityException) {
                            // Permission was revoked
                        }
                    }
                } else {
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(
                    start = 16.dp,
                    bottom = contentPadding.calculateBottomPadding() + 16.dp
                ),
            containerColor = Color(0xFF00AEFF),
            contentColor = Color(0xFFF5F5F5)
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "My location")
        }

        // Filter FAB — bottom-end
        FloatingActionButton(
            onClick = { showFilterSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = 16.dp,
                    bottom = contentPadding.calculateBottomPadding() + 16.dp
                ),
            containerColor = Color(0xFF00AEFF),
            contentColor = Color.White
        ) {
            Icon(Icons.Default.FilterList, contentDescription = "Filter")
        }
    }

    if (showLocationSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showLocationSettingsDialog = false },
            containerColor = Color(0xFF2C332D),
            title = {
                Text("Location disabled", color = Color(0xFFF5F5F5),
                    fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Location is disabled on your device. Would you like to enable it?",
                    color = Color(0xFFF5F5F5))
            },
            confirmButton = {
                TextButton(onClick = {
                    showLocationSettingsDialog = false
                    context.startActivity(
                        Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }) {
                    Text("Open Settings", color = Color(0xFF00AEFF))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLocationSettingsDialog = false }) {
                    Text("Cancel", color = Color(0xFF8B949E))
                }
            }
        )
    }

    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = sheetState,
            containerColor = Color(0xFF806B40)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Filter by sport",
                    color = Color(0xFFF5F5F5),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.size(8.dp))

                // Todos option
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedCategory = null
                            viewModel.setCategory(null)
                            showFilterSheet = false
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = null,
                        tint = Color(0xFF00AEFF),
                        modifier = Modifier.size(28.dp)
                    )
                    Text("All", color = Color(0xFFF5F5F5), fontSize = 16.sp)
                    if (selectedCategory == null) {
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF00AEFF))
                    }
                }

                HorizontalDivider(color = Color(0xFF00AEFF).copy(alpha = 0.3f))

                categories.forEach { cat ->
                    val iconRes = categoryDrawableWhite(cat)
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedCategory = cat
                                viewModel.setCategory(categoryDisplayToKey[cat] ?: cat)
                                showFilterSheet = false
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Image(
                            painterResource(iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            colorFilter = ColorFilter.tint(Color(0xFFF5F5F5))
                        )
                        Text(cat, color = Color(0xFFF5F5F5), fontSize = 16.sp)
                        if (selectedCategory == cat) {
                            Spacer(Modifier.weight(1f))
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF00AEFF))
                        }
                    }
                    HorizontalDivider(color = Color(0xFF00AEFF).copy(alpha = 0.2f))
                }
            }
        }
    }
}
