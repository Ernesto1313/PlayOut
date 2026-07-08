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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.ernesto.playout.R
import com.ernesto.playout.data.model.Facility
import com.ernesto.playout.data.model.Review
import com.ernesto.playout.data.util.RatingCalculator.RatingResult
import kotlinx.coroutines.launch

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

private fun formatDate(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.ENGLISH)
    return sdf.format(java.util.Date(timestamp))
}

@Composable
fun DetailScreen(
    onBack: () -> Unit,
    openReviewOnLaunch: Boolean = false,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val facility by viewModel.facility.collectAsStateWithLifecycle()
    val userLocation by viewModel.userLocation.collectAsStateWithLifecycle()
    val reviews by viewModel.reviews.collectAsStateWithLifecycle()
    val rating by viewModel.rating.collectAsStateWithLifecycle()
    val userExistingReview by viewModel.userExistingReview.collectAsStateWithLifecycle()
    var isImmersive by remember { mutableStateOf(true) }
    var currentSlide by remember { mutableStateOf(0) }
    var showReviewDialog by remember { mutableStateOf(false) }
    var editingReview by remember { mutableStateOf<Review?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(openReviewOnLaunch, userExistingReview) {
        if (openReviewOnLaunch && userExistingReview != null) {
            isImmersive = false
            editingReview = userExistingReview
            showReviewDialog = true
        }
    }

    val showRateButton = userExistingReview == null

    val onRateClick: () -> Unit = {
        when {
            !viewModel.isLoggedIn -> scope.launch {
                snackbarHostState.showSnackbar("Sign in from Settings to leave a review")
            }
            !viewModel.isEmailVerified -> scope.launch {
                snackbarHostState.showSnackbar("Please verify your email to leave a review")
            }
            else -> {
                editingReview = null
                showReviewDialog = true
            }
        }
    }

    val onEditReview: (Review) -> Unit = { review ->
        editingReview = review
        showReviewDialog = true
    }

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

    Box(modifier = Modifier.fillMaxSize()) {
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
                    onBack = onBack,
                    showRateButton = showRateButton,
                    onRateClick = onRateClick
                )
            } else {
                ProfileMode(
                    inst = inst,
                    bgColor = bgColor,
                    description = desc,
                    currentSlide = currentSlide,
                    userLocation = userLocation,
                    rating = rating,
                    reviews = reviews,
                    currentUserId = viewModel.currentUserId,
                    onSwipeDown = { isImmersive = true },
                    onBack = onBack,
                    showRateButton = showRateButton,
                    onRateClick = onRateClick,
                    onEditReview = onEditReview,
                    onDeleteReview = { reviewId -> viewModel.deleteReview(reviewId) { } }
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        if (showReviewDialog) {
            ReviewDialog(
                existingReview = editingReview,
                onDismiss = { showReviewDialog = false },
                onSubmit = { stars, condition, comment, photoUris ->
                    viewModel.submitReviewWithPhotos(stars, condition, comment, photoUris) {
                        scope.launch { snackbarHostState.showSnackbar("Review submitted, thanks!") }
                        showReviewDialog = false
                    }
                },
                onUpdate = { reviewId, stars, condition, comment, existingUrls, newUris ->
                    viewModel.updateReviewWithPhotos(
                        reviewId, stars, condition, comment, existingUrls, newUris
                    ) {
                        scope.launch { snackbarHostState.showSnackbar("Review updated!") }
                        showReviewDialog = false
                    }
                },
                onDelete = { reviewId ->
                    viewModel.deleteReview(reviewId) { showReviewDialog = false }
                }
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
    onBack: () -> Unit,
    showRateButton: Boolean,
    onRateClick: () -> Unit
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

        // Rate button + Bocadillo
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (showRateButton) {
                Button(
                    onClick = onRateClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00AEFF)),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Rate this facility", color = Color.White)
                }
            }
            Box(
                modifier = Modifier
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
}

@Composable
private fun ProfileMode(
    inst: Facility,
    bgColor: Color,
    description: String,
    currentSlide: Int,
    userLocation: android.location.Location?,
    rating: RatingResult,
    reviews: List<Review>,
    currentUserId: String?,
    onSwipeDown: () -> Unit,
    onBack: () -> Unit,
    showRateButton: Boolean,
    onRateClick: () -> Unit,
    onEditReview: (Review) -> Unit,
    onDeleteReview: (String) -> Unit
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
                if (rating.reviewCount > 0) {
                    ConditionText(rating.avgCondition)
                } else {
                    ConditionText(inst.condition)
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Experience: ", color = Color(0xFFF5F5F5), style = MaterialTheme.typography.bodyMedium)
                if (rating.reviewCount > 0) {
                    StarRating(value = rating.avgStars.let { Math.round(it).toInt() }, starSize = 20)
                    Text(
                        " (${rating.reviewCount})",
                        color = Color(0xFF8B949E),
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    StarRating(value = inst.experience, starSize = 20)
                }
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
            inst.neighbourhood?.let { n ->
                Text(
                    "Neighbourhood: $n",
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
            if (reviews.size >= 2) {
                ReviewEvolutionChart(reviews)
                HorizontalDivider(color = Color(0xFFF5F5F5).copy(alpha = 0.3f))
            }
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

            HorizontalDivider(color = Color(0xFFF5F5F5).copy(alpha = 0.3f))
            Text(
                "Reviews",
                color = Color(0xFFF5F5F5),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (reviews.isEmpty()) {
                Text(
                    "No reviews yet.",
                    color = Color(0xFFF5F5F5),
                    fontSize = 14.sp
                )
            } else {
                reviews.forEach { review ->
                    val isOwnReview = currentUserId != null && review.userId == currentUserId
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C332D)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .let {
                                if (isOwnReview) {
                                    it.border(1.dp, Color(0xFF00AEFF), RoundedCornerShape(12.dp))
                                } else it
                            }
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00AEFF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        review.username.firstOrNull()?.uppercase() ?: "?",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column {
                                    Text(
                                        review.username,
                                        color = Color(0xFFF5F5F5),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        formatDate(review.timestamp),
                                        color = Color(0xFF8B949E),
                                        fontSize = 11.sp
                                    )
                                }
                                Spacer(Modifier.weight(1f))
                                if (isOwnReview) {
                                    Text("Your review", color = Color(0xFF00AEFF), fontSize = 10.sp)
                                } else if (review.isInitial) {
                                    Text("Creator", color = Color(0xFF00AEFF), fontSize = 10.sp)
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row {
                                    repeat(review.stars) {
                                        Icon(
                                            Icons.Default.Star, null,
                                            tint = Color(0xFF00AEFF),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                val (condText, condColor) = when (review.condition) {
                                    1 -> "Good" to Color(0xFF4CAF50)
                                    2 -> "Fair" to Color(0xFFFFC107)
                                    else -> "Broken" to Color(0xFFF44336)
                                }
                                Text(
                                    condText, color = condColor, fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (review.comment.isNotBlank()) {
                                Spacer(Modifier.height(6.dp))
                                Text(review.comment, color = Color(0xFFF5F5F5), fontSize = 13.sp)
                            }
                            if (review.photoUrls.isNotEmpty()) {
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    review.photoUrls.forEach { url ->
                                        AsyncImage(
                                            model = url,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(80.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }
                            if (isOwnReview) {
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    TextButton(onClick = { onEditReview(review) }) {
                                        Text("Edit", color = Color(0xFF00AEFF))
                                    }
                                    TextButton(onClick = { onDeleteReview(review.id) }) {
                                        Text("Delete", color = Color(0xFFF44336))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (showRateButton) {
                Button(
                    onClick = onRateClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00AEFF)),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp)
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Rate this facility", color = Color.White)
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

@Composable
private fun ReviewDialog(
    existingReview: Review?,
    onDismiss: () -> Unit,
    onSubmit: (stars: Int, condition: Int, comment: String, photoUris: List<Uri>) -> Unit,
    onUpdate: (
        reviewId: String, stars: Int, condition: Int, comment: String,
        existingPhotoUrls: List<String>, newPhotoUris: List<Uri>
    ) -> Unit,
    onDelete: (reviewId: String) -> Unit
) {
    val isEditMode = existingReview != null
    var stars by remember { mutableStateOf(existingReview?.stars ?: 0) }
    var condition by remember { mutableStateOf(existingReview?.condition ?: 0) }
    var comment by remember { mutableStateOf(existingReview?.comment ?: "") }
    var isSubmitting by remember { mutableStateOf(false) }
    var photoSlots by remember {
        mutableStateOf<List<Any?>>(
            existingReview?.photoUrls?.take(2)?.let { it + List(2 - it.size) { null } }
                ?: listOf(null, null)
        )
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            val current = photoSlots.toMutableList()
            var uriIndex = 0
            for (i in current.indices) {
                if (current[i] == null && uriIndex < uris.size) {
                    current[i] = uris[uriIndex]
                    uriIndex++
                }
            }
            photoSlots = current
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF806B40))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (existingReview != null) "Edit your review" else "Rate this facility",
                    color = Color(0xFFF5F5F5),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))

                Text("Stars", color = Color(0xFFF5F5F5), fontSize = 14.sp)
                Row {
                    repeat(5) { index ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (index < stars) Color(0xFF00AEFF) else Color(0xFF8B949E),
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { stars = index + 1 }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("Condition", color = Color(0xFFF5F5F5), fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        Triple(1, "Good", Color(0xFF4CAF50)),
                        Triple(2, "Fair", Color(0xFFFFC107)),
                        Triple(3, "Broken", Color(0xFFF44336))
                    ).forEach { (value, label, color) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (condition == value) color else Color(0xFF2C332D))
                                .clickable { condition = value }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(label, color = Color(0xFFF5F5F5))
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("Comment", color = Color(0xFFF5F5F5), fontSize = 14.sp)
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF2C332D),
                        unfocusedContainerColor = Color(0xFF2C332D),
                        focusedBorderColor = Color(0xFF00AEFF),
                        unfocusedBorderColor = Color(0xFF00AEFF).copy(alpha = 0.5f),
                        focusedTextColor = Color(0xFFF5F5F5),
                        unfocusedTextColor = Color(0xFFF5F5F5),
                        cursorColor = Color(0xFF00AEFF)
                    ),
                    placeholder = { Text("Optional comment...", color = Color(0xFF8B949E)) }
                )

                Spacer(Modifier.height(16.dp))
                Text("Photos (optional, up to 2)", color = Color(0xFFF5F5F5), fontSize = 14.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    photoSlots.forEachIndexed { index, slot ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(80.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF2C332D))
                                .clickable { galleryLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (slot != null) {
                                AsyncImage(
                                    model = slot,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .clickable {
                                            val current = photoSlots.toMutableList()
                                            current[index] = null
                                            photoSlots = current
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove photo",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            } else {
                                Icon(
                                    Icons.Default.AddAPhoto,
                                    contentDescription = "Add photo",
                                    tint = Color(0xFF8B949E),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
                val canSave = stars > 0 && condition != 0
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (existingReview != null) {
                        TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                            Text("Cancel", color = Color(0xFFF5F5F5))
                        }
                        Spacer(Modifier.weight(1f))
                        Button(
                            onClick = { onDelete(existingReview.id) },
                            enabled = !isSubmitting,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                        ) {
                            Text("Delete", color = Color.White)
                        }
                        Button(
                            onClick = {
                                isSubmitting = true
                                val existingUrls = photoSlots.filterIsInstance<String>()
                                val newUris = photoSlots.filterIsInstance<Uri>()
                                onUpdate(existingReview.id, stars, condition, comment, existingUrls, newUris)
                            },
                            enabled = canSave && !isSubmitting,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00AEFF))
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
                                Text(if (isEditMode) "Update" else "Submit", color = Color.White)
                            }
                        }
                    } else {
                        TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                            Text("Cancel", color = Color(0xFFF5F5F5))
                        }
                        Spacer(Modifier.weight(1f))
                        Button(
                            onClick = {
                                isSubmitting = true
                                val newUris = photoSlots.filterIsInstance<Uri>()
                                onSubmit(stars, condition, comment, newUris)
                            },
                            enabled = canSave && !isSubmitting,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00AEFF))
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
                                Text(if (isEditMode) "Update" else "Submit", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewEvolutionChart(reviews: List<Review>) {
    if (reviews.size < 2) return
    // Sort chronologically (oldest to newest)
    val sorted = reviews.sortedBy { it.timestamp }
    val dateFormat = remember { java.text.SimpleDateFormat("d MMM", java.util.Locale.ENGLISH) }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Text(
            "Rating evolution", color = Color(0xFFF5F5F5),
            fontWeight = FontWeight.Bold, fontSize = 14.sp
        )
        Spacer(Modifier.height(8.dp))

        Canvas(modifier = Modifier.fillMaxWidth().height(140.dp)) {
            val n = sorted.size
            val leftPad = 30f
            val rightPad = 20f
            val topPad = 20f
            val bottomPad = 40f
            val chartWidth = size.width - leftPad - rightPad
            val chartHeight = size.height - topPad - bottomPad
            val stepX = if (n > 1) chartWidth / (n - 1) else chartWidth

            // Y axis: stars 1..5
            // Draw horizontal gridlines for 1..5
            for (star in 1..5) {
                val y = topPad + chartHeight * (1f - (star - 1) / 4f)
                drawLine(
                    color = Color(0xFF8B949E).copy(alpha = 0.2f),
                    start = Offset(leftPad, y),
                    end = Offset(size.width - rightPad, y),
                    strokeWidth = 1f
                )
            }

            // Line connecting star ratings
            val points = sorted.mapIndexed { i, review ->
                val x = leftPad + stepX * i
                val y = topPad + chartHeight * (1f - (review.stars - 1) / 4f)
                Offset(x, y)
            }
            for (i in 0 until points.size - 1) {
                drawLine(
                    color = Color(0xFF00AEFF),
                    start = points[i],
                    end = points[i + 1],
                    strokeWidth = 3f
                )
            }
            // Star points
            points.forEach { p ->
                drawCircle(color = Color(0xFF00AEFF), radius = 5f, center = p)
            }
            // Condition dots below chart
            sorted.forEachIndexed { i, review ->
                val x = leftPad + stepX * i
                val condColor = when (review.condition) {
                    1 -> Color(0xFF4CAF50)
                    2 -> Color(0xFFFFC107)
                    else -> Color(0xFFF44336)
                }
                drawCircle(
                    color = condColor,
                    radius = 6f,
                    center = Offset(x, size.height - bottomPad + 20f)
                )
            }
            // X-axis date labels
            sorted.forEachIndexed { i, review ->
                val x = leftPad + stepX * i
                drawDateLabel(
                    text = dateFormat.format(java.util.Date(review.timestamp)),
                    x = x,
                    y = size.height - 4f
                )
            }
        }

        // Legend
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(top = 4.dp)
        ) {
            LegendDot(Color(0xFF4CAF50), "Good")
            LegendDot(Color(0xFFFFC107), "Fair")
            LegendDot(Color(0xFFF44336), "Broken")
        }
    }
}

private fun DrawScope.drawDateLabel(text: String, x: Float, y: Float) {
    val paint = android.graphics.Paint().apply {
        color = android.graphics.Color.parseColor("#8B949E")
        textSize = 22f
        textAlign = android.graphics.Paint.Align.CENTER
        isAntiAlias = true
    }
    drawContext.canvas.nativeCanvas.drawText(text, x, y, paint)
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(label, color = Color(0xFFF5F5F5), fontSize = 10.sp)
    }
}
