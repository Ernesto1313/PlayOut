package com.ernesto.playout.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ernesto.playout.ui.list.components.InstalacionCard

@Composable
fun ListScreen(
    onInstalacionClick: (Int) -> Unit,
    viewModel: ListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    @Suppress("UNUSED_VARIABLE")
    val userLocation by viewModel.userLocation.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
    ) {
        SortBar(sortOrder = sortOrder, onSortSelected = viewModel::setSortOrder)

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF2C332D))
        ) {
            when (val state = uiState) {
                is ListUiState.Loading -> {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                    )
                }
                is ListUiState.Success -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(state.instalaciones) { instalacion ->
                            InstalacionCard(
                                instalacion = instalacion,
                                distance = viewModel.distanceTo(instalacion),
                                onInstalacionClick = onInstalacionClick,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
                is ListUiState.Error -> {
                    Text(
                        text = state.message,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SortBar(sortOrder: SortOrder, onSortSelected: (SortOrder) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0D1117))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SortChip(
            label = "Distancia",
            selected = sortOrder is SortOrder.ByDistance,
            onClick = { onSortSelected(SortOrder.ByDistance) }
        )
        SortChip(
            label = "Valoración",
            selected = sortOrder is SortOrder.ByRating,
            onClick = { onSortSelected(SortOrder.ByRating) }
        )
        SortChip(
            label = "Categoría",
            selected = sortOrder is SortOrder.ByCategory,
            onClick = { onSortSelected(SortOrder.ByCategory) }
        )
    }
}

@Composable
private fun SortChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(
                color = if (selected) Color(0xFF4CAF50) else Color(0xFF1C2230),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else Color(0xFF8B949E),
            style = MaterialTheme.typography.labelMedium
        )
    }
}
