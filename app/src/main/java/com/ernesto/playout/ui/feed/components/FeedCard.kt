package com.ernesto.playout.ui.feed.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ernesto.playout.data.model.Instalacion
import com.ernesto.playout.ui.utils.categoryDrawable

@Composable
fun FeedCard(
    instalacion: Instalacion,
    distanceMeters: Float?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Layer 1 - Main photo
            val photoModel: Any = remember(instalacion.fid, instalacion.foto) {
                val foto = instalacion.foto
                when {
                    foto != null && foto.startsWith("/") -> java.io.File(foto)
                    else -> "file:///android_asset/photos/${instalacion.fid}_main.jpg"
                }
            }
            AsyncImage(
                model = photoModel,
                contentDescription = instalacion.nombre_sitio,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                error = painterResource(categoryDrawable(instalacion.categoria))
            )

            // Layer 2 - Subtle top gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Color(0x88000000),
                            0.4f to Color.Transparent
                        )
                    )
            )

            // Layer 3 - Category logo (top-start)
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xCC000000))
                        .padding(6.dp)
                ) {
                    Image(
                        painter = painterResource(categoryDrawable(instalacion.categoria)),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        colorFilter = ColorFilter.tint(Color.White)
                    )
                }
            }

            // Layer 4 - Condition badge (top-end)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                val color = when (instalacion.estado) {
                    1 -> Color(0xFF4CAF50)
                    2 -> Color(0xFFFFC107)
                    3 -> Color(0xFFF44336)
                    else -> Color(0xFF8B949E)
                }
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }

            // Layer 5 - Stars (bottom-end)
            val stars = instalacion.experiencia_uso?.coerceIn(0, 5) ?: 0
            if (stars > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .background(
                                color = Color(0xCC000000),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        repeat(stars) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFF00AEFF),
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
