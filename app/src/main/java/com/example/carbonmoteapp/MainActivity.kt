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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale

class MainActivity : AppCompatActivity(), SensorEventListener {
    private var locationManager: LocationManager? = null

    private var sensorManager : SensorManager? = null
    private var running = false
    private var total_steps = 0f
    private var previousTotalSteps = 0f

    private var distance: Double = 0.0
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    private var startLatitude: Double = 0.0
    private var startLongitude: Double = 0.0
    private var localCredits: Int = 0

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    private val retrofit = Retrofit.Builder()
        .baseUrl("YOUR_BASE_URL") // Replace with your Next.js server base URL
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiInterface = retrofit.create(ApiInterface::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Set Listener for button
        val buttonStart = findViewById<Button>(R.id.button_start)
        buttonStart.setOnClickListener {
            if (!running) {
                setStartPoint()
            }
            running = !running
            updateButtonLabel(buttonStart) // Update button text
        }

        //Get Step sensor
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

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

                    //Calculate Distance - Scale it for quick demo
                    setDistance(DistanceCalculator.calculateDistance(startLatitude, startLongitude, latitude, longitude) * 100)

                    //Update UI
                    val dist = findViewById<TextView>(R.id.id_distance)
                    val formattedDistance = String.format("Distance: %.4f Km", distance)
                    dist.text = formattedDistance

                    //Update progress and check for Credit issuance
                    checkProgress(distance)
                }

            }

            @Deprecated("OnStatusChanged is deprecated or something idk")
            override fun onStatusChanged(provider: String, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        locationManager!!.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            2000,
            1f,
            locationListener
        )

    }

    override fun onResume() {
        super.onResume()
        val stepSensor : Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        Log.d("MainActivity", "OnResume, getting step sensor");

        if(stepSensor == null){
            Toast.makeText(this, "No sensor detected on this device", Toast.LENGTH_SHORT).show()
        }else{
            Log.d("MainActivity", "Got step sensor, registering sensor...");
            sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(running){
            total_steps = event!!.values[0]
            val currentSteps = total_steps.toInt() - previousTotalSteps.toInt()

            //Update display text
            val stepsTaken = findViewById<TextView>(R.id.tv_stepsTaken)
            stepsTaken.text = "$currentSteps"
        }
    }

    private fun resetSteps() {
        total_steps = 0f
        val stepsTaken = findViewById<TextView>(R.id.tv_stepsTaken)
        stepsTaken.text = 0.toString()
    }

    private fun saveData(){
        //Should write current values to the DB. - This should be called
        val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val editor  = sharedPreferences.edit()
        editor.putFloat("key1", previousTotalSteps)
        editor.apply()
    }

    private fun loadData(){
        //Should load values from the DB - Should only be called once OnCreate
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

    private fun issueCarbonCredit(){
        //Update Local value the value
        localCredits += 1
        val carbonCredits = findViewById<TextView>(R.id.carbonCredits)
        carbonCredits.text = "$localCredits"

        //Save all data to the DB
        saveData()
    }
    private fun checkProgress(distance : Double){
        //If distance progress is maxed, then we need to issue credit then reset distance
        if(distance >= 100){
            issueCarbonCredit()
            setStartPoint()
            setDistance(0.0)

            //reset progrss bar
            val progressCircular = findViewById<com.mikhaellopez.circularprogressbar.CircularProgressBar>(R.id.circularProgressBar)
            //Reset progress bar
            progressCircular.apply {
                setProgressWithAnimation(0f)
            }
        }else {

            //Update progress circular bar
            val progressCircular =
                findViewById<com.mikhaellopez.circularprogressbar.CircularProgressBar>(R.id.circularProgressBar)
            progressCircular.apply {
                setProgressWithAnimation(distance.toFloat())
            }
        }
    }

    private fun setStartPoint(){
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
        // Capture the start latitude and longitude
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        if (lastKnownLocation != null) {
            startLatitude = lastKnownLocation.latitude
            startLongitude = lastKnownLocation.longitude
        }
    }

    private fun setDistance(dist : Double){
        distance = dist
    }

    private fun fetchData(id: Int?) {
        val call = apiInterface.getData(id)
        call.enqueue(object : Callback<List<DataDAO>> {
            override fun onResponse(
                call: Call<List<DataDAO>>,
                response: Response<List<DataDAO>>
            ) {
                if (response.isSuccessful) {
                    val data = response.body()
                    // Process fetched data
                } else {
                    // Handle unsuccessful response
                }
            }

            override fun onFailure(call: Call<List<DataDAO>>, t: Throwable) {
                // Handle network error
            }
        })
    }

    private fun postData() {
        val dataToPost = DataDAO(0, 10.0, 5, 15) // Replace with actual data
        val call = apiInterface.postData(dataToPost)
        call.enqueue(object : Callback<DataDAO> {
            override fun onResponse(
                call: Call<DataDAO>,
                response: Response<DataDAO>
            ) {
                if (response.isSuccessful) {
                    val postedData = response.body()
                    Log.d("MainActivityDEBUG", "$postedData")
                } else {
                    // Handle unsuccessful response
                }
            }

            override fun onFailure(call: Call<DataDAO>, t: Throwable) {
                // Handle network error
            }
        })
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