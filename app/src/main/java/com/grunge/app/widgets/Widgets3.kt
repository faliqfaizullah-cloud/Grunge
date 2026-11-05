package com.grunge.app.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.unit.ColorProvider
import com.grunge.app.R
import com.grunge.app.Store

private fun cir(size: Int, bg: Color, m: GlanceModifier = GlanceModifier) = m.size(size.dp).background(bg).cornerRadius((size / 2).dp)
private fun gap(w: Int) = GlanceModifier.width(w.dp)
private fun vgap(h: Int) = GlanceModifier.height(h.dp)

private fun lineColor(l: String) = when (l.uppercase()) {
    "A", "C", "E" -> Color(0xFF0039A6)
    "B", "D", "F", "M" -> Color(0xFFFF6319)
    "G" -> Color(0xFF6CBE45)
    "J", "Z" -> Color(0xFF996633)
    "L" -> Color(0xFFA7A9AC)
    "N", "Q", "R", "W" -> Color(0xFFFCCC0A)
    "1", "2", "3" -> Color(0xFFEE352E)
    "4", "5", "6" -> Color(0xFF00933C)
    "7" -> Color(0xFFB933AD)
    else -> Color(0xFF808183)
}

@Composable private fun Bubble(txt: String, bg: Color, fg: Color, size: Int) =
    Box(cir(size, bg), contentAlignment = Alignment.Center) { Text(txt, style = t(fg, (size * 0.55).toInt(), true)) }

@Composable private fun Bullet(l: String, size: Int) =
    Bubble(l.uppercase(), lineColor(l), if (l.uppercase() in listOf("N", "Q", "R", "W")) Color.Black else Color.White, size)

@Composable private fun Ic(res: Int, tint: Color, size: Int) =
    Image(ImageProvider(res), null, GlanceModifier.size(size.dp), colorFilter = ColorFilter.tint(ColorProvider(tint)))

// ─── Subway station signs (white with black top bar, or black) ───
open class SignWidget(kind: String, val dark: Boolean, val defName: String, val defLines: String) :
    GrungeWidget(kind, if (dark) "#000000" else "#FFFFFF", if (dark) "#FFFFFF" else "#111111", null, if (dark) 14 else 0) {
    @Composable override fun Body(ctx: Context, fg: Color) {
        val name = Store.get(ctx, "$kind.name", defName)
        val lines = Store.get(ctx, "$kind.lines", defLines).split(" ").filter { it.isNotBlank() }
        Column(GlanceModifier.fillMaxSize()) {
            if (!dark) Box(GlanceModifier.fillMaxWidth().height(12.dp).background(Color.Black)) {}
            Column(GlanceModifier.fillMaxSize().padding(if (dark) 0.dp else 12.dp)) {
                Text(name, style = t(fg, 20, true))
                Spacer(GlanceModifier.defaultWeight())
                lines.chunked(4).forEach { row ->
                    Row { row.forEach { Bullet(it, 22); Spacer(gap(3)) } }
                    Spacer(vgap(3))
                }
            }
        }
    }
}
class SignOneAvWidget : SignWidget("sign1av", false, "1 Av", "L")
class SignUnionSqWidget : SignWidget("signunion", true, "14 St - Union Sq", "L N Q R 4 5 6")
class SignRawsonWidget : SignWidget("signrawson", true, "33 St - Rawson St", "7")
class SignOneZeroFourWidget : SignWidget("sign104", false, "104 St", "A")
class SignAlabamaWidget : SignWidget("signalabama", false, "Alabama Av", "J")
class SignBronxWidget : SignWidget("sign149", false, "3 Av - 149 St", "2 5")

// ─── Card with stripes, black bar and arrows ───
class MetroCardWidget : GrungeWidget("metrocard", "#FDB913", "#111111", R.drawable.metrocard_bg, 12) {
    @Composable override fun Body(ctx: Context, fg: Color) {
        Column(GlanceModifier.fillMaxSize()) {
            Row(GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("MetroCard", style = t(fg, 20, true))
                Spacer(GlanceModifier.defaultWeight())
                Bubble("M", Color.Black, Color(0xFFFDB913), 24)
            }
            Spacer(GlanceModifier.defaultWeight())
            Row(GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("<<<<<<<<", style = t(fg, 9, true))
                Spacer(GlanceModifier.defaultWeight())
                Text("INSERT THIS WAY", style = t(fg, 9, true))
            }
        }
    }
}

// ─── Add credit (- $14 +, Next) ───
class CreditWidget : GrungeWidget("credit", "#FFFFFF", "#111111") {
    @Composable override fun Body(ctx: Context, fg: Color) {
        val amt = Store.get(ctx, "credit.amt", "14")
        Column(GlanceModifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Add Credit", style = t(fg, 9, true))
            Spacer(GlanceModifier.defaultWeight())
            Row(GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(GlanceModifier.clickable(actionRunCallback<CreditMinus>()).padding(8.dp)) { Text("-", style = t(fg, 16, true)) }
                Spacer(GlanceModifier.defaultWeight())
                Text("\$$amt", style = t(fg, 24, true))
                Spacer(GlanceModifier.defaultWeight())
                Box(GlanceModifier.clickable(actionRunCallback<CreditPlus>()).padding(8.dp)) { Text("+", style = t(fg, 16, true)) }
            }
            Spacer(GlanceModifier.defaultWeight())
            Box(GlanceModifier.background(Color(0xFFD0D0D0)).cornerRadius(10.dp).padding(horizontal = 16.dp, vertical = 4.dp)
                .clickable(actionRunCallback<CreditNext>())) { Text("Next", style = t(Color(0xFF333333), 10, true)) }
        }
    }
}
private fun bump(ctx: Context, d: Int) {
    Store.put(ctx, "credit.amt", ((Store.get(ctx, "credit.amt", "14").toIntOrNull() ?: 14) + d).coerceIn(1, 200).toString())
}
class CreditMinus : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) { bump(context, -1); CreditWidget().updateAll(context) }
}
class CreditPlus : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) { bump(context, 1); CreditWidget().updateAll(context) }
}
class CreditNext : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val bal = (Store.get(context, "credit.balance", "0").toIntOrNull() ?: 0) + (Store.get(context, "credit.amt", "14").toIntOrNull() ?: 14)
        Store.put(context, "credit.balance", bal.toString())
    }
}

// ─── Single ticket (stretch vertically) ───
class TicketWidget : GrungeWidget("ticket", "#FFFFFF", "#111111", null, 0) {
    @Composable override fun Body(ctx: Context, fg: Color) {
        val mins = Store.get(ctx, "ticket.mins", "60'")
        val small = t(fg, 7, false)
        Column(GlanceModifier.fillMaxSize()) {
            Column(GlanceModifier.fillMaxWidth().defaultWeight().padding(12.dp)) {
                Row(GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Bullet(Store.get(ctx, "ticket.line", "1"), 20); Spacer(gap(4))
                    Bubble(mins, Color(0xFF00933C), Color.White, 20)
                    Spacer(GlanceModifier.defaultWeight())
                    Bubble("M", Color.Black, Color.White, 22)
                }
                Spacer(vgap(6))
                Text("Single Ticket", style = t(fg, 15, true))
                Spacer(vgap(3))
                Text("1 Ticket • $mins", style = small)
                Text("Valid on all MTA lines", style = small)
                Spacer(GlanceModifier.defaultWeight())
                Box(GlanceModifier.fillMaxWidth().height(2.dp).background(ImageProvider(R.drawable.dash))) {}
                Spacer(vgap(4))
                Text("1 Ticket • $mins", style = small)
                Text("Valid on all MTA lines", style = small)
            }
            Box(GlanceModifier.fillMaxWidth().height(12.dp).background(Color.Black)) {}
            Row(GlanceModifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 3.dp)) {
                Text("<<<<<<<<", style = t(fg, 7, true))
                Spacer(GlanceModifier.defaultWeight())
                Text("THIS WAY", style = t(fg, 8, true))
            }
        }
    }
}

// ─── Typeface specimen ───
class TypefaceWidget : GrungeWidget("typeface", "#000000", "#FFFFFF") {
    @Composable override fun Body(ctx: Context, fg: Color) {
        Column(GlanceModifier.fillMaxSize()) {
            Text("RM Neue", style = t(fg, 17, true))
            Spacer(GlanceModifier.defaultWeight())
            listOf("Aaa Bb Cc Dd", "Ee Ff Gg Hh Ii", "Jj Kk Ll Mm Nn", "Oo Pp Qq Rr", "Ss Tt Uu Vv Ww", "Xx Yy Zz.,:;£\$?(!)", "0123456789")
                .forEach { Text(it, style = t(fg, 10, true)) }
        }
    }
}

// ─── Direction sign ───
class DirectionWidget : GrungeWidget("direction", "#000000", "#FFFFFF") {
    @Composable override fun Body(ctx: Context, fg: Color) {
        val l = Store.get(ctx, "direction.left", "Penn/Station").split("/")
        val r = Store.get(ctx, "direction.right", "34 St./7 Ave").split("/")
        Row(GlanceModifier.fillMaxSize()) {
            Column(GlanceModifier.defaultWeight().fillMaxHeight()) {
                l.forEach { Text(it, style = t(fg, 18, true)) }
                Spacer(GlanceModifier.defaultWeight()); Ic(R.drawable.arrow_dl, fg, 20)
            }
            Column(GlanceModifier.defaultWeight().fillMaxHeight()) {
                r.forEach { Text(it, style = t(fg, 18, true)) }
                Spacer(GlanceModifier.defaultWeight()); Ic(R.drawable.arrow_dr, fg, 20)
            }
        }
    }
}
