package com.example.practclase_geolocalizacion_sem9

import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.Locale
import android.Manifest
import android.content.Intent
import android.net.Uri

class MainActivity : AppCompatActivity() {

    private lateinit var botonObtenerCoordenadas: Button
    private lateinit var textoLatitud: TextView
    private lateinit var textoLongitud: TextView
    private lateinit var textoDireccion: TextView
    private lateinit var gestorUbicacion: LocationManager
    private lateinit var botonCompartirUbicacion: Button
    private lateinit var botonMostrarMapa: Button

    private val lanzadorSolicitudPermiso = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            obtenerUltimaUbicacion()
        } else {
            Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_LONG).show()
        }
    }

    private lateinit var localizacion: Localizacion
    private var rastreandoUbicacion = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        botonObtenerCoordenadas = findViewById(R.id.btnGetCoordinates)
        textoLatitud = findViewById(R.id.tvLatitude)
        textoLongitud = findViewById(R.id.tvLongitude)
        textoDireccion = findViewById(R.id.tvAddress)
        botonCompartirUbicacion = findViewById(R.id.btnShareLocation)
        botonMostrarMapa = findViewById(R.id.btnShowMap)

        gestorUbicacion = getSystemService(LOCATION_SERVICE) as LocationManager
        localizacion = Localizacion(textoLatitud, textoLongitud, textoDireccion, this)

        botonObtenerCoordenadas.setOnClickListener {
            if (!rastreandoUbicacion) {
                verificarPermisoUbicacion()
                botonObtenerCoordenadas.text = "Detener rastreo"
                rastreandoUbicacion = true
            } else {
                detenerRastreo()
                botonObtenerCoordenadas.text = "Obtener Coordenadas"
                rastreandoUbicacion = false
            }
        }

        botonCompartirUbicacion.setOnClickListener {
            compartirUbicacion()
        }

        botonMostrarMapa.setOnClickListener {
            mostrarMapa()
        }
    }

    private fun verificarPermisoUbicacion() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                obtenerUltimaUbicacion()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Toast.makeText(this, "Se necesita permiso de ubicación para continuar", Toast.LENGTH_LONG).show()
                lanzadorSolicitudPermiso.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            else -> {
                lanzadorSolicitudPermiso.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun obtenerUltimaUbicacion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permiso de ubicación no concedido", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val ultimaUbicacion = gestorUbicacion.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: gestorUbicacion.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            if (ultimaUbicacion != null) {
                textoLatitud.text = String.format("%.6f", ultimaUbicacion.latitude)
                textoLongitud.text = String.format("%.6f", ultimaUbicacion.longitude)
                localizacion.setLastLocation(ultimaUbicacion)
            } else {
                textoLatitud.text = "No disponible"
                textoLongitud.text = "No disponible"
                textoDireccion.text = "Sin dirección"
                Toast.makeText(this@MainActivity, "No se encontró ubicación reciente", Toast.LENGTH_SHORT).show()
            }

            gestorUbicacion.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000,
                0.5f,
                localizacion
            )

            if (gestorUbicacion.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                gestorUbicacion.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    1000,
                    0.5f,
                    localizacion
                )
            }

            Toast.makeText(this, "Rastreo de ubicación iniciado", Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            textoLatitud.text = "Error"
            textoLongitud.text = "Error"
            textoDireccion.text = "Error"
            Toast.makeText(this@MainActivity, "Error de seguridad: ${e.message}", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            textoLatitud.text = "Error"
            textoLongitud.text = "Error"
            textoDireccion.text = "Error"
            Toast.makeText(this@MainActivity, "Error al obtener ubicación: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun detenerRastreo() {
        gestorUbicacion.removeUpdates(localizacion)
        Toast.makeText(this, "Rastreo de ubicación detenido", Toast.LENGTH_SHORT).show()
    }

    private fun compartirUbicacion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permiso de ubicación no concedido", Toast.LENGTH_SHORT).show()
            return
        }

        val ultimaUbicacion = gestorUbicacion.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: gestorUbicacion.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        if (ultimaUbicacion != null) {
            val latitud = ultimaUbicacion.latitude
            val longitud = ultimaUbicacion.longitude
            val mensaje = "Hola, te adjunto mi ubicación: https://maps.google.com/?q=$latitud,$longitud"

            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://api.whatsapp.com/send?text=$mensaje")
            startActivity(intent)
        } else {
            Toast.makeText(this, "Ubicación no disponible", Toast.LENGTH_SHORT).show()
        }
    }


    private fun mostrarMapa() {
        val lat = textoLatitud.text.toString()
        val lon = textoLongitud.text.toString()

        val uri = "geo:$lat,$lon?z=15"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        intent.setPackage("com.google.android.apps.maps")
        startActivity(intent)
    }

    override fun onPause() {
        super.onPause()
        if (rastreandoUbicacion) {
            detenerRastreo()
            botonObtenerCoordenadas.text = "Obtener Coordenadas"
            rastreandoUbicacion = false
        }
    }

    class Localizacion(
        private val textoLatitud: TextView,
        private val textoLongitud: TextView,
        private val textoDireccion: TextView,
        private val contexto: MainActivity
    ) : LocationListener {

        private var ubicacion: Location? = null

        override fun onLocationChanged(location: Location) {
            ubicacion = location
            textoLatitud.text = String.format("%.6f", location.latitude)
            textoLongitud.text = String.format("%.6f", location.longitude)
            obtenerDireccion(location)
        }

        private fun obtenerDireccion(location: Location) {
            val geocoder = Geocoder(contexto, Locale.getDefault())
            try {
                val direccion = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (direccion != null && direccion.isNotEmpty()) {
                    val direccionCompleta = direccion[0].getAddressLine(0)
                    textoDireccion.text = direccionCompleta
                } else {
                    textoDireccion.text = "Dirección no disponible"
                }
            } catch (e: Exception) {
                textoDireccion.text = "Error al obtener dirección"
            }
        }

        fun setLastLocation(location: Location) {
            ubicacion = location
            textoLatitud.text = String.format("%.6f", location.latitude)
            textoLongitud.text = String.format("%.6f", location.longitude)
            obtenerDireccion(location)
        }
    }
}
