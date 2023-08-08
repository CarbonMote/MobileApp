package com.example.carbonmoteapp

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.carbonmoteapp.databinding.ActivityMainBinding
import com.example.carbonmoteapp.service.StepCounterService

class MainActivity : AppCompatActivity() {
    private var TAG = "MainActivity"

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        startStepCounterService()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            askForNotificationPermission()
        }
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

    private fun startStepCounterService() {
        Log.d(TAG, "Attempting to start service")
        val intent = Intent(this, StepCounterService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.main_menu, menu)
//        return true
        return false
    }
}