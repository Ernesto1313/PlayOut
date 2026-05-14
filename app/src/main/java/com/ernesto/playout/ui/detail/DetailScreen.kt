package com.ernesto.playout.ui.detail

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.ernesto.playout.R
import com.ernesto.playout.data.model.Instalacion

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

private fun categoryColor(categoria: String?): Color = when (categoria) {
    "Fútbol", "Futbol" -> Color(0xFF2E7D32)
    "Baloncesto" -> Color(0xFFE65100)
    "Volleyball" -> Color(0xFF1565C0)
    "Ajedrez" -> Color(0xFF37474F)
    "PingPong" -> Color(0xFF558B2F)
    "Minigolf" -> Color(0xFF00695C)
    "Esgrima" -> Color(0xFF6A1B9A)
    "Patinaje" -> Color(0xFF00838F)
    "Calistenia" -> Color(0xFF4527A0)
    "Petanca" -> Color(0xFF4E342E)
    "Atletismo" -> Color(0xFFC62828)
    else -> Color(0xFF37474F)
}

@Composable
private fun EstadoText(estado: Int?, modifier: Modifier = Modifier) {
    when (estado) {
        1 -> Text("Bueno", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, modifier = modifier)
        2 -> Text("Desgastado", color = Color(0xFFFFC107), fontWeight = FontWeight.Bold, modifier = modifier)
        3 -> Text("Roto", color = Color(0xFFF44336), fontWeight = FontWeight.Bold, modifier = modifier)
    }
}

@Composable
fun DetailScreen(
    onBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val instalacion by viewModel.instalacion.collectAsStateWithLifecycle()
    val userLocation by viewModel.userLocation.collectAsStateWithLifecycle()
    var isImmersive by remember { mutableStateOf(true) }
    var currentSlide by remember { mutableStateOf(0) }

    val inst = instalacion
    if (inst == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val bgColor = categoryColor(inst.categoria)
    val descripcion = inst.descripcion ?: ""

    val sentences = descripcion.split(".").map { it.trim() }.filter { it.isNotEmpty() }
    val halfCount = sentences.size / 2
    val descripcionSlide1 = sentences.take(halfCount).joinToString(". ").let { if (it.isNotEmpty()) "$it." else "" }
    val descripcionSlide2 = sentences.drop(halfCount).joinToString(". ").let { if (it.isNotEmpty()) "$it." else "" }

    AnimatedContent(
        targetState = isImmersive,
        transitionSpec = {
            if (targetState) {
                (fadeIn(tween(300)) + slideInVertically(tween(300)) { it })
                    .togetherWith(fadeOut(tween(300)) + slideOutVertically(tween(300)) { it })
            } else {
                (slideInVertically(tween(300)) { it } + fadeIn(tween(300)))
                    .togetherWith(slideOutVertically(tween(300)) { -it } + fadeOut(tween(300)))
            }
        },
        label = "modeTransition"
    ) { immersive ->
        if (immersive) {
            ImmersiveMode(
                inst = inst,
                bgColor = bgColor,
                currentSlide = currentSlide,
                descripcionSlide1 = descripcionSlide1,
                descripcionSlide2 = descripcionSlide2,
                onSlideLeft = { if (currentSlide > 0) currentSlide-- },
                onSlideRight = { if (currentSlide < 3) currentSlide++ },
                onSwipeUp = { isImmersive = false },
                onBack = onBack
            )
        } else {
            ProfileMode(
                inst = inst,
                bgColor = bgColor,
                descripcion = descripcion,
                currentSlide = currentSlide,
                userLocation = userLocation,
                onSwipeDown = { isImmersive = true },
                onBack = onBack
            )
        }
    }
}

@Composable
private fun ImmersiveMode(
    inst: Instalacion,
    bgColor: Color,
    currentSlide: Int,
    descripcionSlide1: String,
    descripcionSlide2: String,
    onSlideLeft: () -> Unit,
    onSlideRight: () -> Unit,
    onSwipeUp: () -> Unit,
    onBack: () -> Unit
) {
    val photoPath = when (currentSlide) {
        0 -> "file:///android_asset/photos/${inst.fid}_main.jpg"
        1 -> "file:///android_asset/photos/${inst.fid}_extra1.jpg"
        2 -> "file:///android_asset/photos/${inst.fid}_extra2.jpg"
        else -> "file:///android_asset/photos/${inst.fid}_extra3.jpg"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .pointerInput(Unit) {
                var totalDrag = 0f
                detectVerticalDragGestures(
                    onDragStart = { totalDrag = 0f },
                    onVerticalDrag = { _, dragAmount ->
                        totalDrag += dragAmount
                        if (totalDrag < -50f) {
                            onSwipeUp()
                            totalDrag = 0f
                        }
                    }
                )
            }
    ) {
        // Background photo — switches with currentSlide
        AsyncImage(
            model = photoPath,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Tap zones — rendered before UI chrome so chrome captures clicks first
        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f).fillMaxHeight().clickable { onSlideLeft() })
            Box(modifier = Modifier.weight(1f).fillMaxHeight().clickable { onSlideRight() })
        }

        // Progress bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 8.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            if (index == currentSlide) Color.White
                            else Color.White.copy(alpha = 0.3f)
                        )
                )
            }
        }

        // Back button
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(top = 28.dp, start = 4.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        // Translucent bocadillo at bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(Color(0xFF2C332D).copy(alpha = 0.75f))
                .padding(16.dp)
        ) {
            when (currentSlide) {
                0 -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = inst.categoria ?: "",
                        color = Color(0xFFF5F5F5),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Estado: ",
                            color = Color(0xFFF5F5F5),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        EstadoText(inst.estado)
                    }
                }
                1 -> Text(
                    text = descripcionSlide1,
                    color = Color(0xFFF5F5F5),
                    style = MaterialTheme.typography.bodyMedium
                )
                2 -> Text(
                    text = descripcionSlide2,
                    color = Color(0xFFF5F5F5),
                    style = MaterialTheme.typography.bodyMedium
                )
                3 -> Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (inst.agua == 1) {
                        Image(
                            painter = painterResource(R.drawable.gota),
                            contentDescription = null,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    if (inst.asientos == 1) {
                        Image(
                            painter = painterResource(R.drawable.banco),
                            contentDescription = null,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    val stars = inst.experiencia_uso ?: 0
                    Text(
                        text = "★".repeat(stars) + "☆".repeat(5 - stars),
                        color = Color(0xFFFFC107),
                        fontSize = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileMode(
    inst: Instalacion,
    bgColor: Color,
    descripcion: String,
    currentSlide: Int,
    userLocation: android.location.Location?,
    onSwipeDown: () -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Fixed photo area — swipe down to return to immersive
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(bgColor)
                .pointerInput(Unit) {
                    var totalDrag = 0f
                    detectVerticalDragGestures(
                        onDragStart = { totalDrag = 0f },
                        onVerticalDrag = { _, dragAmount ->
                            totalDrag += dragAmount
                            if (totalDrag > 50f) {
                                onSwipeDown()
                                totalDrag = 0f
                            }
                        }
                    )
                }
        ) {
            val profilePhotoSuffix = when (currentSlide) {
                0 -> "main"
                1 -> "extra1"
                2 -> "extra2"
                3 -> "extra3"
                else -> "main"
            }
            val profilePhotoPath = "file:///android_asset/photos/${inst.fid}_$profilePhotoSuffix.jpg"
            AsyncImage(
                model = profilePhotoPath,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        }

        // Scrollable detail content
        val fid = inst.fid
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(categoryDrawable(inst.categoria)),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(Color(0xFFF5F5F5))
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = inst.categoria ?: "-",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                val context = LocalContext.current
                Image(
                    painter = painterResource(R.drawable.google_maps_icon),
                    contentDescription = "Abrir en Google Maps",
                    modifier = Modifier
                        .size(32.dp)
                        .clickable {
                            val lat = inst.latitud ?: 0.0
                            val lng = inst.longitud ?: 0.0
                            val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            intent.setPackage("com.google.android.apps.maps")
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            }
                        }
                )
            }
            HorizontalDivider()
            Text(text = descripcion, style = MaterialTheme.typography.bodyMedium)
            HorizontalDivider()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Estado: ", style = MaterialTheme.typography.bodyMedium)
                EstadoText(inst.estado)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Experiencia de uso: ", style = MaterialTheme.typography.bodyMedium)
                StarRating(value = inst.experiencia_uso, starSize = 20)
            }
            val distance = userLocation?.let { loc ->
                val results = FloatArray(1)
                android.location.Location.distanceBetween(
                    loc.latitude, loc.longitude,
                    inst.latitud ?: 0.0,
                    inst.longitud ?: 0.0,
                    results
                )
                results[0]
            }
            if (distance != null) {
                val distanceText = if (distance < 1000f) {
                    "${distance.toInt()} m"
                } else {
                    "${"%.1f".format(distance / 1000f)} km"
                }
                Text(
                    "Distancia: $distanceText",
                    color = Color(0xFF2C332D),
                    fontSize = 14.sp
                )
            }
            if (inst.agua == 1) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.gota),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Text("Agua disponible", style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (inst.asientos == 1) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.banco),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Text("Asientos disponibles", style = MaterialTheme.typography.bodyMedium)
                }
            }
            HorizontalDivider()
            // 2x2 photo gallery
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    AsyncImage(
                        model = "file:///android_asset/photos/${fid}_main.jpg",
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.weight(1f).aspectRatio(1f)
                    )
                    AsyncImage(
                        model = "file:///android_asset/photos/${fid}_extra1.jpg",
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.weight(1f).aspectRatio(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    AsyncImage(
                        model = "file:///android_asset/photos/${fid}_extra2.jpg",
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.weight(1f).aspectRatio(1f)
                    )
                    AsyncImage(
                        model = "file:///android_asset/photos/${fid}_extra3.jpg",
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.weight(1f).aspectRatio(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StarRating(value: Int?, starSize: Int) {
    val filled = value?.coerceIn(0, 5) ?: 0
    Row {
        repeat(5) { index ->
            Text(
                text = if (index < filled) "★" else "☆",
                fontSize = starSize.sp,
                color = if (index < filled) Color(0xFFFFC107) else Color.Gray
            )
        }
    }
}
