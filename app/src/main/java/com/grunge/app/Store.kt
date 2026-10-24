package com.grunge.app

import android.content.Context
import androidx.compose.ui.graphics.Color

object Store {
    private fun p(c: Context) = c.getSharedPreferences("grunge", Context.MODE_PRIVATE)
    fun get(c: Context, k: String, d: String) = p(c).getString(k, d) ?: d
    fun put(c: Context, k: String, v: String) = p(c).edit().putString(k, v).apply()
    fun color(c: Context, k: String, d: String): Color =
        try { Color(android.graphics.Color.parseColor(get(c, k, d))) }
        catch (e: Exception) { Color(android.graphics.Color.parseColor(d)) }
}
