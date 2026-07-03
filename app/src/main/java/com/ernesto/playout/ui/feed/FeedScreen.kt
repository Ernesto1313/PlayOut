package com.ernesto.playout.ui.feed

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ernesto.playout.ui.feed.components.FeedCard
import com.ernesto.playout.ui.list.ListUiState
import com.ernesto.playout.ui.list.ListViewModel
import com.ernesto.playout.ui.utils.categoryDrawable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onInstalacionClick: (Int) -> Unit,
    contentPadding: PaddingValues,
    viewModel: ListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val userLocation by viewModel.userLocation.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedCategory by remember { mutableStateOf("") }
    var sortField by remember { mutableStateOf("") }
    var sortAscending by remember { mutableStateOf(true) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            sortField = "distance"
            sortAscending = false
        }
    }

    val baseList = remember(uiState) {
        if (uiState is ListUiState.Success) (uiState as ListUiState.Success).facilities
        else emptyList()
    }

    val uniqueCategories = remember(baseList) {
        baseList.mapNotNull { it.sport }.distinct().sorted()
    }

    val filteredList = remember(baseList, selectedCategory, sortField, sortAscending, userLocation) {
        var result = if (selectedCategory.isNotEmpty()) {
            baseList.filter { it.sport == selectedCategory }
        } else {
            baseList
        }
        result = when (sortField) {
            "condition" -> result.sortedWith(compareBy(nullsLast()) { it.condition })
            "experience" -> result.sortedWith(compareBy(nullsLast()) { it.experience })
            "distance" -> result.sortedWith(compareBy(nullsLast()) { viewModel.distanceTo(it) })
            else -> result
        }
        if (!sortAscending) result.reversed() else result
    }

    fun toggleSort(chip: String) {
        when {
            sortField != chip -> { sortField = chip; sortAscending = false }
            !sortAscending -> { sortAscending = true }
            else -> { sortField = "" }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2C332D))
            .padding(contentPadding)
    ) {
        // Section 1 - Category filter row
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uniqueCategories.size) { index ->
                val cat = uniqueCategories[index]
                val isSelected = selectedCategory == cat
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color(0xFF00AEFF) else Color(0xFF3A3A3A))
                        .then(
                            if (!isSelected) Modifier.border(1.dp, Color.Gray, CircleShape)
                            else Modifier
                        )
                        .clickable {
                            selectedCategory = if (selectedCategory == cat) "" else cat
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(categoryDrawable(cat)),
                        contentDescription = cat,
                        modifier = Modifier.size(28.dp),
                        colorFilter = ColorFilter.tint(if (isSelected) Color.White else Color.Gray)
                    )
                }
            }
        }

        // Section 2 - Sort chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = sortField == "condition",
                    onClick = { toggleSort("condition") },
                    label = { Text("Condition") },
                    trailingIcon = if (sortField == "condition") {
                        {
                            Icon(
                                imageVector = if (sortAscending) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF00AEFF),
                        selectedLabelColor = Color.White,
                        containerColor = Color.Transparent,
                        labelColor = Color(0xFFF5F5F5)
                    )
                )
            }
            item {
                FilterChip(
                    selected = sortField == "experience",
                    onClick = { toggleSort("experience") },
                    label = { Text("Experience") },
                    trailingIcon = if (sortField == "experience") {
                        {
                            Icon(
                                imageVector = if (sortAscending) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF00AEFF),
                        selectedLabelColor = Color.White,
                        containerColor = Color.Transparent,
                        labelColor = Color(0xFFF5F5F5)
                    )
                )
            }
            item {
                val distanceDisabled = userLocation == null
                FilterChip(
                    selected = sortField == "distance",
                    onClick = {
                        val locationManager = context.getSystemService(
                            Context.LOCATION_SERVICE) as LocationManager
                        val isGpsEnabled = locationManager.isProviderEnabled(
                            LocationManager.GPS_PROVIDER) ||
                            locationManager.isProviderEnabled(
                            LocationManager.NETWORK_PROVIDER)
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED

                        when {
                            !hasPermission -> {
                                locationPermissionLauncher.launch(
                                    Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                            !isGpsEnabled -> {
                                context.startActivity(Intent(
                                    Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                            }
                            else -> {
                                viewModel.refreshLocation()
                                toggleSort("distance")
                            }
                        }
                    },
                    label = { Text("Distance") },
                    trailingIcon = if (sortField == "distance") {
                        {
                            Icon(
                                imageVector = if (sortAscending) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else null,
                    modifier = if (distanceDisabled) Modifier.alpha(0.4f) else Modifier,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF00AEFF),
                        selectedLabelColor = Color.White,
                        containerColor = Color.Transparent,
                        labelColor = Color(0xFFF5F5F5)
                    )
                )
            }
        }

        // Section 3 - Grid 2x2
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredList) { facility ->
                FeedCard(
                    facility = facility,
                    distanceMeters = viewModel.distanceTo(facility),
                    onClick = { onInstalacionClick(facility.fid) }
                )
            }
        }
    }
}
