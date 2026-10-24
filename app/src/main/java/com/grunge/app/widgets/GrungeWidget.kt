package com.grunge.app.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import com.grunge.app.Store

/** Every Grunge widget: 2x2 cell, 28dp corners, user-customisable bg/fg colours. */
abstract class GrungeWidget(val kind: String, val defBg: String, val defFg: String) : GlanceAppWidget() {
    @Composable abstract fun Body(ctx: Context, fg: Color)

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val bg = Store.color(context, "$kind.bg", defBg)
        val fg = Store.color(context, "$kind.fg", defFg)
        provideContent {
            Box(GlanceModifier.fillMaxSize().background(bg).cornerRadius(28.dp).padding(14.dp)) {
                Body(context, fg)
            }
        }
    }
}
