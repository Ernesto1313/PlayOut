package com.ernesto.playout.ui.onboarding

import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PermissionScreen(onDone: () -> Unit) {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .edit().putBoolean("location_permission_requested", true).apply()
        onDone()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2C332D)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = Color(0xFFF5F5F5),
                modifier = Modifier.size(72.dp)
            )
            Text(
                text = "Ubicación",
                color = Color(0xFFF5F5F5),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "PlayOut necesita tu ubicación para mostrarte las instalaciones más cercanas.",
                color = Color(0xFFF5F5F5),
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = { launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Permitir", color = Color(0xFFF5F5F5), fontSize = 16.sp)
            }
            Text(
                text = "Ahora no",
                color = Color(0xFF8B949E),
                fontSize = 14.sp,
                modifier = Modifier.clickable {
                    context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                        .edit().putBoolean("location_permission_requested", true).apply()
                    onDone()
                }
            )
        }
    }
}
