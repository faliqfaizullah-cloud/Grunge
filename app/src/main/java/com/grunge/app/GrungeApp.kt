package com.grunge.app

import android.app.Application
import androidx.work.*
import java.util.concurrent.TimeUnit

class GrungeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Background refresh (location + weather) every 15 min, survives reboots.
        val req = PeriodicWorkRequestBuilder<WeatherWorker>(15, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork("grunge-refresh", ExistingPeriodicWorkPolicy.KEEP, req)
    }
}
