package com.example.practclase_geolocalizacion_sem9

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Obtén las coordenadas pasadas a esta Activity
        latitude = intent.getDoubleExtra("LATITUDE", 0.0)
        longitude = intent.getDoubleExtra("LONGITUDE", 0.0)

        // Configura el mapa
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Configura el mapa para que se enfoque en la ubicación pasada
        val location = LatLng(latitude, longitude)
        mMap.addMarker(MarkerOptions().position(location).title("Mi Ubicación"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
    }
}

