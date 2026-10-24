package com.grunge.app.widgets

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import com.grunge.app.R
import com.grunge.app.Store
import java.util.Calendar

private fun t(fg: Color, size: Int, bold: Boolean = false, mono: Boolean = false, serif: Boolean = false) = TextStyle(
    color = ColorProvider(fg), fontSize = size.sp,
    fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
    fontFamily = if (mono) FontFamily.Monospace else if (serif) FontFamily.Serif else FontFamily.SansSerif)

// 1 ─ Weather (location + background worker)
class WeatherWidget : GrungeWidget("weather", "#111111", "#FFFFFF") {
    @Composable override fun Body(ctx: Context, fg: Color) {
        Column(GlanceModifier.fillMaxSize()) {
            Text(Store.get(ctx, "weather.temp", "--°"), style = t(fg, 30))
            Spacer(GlanceModifier.defaultWeight())
            Text(Store.get(ctx, "weather.msg", "Open Grunge & allow location"), style = t(fg.copy(alpha = .6f), 11))
        }
    }
}

// 2 ─ Battery
class BatteryWidget : GrungeWidget("battery", "#111111", "#FFFFFF") {
    @Composable override fun Body(ctx: Context, fg: Color) {
        val i = ctx.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val pct = (i?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) ?: 0) * 100 / (i?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100)
        val charging = (i?.getIntExtra(BatteryManager.EXTRA_STATUS, 0) ?: 0) == BatteryManager.BATTERY_STATUS_CHARGING
        Column(GlanceModifier.fillMaxSize()) {
            Text("$pct%", style = t(fg, 30))
            Text(if (charging) "Charging" else "On battery", style = t(fg.copy(alpha = .6f), 11))
            Spacer(GlanceModifier.defaultWeight())
            Row {
                val on = pct * 15 / 100
                for (n in 0 until 15) {
                    Box(GlanceModifier.width(3.dp).height(16.dp)
                        .background(if (n < on) Color(0xFFE5483B) else fg.copy(alpha = .2f))) {}
                    Spacer(GlanceModifier.width(2.dp))
                }
            }
        }
    }
}

// 3 ─ Red digital clock (live TextClock)
class ClockWidget : GrungeWidget("clock", "#111111", "#FFFFFF") {
    @Composable override fun Body(ctx: Context, fg: Color) {
        Column(GlanceModifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(GlanceModifier.fillMaxWidth().height(38.dp).background(Color(0xFFE00F12)).cornerRadius(20.dp)) {}
            Spacer(GlanceModifier.defaultWeight())
            val rv = RemoteViews(ctx.packageName, R.layout.clock_digital)
            rv.setInt(R.id.clock, "setTextColor", android.graphics.Color.argb(
                (fg.alpha * 255).toInt(), (fg.red * 255).toInt(), (fg.green * 255).toInt(), (fg.blue * 255).toInt()))
            AndroidRemoteViews(rv, GlanceModifier.fillMaxWidth().height(44.dp))
        }
    }
}

// 4 ─ Year progress (stretchable)
class YearWidget : GrungeWidget("year", "#1B2238", "#F3EAD3") {
    @Composable override fun Body(ctx: Context, fg: Color) {
        val c = Calendar.getInstance()
        val day = c.get(Calendar.DAY_OF_YEAR); val total = c.getActualMaximum(Calendar.DAY_OF_YEAR)
        val weeks = Math.ceil((total - day) / 7.0).toInt()
        Column(GlanceModifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("YEAR PROGRESS", style = t(fg.copy(alpha = .7f), 10, true))
            Spacer(GlanceModifier.defaultWeight())
            Text("${day * 100 / total}%", style = t(fg, 40, true, serif = true))
            Spacer(GlanceModifier.defaultWeight())
            Text("$weeks WEEKS LEFT", style = t(fg.copy(alpha = .7f), 10, true))
        }
    }
}

// 5 ─ Quote (stretchable, editable)
class QuoteWidget : GrungeWidget("quote", "#FFFFFF", "#111111") {
    @Composable override fun Body(ctx: Context, fg: Color) {
        Column(GlanceModifier.fillMaxSize()) {
            Text("“", style = t(fg, 28, true, serif = true))
            Text(Store.get(ctx, "quote.text", "In the middle of difficulty lies opportunity."), style = t(fg, 14, true, serif = true))
            Spacer(GlanceModifier.defaultWeight())
            Text(Store.get(ctx, "quote.author", "Albert Einstein"), style = t(fg.copy(alpha = .6f), 11, serif = true))
        }
    }
}

// 6 ─ Next flight board (stretchable, editable)
class FlightWidget : GrungeWidget("flight", "#1C1C1E", "#F2F2F2") {
    @Composable override fun Body(ctx: Context, fg: Color) {
        Column(GlanceModifier.fillMaxSize()) {
            Text("Next flight", style = t(fg, 13, true))
            Text(Store.get(ctx, "flight.route", "Tokyo - London"), style = t(fg.copy(alpha = .5f), 10))
            Spacer(GlanceModifier.defaultWeight())
            Text(Store.get(ctx, "flight.dest", "TOKYO").uppercase(), style = t(fg, 18, true, mono = true))
            Text(Store.get(ctx, "flight.date", "15 JAN").uppercase(), style = t(fg, 18, true, mono = true))
            Text(Store.get(ctx, "flight.time", "13:30"), style = t(fg, 18, true, mono = true))
        }
    }
}
