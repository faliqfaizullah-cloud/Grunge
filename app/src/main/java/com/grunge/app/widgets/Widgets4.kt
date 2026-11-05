package com.grunge.app.widgets

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.util.TypedValue
import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.unit.ColorProvider
import com.grunge.app.Draw
import com.grunge.app.R
import com.grunge.app.Store
import java.io.File

private val TILE = Color(0xFF1C1C1E)
private val TILE2 = Color(0xFF2C2C2E)
private val GRAY = Color(0xFF8E8E93)

private fun gp(w: Int) = GlanceModifier.width(w.dp)
private fun gh(h: Int) = GlanceModifier.height(h.dp)

@Composable private fun Ico(res: Int, tint: Color, size: Int) =
    Image(ImageProvider(res), null, GlanceModifier.size(size.dp), colorFilter = ColorFilter.tint(ColorProvider(tint)))

/** Live TextClock as RemoteViews. */
internal fun tc(ctx: Context, fmt: String, sizeSp: Float, color: Color, bold: Boolean = true, tz: String? = null): RemoteViews {
    val rv = RemoteViews(ctx.packageName, if (bold) R.layout.tc else R.layout.tc_reg)
    rv.setCharSequence(R.id.tc, "setFormat24Hour", fmt)
    rv.setCharSequence(R.id.tc, "setFormat12Hour", fmt)
    rv.setTextViewTextSize(R.id.tc, TypedValue.COMPLEX_UNIT_SP, sizeSp)
    rv.setInt(R.id.tc, "setTextColor", color.toArgb())
    if (tz != null) try { rv.setString(R.id.tc, "setTimeZone", tz) } catch (e: Exception) {}
    return rv
}

/** Live analog clock as RemoteViews. */
internal fun analog(ctx: Context, layout: Int, tz: String? = null, dial: Bitmap? = null): RemoteViews {
    val rv = RemoteViews(ctx.packageName, layout)
    if (Build.VERSION.SDK_INT >= 31) {
        if (tz != null) rv.setString(R.id.analog, "setTimeZone", tz)
        if (dial != null) rv.setIcon(R.id.analog, "setDial", Icon.createWithBitmap(dial))
    }
    return rv
}

private fun batteryPct(ctx: Context): Int {
    val i = ctx.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    return (i?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) ?: 0) * 100 / (i?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100)
}
private fun storageFreePct(): Int {
    val s = StatFs(Environment.getDataDirectory().path)
    return (s.availableBytes * 100 / s.totalBytes).toInt()
}

// ── 1. Analog clock + two gauges + "Mon 09 30" ──
class ClockGaugeWidget : GrungeWidget("clockgauge", "#000000", "#FFFFFF") {
    @Composable override fun Body(ctx: Context, fg: Color) {
        val g1 = Draw.gauge(220, batteryPct(ctx) * 0.6f, 60)
        val g2 = Draw.gauge(220, storageFreePct() * 0.3f, 30)
        Row(GlanceModifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Box(GlanceModifier.size(104.dp).background(TILE).cornerRadius(28.dp).padding(8.dp)) {
                AndroidRemoteViews(analog(ctx, R.layout.clock_a), GlanceModifier.fillMaxSize())
            }
            Spacer(gp(12))
            Column(GlanceModifier.defaultWeight().fillMaxHeight()) {
                Row {
                    Image(ImageProvider(g1), null, GlanceModifier.size(48.dp)); Spacer(gp(8))
                    Image(ImageProvider(g2), null, GlanceModifier.size(48.dp))
                }
                Spacer(GlanceModifier.defaultWeight())
                AndroidRemoteViews(tc(ctx, "EEE  HH  mm", 24f, fg), GlanceModifier.fillMaxWidth().height(32.dp))
            }
        }
    }
}

// ── 2. Analog clock + mini clocks + bpm + "09 29" ──
class ClockBpmWidget : GrungeWidget("clockbpm", "#000000", "#FFFFFF") {
    @Composable override fun Body(ctx: Context, fg: Color) {
        val wave = Draw.wave(300, 60)
        Row(GlanceModifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Box(GlanceModifier.size(104.dp).background(TILE).cornerRadius(52.dp).padding(8.dp)) {
                AndroidRemoteViews(analog(ctx, R.layout.clock_a), GlanceModifier.fillMaxSize())
            }
            Spacer(gp(10))
            Column(GlanceModifier.defaultWeight().fillMaxHeight()) {
                Row(GlanceModifier.fillMaxWidth()) {
                    Box(GlanceModifier.size(44.dp).background(TILE).cornerRadius(22.dp).padding(4.dp)) {
                        AndroidRemoteViews(analog(ctx, R.layout.clock_small, Store.get(ctx, "clockbpm.tz1", "Asia/Tokyo")), GlanceModifier.fillMaxSize())
                    }
                    Spacer(gp(8))
                    Box(GlanceModifier.size(44.dp).background(TILE).cornerRadius(22.dp).padding(4.dp)) {
                        AndroidRemoteViews(analog(ctx, R.layout.clock_small, Store.get(ctx, "clockbpm.tz2", "Europe/London")), GlanceModifier.fillMaxSize())
                    }
                    Spacer(GlanceModifier.defaultWeight())
                    Box(GlanceModifier.height(44.dp).width(70.dp).background(TILE2).cornerRadius(22.dp), contentAlignment = Alignment.Center) {
                        AndroidRemoteViews(tc(ctx, "HH mm", 17f, fg), GlanceModifier.fillMaxWidth().height(30.dp).padding(start = 10.dp))
                    }
                }
                Spacer(gh(8))
                Box(GlanceModifier.fillMaxWidth().defaultWeight().background(TILE).cornerRadius(22.dp).padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Column(GlanceModifier.fillMaxSize()) {
                        Text(Store.get(ctx, "clockbpm.bpm", "89") + " bpm", style = t(fg.copy(alpha = .8f), 9, true))
                        Image(ImageProvider(wave), null, GlanceModifier.fillMaxWidth().defaultWeight(), contentScale = ContentScale.FillBounds)
                    }
                }
            }
        }
    }
}

// ── 3. Big digital clock with ghost lines ──
class DigitalBigWidget : GrungeWidget("digital", "#000000", "#FFFFFF") {
    @Composable override fun Body(ctx: Context, fg: Color) {
        val ghost = Color(0xFF2E2E30)
        val tz = Store.get(ctx, "digital.tz", "Asia/Tokyo")
        val lab = Store.get(ctx, "digital.label", "TOK")
        Column(GlanceModifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            AndroidRemoteViews(tc(ctx, "dd/MM/yyyy", 28f, ghost), GlanceModifier.fillMaxWidth().height(32.dp))
            Row(GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                AndroidRemoteViews(tc(ctx, "HH:mm:ss", 40f, fg), GlanceModifier.defaultWeight().height(46.dp))
                Column {
                    AndroidRemoteViews(tc(ctx, "EEE", 15f, fg), GlanceModifier.width(40.dp).height(20.dp))
                    Text(Store.get(ctx, "weather.temp", "--°"), style = t(fg.copy(alpha = .6f), 13, true))
                }
            }
            AndroidRemoteViews(tc(ctx, "'$lab' HH:mm", 28f, ghost, tz = tz), GlanceModifier.fillMaxWidth().height(32.dp))
        }
    }
}

// ── 4. App dock (Spotify / Fitness / Store / Heart / Timer) ──
class DockWidget : GrungeWidget("dock", "#000000", "#FFFFFF") {
    private data class Item(val label: String, val icon: Int, val pkg: String)
    private val items = listOf(
        Item("Spotify", R.drawable.ic_music, "com.spotify.music"),
        Item("Fitness", R.drawable.ic_fitness, "com.google.android.apps.fitness"),
        Item("Store", R.drawable.ic_bag, "com.android.vending"),
        Item("Heart", R.drawable.ic_pulse, "com.google.android.apps.healthdata"),
        Item("Timer", R.drawable.ic_timer, "com.google.android.deskclock"))

    @Composable override fun Body(ctx: Context, fg: Color) {
        val sel = Store.get(ctx, "dock.sel", "1").toIntOrNull() ?: 1
        Column(GlanceModifier.fillMaxSize()) {
            Row(GlanceModifier.fillMaxWidth().defaultWeight()) {
                items.forEachIndexed { i, it ->
                    val intent = ctx.packageManager.getLaunchIntentForPackage(it.pkg)
                        ?: Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${it.pkg}"))
                    val on = i == sel
                    Box(GlanceModifier.defaultWeight().fillMaxHeight().background(if (on) Color.White else TILE).cornerRadius(26.dp)
                        .clickable(actionStartActivity(intent)).padding(8.dp)) {
                        Column(GlanceModifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Ico(it.icon, if (on) Color.Black else Color.White, 22)
                            Spacer(GlanceModifier.defaultWeight())
                            Text(it.label, style = t(if (on) Color.Black else GRAY, 9, true), maxLines = 1)
                        }
                    }
                    if (i < items.size - 1) Spacer(gp(6))
                }
            }
            Spacer(gh(8))
            Box(GlanceModifier.width(40.dp).height(4.dp).background(Color.White).cornerRadius(2.dp)) {}
        }
    }
}

// ── 5. Calculator-style stepper (+8, −, +, =) ──
class CalcWidget : GrungeWidget("calc", "#000000", "#FFFFFF") {
    @Composable override fun Body(ctx: Context, fg: Color) {
        val n = Store.get(ctx, "calc.n", "8").toIntOrNull() ?: 8
        val total = Store.get(ctx, "calc.total", "0")
        Row(GlanceModifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Column(GlanceModifier.defaultWeight()) {
                Text(total, style = t(GRAY, 16, true))
                Text(if (n >= 0) "+$n" else "$n", style = t(fg, 46, true))
            }
            Box(GlanceModifier.size(46.dp).background(TILE2).cornerRadius(23.dp).clickable(actionRunCallback<CalcMinus>()), contentAlignment = Alignment.Center) { Text("−", style = t(fg, 22, true)) }
            Spacer(gp(8))
            Box(GlanceModifier.size(46.dp).background(TILE2).cornerRadius(23.dp).clickable(actionRunCallback<CalcPlus>()), contentAlignment = Alignment.Center) { Text("+", style = t(fg, 22, true)) }
            Spacer(gp(8))
            Box(GlanceModifier.width(46.dp).height(76.dp).background(Color(0xFFD9B8A3)).cornerRadius(23.dp).clickable(actionRunCallback<CalcEq>()), contentAlignment = Alignment.Center) { Text("=", style = t(Color(0xFF3A2A22), 24, true)) }
        }
    }
}
class CalcMinus : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        Store.put(context, "calc.n", ((Store.get(context, "calc.n", "8").toIntOrNull() ?: 8) - 1).toString()); CalcWidget().updateAll(context)
    }
}
class CalcPlus : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        Store.put(context, "calc.n", ((Store.get(context, "calc.n", "8").toIntOrNull() ?: 8) + 1).toString()); CalcWidget().updateAll(context)
    }
}
class CalcEq : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val t = (Store.get(context, "calc.total", "0").toIntOrNull() ?: 0) + (Store.get(context, "calc.n", "8").toIntOrNull() ?: 8)
        Store.put(context, "calc.total", t.toString()); CalcWidget().updateAll(context)
    }
}

// ── 6. Playlist ──
private val IDX = ActionParameters.Key<Int>("idx")
class PlaylistWidget : GrungeWidget("playlist", "#000000", "#FFFFFF") {
    @Composable override fun Body(ctx: Context, fg: Color) {
        val tracks = Store.get(ctx, "pl.tracks", "First Impressions/Coasting/Doubt/Closer/Like We Used To").split("/")
        val sel = Store.get(ctx, "pl.sel", "2").toIntOrNull() ?: 2
        val f = File(ctx.filesDir, "art.png")
        val bmp = if (f.exists()) BitmapFactory.decodeFile(f.path) else null
        Row(GlanceModifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Box(GlanceModifier.size(104.dp).background(Color(0xFFD7263D)).cornerRadius(22.dp), contentAlignment = Alignment.Center) {
                if (bmp != null) Image(ImageProvider(bmp), null, GlanceModifier.fillMaxSize().cornerRadius(22.dp), contentScale = ContentScale.Crop)
                else Text("♪", style = t(Color.White, 34, true))
            }
            Spacer(gp(12))
            Column(GlanceModifier.defaultWeight().fillMaxHeight(), verticalAlignment = Alignment.CenterVertically) {
                tracks.take(5).forEachIndexed { i, name ->
                    Box(GlanceModifier.fillMaxWidth().height(22.dp).background(if (i == sel) Color(0xFF3A3A3C) else Color(0x00000000))
                        .cornerRadius(11.dp).padding(horizontal = 8.dp).clickable(actionRunCallback<PlaylistSelect>(actionParametersOf(IDX to i))),
                        contentAlignment = Alignment.CenterStart) {
                        Text(name, style = t(if (i == sel) Color.White else GRAY, 13, true), maxLines = 1)
                    }
                }
            }
        }
    }
}
class PlaylistSelect : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        Store.put(context, "pl.sel", (parameters[IDX] ?: 0).toString()); PlaylistWidget().updateAll(context)
    }
}

// ── 7. Ring clock + TOK pill + date pill ──
class RingClockWidget : GrungeWidget("ringclock", "#000000", "#FFFFFF") {
    @Composable override fun Body(ctx: Context, fg: Color) {
        val tz = Store.get(ctx, "ringclock.tz", "Asia/Tokyo")
        val lab = Store.get(ctx, "ringclock.label", "TOK")
        Row(GlanceModifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Box(GlanceModifier.size(112.dp), contentAlignment = Alignment.Center) {
                AndroidRemoteViews(analog(ctx, R.layout.clock_ring, null, Draw.ring(400)), GlanceModifier.fillMaxSize())
                Box(GlanceModifier.size(width = 66.dp, height = 44.dp).background(TILE2).cornerRadius(22.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(lab, style = t(GRAY, 8, true))
                        AndroidRemoteViews(tc(ctx, "HH:mm", 14f, fg, tz = tz), GlanceModifier.width(48.dp).height(18.dp))
                    }
                }
            }
            Spacer(GlanceModifier.defaultWeight())
            Box(GlanceModifier.width(86.dp).fillMaxHeight().background(TILE).cornerRadius(28.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AndroidRemoteViews(tc(ctx, "dd", 34f, fg), GlanceModifier.width(50.dp).height(40.dp))
                    AndroidRemoteViews(tc(ctx, "MMM", 13f, GRAY, bold = false), GlanceModifier.width(40.dp).height(18.dp))
                }
            }
        }
    }
}
