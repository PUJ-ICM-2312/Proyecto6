package com.example.travelscolombia.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travelscolombia.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val destinations = listOf(
        Destination("Cartagena", R.drawable.cartagena),
        Destination("Medellín", R.drawable.medellin),
        Destination("San Andrés", R.drawable.sanandres),
        Destination("Eje Cafetero", R.drawable.ejecafetero),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Flight, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Explora Colombia", color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A237E))
            )
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "¡Hola viajero! 🌍", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White
            )

            Text(
                text = "Destinos recomendados", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64FFDA)
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(destinations) { dest ->
                    Box(
                        modifier = Modifier
                            .width(200.dp)
                            .height(140.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Gray)
                            .clickable { /* TODO: Navegar al destino */ }
                    ) {
                        Image(
                            painter = painterResource(id = dest.imageRes),
                            contentDescription = dest.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                                    )
                                ),
                            contentAlignment = Alignment.BottomStart
                        ) {
                            Text(
                                text = dest.name,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }

            SectionHeader("🎉 Eventos próximos", Color(0xFFFFD54F))
            PromoCard(
                title = "Feria de las Flores - Medellín",
                subtitle = "Agosto 1 al 10 - Música, cultura y más",
                backgroundColor = Color(0xFF263238)
            )

            SectionHeader("🔥 Promociones exclusivas", Color(0xFFFF80AB))
            PromoCard(
                title = "¡20% en vuelos a San Andrés!",
                subtitle = "Válido hasta el 15 de mayo",
                backgroundColor = Color(0xFF6A1B9A)
            )
        }
    }
}

@Composable
fun SectionHeader(text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Star, contentDescription = null, tint = color)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun PromoCard(title: String, subtitle: String, backgroundColor: Color) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = Color.White)
            Text(subtitle, color = Color.LightGray)
        }
    }
}

data class Destination(val name: String, val imageRes: Int)
