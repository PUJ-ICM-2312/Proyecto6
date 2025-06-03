package com.example.travelscolombia

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun ChatScreen(
    friendId: String,
    friendName: String,
    friendPhoto: String?
) {
    val context = LocalContext.current
    val db = Firebase.firestore
    val auth = Firebase.auth
    val userId = auth.currentUser?.uid ?: return
    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf<Message>()) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            sendMessage(db, userId, friendId, null, imageUrl = it.toString())
        }
    }

    LaunchedEffect(friendId) {
        db.collection("chats")
            .document(chatId(userId, friendId))
            .collection("mensajes")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->
                messages = snapshot?.documents?.mapNotNull {
                    it.toObject(Message::class.java)
                } ?: emptyList()
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(friendPhoto ?: ""),
                contentDescription = null,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(friendName, style = MaterialTheme.typography.titleLarge, color = Color.White)
        }

        Divider(color = Color.DarkGray)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            messages.forEach { msg ->
                val isMe = msg.senderId == userId
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                ) {
                    Surface(
                        color = if (isMe) Color(0xFF4CAF50) else Color(0xFF1E1E1E),
                        shape = RoundedCornerShape(10.dp),
                        tonalElevation = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            msg.text?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                            }

                            msg.imageUrl?.let { url ->
                                Spacer(modifier = Modifier.height(4.dp))
                                Image(
                                    painter = rememberAsyncImagePainter(url),
                                    contentDescription = "Imagen",
                                    modifier = Modifier
                                        .fillMaxWidth(0.7f)
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                            }
                        }
                    }
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            IconButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                Icon(Icons.Default.AttachFile, contentDescription = "Adjuntar", tint = Color.White)
            }

            TextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text("Mensaje...", color = Color.LightGray) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.DarkGray,
                    unfocusedContainerColor = Color.DarkGray,
                    cursorColor = Color.White
                ),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (messageText.isNotBlank()) {
                            sendMessage(db, userId, friendId, messageText)
                            messageText = ""
                        }
                    }
                )
            )

            IconButton(onClick = {
                if (messageText.isNotBlank()) {
                    sendMessage(db, userId, friendId, messageText)
                    messageText = ""
                }
            }) {
                Icon(Icons.Default.Send, contentDescription = "Enviar", tint = Color.White)
            }
        }
    }
}

fun chatId(uid1: String, uid2: String): String =
    if (uid1 < uid2) "${uid1}_$uid2" else "${uid2}_$uid1"

data class Message(
    val senderId: String = "",
    val receiverId: String = "",
    val text: String? = null,
    val imageUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

fun sendMessage(
    db: com.google.firebase.firestore.FirebaseFirestore,
    senderId: String,
    receiverId: String,
    text: String? = null,
    imageUrl: String? = null
) {
    val message = Message(
        senderId = senderId,
        receiverId = receiverId,
        text = text,
        imageUrl = imageUrl,
        timestamp = System.currentTimeMillis()
    )
    db.collection("chats")
        .document(chatId(senderId, receiverId))
        .collection("mensajes")
        .add(message)
}
