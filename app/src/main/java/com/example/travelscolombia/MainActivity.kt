package com.example.travelscolombia

import android.Manifest
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresPermission
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.travelscolombia.ui.theme.TravelsColombiaTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.android.gms.location.LocationServices
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : FragmentActivity() {
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Guardar ubicación del usuario en Firestore
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val db = Firebase.firestore
        val uid = Firebase.auth.currentUser?.uid

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val userLocation = mapOf("lat" to it.latitude, "lon" to it.longitude)
                uid?.let {
                    db.collection("usuarios").document(uid)
                        .update("ubicacion", userLocation)
                }
            }
        }

        // Obtener e imprimir el FCM Registration Token
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM Token", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d("FCM Token", "Token: $token")
            // Puedes copiar este token desde Logcat y usarlo en Firebase Console para pruebas
        }

        setContent {
            TravelsColombiaTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel()

                NavHost(navController = navController, startDestination = "welcome") {
                    composable("welcome") {
                        WelcomeScreen(
                            onLoginClick = { navController.navigate("login") },
                            onRegisterClick = { navController.navigate("register") }
                        )
                    }
                    composable("login") {
                        LoginScreen(
                            authViewModel = authViewModel,
                            onLoginSuccess = {
                                navController.navigate("main") {
                                    popUpTo("welcome") { inclusive = true }
                                }
                            },
                            onRegister = { navController.navigate("register") },
                            onForgotPassword = { navController.navigate("reset_password") }
                        )
                    }
                    composable("register") {
                        RegisterScreen(
                            authViewModel = authViewModel,
                            onRegisterSuccess = {
                                navController.navigate("main") {
                                    popUpTo("welcome") { inclusive = true }
                                }
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("reset_password") {
                        PasswordResetScreen(
                            authViewModel = authViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("main") {
                        MainScreen(
                            onLogout = {
                                Firebase.auth.signOut()
                                navController.navigate("welcome") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
