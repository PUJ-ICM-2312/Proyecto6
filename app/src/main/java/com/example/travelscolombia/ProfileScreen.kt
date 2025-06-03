package com.example.travelscolombia

import android.Manifest
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.travelscolombia.R
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val auth = Firebase.auth
    val user = auth.currentUser
    val db = Firebase.firestore
    val storage = Firebase.storage
    val userId = user?.uid

    var nombre by remember { mutableStateOf("Usuario") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var password by remember { mutableStateOf("") }
    var nuevaPassword by remember { mutableStateOf("") }
    var showPasswordField by remember { mutableStateOf(false) }
    var photoUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        userId?.let { id ->
            db.collection("usuarios").document(id).get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    nombre = doc.getString("nombre") ?: "Usuario"
                    photoUrl = doc.getString("foto")
                }
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && userId != null) {
            val storageRef = storage.reference.child("fotos_perfil/$userId.jpg")
            storageRef.putFile(uri).addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    photoUrl = downloadUrl.toString()
                    Toast.makeText(context, "Foto subida", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(context, "Error al subir foto: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            imagePickerLauncher.launch("image/*")
        } else {
            Toast.makeText(context, "Permiso denegado para acceder a fotos", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil", color = Color.White) },
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .clickable {
                        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Manifest.permission.READ_MEDIA_IMAGES
                        } else {
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        }
                        permissionLauncher.launch(permission)
                    }
            ) {
                if (photoUrl != null) {
                    Image(
                        painter = rememberAsyncImagePainter(photoUrl),
                        contentDescription = "Foto de perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.foto),
                        contentDescription = "Foto por defecto",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {},
                label = { Text("Correo electrónico") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña actual") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors(),
                visualTransformation = PasswordVisualTransformation()
            )

            if (showPasswordField) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = nuevaPassword,
                    onValueChange = { nuevaPassword = it },
                    label = { Text("Nueva contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors(),
                    visualTransformation = PasswordVisualTransformation()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Button(onClick = {
                    showPasswordField = !showPasswordField
                }) {
                    Text(if (showPasswordField) "Cancelar" else "Actualizar contraseña")
                }

                Button(onClick = {
                    if (nombre.isNotBlank()) {
                        userId?.let { id ->
                            db.collection("usuarios").document(id).set(
                                mapOf(
                                    "nombre" to nombre,
                                    "foto" to photoUrl
                                )
                            ).addOnSuccessListener {
                                Toast.makeText(context, "Datos actualizados", Toast.LENGTH_SHORT).show()
                            }.addOnFailureListener {
                                Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    if (showPasswordField && nuevaPassword.isNotBlank() && password.isNotBlank()) {
                        val credential = EmailAuthProvider.getCredential(email, password)
                        user?.reauthenticate(credential)?.addOnCompleteListener { authTask ->
                            if (authTask.isSuccessful) {
                                user.updatePassword(nuevaPassword).addOnCompleteListener { updateTask ->
                                    if (updateTask.isSuccessful) {
                                        Toast.makeText(context, "Contraseña actualizada", Toast.LENGTH_SHORT).show()
                                        password = ""
                                        nuevaPassword = ""
                                        showPasswordField = false
                                    } else {
                                        Toast.makeText(context, "Error: ${updateTask.exception?.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Contraseña actual incorrecta", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }) {
                    Text("Guardar cambios")
                }
            }
        }
    }
}

@Composable
fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedContainerColor = Color.DarkGray,
    unfocusedContainerColor = Color.DarkGray,
    focusedBorderColor = Color.White,
    unfocusedBorderColor = Color.Gray,
    focusedLabelColor = Color.White,
    unfocusedLabelColor = Color.LightGray
)
