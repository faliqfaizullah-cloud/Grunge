package com.grunge.app

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.updateAll
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.grunge.app.widgets.*
import kotlinx.coroutines.launch

private data class Field(val key: String, val label: String, val def: String)
private data class Entry(val name: String, val widget: GrungeWidget, val fields: List<Field>)

private val entries = listOf(
    Entry("Weather", WeatherWidget(), colors("weather", "#111111", "#FFFFFF")),
    Entry("Battery", BatteryWidget(), colors("battery", "#111111", "#FFFFFF")),
    Entry("Clock", ClockWidget(), colors("clock", "#111111", "#FFFFFF")),
    Entry("Year progress", YearWidget(), colors("year", "#1B2238", "#F3EAD3")),
    Entry("Quote", QuoteWidget(), colors("quote", "#FFFFFF", "#111111") +
        listOf(Field("quote.text", "Quote", "In the middle of difficulty lies opportunity."), Field("quote.author", "Author", "Albert Einstein"))),
    Entry("Next flight", FlightWidget(), colors("flight", "#1C1C1E", "#F2F2F2") +
        listOf(Field("flight.route", "Route", "Tokyo - London"), Field("flight.dest", "Destination", "TOKYO"),
            Field("flight.date", "Date", "15 JAN"), Field("flight.time", "Time", "13:30"))),
    Entry("Weather text", CityWeatherWidget(), colors("cityweather", "#1F1F1F", "#FFFFFF")),
    Entry("Meeting", MeetingWidget(), colors("meeting", "#E7B5F5", "#111111") + listOf(Field("meeting.who", "Who", "@JackReed"), Field("meeting.title", "Place / title", "SAITO"), Field("meeting.when", "When", "12 Jan, 20:00"))),
    Entry("Stopwatch", StopwatchWidget(), colors("stopwatch", "#E8441C", "#FFFFFF")),
    Entry("Profile", ProfileWidget(), colors("profile", "#F7B6C8", "#3A2A2F") + listOf(Field("profile.name", "Username", "Hiroto_98"), Field("profile.likes", "Likes", "8"), Field("profile.comments", "Comments", "4"), Field("profile.shares", "Shares", "9"))),
    Entry("Contact", ContactWidget(), colors("contact", "#4B4B4D", "#FFFFFF") + listOf(Field("contact.name", "Name", "Zion Carter"), Field("contact.sub", "Subtitle", "best dude"), Field("contact.phone", "Phone number", ""))),
    Entry("Drone battery", DroneWidget(), colors("drone", "#222222", "#FFFFFF") + listOf(Field("drone.name", "Device", "Dron DJI Neo"), Field("drone.mah", "Capacity", "1435 mAh"), Field("drone.level", "Level %", "60"), Field("drone.status", "Status", "Charging..."))),
    Entry("Balance card", BalanceWidget(), colors("balance", "#A8C257", "#FFFFFF") + listOf(Field("balance.amount", "Balance", "\$450,80"), Field("balance.card", "Card", "5294 **** **** 2468"))),
    Entry("USDC", UsdcWidget(), listOf(Field("usdc.fg", "Text #hex", "#111111"), Field("usdc.amount", "Amount", "987.86"), Field("usdc.change", "Change", "-99.56"))),
    Entry("Now playing", MusicWidget(), colors("music", "#4A4A4A", "#FFFFFF")),
    Entry("Lamp", LampWidget(), colors("lamp", "#1E1E1E", "#FFFFFF")),
)
private fun colors(k: String, bg: String, fg: String) =
    listOf(Field("$k.bg", "Background #hex", bg), Field("$k.fg", "Text #hex", fg))

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { Screen() } }
    }

    @Composable private fun Screen() {
        val scope = rememberCoroutineScope()
        val bgLoc = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            WorkManager.getInstance(this).enqueue(OneTimeWorkRequestBuilder<WeatherWorker>().build())
        }
        val fgLoc = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (Build.VERSION.SDK_INT >= 29) bgLoc.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        LazyColumn(Modifier.fillMaxSize().padding(16.dp).statusBarsPadding(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Text("Grunge", style = MaterialTheme.typography.headlineLarge)
                Text("Long-press your home screen → Widgets → Grunge. Customise below.")
                Spacer(Modifier.height(8.dp))
                Button({ fgLoc.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) }) {
                    Text("Allow location (weather)")
                }
                OutlinedButton({
                    startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:$packageName")))
                }) { Text("Allow background usage") }
                OutlinedButton({ startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }) { Text("Allow media access (Now playing)") }
            }
            items(entries) { e ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(e.name, style = MaterialTheme.typography.titleMedium)
                        val vals = remember { e.fields.associate { it.key to mutableStateOf(Store.get(this@MainActivity, it.key, it.def)) } }
                        e.fields.forEach { f ->
                            OutlinedTextField(vals[f.key]!!.value, { vals[f.key]!!.value = it },
                                label = { Text(f.label) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        }
                        Button({
                            vals.forEach { (k, v) -> Store.put(this@MainActivity, k, v.value) }
                            scope.launch { e.widget.updateAll(this@MainActivity) }
                        }) { Text("Apply") }
                    }
                }
            }
        }
    }
}
