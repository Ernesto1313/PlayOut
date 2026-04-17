package com.ernesto.playout.ui.list.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ernesto.playout.data.model.Instalacion

private fun estadoToText(estado: Int): String = when (estado) {
    1 -> "Bueno"
    2 -> "Desgastado"
    3 -> "Roto"
    else -> "Desconocido"
}

@Composable
fun InstalacionCard(
    instalacion: Instalacion,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = instalacion.nombre_sitio,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = instalacion.categoria,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = estadoToText(instalacion.estado),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
