package com.example.travelscolombia

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

class AuthViewModel : ViewModel() {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _user = MutableStateFlow(firebaseAuth.currentUser)
    val user = _user.asStateFlow()

    fun login(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                _user.value = firebaseAuth.currentUser
                onSuccess()
            }
            .addOnFailureListener {
                onError(it.message ?: "Error al iniciar sesión")
            }
    }

    fun register(
        email: String,
        password: String,
        nombre: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                if (user != null) {
                    val uid = user.uid
                    val usuarioData = hashMapOf(
                        "uid" to uid,
                        "nombre" to nombre,
                        "email" to email,
                        "foto" to "https://via.placeholder.com/120", // Puedes cambiar esto luego
                        "fechaRegistro" to Date()
                    )

                    firestore.collection("usuarios").document(uid)
                        .set(usuarioData)
                        .addOnSuccessListener {
                            _user.value = user
                            onSuccess()
                        }
                        .addOnFailureListener { e ->
                            onError("Error guardando datos en Firestore: ${e.message}")
                        }
                } else {
                    onError("No se pudo obtener el usuario")
                }
            }
            .addOnFailureListener {
                onError(it.message ?: "Error al registrar usuario")
            }
    }

    fun resetPassword(email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onError(task.exception?.message ?: "Error al enviar correo de recuperación")
                }
            }
    }

    fun logout() {
        firebaseAuth.signOut()
        _user.value = null
    }
}
