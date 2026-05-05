package com.ernesto.playout.ui.detail

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.ernesto.playout.R
import com.ernesto.playout.data.model.Instalacion

@DrawableRes
private fun categoryDrawable(categoria: String?): Int = when (categoria) {
    "Fútbol" -> R.drawable.futbol
    "Baloncesto" -> R.drawable.baloncesto
    "Volleyball" -> R.drawable.volleyball
    "Ajedrez" -> R.drawable.ajedrez
    "PingPong" -> R.drawable.pingpong
    "Minigolf" -> R.drawable.minigolf
    "Esgrima" -> R.drawable.esgrima
    "Patinaje" -> R.drawable.skate
    "Calistenia" -> R.drawable.calistenia
    "Petanca" -> R.drawable.balls
    "Atletismo" -> R.drawable.atletismo
    else -> R.drawable.otro
}

private fun categoryColor(categoria: String?): Color = when (categoria) {
    "Fútbol" -> Color(0xFF2E7D32)
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
    val midPoint = descripcion.length / 2

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
                descripcionFirstHalf = descripcion.take(midPoint),
                descripcionSecondHalf = descripcion.drop(midPoint),
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
    descripcionFirstHalf: String,
    descripcionSecondHalf: String,
    onSlideLeft: () -> Unit,
    onSlideRight: () -> Unit,
    onSwipeUp: () -> Unit,
    onBack: () -> Unit
) {
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
        // Background photo (bgColor shows while image loads)
        AsyncImage(
            model = "file:///android_asset/photos/${inst.fid}_main.jpg",
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

        // Slide content anchored to bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 40.dp)
        ) {
            when (currentSlide) {
                0 -> Column {
                    Image(
                        painter = painterResource(categoryDrawable(inst.categoria)),
                        contentDescription = null,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = inst.nombre_sitio ?: "",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                1 -> Text(
                    text = descripcionFirstHalf,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
                2 -> Text(
                    text = descripcionSecondHalf,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
                3 -> Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (inst.agua == 1) {
                        Image(
                            painter = painterResource(R.drawable.gota),
                            contentDescription = "Agua",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    if (inst.asientos == 1) {
                        Image(
                            painter = painterResource(R.drawable.banco),
                            contentDescription = "Asientos",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    EstadoText(inst.estado)
                    StarRating(value = inst.experiencia_uso, starSize = 24)
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
            AsyncImage(
                model = "file:///android_asset/photos/${inst.fid}_main.jpg",
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
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(24.dp)
            ) {
                Image(
                    painter = painterResource(categoryDrawable(inst.categoria)),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = inst.nombre_sitio ?: "",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
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
            Text(
                text = inst.nombre_sitio ?: "",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(
                    painter = painterResource(categoryDrawable(inst.categoria)),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Text(text = inst.categoria ?: "-", style = MaterialTheme.typography.titleMedium)
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
