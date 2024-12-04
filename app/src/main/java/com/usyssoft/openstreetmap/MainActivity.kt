package com.usyssoft.openstreetmap

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MainActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var myLocationOverlay: MyLocationNewOverlay
    private lateinit var locationPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = packageName
        setContentView(R.layout.activity_main)

        mapView = findViewById(R.id.mapView)

        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(15.0)

        val startPoint = GeoPoint(37.7749, -122.4194)
        mapView.controller.setCenter(startPoint)

        addMarker(startPoint, "San Francisco", "This is San Francisco.")

        addCompass()

        addScaleBar()

        resultLocationPermission()

        checkLocationPermission()



    }



    private fun addMarker(geoPoint: GeoPoint, title: String, description: String) {
        val marker = Marker(mapView)
        marker.position = geoPoint
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = title
        marker.snippet = description
        mapView.overlays.add(marker)
    }

    private fun addCompass() {
        val compassOverlay = CompassOverlay(this, mapView)
        compassOverlay.enableCompass()
        mapView.overlays.add(compassOverlay)
    }

    private fun addScaleBar() {
        val scaleBarOverlay = ScaleBarOverlay(mapView)
        scaleBarOverlay.setAlignBottom(true)
        mapView.overlays.add(scaleBarOverlay)
    }

    private fun checkLocationPermission() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1221
            )
        } else {
            showUserLocation()
        }
    }

    private fun showUserLocation() {
        println("izin verildi showUserLocation")
        val locationProvider = GpsMyLocationProvider(this)

        locationProvider.locationUpdateMinTime = 1000 // 1 saniye

        myLocationOverlay = MyLocationNewOverlay(locationProvider, mapView)
        myLocationOverlay.enableMyLocation() // Kullanıcının konumunu etkinleştir
        myLocationOverlay.enableFollowLocation() // Harita kullanıcı konumunu takip etsin

        // Overlay'i haritaya ekle
        mapView.overlays.add(myLocationOverlay)

        // Gerekirse haritayı kullanıcı konumuna ortala
        myLocationOverlay.runOnFirstFix {
            val userLocation: GeoPoint? = myLocationOverlay.myLocation
            if (userLocation != null) {
                val geoPoint = GeoPoint(userLocation.latitude, userLocation.longitude)
                runOnUiThread {
                    mapView.controller.animateTo(geoPoint) // Haritayı konuma ortala
                }
            }
        }
    }

    private fun resultLocationPermission() {
        locationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // İzin verildiyse
                println("izin verildi")
                showUserLocation()
            } else {
                println("izin verilmedi")
                // İzin reddedildiyse
                //toast("Location permission denied. Cannot show location.")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume() // Harita etkinleştirme
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause() // Harita durdurma
    }
}