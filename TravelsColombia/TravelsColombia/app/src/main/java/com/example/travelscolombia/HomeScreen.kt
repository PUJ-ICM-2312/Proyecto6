package com.example.travelscolombia.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travelscolombia.R

data class Destination(val name: String, val imageRes: Int)

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
                    Text(
                        "Explora Colombia",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .background(Color.Black),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "¡Hola viajero! 🌎",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Text(
                text = "Recomendaciones para ti",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF80DEEA) // Cyan claro
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(destinations) { dest ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(6.dp),
                        modifier = Modifier.size(width = 220.dp, height = 140.dp)
                    ) {
                        Box {
                            Image(
                                painter = painterResource(id = dest.imageRes),
                                contentDescription = dest.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp))
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Text(
                                    text = dest.name,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }

            Text("🎉 Eventos próximos", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFFFD54F)) // amarillo
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF424242)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Feria de las Flores - Medellín", fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Agosto 1 al 10 - Música, cultura y más", color = Color.LightGray)
                }
            }

            Text("🔥 Promociones exclusivas", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFFF80AB)) // rosa vibrante
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF4A148C)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("¡20% en vuelos a San Andrés!", fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Válido hasta el 15 de mayo", color = Color.LightGray)
                }
            }
        }
    }
}
