"""Run from the project root (~/Grunge) after unzipping the add-on:  python apply.py"""
import os, re

LIST = [("MonoClock","monoclock","Mono Clock","s"),("MonoWide","monowide","Mono Wide Clock","w"),
        ("DialTime","dialtime","Dial Time","s"),("Activity","activity","Activity 30d","s"),
        ("Rings","rings","Rings + Profile","t"),("Game","game","Score Game","s"),("Dome","dome","Weather Dome","s"),
        ("Blob","blob","Blue Glow","s"),("Greeting","greeting","Greeting","s"),("Player","player","Player","s"),
        ("Orbit","orbit","Focus Dial","s")]
A = {"w": 'android:minWidth="250dp" android:minHeight="110dp" android:targetCellWidth="4" android:targetCellHeight="2" android:resizeMode="horizontal" android:minResizeWidth="250dp" android:maxResizeWidth="400dp"',
     "t": 'android:minWidth="110dp" android:minHeight="250dp" android:targetCellWidth="2" android:targetCellHeight="4" android:resizeMode="vertical" android:minResizeHeight="250dp" android:maxResizeHeight="400dp"',
     "s": 'android:minWidth="110dp" android:minHeight="110dp" android:targetCellWidth="2" android:targetCellHeight="2" android:resizeMode="none"'}
M = "app/src/main"; P = M + "/java/com/grunge/app"

def rw(path, fn):
    s = open(path).read(); n = fn(s)
    if n != s: open(path, "w").write(n); print("patched", path)
    else: print("already patched", path)

# provider xml
for C, n, L, S in LIST:
    open(f"{M}/res/xml/w_{n}.xml", "w").write(
        f'<appwidget-provider xmlns:android="http://schemas.android.com/apk/res/android"\n    {A[S]} android:updatePeriodMillis="1800000" android:widgetCategory="home_screen"\n    android:initialLayout="@layout/glance_default_loading_layout" />\n')

# receivers
def recv(s):
    for C, *_ in LIST:
        if f"class {C}Receiver" not in s:
            s = s.rstrip("\n") + f"\nclass {C}Receiver : GlanceAppWidgetReceiver() {{ override val glanceAppWidget: GlanceAppWidget = {C}Widget() }}\n"
    return s
rw(P + "/widgets/Receivers.kt", recv)

# manifest
def man(s):
    if ".widgets.MonoClockReceiver" in s: return s
    r = "".join(f'        <receiver android:name=".widgets.{C}Receiver" android:exported="true" android:label="Grunge · {L}">\n            <intent-filter><action android:name="android.appwidget.action.APPWIDGET_UPDATE" /></intent-filter>\n            <meta-data android:name="android.appwidget.provider" android:resource="@xml/w_{n}" />\n        </receiver>\n' for C, n, L, S in LIST)
    return s.replace("    </application>", r + "    </application>")
rw(M + "/AndroidManifest.xml", man)

# app screen entries
ENT = '''
    Entry("Mono clock", MonoClockWidget(), listOf()),
    Entry("Mono wide clock", MonoWideWidget(), listOf(Field("monowide.alarm", "Alarm (if none set)", "06:30"), Field("monowide.steps", "Steps", "4.7K"))),
    Entry("Dial time", DialTimeWidget(), listOf()),
    Entry("Activity 30d", ActivityWidget(), listOf()),
    Entry("Rings + profile", RingsWidget(), listOf(Field("rings.focus", "Focus %", "64"), Field("rings.sleep", "Sleep %", "100"), Field("rings.water", "Water %", "50"), Field("rings.name", "Name", "Alex"), Field("rings.level", "Level", "3"))),
    Entry("Score game (tap tile)", GameWidget(), listOf(Field("game.lvl", "Level", "3"), Field("game.score", "Score", "50"))),
    Entry("Weather dome", DomeWidget(), listOf()),
    Entry("Blue glow", BlobWidget(), listOf()),
    Entry("Greeting", GreetingWidget(), listOf(Field("greeting.name", "Name", "Alex"), Field("greeting.meetings", "Meetings", "2"), Field("greeting.until", "Until", "4 pm"))),
    Entry("Player", PlayerWidget(), listOf()),
    Entry("Focus dial (tap tile)", OrbitWidget(), listOf()),'''
def ent(s):
    if "MonoClockWidget()" in s: return s
    a = '    Entry("Lamp", LampWidget(), colors("lamp", "#1E1E1E", "#FFFFFF")),'
    if a not in s: raise SystemExit("Could not find the Lamp entry in MainActivity.kt")
    return s.replace(a, a + ENT, 1)
rw(P + "/MainActivity.kt", ent)

rw("app/build.gradle.kts", lambda s: re.sub(r'versionCode = \d+; versionName = "[^"]+"', 'versionCode = 6; versionName = "0.6.0"', s))
print("done")
