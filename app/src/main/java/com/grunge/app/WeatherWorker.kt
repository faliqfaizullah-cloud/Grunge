package com.grunge.app

import android.content.Context
import android.location.LocationManager
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.grunge.app.widgets.WeatherWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.util.Calendar
import kotlin.math.roundToInt

class WeatherWorker(c: Context, p: WorkerParameters) : CoroutineWorker(c, p) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val lm = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val loc = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)
                .mapNotNull { try { lm.getLastKnownLocation(it) } catch (e: SecurityException) { null } }
                .maxByOrNull { it.time } ?: return@withContext Result.retry()
            val url = "https://api.open-meteo.com/v1/forecast?latitude=${loc.latitude}&longitude=${loc.longitude}" +
                "&current=temperature_2m&hourly=precipitation_probability&forecast_days=2&timezone=auto"
            val j = JSONObject(URL(url).readText())
            val temp = j.getJSONObject("current").getDouble("temperature_2m").roundToInt()
            val pr = j.getJSONObject("hourly").getJSONArray("precipitation_probability")
            val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val rain = (h..h + 2).any { it < pr.length() && pr.getInt(it) >= 50 }
            Store.put(applicationContext, "weather.temp", "$temp°")
            Store.put(applicationContext, "weather.msg",
                if (rain) "Rain is expected in the next 2 hours" else "No rain is expected in the next 2 hours")
            WeatherWidget().updateAll(applicationContext)
            Result.success()
        } catch (e: Exception) { Result.retry() }
    }
}
