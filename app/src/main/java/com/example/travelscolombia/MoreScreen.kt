package com.example.travelscolombia

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(onLogout: () -> Unit) {
    val options = listOf(
        "🔍 Explorar" to {},
        "⭐ Favoritos" to {},
        "📝 Calificaciones y Reseñas" to {},
        "🚗 Rutas y Transporte" to {},
        "🎉 Eventos Locales" to {},
        "📖 Mis Viajes" to {},
        "💬 Comunidad" to {},
        "🎁 Ofertas y Aliados" to {}
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Más opciones", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .background(Color.Black),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            options.forEach { (label, action) ->
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = Color.DarkGray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = action)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onLogout)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Cerrar Sesión",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cerrar Sesión", color = Color.White)
                }
            }
        }
    }
}
