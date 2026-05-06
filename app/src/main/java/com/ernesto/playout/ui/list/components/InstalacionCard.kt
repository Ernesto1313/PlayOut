package com.ernesto.playout.ui.list.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.ernesto.playout.R
import com.ernesto.playout.data.model.Instalacion
import java.util.Locale

@DrawableRes
private fun categoryDrawable(categoria: String?): Int = when (categoria) {
    "Ajedrez" -> R.drawable.ajedrez
    "Pingpong" -> R.drawable.pingpong
    "Futbol" -> R.drawable.futbol
    "Esgrima" -> R.drawable.esgrima
    "Patinaje" -> R.drawable.skate
    "Volleyball" -> R.drawable.volleyball
    "Baloncesto" -> R.drawable.baloncesto
    "Calistenia" -> R.drawable.calistenia
    "Atletismo" -> R.drawable.atletismo
    "Otro" -> R.drawable.otro
    "Minigolf" -> R.drawable.minigolf
    "Petanca" -> R.drawable.petanca
    else -> R.drawable.otro
}

@Composable
fun InstalacionCard(
    instalacion: Instalacion,
    distance: Float?,
    onInstalacionClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onInstalacionClick(instalacion.fid) },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            AsyncImage(
                model = "file:///android_asset/photos/${instalacion.fid}_main.jpg",
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(100.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
            )
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Image(
                        painter = painterResource(categoryDrawable(instalacion.categoria)),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = instalacion.categoria ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF2C332D)
                    )
                }
                Row {
                    val stars = instalacion.experiencia_uso?.coerceIn(0, 5) ?: 0
                    repeat(5) { index ->
                        Text(
                            text = if (index < stars) "★" else "☆",
                            fontSize = 14.sp,
                            color = Color(0xFFFFC107)
                        )
                    }
                }
                val (estadoText, estadoColor) = when (instalacion.estado) {
                    1 -> "Bueno" to Color(0xFF4CAF50)
                    2 -> "Desgastado" to Color(0xFFFFC107)
                    3 -> "Roto" to Color(0xFFF44336)
                    else -> "Desconocido" to Color(0xFF8B949E)
                }
                Text(text = estadoText, fontSize = 12.sp, color = estadoColor)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (instalacion.agua == 1) {
                        Image(
                            painter = painterResource(R.drawable.gota),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    if (instalacion.asientos == 1) {
                        Image(
                            painter = painterResource(R.drawable.banco),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    if (distance != null) {
                        Text(
                            text = if (distance < 1000f) {
                                "${distance.toInt()}m"
                            } else {
                                String.format(Locale.getDefault(), "%.1fkm", distance / 1000f)
                            },
                            fontSize = 12.sp,
                            color = Color(0xFF8B949E)
                        )
                    }
                }
            }
        }
    }
}
