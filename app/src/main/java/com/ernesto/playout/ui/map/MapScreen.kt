package com.ernesto.playout.ui.map

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapScreen(viewModel: MapViewModel = hiltViewModel()) {
    val instalaciones by viewModel.instalaciones.collectAsState()
    val context = LocalContext.current

    val aachen = LatLng(50.7753, 6.0839)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(aachen, 13f)
    }

    val locationPermissionGranted = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = locationPermissionGranted)
    ) {
        instalaciones.forEach { instalacion ->
            val lat = instalacion.latitud
            val lng = instalacion.longitud
            if (lat != null && lng != null) {
                Marker(
                    state = MarkerState(position = LatLng(lat, lng)),
                    title = instalacion.categoria?.replaceFirstChar { it.uppercase() },
                    icon = BitmapDescriptorFactory.defaultMarker()
                )
            }
        }
    }
}
