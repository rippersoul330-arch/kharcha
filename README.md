# Kharcha 💸

A dead-simple **Android** expense tracker built around one idea: logging a spend
should take about two seconds. Shake your phone from *anywhere* — even inside
YouTube or Instagram — and a floating button pops up. Tap it, type the amount,
pick a category, done. Open the app any time to see a **pie chart** of the
month's spending.

> Working name: **Kharcha** (Hindi for "expense/spending"). Currency: ₹ (INR).

## Features (v1)
- 🤳 **Shake to log** from any screen — a floating button appears over other apps.
- ⚡ **Quick entry**: amount + category (+ optional note), then the button disappears.
- ⏱️ **Auto-hide** if you shake by accident and don't respond.
- 🏷️ **Categories**: built-in starters (Snacks, Food, Travel…) plus **add your own**.
- 🥧 **Monthly pie chart** by category, with month-to-month navigation (calendar months).
- ✏️ **Edit / delete** any expense (tap it in the list).
- 🔒 **Private by default**: all data stays on the phone (Room/SQLite). No accounts, no internet.
- 💾 **Backup / export** to a CSV file.
- 💬 **Send feedback** button for testers.

## How the shake-from-anywhere works
A small **foreground service** keeps the accelerometer listening for a shake.
Android requires:
1. The **"appear on top" / draw-over-other-apps** permission (`SYSTEM_ALERT_WINDOW`).
2. A persistent **"Kharcha is running"** notification (mandatory for foreground services).
3. Ideally, exempting the app from **battery optimisation** so aggressive phones
   (Samsung/Xiaomi/Oppo, etc.) don't kill the listener. There's a shortcut in Settings.

Toggle it on under the app's **Settings → Shake to log**.

---

## Build & run (Android Studio)

**Requirements**
- Android Studio (Koala/2024.1 or newer recommended)
- JDK 17 (bundled with recent Android Studio)
- An Android phone (Android 8.0 / API 26 or newer) with USB debugging, or an emulator
  - Note: the shake + overlay features are best tested on a **real phone**.

**Steps**
1. Open the project folder in Android Studio (`File → Open`), let Gradle sync.
   Android Studio will create `local.properties` pointing at your Android SDK.
2. Plug in your phone (enable *Developer options → USB debugging*) or start an emulator.
3. Press **Run ▶**. The app installs and launches.
4. In the app: go to **Settings**, turn on **Shake to log**, and grant the
   *appear on top* permission when asked.
5. Try it: shake the phone (or press the emulator's shake control), tap the
   floating button, log an expense.

**Build an installable APK to share with testers**
- `Build → Build Bundle(s) / APK(s) → Build APK(s)`, or from a terminal:
  ```
  ./gradlew assembleDebug
  ```
  The APK lands in `app/build/outputs/apk/debug/app-debug.apk`. Send that file to
  testers; they enable "install unknown apps" once, then install it.

## Project layout
```
app/src/main/java/com/kharcha/app/
├── KharchaApplication.kt      # DB + repository singletons, seeds default categories
├── MainActivity.kt            # Hosts Compose UI, permissions, export, feedback
├── data/                      # Room: Category, Expense, DAOs, database, repository
├── service/                   # ShakeDetector, ShakeDetectionService, BootReceiver
├── overlay/                   # OverlayController (floating button), QuickEntryActivity
├── ui/                        # Compose screens: MainScreen, PieChart, QuickEntryScreen…
└── util/                      # Money (₹/paise), MonthRange, Prefs, Exporter
```

## Notes / TODO
- Set the feedback email in `app/src/main/res/values/strings.xml` (`feedback_email`).
- Amounts are stored in **paise** (integer) to avoid rounding errors.
- Data is on-device only. "Export" is the migration path if cloud sync is added later.
