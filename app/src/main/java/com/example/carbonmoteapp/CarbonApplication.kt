package com.example.carbonmoteapp


import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.preference.PreferenceManager
import androidx.room.Room
import com.google.android.material.color.DynamicColors
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate

class CarbonApplication : Application() {

//    lateinit var settingsStore: SettingsStore
//    lateinit var carbonDatabase: ForestDatabase

    val currentDate = MutableStateFlow<LocalDate>(LocalDate.now())

    override fun onCreate() {
        super.onCreate()

        DynamicColors.applyToActivitiesIfAvailable(this)
//        PreferenceManager.setDefaultValues(this, R.xml.settings, false)
        registerMidnightTimer()

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
//        settingsStore = SettingsStoreImpl(sharedPreferences)

//        forestDatabase = Room.databaseBuilder(
//            applicationContext,
//            ForestDatabase::class.java,
//            ForestDatabase.DATABASE_NAME
//        ).build()
    }

    private fun registerMidnightTimer() {
        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        registerReceiver(midnightBroadcastReceiver, intentFilter)
    }

    private val midnightBroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            val today = LocalDate.now()
            if (today != currentDate.value) {
                currentDate.value = today
            }
        }
    }
}