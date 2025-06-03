package com.example.travelscolombia

import android.Manifest
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.*
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import com.google.gson.JsonParser
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.net.URLEncoder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// Data classes

data class LugarTuristico(val nombre: String, val ubicacion: LatLng, val descripcion: String)
data class CiudadJson(val nombre: String, val lat: Double, val lon: Double, val descripcion: String)

fun cargarLugaresDesdeAssets(context: Context): List<LugarTuristico> {
    val json = context.assets.open("municipios_colombia_por_region.json")
        .bufferedReader().use { it.readText() }

    val gson = Gson()
    val type = object : TypeToken<Map<String, List<CiudadJson>>>() {}.type
    val regiones: Map<String, List<CiudadJson>> = gson.fromJson(json, type)

    return regiones.flatMap { (_, ciudades) ->
        ciudades.map {
            LugarTuristico(
                nombre = it.nombre,
                ubicacion = LatLng(it.lat, it.lon),
                descripcion = it.descripcion
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen() {
    val context = LocalContext.current
    val lugares by remember { mutableStateOf(cargarLugaresDesdeAssets(context)) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var lugarSeleccionado by remember { mutableStateOf<LugarTuristico?>(null) }
    var mostrarDialogo by remember { mutableStateOf(false) }
    var rutaPolyline by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var ubicacionActual by remember { mutableStateOf<LatLng?>(null) }
    var lugaresCercanos by remember { mutableStateOf<List<LugarTuristico>>(emptyList()) }
    var amigosUbicaciones by remember { mutableStateOf<List<LugarTuristico>>(emptyList()) }
    var modoBrujulaActivo by remember { mutableStateOf(false) }
    val bearingState = remember { mutableStateOf(0f) }

    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    var azimut by remember { mutableStateOf(0f) }
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val rotation = animateFloatAsState(targetValue = azimut)
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        val gravity = FloatArray(3)
        val geomagnetic = FloatArray(3)

        val sensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> gravity.indices.forEach { gravity[it] = event.values[it] }
                    Sensor.TYPE_MAGNETIC_FIELD -> geomagnetic.indices.forEach { geomagnetic[it] = event.values[it] }
                }

                val R = FloatArray(9)
                val I = FloatArray(9)
                if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(R, orientation)
                    if (modoBrujulaActivo) {
                        bearingState.value = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    }
                    azimut = Math.toDegrees(orientation[0].toDouble()).toFloat()
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_UI)

        onDispose {
            sensorManager.unregisterListener(sensorListener)
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(4.7110, -74.0721), 5.8f)
    }

    LaunchedEffect(bearingState.value, modoBrujulaActivo) {
        if (modoBrujulaActivo) {
            cameraPositionState.move(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.Builder()
                        .target(cameraPositionState.position.target)
                        .zoom(cameraPositionState.position.zoom)
                        .tilt(cameraPositionState.position.tilt)
                        .bearing(bearingState.value)
                        .build()
                )
            )
        }
    }

    LaunchedEffect(Unit) {
        if (!locationPermission.status.isGranted) {
            locationPermission.launchPermissionRequest()
        } else {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    ubicacionActual = LatLng(it.latitude, it.longitude)
                }
            }
            amigosUbicaciones = obtenerUbicacionesDeAmigos()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mapa Turístico", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column {
                GoogleMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = true,
                        myLocationButtonEnabled = true
                    ),
                    properties = MapProperties(
                        isMyLocationEnabled = locationPermission.status.isGranted
                    )
                ) {
                    lugares.forEach { lugar ->
                        Marker(
                            state = MarkerState(position = lugar.ubicacion),
                            title = lugar.nombre,
                            snippet = lugar.descripcion,
                            onClick = {
                                lugarSeleccionado = lugar
                                coroutineScope.launch {
                                    lugaresCercanos = buscarLugaresCercanos(lugar.ubicacion)
                                }
                                false
                            }
                        )
                    }

                    lugaresCercanos.forEach { lugar ->
                        Marker(
                            state = MarkerState(position = lugar.ubicacion),
                            title = lugar.nombre,
                            snippet = lugar.descripcion,
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE),
                            onClick = {
                                lugarSeleccionado = lugar
                                mostrarDialogo = true
                                false
                            }
                        )
                    }

                    amigosUbicaciones.forEach { amigo ->
                        Marker(
                            state = MarkerState(position = amigo.ubicacion),
                            title = amigo.nombre,
                            snippet = amigo.descripcion,
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                        )
                    }

                    if (ubicacionActual != null) {
                        Marker(
                            state = MarkerState(position = ubicacionActual!!),
                            title = "Tú estás aquí",
                            snippet = "Mi ubicación actual",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                        )
                    }

                    if (rutaPolyline.isNotEmpty()) {
                        Polyline(points = rutaPolyline)
                    }
                }

                lugarSeleccionado?.let { lugar ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.DarkGray)
                            .padding(16.dp)
                    ) {
                        Text(lugar.nombre, style = MaterialTheme.typography.headlineSmall, color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(lugar.descripcion, color = Color.LightGray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { mostrarDialogo = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                        ) {
                            Text("Ver rutas", color = Color.Black)
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = { modoBrujulaActivo = !modoBrujulaActivo },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Explore, contentDescription = "Modo brújula")
            }
        }

        if (mostrarDialogo && lugarSeleccionado != null) {
            AlertDialog(
                onDismissRequest = { mostrarDialogo = false },
                title = { Text("¿Cómo quieres ver la ruta?", color = Color.White) },
                containerColor = Color(0xFF121212),
                text = {
                    Column {
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                val uri = Uri.parse("google.navigation:q=${lugarSeleccionado!!.ubicacion.latitude},${lugarSeleccionado!!.ubicacion.longitude}")
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                intent.setPackage("com.google.android.apps.maps")
                                context.startActivity(intent)
                                mostrarDialogo = false
                            }
                        ) {
                            Text("Abrir en Google Maps")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                mostrarDialogo = false
                                ubicacionActual?.let { origen ->
                                    lugarSeleccionado?.let { destino ->
                                        obtenerRuta(origen, destino.ubicacion) { polyline ->
                                            rutaPolyline = polyline
                                        }
                                    }
                                }
                            }
                        ) {
                            Text("Ver en el mapa de la app")
                        }
                    }
                },
                confirmButton = {}
            )
        }
    }
}

fun obtenerRuta(origen: LatLng, destino: LatLng, onRutaObtenida: (List<LatLng>) -> Unit) {
    val apiKey = "AIzaSyDbTLM9xtRB996xePRe5KuRaeMaRi-tGRA"
    val url = "https://maps.googleapis.com/maps/api/directions/json?origin=${origen.latitude},${origen.longitude}&destination=${destino.latitude},${destino.longitude}&key=$apiKey"

    val scope = kotlinx.coroutines.CoroutineScope(Dispatchers.Main)
    scope.launch {
        try {
            val json = withContext(Dispatchers.IO) {
                URL(url).readText()
            }
            val jsonObject = JsonParser.parseString(json).asJsonObject
            val routesArray = jsonObject.getAsJsonArray("routes")

            if (routesArray != null && routesArray.size() > 0) {
                val route = routesArray[0].asJsonObject
                val polylineObject = route.getAsJsonObject("overview_polyline")
                val encodedPoints = polylineObject?.get("points")?.asString

                if (encodedPoints != null) {
                    val decoded = PolyUtil.decode(encodedPoints)
                    onRutaObtenida(decoded)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

suspend fun buscarLugaresCercanos(ubicacion: LatLng): List<LugarTuristico> {
    val apiKey = "AIzaSyDbTLM9xtRB996xePRe5KuRaeMaRi-tGRA"
    val location = "${ubicacion.latitude},${ubicacion.longitude}"
    val radius = 3000
    val type = "tourist_attraction"
    val keyword = URLEncoder.encode("turismo", "UTF-8")
    val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=${URLEncoder.encode(location, "UTF-8")}&radius=$radius&type=$type&keyword=$keyword&key=$apiKey"

    return withContext(Dispatchers.IO) {
        try {
            val json = URL(url).readText()
            val jsonObject = JsonParser.parseString(json).asJsonObject
            val results = jsonObject.getAsJsonArray("results")

            results.mapNotNull { result ->
                val obj = result.asJsonObject
                val name = obj.get("name")?.asString ?: return@mapNotNull null
                val geometry = obj.getAsJsonObject("geometry")
                val locationObj = geometry.getAsJsonObject("location")
                val lat = locationObj.get("lat")?.asDouble ?: return@mapNotNull null
                val lng = locationObj.get("lng")?.asDouble ?: return@mapNotNull null
                LugarTuristico(name, LatLng(lat, lng), "Lugar cercano sugerido")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}

suspend fun obtenerUbicacionesDeAmigos(): List<LugarTuristico> {
    val firestore = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return emptyList()

    return try {
        // 1. Obtener IDs de amigos
        val amigosSnapshot = firestore.collection("usuarios")
            .document(currentUserId)
            .collection("amigos")
            .get()
            .await()

        val idsDeAmigos = amigosSnapshot.documents.map { it.id }.toSet()

        // 2. Obtener ubicaciones y filtrar por IDs de amigos
        val ubicacionesSnapshot = firestore.collection("ubicaciones").get().await()
        ubicacionesSnapshot.documents.mapNotNull { doc ->
            if (doc.id !in idsDeAmigos) return@mapNotNull null
            val lat = doc.getDouble("lat") ?: return@mapNotNull null
            val lng = doc.getDouble("lng") ?: return@mapNotNull null
            val nombre = firestore.collection("usuarios").document(doc.id).get().await()
                .getString("nombre") ?: "Amigo"
            LugarTuristico(nombre, LatLng(lat, lng), "Ubicación compartida por tu amigo")
        }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}
@Composable
fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x99000000)), // Fondo negro con opacidad
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Cargando ubicación...", color = Color.White)
        }
    }
}

