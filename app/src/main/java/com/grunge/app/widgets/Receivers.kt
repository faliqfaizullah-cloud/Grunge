package com.grunge.app.widgets

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class WeatherReceiver : GlanceAppWidgetReceiver() { override val glanceAppWidget: GlanceAppWidget = WeatherWidget() }
class BatteryReceiver : GlanceAppWidgetReceiver() { override val glanceAppWidget: GlanceAppWidget = BatteryWidget() }
class ClockReceiver : GlanceAppWidgetReceiver() { override val glanceAppWidget: GlanceAppWidget = ClockWidget() }
class YearReceiver : GlanceAppWidgetReceiver() { override val glanceAppWidget: GlanceAppWidget = YearWidget() }
class QuoteReceiver : GlanceAppWidgetReceiver() { override val glanceAppWidget: GlanceAppWidget = QuoteWidget() }
class FlightReceiver : GlanceAppWidgetReceiver() { override val glanceAppWidget: GlanceAppWidget = FlightWidget() }
