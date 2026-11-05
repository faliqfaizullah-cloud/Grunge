package com.grunge.app

import android.graphics.*

object DotMatrix {
    /** Renders text as an LED dot-matrix bitmap (one colour per line). */
    fun render(lines: List<String>, cols: Int, rows: Int, colors: List<Int>, off: Int, cell: Int = 10): Bitmap {
        val src = Bitmap.createBitmap(cols, rows * lines.size, Bitmap.Config.ARGB_8888)
        val c = Canvas(src)
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD); color = Color.WHITE; textSize = rows * 1.05f
        }
        p.textScaleX = 1f
        p.textScaleX = ((cols / 6f) / p.measureText("M")).coerceIn(0.3f, 2f)
        lines.forEachIndexed { i, s -> c.drawText(s, 0f, i * rows + rows * 0.86f, p) }
        val out = Bitmap.createBitmap(cols * cell, rows * lines.size * cell, Bitmap.Config.ARGB_8888)
        val oc = Canvas(out); val dp = Paint(Paint.ANTI_ALIAS_FLAG)
        for (y in 0 until src.height) for (x in 0 until cols) {
            val on = Color.alpha(src.getPixel(x, y)) > 90
            dp.color = if (on) colors[(y / rows).coerceAtMost(colors.size - 1)] else off
            oc.drawCircle(x * cell + cell / 2f, y * cell + cell / 2f, cell * 0.38f, dp)
        }
        return out
    }
}
