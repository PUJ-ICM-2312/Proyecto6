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
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import com.google.gson.JsonParser

data class LugarTuristico(val nombre: String, val ubicacion: LatLng, val descripcion: String)

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen() {
    val lugares = listOf(
        LugarTuristico("Bogotá", LatLng(4.7110, -74.0721), "Capital con cultura e historia."),
        LugarTuristico("Cartagena", LatLng(10.3910, -75.4794), "Ciudad colonial con playa."),
        LugarTuristico("Medellín", LatLng(6.2442, -75.5812), "Ciudad de la eterna primavera."),
        LugarTuristico("San Andrés", LatLng(12.5847, -81.7006), "Isla en el Caribe colombiano."),
        LugarTuristico("Santa Marta", LatLng(11.2408, -74.1990), "Acceso a Tayrona y Sierra Nevada.")
    )

    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var lugarSeleccionado by remember { mutableStateOf<LugarTuristico?>(null) }
    var mostrarDialogo by remember { mutableStateOf(false) }
    var rutaPolyline by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var ubicacionActual by remember { mutableStateOf<LatLng?>(null) }

    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    // SENSOR: Azimut (grados)
    var azimut by remember { mutableStateOf(0f) }
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val rotation = animateFloatAsState(targetValue = azimut)

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
                    azimut = Math.toDegrees(orientation[0].toDouble()).toFloat()
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(
            sensorListener,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_UI
        )
        sensorManager.registerListener(
            sensorListener,
            sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
            SensorManager.SENSOR_DELAY_UI
        )

        onDispose {
            sensorManager.unregisterListener(sensorListener)
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
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(4.7110, -74.0721), 5.8f)
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
                                false
                            }
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

            Image(
                painter = painterResource(id = R.drawable.brujula),
                contentDescription = "Brújula",
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp)
                    .rotate(-rotation.value)
            )


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

    Thread {
        try {
            val json = URL(url).readText()
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
    }.start()
}
