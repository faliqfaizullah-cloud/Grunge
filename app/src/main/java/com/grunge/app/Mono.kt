package com.grunge.app

import android.graphics.*
import java.util.Calendar
import java.util.Random
import kotlin.math.*

/** Monochrome tile drawings (design units: 330 = one tile). */
object Mono {
    private fun c(s: String) = Color.parseColor(s)
    private val BLACK = c("#0C0C0D"); private val CARD = c("#1B1B1C"); private val GREY = c("#8C8C8F")
    private val WHITE = c("#F2F2F2"); private val BLUE = c("#7FB4D4")

    private fun p(color: Int, stroke: Float = 0f) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        if (stroke > 0f) { style = Paint.Style.STROKE; strokeWidth = stroke; strokeCap = Paint.Cap.ROUND }
    }
    private fun tf(bold: Boolean, font: String) = Typeface.create(font, if (bold) Typeface.BOLD else Typeface.NORMAL)
    private fun measure(s: String, size: Float, bold: Boolean = false, font: String = "sans-serif") =
        p(0).apply { textSize = size; typeface = tf(bold, font) }.measureText(s)
    private fun Canvas.t(s: String, x: Float, y: Float, size: Float, color: Int, bold: Boolean = false,
                         align: Paint.Align = Paint.Align.LEFT, font: String = "sans-serif") {
        drawText(s, x, y, p(color).apply { textSize = size; textAlign = align; typeface = tf(bold, font) })
    }
    private fun frame(w: Int, h: Int, dw: Float, dh: Float, bg: Int?, block: Canvas.() -> Unit): Bitmap {
        val b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888); val cv = Canvas(b)
        if (bg != null) cv.drawColor(bg)
        val s = min(w / dw, h / dh); cv.translate((w - dw * s) / 2f, (h - dh * s) / 2f); cv.scale(s, s); cv.block(); return b
    }
    private fun blend(a: Int, b: Int, t: Float) = Color.rgb(
        (Color.red(a) + (Color.red(b) - Color.red(a)) * t).toInt(),
        (Color.green(a) + (Color.green(b) - Color.green(a)) * t).toInt(),
        (Color.blue(a) + (Color.blue(b) - Color.blue(a)) * t).toInt())

    fun activity(w: Int, h: Int): Bitmap = frame(w, h, 330f, 330f, BLACK) {
        t("Activity", 30f, 62f, 22f, c("#E6E6E6")); t("30d", 300f, 62f, 22f, GREY, align = Paint.Align.RIGHT)
        val cal = Calendar.getInstance()
        val rnd = Random((cal.get(Calendar.YEAR) * 1000 + cal.get(Calendar.DAY_OF_YEAR)).toLong())
        for (cx in 0 until 11) for (ry in 0 until 6) {
            val v = rnd.nextFloat()
            val k = if (v > .93f) 1f else if (v > .75f) .6f else if (v > .45f) .3f else .08f
            drawCircle(36f + cx * 25.8f, 104f + ry * 28f, 9f, p(blend(c("#25292D"), c("#A9DDF7"), k)))
        }
        val names = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val m = cal.get(Calendar.MONTH)
        listOf(70f, 165f, 262f).forEachIndexed { i, x -> t(names[(m + 10 + i) % 12], x, 292f, 19f, GREY, align = Paint.Align.CENTER) }
    }

    fun rings(w: Int, h: Int, focus: Int, sleep: Int, water: Int, name: String, level: String): Bitmap = frame(w, h, 330f, 690f, BLACK) {
        fun ring(cx: Float, cy: Float, r: Float, pct: Int, label: String, arc: Int, txt: Int) {
            drawCircle(cx, cy, r, p(c("#101214"))); drawCircle(cx, cy, r, p(c("#2A2F34"), 2f))
            drawArc(RectF(cx - r, cy - r, cx + r, cy + r), -90f, 360f * pct.coerceIn(0, 100) / 100f, false, p(arc, 3.5f))
            t("$pct%", cx, cy + 8f, 36f, txt, align = Paint.Align.CENTER, font = "sans-serif-light")
            t(label, cx, cy + 36f, 20f, GREY, align = Paint.Align.CENTER)
        }
        ring(100f, 135f, 72f, focus, "Focus", c("#D8D8D8"), WHITE)
        ring(232f, 135f, 72f, sleep, "Sleep", c("#9CC7E3"), WHITE)
        ring(168f, 248f, 62f, water, "Water", BLUE, BLUE)
        drawRoundRect(RectF(22f, 390f, 308f, 672f), 52f, 52f, p(CARD))
        drawCircle(165f, 488f, 60f, Paint(Paint.ANTI_ALIAS_FLAG).apply { shader = LinearGradient(120f, 430f, 210f, 550f, c("#E4E4E4"), c("#6B6B6E"), Shader.TileMode.CLAMP) })
        t(name.take(1).uppercase(), 165f, 510f, 62f, c("#2A2A2C"), true, Paint.Align.CENTER)
        t(name, 165f, 600f, 26f, WHITE, true, Paint.Align.CENTER)
        t("Level $level", 165f, 636f, 22f, GREY, align = Paint.Align.CENTER)
    }

    fun game(w: Int, h: Int, lvl: Int, score: Int): Bitmap = frame(w, h, 330f, 330f, BLACK) {
        t("Lvl ", 30f, 52f, 21f, GREY); t("$lvl", 30f + measure("Lvl ", 21f), 52f, 21f, WHITE)
        t("Score ", 300f - measure("Score $score", 21f), 52f, 21f, GREY)
        t("$score", 300f - measure("$score", 21f), 52f, 21f, WHITE)
        val cols = 8; val rows = 5
        for (i in 0 until cols) for (j in 0 until rows) drawCircle(48f + i * 33.5f, 96f + j * 36f, 11f, p(c("#262628"), 1.6f))
        val path = (0 until cols).map { it to rows - 1 } + (rows - 2 downTo 0).map { (cols - 1) to it }
        val lit = (score / 10).coerceIn(0, path.size)
        for (k in 0 until lit) {
            val (i, j) = path[k]
            drawCircle(48f + i * 33.5f, 96f + j * 36f, 11f, p(if (k == lit - 1) WHITE else c("#8E8E91")))
        }
    }

    fun dome(w: Int, h: Int, temp: String, city: String): Bitmap = frame(w, h, 330f, 330f, BLACK) {
        val sh = RadialGradient(165f, 150f, 112f, intArrayOf(c("#0F1B25"), c("#2F6E9C"), c("#5FA3D0"), c("#0C0C0D")),
            floatArrayOf(0f, .62f, .82f, 1f), Shader.TileMode.CLAMP)
        drawCircle(165f, 150f, 112f, Paint(Paint.ANTI_ALIAS_FLAG).apply { shader = sh })
        drawRect(0f, 196f, 330f, 330f, p(c("#13181D")))
        t(temp.ifBlank { "--°" }, 165f, 170f, 82f, WHITE, align = Paint.Align.CENTER, font = "sans-serif-light")
        t(city, 165f, 284f, 22f, GREY, align = Paint.Align.CENTER)
    }

    fun blob(w: Int, h: Int): Bitmap = frame(w, h, 330f, 330f, c("#C8C8CA")) {
        val sh = RadialGradient(165f, 165f, 118f, intArrayOf(c("#68737E"), c("#5E8EB0"), c("#7DB6DD"), c("#B7CDDB"), c("#C8C8CA")),
            floatArrayOf(0f, .45f, .7f, .88f, 1f), Shader.TileMode.CLAMP)
        drawCircle(165f, 165f, 118f, Paint(Paint.ANTI_ALIAS_FLAG).apply { shader = sh })
    }

    private fun Canvas.runs(x0: Float, y: Float, segs: List<Triple<String, Int, Boolean>>): Float {
        var x = x0
        for ((s, col, b) in segs) { t(s, x, y, 22f, col, b); x += measure(s, 22f, b) }
        return x
    }

    fun greeting(w: Int, h: Int, hello: String, name: String, temp: String, cond: String, meetings: String, until: String): Bitmap =
        frame(w, h, 330f, 330f, BLACK) {
            val dim = c("#6F6F72")
            val ys = listOf(56f, 114f, 172f, 230f, 288f)
            listOf(76f, 134f, 192f, 250f).forEach { drawLine(28f, it, 302f, it, p(c("#2A2A2C"), 1.2f)) }
            t(hello, 30f, ys[0], 22f, dim)
            drawCircle(40f, ys[1] - 8f, 10f, p(WHITE, 1.8f))
            var x = runs(58f, ys[1], listOf(Triple("$name.", WHITE, false), Triple(" It's ", dim, false)))
            drawCircle(x + 9f, ys[1] - 8f, 9f, p(BLUE)); x += 26f
            runs(x, ys[1], listOf(Triple(temp, WHITE, false)))
            runs(30f, ys[2], listOf(Triple("and ", dim, false), Triple(cond, WHITE, true), Triple(". You have", dim, false)))
            drawRoundRect(RectF(30f, ys[3] - 18f, 48f, ys[3] - 2f), 4f, 4f, p(BLUE))
            runs(58f, ys[3], listOf(Triple("$meetings meetings", WHITE, true), Triple(" to do", dim, false)))
            t("until $until.", 30f, ys[4], 22f, dim)
        }

    fun player(w: Int, h: Int, title: String, artist: String, playing: Boolean): Bitmap = frame(w, h, 330f, 330f, BLACK) {
        val sh = RadialGradient(165f, 112f, 80f, intArrayOf(c("#0F2231"), c("#1F4766"), c("#4E86AB"), c("#0C0C0D")),
            floatArrayOf(0f, .55f, .86f, 1f), Shader.TileMode.CLAMP)
        drawCircle(165f, 112f, 80f, Paint(Paint.ANTI_ALIAS_FLAG).apply { shader = sh })
        if (playing) {
            drawRoundRect(RectF(150f, 90f, 161f, 134f), 3f, 3f, p(Color.WHITE)); drawRoundRect(RectF(171f, 90f, 182f, 134f), 3f, 3f, p(Color.WHITE))
        } else {
            val tri = Path().apply { moveTo(152f, 88f); lineTo(152f, 136f); lineTo(188f, 112f); close() }; drawPath(tri, p(Color.WHITE))
        }
        drawLine(26f, 100f, 26f, 125f, p(c("#B8B8BB"), 2.5f)); drawLine(36f, 106f, 36f, 119f, p(c("#6F6F72"), 2.5f))
        fun cut(s: String) = if (s.length > 13) s.take(12) + "…" else s
        t(cut(title), 165f, 236f, 25f, WHITE, true, Paint.Align.CENTER)
        t(cut(artist), 165f, 274f, 22f, GREY, align = Paint.Align.CENTER)
        drawLine(130f, 304f, 166f, 304f, p(c("#E6E6E6"), 3f)); drawLine(166f, 304f, 202f, 304f, p(c("#3A3A3C"), 3f))
    }

    fun orbit(w: Int, h: Int, sel: Int): Bitmap = frame(w, h, 330f, 330f, BLACK) {
        drawCircle(165f, 165f, 128f, p(c("#232325"), 2f)); drawCircle(165f, 165f, 98f, p(c("#2E2F31"), 2f)); drawCircle(165f, 165f, 66f, p(c("#383C40"), 2f))
        save(); rotate(sel * 22f, 165f, 165f)
        val q = Path().apply { moveTo(122f, 120f); lineTo(208f, 106f); lineTo(218f, 204f); lineTo(148f, 218f); close() }
        drawPath(q, Paint(Paint.ANTI_ALIAS_FLAG).apply { shader = LinearGradient(122f, 110f, 218f, 218f, c("#8AA9BD"), c("#26343F"), Shader.TileMode.CLAMP); alpha = 150 })
        drawPath(q, p(c("#B7D6EA"), 2f)); restore()
        val labels = listOf("Focus", "Sleep", "Move", "Mood")
        val pos = listOf(Triple(62f, 96f, -35f), Triple(274f, 96f, 35f), Triple(272f, 246f, -35f), Triple(60f, 246f, 35f))
        labels.forEachIndexed { i, s ->
            val (x, y, a) = pos[i]; save(); rotate(a, x, y)
            t(s, x, y, 20f, if (i == sel) WHITE else c("#55575A"), i == sel, Paint.Align.CENTER); restore()
        }
    }

    /** Dark grey disc with a white dot at the hour-hand angle (transparent background). */
    fun disc(w: Int, h: Int): Bitmap = frame(w, h, 330f, 330f, null) {
        drawCircle(165f, 165f, 122f, p(c("#5B5B5B")))
        val cal = Calendar.getInstance()
        val ang = Math.toRadians((((cal.get(Calendar.HOUR) + cal.get(Calendar.MINUTE) / 60.0) * 30.0) - 90.0))
        drawCircle(165f + cos(ang).toFloat() * 100f, 165f + sin(ang).toFloat() * 100f, 11f, p(Color.WHITE))
    }
}
