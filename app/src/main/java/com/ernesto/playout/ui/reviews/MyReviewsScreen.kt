package com.ernesto.playout.ui.reviews

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.ernesto.playout.R
import com.ernesto.playout.ui.utils.categoryDrawable

private fun photoModel(path: String?): Any {
    return when {
        path != null && path.startsWith("https://") -> path
        path != null && path.startsWith("/") -> java.io.File(path)
        else -> R.drawable.other
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.ENGLISH)
    return sdf.format(java.util.Date(timestamp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReviewsScreen(
    onBack: () -> Unit,
    onReviewClick: (Int) -> Unit,
    viewModel: MyReviewsViewModel = hiltViewModel()
) {
    val reviews by viewModel.reviews.collectAsStateWithLifecycle()
    val availableSports by viewModel.availableSports.collectAsStateWithLifecycle()
    val sportFilter by viewModel.sportFilter.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    var showSortMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF806B40))
    ) {
        TopAppBar(
            title = {
                Text("My Reviews", color = Color(0xFFF5F5F5), fontWeight = FontWeight.Bold)
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFFF5F5F5)
                    )
                }
            },
            actions = {
                Box {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.Default.Sort, contentDescription = "Sort", tint = Color(0xFFF5F5F5))
                    }
                    DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                        DropdownMenuItem(text = { Text("Newest first") },
                            onClick = { viewModel.setSortMode("date_desc"); showSortMenu = false })
                        DropdownMenuItem(text = { Text("Oldest first") },
                            onClick = { viewModel.setSortMode("date_asc"); showSortMenu = false })
                        DropdownMenuItem(text = { Text("Highest rated") },
                            onClick = { viewModel.setSortMode("rating_desc"); showSortMenu = false })
                        DropdownMenuItem(text = { Text("Lowest rated") },
                            onClick = { viewModel.setSortMode("rating_asc"); showSortMenu = false })
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF806B40))
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                SportFilterChip(
                    label = "All",
                    selected = sportFilter == null,
                    onClick = { viewModel.setSportFilter(null) }
                )
            }
            items(availableSports) { sport ->
                SportFilterChip(
                    label = sport,
                    selected = sportFilter == sport,
                    onClick = { viewModel.setSportFilter(sport) }
                )
            }
        }

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF00AEFF))
                }
            }
            reviews.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "You haven't written any reviews yet.",
                        color = Color(0xFFF5F5F5),
                        fontSize = 14.sp
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(reviews, key = { it.review.id }) { item ->
                        MyReviewCard(
                            item = item,
                            onClick = { onReviewClick(item.review.facilityFid) },
                            onEdit = { onReviewClick(item.review.facilityFid) },
                            onDelete = { viewModel.deleteReview(item.review.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SportFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) Color(0xFF00AEFF) else Color(0xFF2C332D))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(label, color = Color(0xFFF5F5F5), fontSize = 13.sp)
    }
}

@Composable
private fun MyReviewCard(
    item: ReviewWithFacility,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val review = item.review
    val facility = item.facility
    var showMenu by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var enlargedPhoto by remember { mutableStateOf<String?>(null) }

    val showExpandArrow = review.comment.length > 80 || review.photoUrls.isNotEmpty()

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C332D)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                AsyncImage(
                    model = photoModel(facility?.photo),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Image(
                            painter = painterResource(categoryDrawable(facility?.sport)),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            colorFilter = ColorFilter.tint(Color(0xFF00AEFF))
                        )
                        Text(
                            facility?.neighbourhood ?: facility?.sport ?: "Unknown location",
                            color = Color(0xFFF5F5F5),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                        Text(condText, color = condColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(formatDate(review.timestamp), color = Color(0xFF8B949E), fontSize = 11.sp)
                    if (review.comment.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        if (expanded) {
                            Text(
                                review.comment,
                                color = Color(0xFFF5F5F5),
                                fontSize = 13.sp
                            )
                        } else {
                            Text(
                                review.comment,
                                color = Color(0xFFF5F5F5),
                                fontSize = 13.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color(0xFF8B949E))
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = { showMenu = false; onEdit() }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = { showMenu = false; onDelete() }
                        )
                    }
                }
            }
            if (expanded && review.photoUrls.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    review.photoUrls.forEach { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { enlargedPhoto = url }
                        )
                    }
                }
            }
            if (showExpandArrow) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            if (expanded) Icons.Default.KeyboardArrowUp
                            else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (expanded) "Collapse" else "Expand",
                            tint = Color(0xFF00AEFF)
                        )
                    }
                }
            }
        }
    }

    enlargedPhoto?.let { url ->
        Dialog(onDismissRequest = { enlargedPhoto = null }) {
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { enlargedPhoto = null }
            )
        }
    }
}
