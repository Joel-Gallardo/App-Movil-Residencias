package com.example.demo_gobierna_ciudadano

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.demo_gobierna_ciudadano.databinding.ActivityMapaBinding

class mapa : AppCompatActivity(), OnMapReadyCallback {

    var latitud: Double ? = null
    var longitud: Double ? = null
    val PERMISSION_ID = 42

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 42
    }

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //recuperar los datos enviados desde el activity anterior
        val datos = this.intent.extras
        latitud = datos!!.getString("latitud")?.toDouble()
        longitud = datos!!.getString("longitud")?.toDouble()

        if (checkPermissions()){
            Toast.makeText(this, "Generando mapa de ubicación", Toast.LENGTH_SHORT).show()
        } else {
            requestPermissions()
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )

            googleMap.isMyLocationEnabled = true
            googleMap.uiSettings.isZoomControlsEnabled = true

            googleMap?.apply {
                val ubicacion_actual = LatLng(latitud!!, longitud!!)
                addMarker(
                    MarkerOptions()
                        .position(ubicacion_actual)
                        .title("Ubicacion actual")
                )?.showInfoWindow()
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacion_actual, 17f))
            }
        }

    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_ID
        )
    }


    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

            }
        }
    }
}