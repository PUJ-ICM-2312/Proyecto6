package com.example.travelscolombia

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.Executor

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onRegister: () -> Unit,
    onForgotPassword: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as androidx.fragment.app.FragmentActivity
    val executor: Executor = ContextCompat.getMainExecutor(context)

    var email by remember { mutableStateOf(SecurePrefs.getEmail(context) ?: "") }
    var password by remember { mutableStateOf(SecurePrefs.getPassword(context) ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val promptInfo = remember {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("Autenticación biométrica")
            .setSubtitle("Inicia sesión con tu huella")
            .setNegativeButtonText("Cancelar")
            .build()
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            errorMessage = "Permiso de huella no concedido"
        }
    }

    // Solicita automáticamente el permiso al abrir si es Android 9 o 10
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT in 28..29) {
            requestPermissionLauncher.launch(Manifest.permission.USE_BIOMETRIC)
        }
    }

    fun showBiometricPrompt() {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Ingresa tu correo y contraseña antes de usar la huella"
            return
        }

        val biometricManager = BiometricManager.from(context)
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
            == BiometricManager.BIOMETRIC_SUCCESS
        ) {
            val biometricPrompt = BiometricPrompt(
                activity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        authViewModel.login(
                            email, password,
                            onSuccess = onLoginSuccess,
                            onError = { errorMessage = it }
                        )
                    }

                    override fun onAuthenticationError(code: Int, errString: CharSequence) {
                        errorMessage = "Error de huella: $errString"
                    }

                    override fun onAuthenticationFailed() {
                        errorMessage = "Huella incorrecta"
                    }
                }
            )
            biometricPrompt.authenticate(promptInfo)
        } else {
            errorMessage = "Tu dispositivo no tiene huella configurada o disponible"
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.inicio),
            contentDescription = "Fondo Login",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xAA000000))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Iniciar sesión", color = Color.White, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo", color = Color.White) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.Gray,
                    cursorColor = Color.White,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.LightGray
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña", color = Color.White) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.Gray,
                    cursorColor = Color.White,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.LightGray
                )
            )

            errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, color = Color.Red)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        SecurePrefs.saveCredentials(context, email, password)
                        authViewModel.login(
                            email, password,
                            onSuccess = onLoginSuccess,
                            onError = { errorMessage = it }
                        )
                    } else {
                        errorMessage = "Todos los campos son obligatorios"
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Entrar")
            }

            Spacer(modifier = Modifier.height(16.dp))

            IconButton(onClick = { showBiometricPrompt() }) {
                Icon(
                    painter = painterResource(id = R.drawable.huella),
                    contentDescription = "Autenticación por huella",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onRegister) {
                Text("¿No tienes cuenta? Regístrate", color = Color.White)
            }

            TextButton(onClick = onForgotPassword) {
                Text("¿Olvidaste tu contraseña?", color = Color.White)
            }
        }
    }
}
