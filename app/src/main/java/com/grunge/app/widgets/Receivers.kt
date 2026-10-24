package com.grunge.app.widgets

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class WeatherReceiver : GlanceAppWidgetReceiver() { override val glanceAppWidget: GlanceAppWidget = WeatherWidget() }
class BatteryReceiver : GlanceAppWidgetReceiver() { override val glanceAppWidget: GlanceAppWidget = BatteryWidget() }
class ClockReceiver : GlanceAppWidgetReceiver() { override val glanceAppWidget: GlanceAppWidget = ClockWidget() }
class YearReceiver : GlanceAppWidgetReceiver() { override val glanceAppWidget: GlanceAppWidget = YearWidget() }
class QuoteReceiver : GlanceAppWidgetReceiver() { override val glanceAppWidget: GlanceAppWidget = QuoteWidget() }
class FlightReceiver : GlanceAppWidgetReceiver() { override val glanceAppWidget: GlanceAppWidget = FlightWidget() }
class CityWeatherReceiver : GlanceAppWidgetReceiver() { override val glanceAppWidget: GlanceAppWidget = CityWeatherWidget() }
class MeetingReceiver : GlanceAppWidgetReceiver() { override val glanceAppWidget: GlanceAppWidget = MeetingWidget() }
class StopwatchReceiver : GlanceAppWidgetReceiver() { override val glanceAppWidget: GlanceAppWidget = StopwatchWidget() }
class ProfileReceiver : GlanceAppWidgetReceiver() { override val glanceAppWidget: GlanceAppWidget = ProfileWidget() }
class ContactReceiver : GlanceAppWidgetReceiver() { override val glanceAppWidget: GlanceAppWidget = ContactWidget() }
class DroneReceiver : GlanceAppWidgetReceiver() { override val glanceAppWidget: GlanceAppWidget = DroneWidget() }
class BalanceReceiver : GlanceAppWidgetReceiver() { override val glanceAppWidget: GlanceAppWidget = BalanceWidget() }
class UsdcReceiver : GlanceAppWidgetReceiver() { override val glanceAppWidget: GlanceAppWidget = UsdcWidget() }
class MusicReceiver : GlanceAppWidgetReceiver() { override val glanceAppWidget: GlanceAppWidget = MusicWidget() }
class LampReceiver : GlanceAppWidgetReceiver() { override val glanceAppWidget: GlanceAppWidget = LampWidget() }
