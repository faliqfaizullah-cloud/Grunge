package com.grunge.app.widgets

import android.app.AlarmManager
import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.Text
import com.grunge.app.Mono
import com.grunge.app.R
import com.grunge.app.Store
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

/** Tile drawn as a bitmap at the widget's real size (crisp at any resize). */
abstract class MonoWidget(kind: String, bgRes: Int? = null, bg: String = "#0C0C0D") : GrungeWidget(kind, bg, "#FFFFFF", bgRes, 0) {
    override val sizeMode: SizeMode = SizeMode.Exact
    abstract fun draw(ctx: Context, w: Int, h: Int): Bitmap
    open fun tap(m: GlanceModifier): GlanceModifier = m
    @Composable open fun Overlay(ctx: Context) {}

    @Composable override fun Body(ctx: Context, fg: Color) {
        val s = LocalSize.current
        val d = ctx.resources.displayMetrics.density
        val w = (s.width.value * d).toInt().coerceIn(120, 800)
        val h = (s.height.value * d).toInt().coerceIn(120, 900)
        Box(tap(GlanceModifier.fillMaxSize())) {
            Image(ImageProvider(draw(ctx, w, h)), null, GlanceModifier.fillMaxSize(), contentScale = ContentScale.FillBounds)
            Overlay(ctx)
        }
    }
}

private fun g(ctx: Context, k: String, d: String) = Store.get(ctx, k, d)
private fun gi(ctx: Context, k: String, d: Int) = Store.get(ctx, k, d.toString()).toIntOrNull() ?: d

// white clock, black outline, blue second hand
class MonoClockWidget : GrungeWidget("monoclock", "#F2F2F2", "#111111", R.drawable.mono_white_bg, 10) {
    @Composable override fun Body(ctx: Context, fg: Color) {
        AndroidRemoteViews(analog(ctx, R.layout.clock_mono), GlanceModifier.fillMaxSize())
    }
}

// wide clock with Alarm / Steps pills
class MonoWideWidget : GrungeWidget("monowide", "#F2F2F2", "#111111", R.drawable.mono_white_bg, 10) {
    override val sizeMode: SizeMode = SizeMode.Exact

    @Composable private fun Pill(label: String, value: String) =
        Box(GlanceModifier.background(Color(0xFF5B5B5B)).cornerRadius(20.dp).padding(horizontal = 14.dp, vertical = 7.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(label, style = t(Color(0xFFD0D0D0), 8, false))
                Text(value, style = t(Color.White, 16, false))
            }
        }

    @Composable override fun Body(ctx: Context, fg: Color) {
        val h = (LocalSize.current.height.value - 20f).coerceAtLeast(60f)
        val next = (ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager).nextAlarmClock
        val alarm = next?.let { SimpleDateFormat("HH:mm").format(Date(it.triggerTime)) } ?: g(ctx, "monowide.alarm", "06:30")
        Row(GlanceModifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Spacer(GlanceModifier.defaultWeight()); Pill("Alarm", alarm)
            Spacer(GlanceModifier.defaultWeight())
            AndroidRemoteViews(analog(ctx, R.layout.clock_mono), GlanceModifier.size(h.dp))
            Spacer(GlanceModifier.defaultWeight()); Pill("Steps", g(ctx, "monowide.steps", "4.7K"))
            Spacer(GlanceModifier.defaultWeight())
        }
    }
}

// grey disc with live time and an orbiting dot
class DialTimeWidget : MonoWidget("dialtime", R.drawable.mono_white_bg, "#F2F2F2") {
    override fun draw(ctx: Context, w: Int, h: Int) = Mono.disc(w, h)
    @Composable override fun Overlay(ctx: Context) {
        Box(GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AndroidRemoteViews(tc(ctx, "h:mm", 14f, Color.White), GlanceModifier.width(36.dp).height(22.dp))
                AndroidRemoteViews(tc(ctx, "a", 14f, Color(0xFFBDBDBD), bold = false), GlanceModifier.width(24.dp).height(22.dp))
            }
        }
    }
}

class ActivityWidget : MonoWidget("activity") {
    override fun draw(ctx: Context, w: Int, h: Int) = Mono.activity(w, h)
}

class RingsWidget : MonoWidget("rings") {
    override fun draw(ctx: Context, w: Int, h: Int) = Mono.rings(w, h, gi(ctx, "rings.focus", 64), gi(ctx, "rings.sleep", 100),
        gi(ctx, "rings.water", 50), g(ctx, "rings.name", "Alex"), g(ctx, "rings.level", "3"))
}

class GameWidget : MonoWidget("game") {
    override fun draw(ctx: Context, w: Int, h: Int) = Mono.game(w, h, gi(ctx, "game.lvl", 3), gi(ctx, "game.score", 50))
    override fun tap(m: GlanceModifier) = m.clickable(actionRunCallback<GameTap>())
}
class GameTap : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        var s = gi(context, "game.score", 50) + 10; var l = gi(context, "game.lvl", 3)
        if (s >= 100) { s = 0; l++ }
        Store.put(context, "game.score", s.toString()); Store.put(context, "game.lvl", l.toString())
        GameWidget().updateAll(context)
    }
}

class DomeWidget : MonoWidget("dome") {
    override fun draw(ctx: Context, w: Int, h: Int) = Mono.dome(w, h, g(ctx, "weather.temp", "--°"), g(ctx, "weather.city", "Your area"))
}

class BlobWidget : MonoWidget("blob", null, "#C8C8CA") {
    override fun draw(ctx: Context, w: Int, h: Int) = Mono.blob(w, h)
}

class GreetingWidget : MonoWidget("greeting") {
    override fun draw(ctx: Context, w: Int, h: Int): Bitmap {
        val hr = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val hello = if (hr < 12) "Good morning." else if (hr < 18) "Good afternoon." else "Good evening."
        val word = g(ctx, "weather.cond", "clear").split(" ").first().let { if (it == "clear") "sunny" else it }
        return Mono.greeting(w, h, hello, g(ctx, "greeting.name", "Alex"), g(ctx, "weather.temp", "--°"), word,
            g(ctx, "greeting.meetings", "2"), g(ctx, "greeting.until", "4 pm"))
    }
}

class PlayerWidget : MonoWidget("player") {
    override fun draw(ctx: Context, w: Int, h: Int) = Mono.player(w, h, g(ctx, "music.title", "Not playing"),
        g(ctx, "music.artist", ""), g(ctx, "music.playing", "0") == "1")
    override fun tap(m: GlanceModifier) = m.clickable(actionRunCallback<PlayPauseCb>())
}

class OrbitWidget : MonoWidget("orbit") {
    override fun draw(ctx: Context, w: Int, h: Int) = Mono.orbit(w, h, gi(ctx, "orbit.sel", 2))
    override fun tap(m: GlanceModifier) = m.clickable(actionRunCallback<OrbitTap>())
}
class OrbitTap : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        Store.put(context, "orbit.sel", ((gi(context, "orbit.sel", 2) + 1) % 4).toString()); OrbitWidget().updateAll(context)
    }
}
