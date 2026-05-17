package com.ernesto.playout.ui.list.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.ernesto.playout.R
import com.ernesto.playout.data.model.Instalacion

private fun getPhotoModel(path: String?): Any {
    if (path == null) return R.drawable.other
    return if (path.startsWith("/")) java.io.File(path)
    else "file:///android_asset/photos/$path"
}

@DrawableRes
private fun categoryDrawable(categoria: String?): Int = when (categoria) {
    "Ajedrez", "Chess" -> R.drawable.chess
    "Pingpong", "Ping-Pong" -> R.drawable.pingpong
    "Futbol", "Fútbol", "Football" -> R.drawable.football
    "Esgrima", "Fencing" -> R.drawable.fencing
    "Patinaje", "Skating" -> R.drawable.skate
    "Volleyball" -> R.drawable.volleyball
    "Baloncesto", "Basketball" -> R.drawable.basketball
    "Calistenia", "Calisthenics" -> R.drawable.calisthenics
    "Atletismo", "Athletics" -> R.drawable.atletism
    "Otro", "Other" -> R.drawable.other
    "Minigolf", "Mini Golf" -> R.drawable.minigolf
    "Petanca", "Pétanque" -> R.drawable.petanque
    else -> R.drawable.other
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
            .height(100.dp)
            .clickable { onInstalacionClick(instalacion.fid) },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
        Row {
            val photoModel: Any = remember(instalacion.fid, instalacion.foto) {
                val foto = instalacion.foto
                if (foto != null && foto.startsWith("/")) {
                    java.io.File(foto)
                } else {
                    "file:///android_asset/photos/${instalacion.fid}_main.jpg"
                }
            }
            AsyncImage(
                model = photoModel,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(80.dp)
                    .height(100.dp)
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
                        modifier = Modifier.size(20.dp),
                        colorFilter = ColorFilter.tint(Color(0xFFF5F5F5))
                    )
                    Text(
                        text = instalacion.categoria ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF2C332D)
                    )
                }
                Box(
                    modifier = Modifier
                        .background(
                            color = Color(0xFFFFC107).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
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
                }
                val (estadoText, estadoColor, estadoBg) = when (instalacion.estado) {
                    1 -> Triple("Good", Color(0xFF4CAF50), Color(0xFF4CAF50).copy(alpha = 0.15f))
                    2 -> Triple("Fair", Color(0xFFFFC107), Color(0xFFFFC107).copy(alpha = 0.15f))
                    3 -> Triple("Broken", Color(0xFFF44336), Color(0xFFF44336).copy(alpha = 0.15f))
                    else -> Triple("Unknown", Color(0xFF8B949E), Color(0xFF8B949E).copy(alpha = 0.15f))
                }
                Box(
                    modifier = Modifier
                        .background(color = estadoBg, shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(text = estadoText, fontSize = 12.sp, color = estadoColor)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (instalacion.agua == 1) {
                        Image(
                            painter = painterResource(R.drawable.drop),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    if (instalacion.asientos == 1) {
                        Image(
                            painter = painterResource(R.drawable.bench),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
        if (distance != null) {
            val distanceText = if (distance < 1000f) {
                "${distance.toInt()} m"
            } else {
                "${"%.1f".format(distance / 1000f)} km"
            }
            Text(
                text = distanceText,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 6.dp, bottom = 4.dp),
                color = Color(0xFF8B949E),
                fontSize = 11.sp
            )
        }
        }
    }
}
