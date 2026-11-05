package com.grunge.app.widgets

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.updateAll
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import com.grunge.app.Draw
import com.grunge.app.Store
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

object ScheduleData {
    const val DEFAULT = "Wake up@10:00/Breakfast@11:00/Inst@12:00/Lunch@14:00/Smoking@15:00/TikTok@18:00/Dinner@20:00/Movies@22:00/Sleep@01:00"

    fun items(ctx: Context): List<Pair<String, String>> =
        Store.get(ctx, "schedule.items", DEFAULT).split("/").mapNotNull {
            val p = it.split("@"); if (p.size == 2) p[0].trim() to p[1].trim() else null
        }

    private fun mins(s: String): Int {
        val p = s.split(":"); return (p[0].toIntOrNull() ?: 0) * 60 + (p.getOrNull(1)?.toIntOrNull() ?: 0)
    }

    /** Index of the row whose time has most recently started (wraps past midnight). */
    fun current(items: List<Pair<String, String>>): Int {
        if (items.isEmpty()) return 0
        val first = mins(items[0].second)
        val c = Calendar.getInstance()
        val off = (c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE) - first + 1440) % 1440
        var cur = 0
        items.forEachIndexed { i, it -> if ((mins(it.second) - first + 1440) % 1440 <= off) cur = i }
        return cur
    }

    /** Wake the widget at the next row boundary so the dot moves on time. */
    fun scheduleNext(ctx: Context) {
        val now = System.currentTimeMillis()
        val next = items(ctx).map {
            val m = mins(it.second)
            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, m / 60); set(Calendar.MINUTE, m % 60); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= now) add(Calendar.DAY_OF_MONTH, 1)
            }.timeInMillis
        }.minOrNull() ?: return
        val pi = PendingIntent.getBroadcast(ctx, 77, Intent(ctx, ScheduleTick::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        (ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager).setAndAllowWhileIdle(AlarmManager.RTC, next, pi)
    }
}

class ScheduleWidget : GrungeWidget("schedule", "#EFEFEF", "#222222", null, 0) {
    override val sizeMode = SizeMode.Exact

    @Composable override fun Body(ctx: Context, fg: Color) {
        val size = LocalSize.current
        val d = ctx.resources.displayMetrics.density
        val w = (size.width.value * d).toInt().coerceIn(200, 800)
        val h = (size.height.value * d).toInt().coerceIn(200, 800)
        val items = ScheduleData.items(ctx)
        val bmp = Draw.schedule(w, h, items.map { it.first }, items.map { it.second }, ScheduleData.current(items), fg.toArgb())
        Image(ImageProvider(bmp), null, GlanceModifier.fillMaxSize(), contentScale = ContentScale.FillBounds)
    }
}

class ScheduleReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ScheduleWidget()
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        ScheduleData.scheduleNext(context)
    }
    override fun onEnabled(context: Context) { super.onEnabled(context); ScheduleData.scheduleNext(context) }
}

class ScheduleTick : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pr = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try { ScheduleWidget().updateAll(context); ScheduleData.scheduleNext(context) } finally { pr.finish() }
        }
    }
}
