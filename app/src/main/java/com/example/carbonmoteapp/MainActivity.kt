package com.example.carbonmoteapp

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.carbonmoteapp.databinding.ActivityMainBinding
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private var TAG = "MainActivity"
    var locationManager: LocationManager? = null
    var deltaX:TextView? = null
    var longitude:kotlin.Double = 0.0
    var latitude:kotlin.Double = 0.0
    var distance:kotlin.Double = 0.0

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var geocoder: Geocoder = Geocoder(this, Locale.US)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            askForNotificationPermission()
            askForLocationPermission()
        }
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                // Update UI with new location information
                val latitude = location.latitude
                val longitude = location.longitude
//                var address = findViewById<TextView>(R.id.id_address)
                val lon = findViewById<TextView>(R.id.id_longitude)
                val lat = findViewById<TextView>(R.id.id_latitude)
                lat.text = "Latitude: $latitude"
                lon.text = "Longitude: $longitude"
                
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        locationManager!!.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            1000,
            1f,
            locationListener
        )
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun askForNotificationPermission() {
        val notificationPermission = android.Manifest.permission.POST_NOTIFICATIONS
        val notificationPermissionStatus = ContextCompat
            .checkSelfPermission(this, notificationPermission)
        if (notificationPermissionStatus == PackageManager.PERMISSION_DENIED) {
            requestPermissionLauncher.launch(notificationPermission)
        }
    }
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun askForLocationPermission() {
        val locationPermission = android.Manifest.permission.ACCESS_FINE_LOCATION
        val notificationPermissionStatus = ContextCompat
            .checkSelfPermission(this, locationPermission)
        if (notificationPermissionStatus == PackageManager.PERMISSION_DENIED) {
            requestPermissionLauncher.launch(locationPermission)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.main_menu, menu)
//        return true
        return false
    }
}