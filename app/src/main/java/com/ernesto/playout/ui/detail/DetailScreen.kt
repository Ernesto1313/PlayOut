package com.ernesto.playout.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    onBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val instalacion by viewModel.instalacion.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = instalacion?.nombre_sitio ?: "")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        instalacion?.let { inst ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Text(
                    text = inst.nombre_sitio ?: "",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(text = "Categoría: ${inst.categoria ?: "-"}")
                Text(text = "Descripción: ${inst.descripcion ?: "-"}")
                Text(text = "Estado: ${estadoToText(inst.estado)}")
                Text(text = "Agua: ${boolToText(inst.agua)}")
                Text(text = "Asientos: ${inst.asientos ?: "-"}")
                Text(text = "Experiencia de uso: ${inst.experiencia_uso ?: "-"}")
            }
        }
    }
}

private fun estadoToText(estado: Int?): String = when (estado) {
    1 -> "Bueno"
    2 -> "Desgastado"
    3 -> "Roto"
    else -> "-"
}

private fun boolToText(value: Int?): String = when (value) {
    1 -> "Sí"
    0 -> "No"
    else -> "-"
}
