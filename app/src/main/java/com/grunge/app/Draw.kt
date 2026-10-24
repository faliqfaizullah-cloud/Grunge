package com.grunge.app

import android.graphics.*
import kotlin.math.cos
import kotlin.math.sin

object Draw {
    private fun p(c: String, w: Float = 0f, fill: Boolean = true) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor(c); style = if (fill) Paint.Style.FILL else Paint.Style.STROKE; strokeWidth = w; strokeCap = Paint.Cap.ROUND
    }

    /** Stopwatch-style gauge: 60 ticks, numbers every max/6, orange needle. */
    fun gauge(px: Int, value: Float, max: Int): Bitmap {
        val b = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888); val c = Canvas(b); val r = px / 2f
        c.drawCircle(r, r, r, p("#1C1C1E"))
        val tick = p("#8E8E93", px * 0.012f, false)
        for (i in 0 until 60) {
            val a = Math.toRadians(i * 6.0 - 90); val l = if (i % 5 == 0) .1f else .05f
            c.drawLine(r + cos(a).toFloat() * r * .93f, r + sin(a).toFloat() * r * .93f,
                r + cos(a).toFloat() * r * (.93f - l), r + sin(a).toFloat() * r * (.93f - l), tick)
        }
        val tp = p("#FFFFFF").apply { textAlign = Paint.Align.CENTER; textSize = px * .13f; typeface = Typeface.DEFAULT_BOLD }
        for (k in 1..6) {
            val a = Math.toRadians(k * 60.0 - 90)
            c.drawText((max / 6 * k).toString(), r + cos(a).toFloat() * r * .62f, r + sin(a).toFloat() * r * .62f + tp.textSize / 3, tp)
        }
        val a = Math.toRadians(value / max * 360.0 - 90)
        val n = p("#F5A623", px * .03f, false)
        c.drawLine(r, r, r + cos(a).toFloat() * r * .8f, r + sin(a).toFloat() * r * .8f, n)
        c.drawCircle(r, r, px * .04f, p("#F5A623"))
        return b
    }

    /** Ring dial with 60 ticks and 12/03/06/09 numbers (transparent background). */
    fun ring(px: Int): Bitmap {
        val b = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888); val c = Canvas(b); val r = px / 2f
        val thin = p("#8E8E93", px * .006f, false); val thick = p("#FFFFFF", px * .012f, false)
        for (i in 0 until 60) {
            val a = Math.toRadians(i * 6.0 - 90); val major = i % 5 == 0
            val l = if (major) .09f else .04f
            c.drawLine(r + cos(a).toFloat() * r * .97f, r + sin(a).toFloat() * r * .97f,
                r + cos(a).toFloat() * r * (.97f - l), r + sin(a).toFloat() * r * (.97f - l), if (major) thick else thin)
        }
        val tp = p("#FFFFFF").apply { textAlign = Paint.Align.CENTER; textSize = px * .075f; typeface = Typeface.DEFAULT_BOLD }
        listOf("12" to 0, "03" to 90, "06" to 180, "09" to 270).forEach { (s, deg) ->
            val a = Math.toRadians(deg - 90.0)
            c.drawText(s, r + cos(a).toFloat() * r * .78f, r + sin(a).toFloat() * r * .78f + tp.textSize / 3, tp)
        }
        return b
    }

    /** Red heart-rate style wave. */
    fun wave(w: Int, h: Int): Bitmap {
        val b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888); val c = Canvas(b)
        val path = Path(); val mid = h / 2f
        for (x in 0..w) {
            val t = x / w.toFloat()
            val y = mid - (sin(t * 24.0) * 0.55 + sin(t * 61.0) * 0.25).toFloat() * h * .4f * (0.4f + 0.6f * sin(t * Math.PI).toFloat())
            if (x == 0) path.moveTo(0f, y) else path.lineTo(x.toFloat(), y)
        }
        c.drawPath(path, p("#E5483B", h * .05f, false))
        return b
    }
}
