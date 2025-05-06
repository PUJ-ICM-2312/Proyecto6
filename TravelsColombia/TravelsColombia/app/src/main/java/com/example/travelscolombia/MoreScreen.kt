package com.example.travelscolombia

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment

@Composable
fun MoreScreen(onLogout: () -> Unit) {
    val options = listOf(
        "🔍 Explorar" to "explore",
        "⭐ Favoritos" to "favorites",
        "📝 Calificaciones y Reseñas" to "reviews",
        "🚗 Rutas y Transporte" to "routes",
        "🎉 Eventos Locales" to "events",
        "📖 Mis Viajes" to "my_trips",
        "💬 Comunidad" to "community",
        "🎁 Ofertas y Aliados" to "offers"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text("Más opciones", style = MaterialTheme.typography.headlineMedium)
        HorizontalDivider()

        options.forEach { (label, route) ->
            MoreItem(label) {
                // Agrega navegación si defines rutas específicas
            }
        }

        Text(
            text = "Cerrar Sesión",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onLogout() }
                .padding(vertical = 8.dp)
        )
    }
}

@Composable
fun MoreItem(label: String, onClick: () -> Unit) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    )
}
