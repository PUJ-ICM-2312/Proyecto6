package com.example.travelscolombia

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenuBox(selected: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Button(
            onClick = { expanded = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = if (selected == "correo") "Correo" else "Nombre",
                color = Color.White
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color(0xFF1E1E1E))
        ) {
            DropdownMenuItem(
                text = { Text("Nombre", color = Color.White) },
                onClick = {
                    onSelected("nombre")
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Correo", color = Color.White) },
                onClick = {
                    onSelected("correo")
                    expanded = false
                }
            )
        }
    }
}

@Composable
fun FriendsScreen(navController: NavController) {
    val context = LocalContext.current
    val db = Firebase.firestore
    val userId = Firebase.auth.currentUser?.uid
    var searchQuery by remember { mutableStateOf("") }
    var searchBy by remember { mutableStateOf("nombre") }
    var selectedTab by remember { mutableStateOf(0) }
    var friendsList by remember { mutableStateOf(listOf<Pair<String, String>>()) }
    var filteredUsers by remember { mutableStateOf(listOf<Triple<String, String, String?>>()) }

    fun cargarAmigos() {
        userId?.let { uid ->
            db.collection("usuarios").document(uid).collection("amigos").get()
                .addOnSuccessListener { result ->
                    friendsList = result.documents.mapNotNull {
                        val name = it.getString("nombre")
                        val id = it.id
                        if (name != null) id to name else null
                    }
                }
        }
    }

    fun buscarUsuariosFirebase() {
        if (searchQuery.isBlank()) {
            filteredUsers = emptyList()
            return
        }

        db.collection("usuarios").get().addOnSuccessListener { result ->
            filteredUsers = result.documents.mapNotNull {
                val id = it.id
                if (id == userId) return@mapNotNull null
                val name = it.getString("nombre") ?: return@mapNotNull null
                val email = it.getString("correo") ?: ""
                val photo = it.getString("foto")
                val matches = when (searchBy) {
                    "correo" -> email.contains(searchQuery, ignoreCase = true)
                    else -> name.contains(searchQuery, ignoreCase = true)
                }
                if (matches) Triple(id, name, photo) else null
            }
        }
    }

    fun agregarAmigo(amigo: Triple<String, String, String?>) {
        userId?.let { uid ->
            db.collection("usuarios").document(uid).collection("amigos").document(amigo.first)
                .set(mapOf("nombre" to amigo.second))
                .addOnSuccessListener {
                    friendsList = friendsList + (amigo.first to amigo.second)
                    Toast.makeText(context, "Amigo agregado", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    LaunchedEffect(Unit) { cargarAmigos() }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(24.dp)
    ) {
        Text("Amigos", style = MaterialTheme.typography.headlineMedium, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.DarkGray,
            contentColor = Color.White
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text("Mis amigos", modifier = Modifier.padding(16.dp))
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text("Agregar amigo", modifier = Modifier.padding(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTab == 0) {
            if (friendsList.isEmpty()) {
                Text("Aún no tienes amigos agregados.", color = Color.LightGray)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    friendsList.forEach { friend ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(friend.second, color = Color.White, fontWeight = FontWeight.Bold)
                                Row {
                                    val safePhoto = URLEncoder.encode("https://via.placeholder.com/150", StandardCharsets.UTF_8.toString())
                                    IconButton(onClick = {
                                        navController.navigate("chat/${friend.first}/${friend.second}/$safePhoto")
                                    }) {
                                        Icon(painterResource(id = R.drawable.chat), contentDescription = "Chat", tint = Color.White)
                                    }
                                    IconButton(onClick = {
                                        Toast.makeText(context, "Llamando a ${friend.second}", Toast.LENGTH_SHORT).show()
                                    }) {
                                        Icon(painterResource(id = R.drawable.call), contentDescription = "Llamar", tint = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Buscar por:", color = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                DropdownMenuBox(searchBy) { selected -> searchBy = selected }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar amigo") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.DarkGray,
                    unfocusedContainerColor = Color.DarkGray,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.LightGray,
                    cursorColor = Color.White
                ),
                textStyle = LocalTextStyle.current.copy(color = Color.White)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { buscarUsuariosFirebase() },
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("Buscar", color = Color.White)
            }

            val amigosIds = friendsList.map { it.first }.toSet()

            filteredUsers.forEach { usuario ->
                val yaEsAmigo = amigosIds.contains(usuario.first)
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Row(
                        Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = rememberAsyncImagePainter(usuario.third ?: ""),
                                contentDescription = "Foto",
                                modifier = Modifier.size(40.dp).clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(usuario.second, color = Color.White)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!yaEsAmigo) {
                                IconButton(onClick = { agregarAmigo(usuario) }) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = "Agregar", tint = Color.White)
                                }
                            } else {
                                Text("Ya es tu amigo", color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                            }

                            val photoSafe = URLEncoder.encode(usuario.third ?: "https://via.placeholder.com/150", StandardCharsets.UTF_8.toString())
                            IconButton(onClick = {
                                navController.navigate("chat/${usuario.first}/${usuario.second}/$photoSafe")
                            }) {
                                Icon(painterResource(id = R.drawable.chat), contentDescription = "Chat", tint = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
