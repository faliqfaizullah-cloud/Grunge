package com.grunge.app.widgets

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.SystemClock
import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
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
import com.grunge.app.DotMatrix
import com.grunge.app.MediaListener
import com.grunge.app.R
import com.grunge.app.Store
import java.io.File

private val LIME = Color(0xFFD4F36B)
private val LILAC = Color(0xFFE3B8F5)
private val INK = Color(0xFF111111)
private val DARK = Color(0xFF262626)
private val RED = Color(0xFFE5483B)
private val ORANGE = Color(0xFFE8512F)

private fun circle(size: Int, bg: Color, m: GlanceModifier = GlanceModifier) =
    m.size(size.dp).background(bg).cornerRadius((size / 2).dp)

private fun sp(w: Int) = GlanceModifier.width(w.dp)
private fun sh(h: Int) = GlanceModifier.height(h.dp)

@Composable private fun Icon(res: Int, tint: Color, size: Int) =
    Image(ImageProvider(res), null, GlanceModifier.size(size.dp), colorFilter = ColorFilter.tint(ColorProvider(tint)))

// "It's 3° now in Tokyo. Will be rainy and cold."
class CityWeatherWidget : GrungeWidget("cityweather", "#1F1F1F", "#FFFFFF") {
    @Composable override fun Body(ctx: Context, fg: Color) {
        val dim = fg.copy(alpha = .5f)
        val city = Store.get(ctx, "weather.city", "your area").split(" ")
        val toks = mutableListOf<Pair<String, Color>>()
        toks += "It's" to dim; toks += Store.get(ctx, "weather.temp", "--°") to fg; toks += "now" to dim; toks += "in" to dim
        city.forEachIndexed { i, w -> toks += (if (i == city.size - 1) "$w." else w) to Color(0xFFD9A7F5) }
        toks += "Will" to dim; toks += "be" to dim
        Store.get(ctx, "weather.cond", "clear").split(" ").forEach { toks += it to fg }
        val lines = mutableListOf(mutableListOf<Pair<String, Color>>()); var len = 0
        for (tk in toks) {
            val add = tk.first.length + if (lines.last().isEmpty()) 0 else 1
            if (len + add > 12 && lines.last().isNotEmpty()) { lines += mutableListOf<Pair<String, Color>>(); len = tk.first.length } else len += add
            lines.last() += tk
        }
        Column(GlanceModifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            lines.forEach { l -> Row { l.forEachIndexed { i, (w, c) -> Text(if (i < l.size - 1) "$w " else w, style = t(c, 19, true)) } } }
        }
    }
}

// Meeting card
class MeetingWidget : GrungeWidget("meeting", "#E7B5F5", "#111111") {
    @Composable override fun Body(ctx: Context, fg: Color) {
        val who = Store.get(ctx, "meeting.who", "@JackReed")
        Column(GlanceModifier.fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(circle(26, INK), contentAlignment = Alignment.Center) { Text(who.removePrefix("@").take(1).uppercase(), style = t(Color.White, 12, true)) }
                Spacer(sp(6))
                Column { Text("Meeting", style = t(fg, 14, true)); Text("with $who", style = t(RED, 10, true)) }
            }
            Spacer(sh(6))
            Box(GlanceModifier.fillMaxWidth().defaultWeight().background(Color(0xFF1B1B1B)).cornerRadius(18.dp).padding(6.dp)) {
                Column(GlanceModifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(Store.get(ctx, "meeting.title", "SAITO"), style = t(LIME, 18, true))
                    Spacer(GlanceModifier.defaultWeight())
                    Box(GlanceModifier.background(INK).cornerRadius(10.dp).padding(horizontal = 8.dp, vertical = 3.dp)) {
                        Text(Store.get(ctx, "meeting.when", "12 Jan, 20:00"), style = t(Color.White, 9, true))
                    }
                }
            }
        }
    }
}

// Stopwatch (live Chronometer). Tap play to start/stop, tap the cat to reset.
class StopwatchWidget : GrungeWidget("stopwatch", "#E8441C", "#FFFFFF") {
    @Composable override fun Body(ctx: Context, fg: Color) {
        val running = Store.get(ctx, "sw.running", "0") == "1"
        val el = Store.get(ctx, "sw.elapsed", "0").toLong()
        val base = if (running) Store.get(ctx, "sw.base", "0").toLong() else SystemClock.elapsedRealtime() - el
        val rv = RemoteViews(ctx.packageName, R.layout.stopwatch)
        rv.setChronometer(R.id.chrono, base, null, running)
        Column(GlanceModifier.fillMaxSize()) {
            Row(GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(circle(28, DARK, GlanceModifier.clickable(actionRunCallback<ToggleStopwatch>())), contentAlignment = Alignment.Center) {
                    Text(if (running) "II" else "▶", style = t(Color.White, 11, true))
                }
                Spacer(sp(6))
                AndroidRemoteViews(rv, GlanceModifier.defaultWeight().height(28.dp))
            }
            Spacer(GlanceModifier.defaultWeight())
            Image(ImageProvider(R.drawable.cat), null,
                GlanceModifier.fillMaxWidth().height(62.dp).clickable(actionRunCallback<ResetStopwatch>()), contentScale = ContentScale.Fit)
        }
    }
}
class ToggleStopwatch : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val now = SystemClock.elapsedRealtime()
        if (Store.get(context, "sw.running", "0") == "1") {
            Store.put(context, "sw.elapsed", (now - Store.get(context, "sw.base", "0").toLong()).toString()); Store.put(context, "sw.running", "0")
        } else {
            Store.put(context, "sw.base", (now - Store.get(context, "sw.elapsed", "0").toLong()).toString()); Store.put(context, "sw.running", "1")
        }
        StopwatchWidget().updateAll(context)
    }
}
class ResetStopwatch : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        Store.put(context, "sw.running", "0"); Store.put(context, "sw.elapsed", "0")
        StopwatchWidget().updateAll(context)
    }
}

// Profile card
class ProfileWidget : GrungeWidget("profile", "#F7B6C8", "#3A2A2F") {
    @Composable private fun Pill(s: String) =
        Box(GlanceModifier.background(ORANGE).cornerRadius(10.dp).padding(horizontal = 5.dp, vertical = 3.dp)) { Text(s, style = t(Color.White, 9, true)) }

    @Composable override fun Body(ctx: Context, fg: Color) {
        Column(GlanceModifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(circle(46, Color(0xFFFAD0DC)), contentAlignment = Alignment.Center) { Icon(R.drawable.ball, Color(0xFFE0527A), 30) }
            Spacer(GlanceModifier.defaultWeight())
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(Store.get(ctx, "profile.name", "Hiroto_98"), style = t(fg, 12, true))
                Text(" ✔", style = t(ORANGE, 11, true))
            }
            Spacer(sh(6))
            Row {
                Pill("♥ " + Store.get(ctx, "profile.likes", "8")); Spacer(sp(3))
                Pill("● " + Store.get(ctx, "profile.comments", "4")); Spacer(sp(3))
                Pill("➤ " + Store.get(ctx, "profile.shares", "9"))
            }
        }
    }
}

// Contact card (call / message / video)
class ContactWidget : GrungeWidget("contact", "#4B4B4D", "#FFFFFF") {
    @Composable private fun Btn(icon: Int, intent: Intent) =
        Box(circle(28, Color(0xFF2A2A2A), GlanceModifier.clickable(actionStartActivity(intent))), contentAlignment = Alignment.Center) { Icon(icon, LIME, 14) }

    @Composable override fun Body(ctx: Context, fg: Color) {
        val name = Store.get(ctx, "contact.name", "Zion Carter")
        val phone = Store.get(ctx, "contact.phone", "")
        Column(GlanceModifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(circle(40, Color(0xFFD9D9D9)), contentAlignment = Alignment.Center) { Text(name.take(1).uppercase(), style = t(INK, 16, true)) }
            Spacer(sh(4))
            Text(name, style = t(fg, 12, true))
            Text(Store.get(ctx, "contact.sub", "best dude"), style = t(fg.copy(alpha = .5f), 9, true))
            Spacer(GlanceModifier.defaultWeight())
            Row {
                Btn(R.drawable.ic_call, Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))); Spacer(sp(4))
                Btn(R.drawable.ic_chat, Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phone"))); Spacer(sp(4))
                Btn(R.drawable.ic_video, Intent(Intent.ACTION_VIEW, Uri.parse("https://meet.google.com/new")))
            }
        }
    }
}

// Drone battery
class DroneWidget : GrungeWidget("drone", "#222222", "#FFFFFF") {
    @Composable override fun Body(ctx: Context, fg: Color) {
        val pct = Store.get(ctx, "drone.level", "60").toIntOrNull()?.coerceIn(0, 100) ?: 60
        Column(GlanceModifier.fillMaxSize()) {
            Row(GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(R.drawable.drone, fg.copy(alpha = .35f), 30)
                Spacer(GlanceModifier.defaultWeight())
                Box(circle(24, Color(0xFF333333)), contentAlignment = Alignment.Center) { Text("•••", style = t(fg, 8, true)) }
            }
            Spacer(GlanceModifier.defaultWeight())
            Column(GlanceModifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                Text(Store.get(ctx, "drone.name", "Dron DJI Neo"), style = t(fg, 10, true))
                Text(Store.get(ctx, "drone.mah", "1435 mAh"), style = t(fg.copy(alpha = .5f), 9, true))
            }
            Spacer(sh(5))
            Box(GlanceModifier.fillMaxWidth().height(16.dp).background(Color(0xFF333333)).cornerRadius(8.dp).padding(2.dp)) {
                Box(GlanceModifier.width(maxOf(10f, pct * 0.9f).dp).fillMaxHeight().background(LIME).cornerRadius(6.dp)) {}
            }
            Spacer(sh(4))
            Text("⚡ " + Store.get(ctx, "drone.status", "Charging..."), style = t(fg.copy(alpha = .5f), 9))
        }
    }
}

// Balance card
class BalanceWidget : GrungeWidget("balance", "#A8C257", "#FFFFFF") {
    @Composable override fun Body(ctx: Context, fg: Color) {
        Column(GlanceModifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(GlanceModifier.fillMaxWidth().height(66.dp).background(ImageProvider(R.drawable.bg_card_green)).cornerRadius(14.dp).padding(8.dp)) {
                Column(GlanceModifier.fillMaxSize()) {
                    Text("Balance", style = t(fg.copy(alpha = .85f), 9, true))
                    Spacer(GlanceModifier.defaultWeight())
                    Text(Store.get(ctx, "balance.amount", "\$450,80"), style = t(fg, 18, true))
                    Text(Store.get(ctx, "balance.card", "5294 **** **** 2468"), style = t(fg.copy(alpha = .7f), 7))
                }
            }
            Spacer(GlanceModifier.defaultWeight())
            Row(GlanceModifier.background(Color(0xFF1E1E1E)).cornerRadius(16.dp).padding(horizontal = 6.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(circle(22, Color(0xFFF2B6E6)), contentAlignment = Alignment.Center) { Text("▭", style = t(INK, 11, true)) }
                Spacer(sp(8)); Text("↗", style = t(fg.copy(alpha = .5f), 12, true)); Spacer(sp(4))
            }
        }
    }
}

// USDC card
class UsdcWidget : GrungeWidget("usdc", "#E3B8F5", "#111111", R.drawable.bg_lilac) {
    @Composable override fun Body(ctx: Context, fg: Color) {
        Column(GlanceModifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(GlanceModifier.fillMaxWidth()) { Box(circle(26, INK), contentAlignment = Alignment.Center) { Text("\$", style = t(LIME, 12, true)) } }
            Text("USDC", style = t(fg.copy(alpha = .6f), 9, true))
            Text(Store.get(ctx, "usdc.amount", "987.86"), style = t(fg, 26, true))
            Text(Store.get(ctx, "usdc.change", "-99.56"), style = t(RED, 12, true))
            Spacer(GlanceModifier.defaultWeight())
            Row(GlanceModifier.fillMaxWidth()) {
                Box(GlanceModifier.defaultWeight().height(26.dp).background(INK).cornerRadius(13.dp), contentAlignment = Alignment.Center) { Text("Send", style = t(Color.White, 10, true)) }
                Spacer(sp(4))
                Box(GlanceModifier.defaultWeight().height(26.dp).background(INK).cornerRadius(13.dp), contentAlignment = Alignment.Center) { Text("Swap", style = t(Color.White, 10, true)) }
            }
        }
    }
}

// Now playing (needs Media access)
class MusicWidget : GrungeWidget("music", "#4A4A4A", "#FFFFFF") {
    @Composable override fun Body(ctx: Context, fg: Color) {
        val title = Store.get(ctx, "music.title", "Nothing playing")
        val artist = Store.get(ctx, "music.artist", "")
        val playing = Store.get(ctx, "music.playing", "0") == "1"
        val f = File(ctx.filesDir, "art.png")
        val bmp = if (f.exists()) BitmapFactory.decodeFile(f.path) else null
        Column(GlanceModifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(GlanceModifier.fillMaxWidth().defaultWeight().cornerRadius(14.dp).background(Color(0xFF222222)), contentAlignment = Alignment.Center) {
                if (bmp != null) Image(ImageProvider(bmp), null, GlanceModifier.fillMaxSize(), contentScale = ContentScale.Crop)
                else Text("♪", style = t(fg.copy(alpha = .6f), 22, true))
            }
            Spacer(sh(4))
            Text(if (artist.isEmpty()) title else "$title - $artist", style = t(fg.copy(alpha = .6f), 10, true), maxLines = 1)
            Spacer(sh(4))
            Row(GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(circle(28, Color(0xFF333333)), contentAlignment = Alignment.Center) { Text("♪", style = t(LIME, 12, true)) }
                Spacer(sp(4))
                Row(GlanceModifier.defaultWeight().height(30.dp).background(Color(0xFF2A2A2A)).cornerRadius(15.dp).padding(3.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(circle(24, LILAC, GlanceModifier.clickable(actionRunCallback<PlayPauseCb>())), contentAlignment = Alignment.Center) {
                        Text(if (playing) "II" else "▶", style = t(INK, 10, true))
                    }
                    Spacer(GlanceModifier.defaultWeight())
                    Box(GlanceModifier.clickable(actionRunCallback<NextCb>()).padding(horizontal = 6.dp)) { Text("»", style = t(fg, 14, true)) }
                }
            }
        }
    }
}
class PlayPauseCb : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val c = MediaListener.active(context) ?: return
        if (c.playbackState?.state == android.media.session.PlaybackState.STATE_PLAYING) c.transportControls.pause() else c.transportControls.play()
    }
}
class NextCb : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        MediaListener.active(context)?.transportControls?.skipToNext()
    }
}

// Lamp (toggles the phone flashlight)
class LampWidget : GrungeWidget("lamp", "#1E1E1E", "#FFFFFF") {
    @Composable override fun Body(ctx: Context, fg: Color) {
        val on = Store.get(ctx, "lamp.on", "0") == "1"
        Column(GlanceModifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Image(ImageProvider(if (on) R.drawable.lamp_on else R.drawable.lamp_off), null,
                GlanceModifier.fillMaxWidth().defaultWeight(), contentScale = ContentScale.Fit)
            Spacer(sh(4))
            Row(GlanceModifier.background(Color(0xFF2A2A2A)).cornerRadius(18.dp).padding(3.dp)) {
                Box(circle(26, if (!on) Color(0xFF444444) else Color(0xFF2A2A2A), GlanceModifier.clickable(actionRunCallback<LampOff>())), contentAlignment = Alignment.Center) {
                    Icon(R.drawable.ic_bulb, Color(0xFF9A9A9A), 14)
                }
                Spacer(sp(3))
                Box(circle(26, if (on) LILAC else Color(0xFF2A2A2A), GlanceModifier.clickable(actionRunCallback<LampOn>())), contentAlignment = Alignment.Center) {
                    Icon(R.drawable.ic_bulb, if (on) INK else Color(0xFF9A9A9A), 14)
                }
            }
        }
    }
}
private fun torch(ctx: Context, on: Boolean) {
    try {
        val cm = ctx.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val id = cm.cameraIdList.firstOrNull { cm.getCameraCharacteristics(it).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true }
        if (id != null) cm.setTorchMode(id, on)
    } catch (e: Exception) {}
    Store.put(ctx, "lamp.on", if (on) "1" else "0")
}
class LampOn : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) { torch(context, true); LampWidget().updateAll(context) }
}
class LampOff : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) { torch(context, false); LampWidget().updateAll(context) }
}

// Next flight: LED dot-matrix board
class FlightWidget : GrungeWidget("flight", "#222222", "#FFFFFF") {
    @Composable override fun Body(ctx: Context, fg: Color) {
        val bmp = DotMatrix.render(
            listOf(Store.get(ctx, "flight.dest", "TOKYO").uppercase().take(6), Store.get(ctx, "flight.date", "15 JAN").uppercase().take(6), Store.get(ctx, "flight.time", "13:30").take(6)),
            28, 7, listOf(LIME.toArgb(), fg.toArgb(), fg.toArgb()), fg.copy(alpha = .18f).toArgb())
        Column(GlanceModifier.fillMaxSize()) {
            Text("Next flight", style = t(fg, 12, true))
            Text(Store.get(ctx, "flight.route", "Tokyo - London"), style = t(fg.copy(alpha = .5f), 9, true))
            Spacer(sh(6))
            Image(ImageProvider(bmp), null, GlanceModifier.fillMaxWidth().defaultWeight(), contentScale = ContentScale.Fit)
        }
    }
}
