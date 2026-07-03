package com.ernesto.playout.ui.detail

import android.util.Log
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
import androidx.compose.ui.graphics.painter.ColorPainter
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
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.ernesto.playout.R
import com.ernesto.playout.data.model.Facility

private fun getPhotoModel(path: String?): Any {
    return when {
        path != null && path.startsWith("https://") -> path
        path != null && path.startsWith("/") -> java.io.File(path)
        else -> R.drawable.other
    }
}

private fun slidePath(facility: Facility, suffix: String): String? {
    return if (facility.photo?.startsWith("/") == true) {
        val dir = java.io.File(facility.photo!!).parent
        "$dir/${facility.fid}_$suffix.jpg"
    } else {
        "${facility.fid}_$suffix.jpg"
    }
}

private fun facilityPhotoUrls(facility: Facility): List<String?> {
    return facility.photoUrlsJson?.split(",")
        ?: listOf(
            facility.photo,
            slidePath(facility, "extra1"),
            slidePath(facility, "extra2"),
            slidePath(facility, "extra3")
        )
}

private fun photoModelForSlide(facility: Facility, slide: Int): Any {
    val urls = facilityPhotoUrls(facility)
    Log.d("PlayOut_Photo", "Slide $slide urls: ${facilityPhotoUrls(facility)}")
    return getPhotoModel(urls.getOrNull(slide) ?: urls.getOrNull(0))
}

@DrawableRes
private fun categoryDrawable(sport: String?): Int = when (sport) {
    "Chess" -> R.drawable.chess
    "Ping-Pong", "Pingpong", "PingPong", "ping-pong" -> R.drawable.pingpong
    "Football" -> R.drawable.football
    "Fencing" -> R.drawable.fencing
    "Skating" -> R.drawable.skate
    "Volleyball" -> R.drawable.volleyball
    "Basketball" -> R.drawable.basketball
    "Calisthenics" -> R.drawable.calisthenics
    "Athletics" -> R.drawable.atletism
    "Other", "Otro", "other" -> R.drawable.other
    "Minigolf" -> R.drawable.minigolf
    "Pétanque" -> R.drawable.petanque
    else -> R.drawable.other
}

private fun categoryColor(sport: String?): Color = when (sport) {
    "Football" -> Color(0xFF2E7D32)
    "Basketball" -> Color(0xFFE65100)
    "Volleyball" -> Color(0xFF1565C0)
    "Chess" -> Color(0xFF37474F)
    "Ping-Pong", "Pingpong", "PingPong", "ping-pong" -> Color(0xFF558B2F)
    "Minigolf" -> Color(0xFF00695C)
    "Fencing" -> Color(0xFF6A1B9A)
    "Skating" -> Color(0xFF00838F)
    "Calisthenics" -> Color(0xFF4527A0)
    "Pétanque" -> Color(0xFF4E342E)
    "Athletics" -> Color(0xFFC62828)
    else -> Color(0xFF37474F)
}

@Composable
private fun ConditionText(condition: Int?, modifier: Modifier = Modifier) {
    when (condition) {
        1 -> Text("Good", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, modifier = modifier)
        2 -> Text("Fair", color = Color(0xFFFFC107), fontWeight = FontWeight.Bold, modifier = modifier)
        3 -> Text("Broken", color = Color(0xFFF44336), fontWeight = FontWeight.Bold, modifier = modifier)
    }
}

@Composable
fun DetailScreen(
    onBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val facility by viewModel.facility.collectAsStateWithLifecycle()
    val userLocation by viewModel.userLocation.collectAsStateWithLifecycle()
    var isImmersive by remember { mutableStateOf(true) }
    var currentSlide by remember { mutableStateOf(0) }

    val inst = facility
    if (inst == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val bgColor = categoryColor(inst.sport)
    val desc = inst.description ?: ""

    val sentences = desc.split(".").map { it.trim() }.filter { it.isNotEmpty() }
    val halfCount = sentences.size / 2
    val descSlide1 = sentences.take(halfCount).joinToString(". ").let { if (it.isNotEmpty()) "$it." else "" }
    val descSlide2 = sentences.drop(halfCount).joinToString(". ").let { if (it.isNotEmpty()) "$it." else "" }

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
                descSlide1 = descSlide1,
                descSlide2 = descSlide2,
                onSlideLeft = { if (currentSlide > 0) currentSlide-- },
                onSlideRight = { if (currentSlide < 3) currentSlide++ },
                onSwipeUp = { isImmersive = false },
                onBack = onBack
            )
        } else {
            ProfileMode(
                inst = inst,
                bgColor = bgColor,
                description = desc,
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
    inst: Facility,
    bgColor: Color,
    currentSlide: Int,
    descSlide1: String,
    descSlide2: String,
    onSlideLeft: () -> Unit,
    onSlideRight: () -> Unit,
    onSwipeUp: () -> Unit,
    onBack: () -> Unit
) {
    val photoModel = photoModelForSlide(inst, currentSlide)

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
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(photoModel)
                .crossfade(300)
                .build(),
            contentDescription = null,
            placeholder = ColorPainter(Color(0xFF2C332D)),
            error = ColorPainter(Color(0xFF2C332D)),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

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
                            if (index == currentSlide) Color(0xFF00AEFF)
                            else Color(0xFFF5F5F5).copy(alpha = 0.4f)
                        )
                )
            }
        }

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

        // Bocadillo
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(Color(0xFF806B40).copy(alpha = 0.85f))
                .padding(16.dp)
        ) {
            when (currentSlide) {
                0 -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = inst.sport ?: "",
                        color = Color(0xFFF5F5F5),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Condition: ",
                            color = Color(0xFFF5F5F5),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        ConditionText(inst.condition)
                    }
                }
                1 -> Text(
                    text = descSlide1,
                    color = Color(0xFFF5F5F5),
                    style = MaterialTheme.typography.bodyMedium
                )
                2 -> Text(
                    text = descSlide2,
                    color = Color(0xFFF5F5F5),
                    style = MaterialTheme.typography.bodyMedium
                )
                3 -> Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (inst.water == 1) {
                        Box(
                            modifier = Modifier
                                .background(color = Color(0xFFF5F5F5), shape = RoundedCornerShape(8.dp))
                                .padding(6.dp)
                        ) {
                            Image(
                                painter = painterResource(R.drawable.drop),
                                contentDescription = null,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    if (inst.seats == 1) {
                        Box(
                            modifier = Modifier
                                .background(color = Color(0xFFF5F5F5), shape = RoundedCornerShape(8.dp))
                                .padding(6.dp)
                        ) {
                            Image(
                                painter = painterResource(R.drawable.bench),
                                contentDescription = null,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    val stars = inst.experience ?: 0
                    Text(
                        text = "★".repeat(stars) + "☆".repeat(5 - stars),
                        color = Color(0xFF00AEFF),
                        fontSize = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileMode(
    inst: Facility,
    bgColor: Color,
    description: String,
    currentSlide: Int,
    userLocation: android.location.Location?,
    onSwipeDown: () -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
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
                model = ImageRequest.Builder(LocalContext.current)
                    .data(photoModelForSlide(inst, currentSlide))
                    .crossfade(300)
                    .build(),
                contentDescription = null,
                placeholder = ColorPainter(Color(0xFF2C332D)),
                error = ColorPainter(Color(0xFF2C332D)),
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
                    tint = Color(0xFFF5F5F5)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF806B40))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(categoryDrawable(inst.sport)),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(Color(0xFFF5F5F5))
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = inst.sport ?: "-",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFF5F5F5),
                    modifier = Modifier.weight(1f)
                )
                val context = LocalContext.current
                Image(
                    painter = painterResource(R.drawable.google_maps_icon),
                    contentDescription = "Open in Google Maps",
                    modifier = Modifier
                        .size(32.dp)
                        .clickable {
                            val lat = inst.latitude ?: 0.0
                            val lng = inst.longitude ?: 0.0
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
            HorizontalDivider(color = Color(0xFFF5F5F5).copy(alpha = 0.3f))
            Text(text = description, color = Color(0xFFF5F5F5), style = MaterialTheme.typography.bodyMedium)
            HorizontalDivider(color = Color(0xFFF5F5F5).copy(alpha = 0.3f))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Condition: ", color = Color(0xFFF5F5F5), style = MaterialTheme.typography.bodyMedium)
                ConditionText(inst.condition)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Experience: ", color = Color(0xFFF5F5F5), style = MaterialTheme.typography.bodyMedium)
                StarRating(value = inst.experience, starSize = 20)
            }
            val distance = userLocation?.let { loc ->
                val results = FloatArray(1)
                android.location.Location.distanceBetween(
                    loc.latitude, loc.longitude,
                    inst.latitude ?: 0.0,
                    inst.longitude ?: 0.0,
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
                    "Distance: $distanceText",
                    color = Color(0xFFF5F5F5),
                    fontSize = 14.sp
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (inst.water == 1) {
                    Box(
                        modifier = Modifier
                            .background(color = Color(0xFFF5F5F5), shape = RoundedCornerShape(8.dp))
                            .padding(6.dp)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.drop),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                if (inst.seats == 1) {
                    Box(
                        modifier = Modifier
                            .background(color = Color(0xFFF5F5F5), shape = RoundedCornerShape(8.dp))
                            .padding(6.dp)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.bench),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            HorizontalDivider(color = Color(0xFFF5F5F5).copy(alpha = 0.3f))
            // 2x2 photo gallery
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(photoModelForSlide(inst, 0))
                            .crossfade(300)
                            .build(),
                        contentDescription = null,
                        placeholder = ColorPainter(Color(0xFF2C332D)),
                        error = ColorPainter(Color(0xFF2C332D)),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.weight(1f).aspectRatio(1f)
                    )
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(photoModelForSlide(inst, 1))
                            .crossfade(300)
                            .build(),
                        contentDescription = null,
                        placeholder = ColorPainter(Color(0xFF2C332D)),
                        error = ColorPainter(Color(0xFF2C332D)),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.weight(1f).aspectRatio(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(photoModelForSlide(inst, 2))
                            .crossfade(300)
                            .build(),
                        contentDescription = null,
                        placeholder = ColorPainter(Color(0xFF2C332D)),
                        error = ColorPainter(Color(0xFF2C332D)),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.weight(1f).aspectRatio(1f)
                    )
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(photoModelForSlide(inst, 3))
                            .crossfade(300)
                            .build(),
                        contentDescription = null,
                        placeholder = ColorPainter(Color(0xFF2C332D)),
                        error = ColorPainter(Color(0xFF2C332D)),
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
                color = if (index < filled) Color(0xFF00AEFF) else Color.Gray
            )
        }
    }
}
