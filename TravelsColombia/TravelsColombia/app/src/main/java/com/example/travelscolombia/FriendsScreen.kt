package com.example.travelscolombia

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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

    LaunchedEffect(Unit) {
        cargarAmigos()
    }

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Amigos", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        TabRow(selectedTabIndex = selectedTab) {
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
                Text("Aún no tienes amigos agregados.")
            } else {
                friendsList.forEach { friend ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(friend.second)
                        Row {
                            val safePhoto = URLEncoder.encode("https://via.placeholder.com/150", StandardCharsets.UTF_8.toString())
                            IconButton(onClick = {
                                navController.navigate("chat/${friend.first}/${friend.second}/$safePhoto")
                            }) {
                                Icon(painterResource(id = R.drawable.chat), contentDescription = "Chat")
                            }

                            IconButton(onClick = {
                                Toast.makeText(context, "Llamando a ${friend.second}", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(painterResource(id = R.drawable.call), contentDescription = "Llamar")
                            }
                        }
                    }
                }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Buscar por:")
                Spacer(modifier = Modifier.width(8.dp))
                DropdownMenuBox(searchBy) { selected -> searchBy = selected }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar amigo") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = { buscarUsuariosFirebase() }, modifier = Modifier.align(Alignment.End)) {
                Text("Buscar")
            }

            val amigosIds = friendsList.map { it.first }.toSet()

            filteredUsers.forEach { usuario ->
                val yaEsAmigo = amigosIds.contains(usuario.first)
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 8.dp),
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
                        Text(usuario.second)
                    }

                    if (!yaEsAmigo) {
                        Button(onClick = { agregarAmigo(usuario) }) {
                            Text("Agregar")
                        }
                    } else {
                        Text("Ya es tu amigo", style = MaterialTheme.typography.bodySmall)
                    }

                    val photoSafe = URLEncoder.encode(usuario.third ?: "https://via.placeholder.com/150", StandardCharsets.UTF_8.toString())
                    IconButton(onClick = {
                        navController.navigate("chat/${usuario.first}/${usuario.second}/$photoSafe")
                    }) {
                        Icon(painterResource(id = R.drawable.chat), contentDescription = "Chat")
                    }
                }
            }
        }
    }
}

@Composable
fun DropdownMenuBox(selected: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { expanded = true }) {
            Text(if (selected == "correo") "Correo" else "Nombre")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("Nombre") }, onClick = {
                onSelected("nombre")
                expanded = false
            })
            DropdownMenuItem(text = { Text("Correo") }, onClick = {
                onSelected("correo")
                expanded = false
            })
        }
    }
}
