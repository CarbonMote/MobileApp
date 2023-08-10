package com.example.carbonmoteapp

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.carbonmoteapp.databinding.ActivityMainBinding
import java.util.Locale

class MainActivity : AppCompatActivity(), SensorEventListener {
    var locationManager: LocationManager? = null
    var deltaX:TextView? = null
    var distance: Double = 0.0

    private var sensorManager : SensorManager? = null
    private var running = false
    private var total_steps = 0f
    private var previousTotalSteps = 0f

    var latitude: Double = 0.0
    var longitude: Double = 0.0
    private var startLatitude: Double = 0.0
    private var startLongitude: Double = 0.0
//    private var endLatitude: Double = 0.0
//    private var endLongitude: Double = 0.0

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Set Listener for button
        val buttonStart = findViewById<Button>(R.id.button_start)
        buttonStart.setOnClickListener {
            if (!running) {
                // Capture the start latitude and longitude
                val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
                val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (lastKnownLocation != null) {
                    startLatitude = lastKnownLocation.latitude
                    startLongitude = lastKnownLocation.longitude
                }
            }
//            else {
//                // Capture the end latitude and longitude
//                val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
//                val lastKnownLocation =
//                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
//                if (lastKnownLocation != null) {
//                    endLatitude = lastKnownLocation.latitude
//                    endLongitude = lastKnownLocation.longitude
//                }
//            }
            running = !running // Toggle the "running" variable

            updateButtonLabel(buttonStart) // Update button text
        }

        //Get Step sensor
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
//        var geocoder: Geocoder = Geocoder(this, Locale.US)

        //Check for old data
        loadData()
        resetSteps()

        //Ask for location permission
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION
            val locationPermission2 = Manifest.permission.ACCESS_COARSE_LOCATION
            requestPermissionLauncher.launch(locationPermission)
            requestPermissionLauncher.launch(locationPermission2)
        }

        //Ask for activity permission
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val activityPermission = Manifest.permission.ACTIVITY_RECOGNITION
            requestPermissionLauncher.launch(activityPermission)
        }

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                if(running) {
                    // Update UI with new location information
                    latitude = location.latitude
                    longitude = location.longitude
                    val lon = findViewById<TextView>(R.id.id_longitude)
                    val lat = findViewById<TextView>(R.id.id_latitude)
                    val dist = findViewById<TextView>(R.id.id_distance)
                    lat.text = "Latitude: $latitude"
                    lon.text = "Longitude: $longitude"

                    // Compare old and new values
                    val latitudeDifference = latitude - startLatitude
                    val longitudeDifference = longitude - startLongitude
                    dist.text = "$latitudeDifference | $longitudeDifference"
                }

            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        locationManager!!.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            1000,
            1f,
            locationListener
        )

    }

    override fun onResume() {
        super.onResume()
        running = true
        val stepSensor : Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        Log.d("MainActivity", "OnResume, getting step sensor");

        if(stepSensor == null){
            Toast.makeText(this, "No sensor detected on this device", Toast.LENGTH_SHORT).show()
        }else{
            Log.d("MainActivity", "Got step sensor, registering sensor...");
            sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI!!)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(running){
            total_steps = event!!.values[0]
            val currentSteps = total_steps.toInt() - previousTotalSteps.toInt()

            //Update display text
            val stepsTaken = findViewById<TextView>(R.id.tv_stepsTaken)
            stepsTaken.text = "$currentSteps"

            //Update progress circular bar
            val progressCircular = findViewById<com.mikhaellopez.circularprogressbar.CircularProgressBar>(R.id.circularProgressBar)
            progressCircular.apply {
               setProgressWithAnimation(currentSteps.toFloat())
            }
        }
    }

    private fun resetSteps(){
        val stepsTaken = findViewById<TextView>(R.id.tv_stepsTaken)
        val progressCircular = findViewById<com.mikhaellopez.circularprogressbar.CircularProgressBar>(R.id.circularProgressBar)

        //Check for single tap
        stepsTaken.setOnClickListener { Toast.makeText(this, "Long Tap to Reset Steps", Toast.LENGTH_SHORT).show() }

        //Check for long tap
        stepsTaken.setOnLongClickListener {
            previousTotalSteps = total_steps

            //Reset steps
            stepsTaken.text = 0.toString()

            //Reset progress bar
            progressCircular.apply {
                setProgressWithAnimation(0f)
            }

            saveData()
            true
        }
    }

    private fun saveData(){
        val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val editor  = sharedPreferences.edit()
        editor.putFloat("key1", previousTotalSteps)
        editor.apply()
    }

    private fun loadData(){
        val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val savedNumber = sharedPreferences.getFloat("key1", 0f)
        Log.d("MainActivity", "$savedNumber")
        previousTotalSteps = savedNumber
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    private fun updateButtonLabel(button: Button) {
        if (running) {
            button.text = "Stop"
        } else {
            button.text = "Start"
        }
    }
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun askForLocationPermission() {
        Log.d("MainActivity", "Asking for location permission");
        val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION
        val notificationPermissionStatus = ContextCompat
            .checkSelfPermission(this, locationPermission)
        if (notificationPermissionStatus == PackageManager.PERMISSION_DENIED) {
            requestPermissionLauncher.launch(locationPermission)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun askForActivityPermission() {
        Log.d("MainActivity", "Asking for activity permission");
        val activityPermission = Manifest.permission.ACTIVITY_RECOGNITION
        val activityPermissionStatus = ContextCompat
            .checkSelfPermission(this, activityPermission)
        if (activityPermissionStatus == PackageManager.PERMISSION_DENIED) {
            requestPermissionLauncher.launch(activityPermission)
        }
    }
}