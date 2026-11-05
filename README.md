# Grunge
Customisable home-screen widget pack (Jetpack Glance). Every widget is a 2×2 cell with **28 dp** corners; Year, Quote and Flight stretch horizontally.

## Build the APK
Push to GitHub → Actions → **Build APK** → download `Grunge-debug-apk`.
Locally: Android Studio (Koala+) → open folder → Run. (CLI: `gradle assembleDebug`, JDK 17.)

## Implemented
Weather (location + background WorkManager), Battery, Red clock, Year progress, Quote, Next flight.

## Roadmap (from the reference images)
Pixel pet, Camera exposure, Yes/No ball, Timer, Analog clock, Speed graph, Music player, Spending, Social-media time,
Blink reminder, Streak counter, iPad storage, Basketball task, Book progress, Delivery status, Balance, Pollen forecast,
VR membership, Horoscope, Coffee loyalty, Memories, Figma shortcut, Meeting card, Stopwatch, Contact, Drone battery,
USDC, Now playing, Lamp toggle. Add each as a `GrungeWidget` subclass + receiver + `res/xml/w_*.xml` + manifest entry.
