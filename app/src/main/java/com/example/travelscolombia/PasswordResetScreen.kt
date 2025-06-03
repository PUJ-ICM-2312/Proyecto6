package com.example.travelscolombia

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PasswordResetScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .background(Color.Black.copy(alpha = 0.8f)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Recuperar contraseña", style = MaterialTheme.typography.headlineMedium, color = Color.White)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color.White,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.LightGray
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (email.isNotBlank()) {
                authViewModel.resetPassword(
                    email = email,
                    onSuccess = { message = "Revisa tu correo para restablecer tu contraseña." },
                    onError = { message = it }
                )
            } else {
                message = "Debes ingresar tu correo."
            }
        }) {
            Text("Enviar enlace")
        }

        message?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(it, color = Color.White)
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = onBack) {
            Text("Volver", color = Color.White)
        }
    }
}
