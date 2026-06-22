package com.ernesto.playout.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ernesto.playout.R

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onEditFacility: (Int) -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val customInstalaciones by viewModel.customInstalaciones.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF806B40))
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xFFF5F5F5)
            )
        }
        Text(
            text = "Settings",
            color = Color(0xFFF5F5F5),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        HorizontalDivider(
            color = Color(0xFF00AEFF),
            thickness = 1.dp,
            modifier = Modifier.padding(top = 8.dp)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFF00AEFF)
            )
            Text(text = "Version 1.0.0", color = Color(0xFFF5F5F5), fontSize = 16.sp)
        }
        HorizontalDivider(color = Color(0xFF00AEFF), thickness = 1.dp)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = null,
                tint = Color(0xFF00AEFF)
            )
            Text(text = "Aachen, Deutschland", color = Color(0xFFF5F5F5), fontSize = 16.sp)
        }
        HorizontalDivider(color = Color(0xFF00AEFF), thickness = 1.dp)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color(0xFF00AEFF)
            )
            Text(text = "Ernesto Gimeno García", color = Color(0xFFF5F5F5), fontSize = 16.sp)
        }

        HorizontalDivider(
            color = Color(0xFF00AEFF),
            thickness = 1.dp,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Text(
            "My locations",
            color = Color(0xFFF5F5F5),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (customInstalaciones.isEmpty()) {
            Text(
                "You haven't added any locations yet.",
                color = Color(0xFFF5F5F5),
                fontSize = 14.sp
            )
        } else {
            customInstalaciones.forEach { inst ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        val iconRes = when (inst.sport) {
                            "Football" -> R.drawable.football
                            "Basketball" -> R.drawable.basketball
                            "Ping-Pong", "Pingpong", "PingPong", "ping-pong" -> R.drawable.pingpong
                            "Volleyball" -> R.drawable.volleyball
                            "Chess" -> R.drawable.chess
                            "Fencing" -> R.drawable.fencing
                            "Skating" -> R.drawable.skate
                            "Calisthenics" -> R.drawable.calisthenics
                            "Athletics" -> R.drawable.atletism
                            "Minigolf" -> R.drawable.minigolf
                            "Pétanque" -> R.drawable.petanque
                            else -> R.drawable.other
                        }
                        Image(
                            painterResource(iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            colorFilter = ColorFilter.tint(Color(0xFF00AEFF))
                        )
                        Column {
                            Text(
                                inst.sport ?: "No category",
                                color = Color(0xFFF5F5F5),
                                fontSize = 15.sp
                            )
                            val conditionText = when (inst.condition) {
                                1 -> "Good"; 2 -> "Fair"; 3 -> "Broken"; else -> ""
                            }
                            if (conditionText.isNotEmpty()) {
                                Text(conditionText, color = Color(0xFF8B949E), fontSize = 12.sp)
                            }
                        }
                    }
                    IconButton(onClick = { onEditFacility(inst.fid) }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color(0xFF00AEFF)
                        )
                    }
                    IconButton(onClick = { viewModel.delete(inst) }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFF44336)
                        )
                    }
                }
                HorizontalDivider(color = Color(0xFF2C332D), thickness = 1.dp)
            }
        }
    }
}
