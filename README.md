# Modular – Reliable Android Focus Mode App

**Modular** is a production-ready, offline-first Android application designed to eliminate smartphone distractions through strict whitelist-based focus modes and un-bypassable friction exit timers.

---

## 🎯 Core Philosophy

Modern smartphones encourage impulsive app switching. Modular answers only one question:
> **"What am I doing right now?"**

If the answer is **Study**, only study-related apps exist. Everything else disappears behind an un-bypassable blocking screen.

Leaving a focus mode requires **5 uninterrupted minutes** of waiting. If the phone is locked, process killed, app switched, or accessibility revoked, the timer **immediately resets**.

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin 1.9
- **UI Framework**: Jetpack Compose + Material 3 (Monochrome Pixel Aesthetic)
- **Database**: Room Database (SQLite)
- **Background Enforcement**: Android `AccessibilityService` (`TYPE_WINDOW_STATE_CHANGED`)
- **Overlay Enforcement**: `SYSTEM_ALERT_WINDOW` (`BlockingOverlayActivity`)
- **Architecture Pattern**: MVVM + Clean Architecture + Repository Pattern

---

## 📂 Project Structure

```
Modular/
├── app/
│   ├── src/main/
│   │   ├── java/com/modular/app/
│   │   │   ├── ModularApplication.kt        # App entrypoint & DB initialization
│   │   │   ├── MainActivity.kt              # Host activity & permission gateway
│   │   │   ├── data/
│   │   │   │   ├── local/
│   │   │   │   │   ├── dao/                 # Room DAOs (ModeDao, SessionDao)
│   │   │   │   │   ├── entity/              # Database entities & relations
│   │   │   │   │   └── ModularDatabase.kt   # Room Database class
│   │   │   │   ├── model/                   # Domain models (InstalledApp, Mode)
│   │   │   │   ├── repository/              # Repositories for modes, apps, sessions
│   │   │   │   └── util/                    # System app emergency whitelist validator
│   │   │   ├── service/
│   │   │   │   ├── ModularAccessibilityService.kt  # Real-time foreground app blocker
│   │   │   │   ├── BlockingOverlayActivity.kt      # Full-screen un-bypassable overlay
│   │   │   │   └── ExitTimerManager.kt             # 5-minute uninterrupted friction timer
│   │   │   └── ui/
│   │   │       ├── components/              # Reusable Compose Material 3 components
│   │   │       ├── navigation/              # NavGraph & routes
│   │   │       ├── screens/
│   │   │       │   ├── home/                # Active mode dashboard & mode list
│   │   │       │   ├── mode_editor/         # Mode creation & whitelist app picker
│   │   │       │   ├── active_session/      # 5-min exit friction timer screen
│   │   │       │   ├── blocking/            # Full-screen blocking UI
│   │   │       │   └── permissions/         # Onboarding permission wizard
│   │   │       ├── theme/                   # Monochrome dark theme design tokens
│   │   │       └── viewmodel/               # ViewModels for MVVM state management
│   │   └── AndroidManifest.xml              # Declarations & service definitions
│   └── build.gradle.kts                     # App module dependencies & compilation
├── build.gradle.kts                         # Root Gradle config
├── settings.gradle.kts                      # Module includes & repository sources
└── gradle/libs.versions.toml                # Dependency version catalog
```

---

## 🔒 Required Permissions & Rationale

1. `android.permission.BIND_ACCESSIBILITY_SERVICE`
   - **Why**: Allows real-time detection of the foreground app package (`TYPE_WINDOW_STATE_CHANGED`) to trigger blocking instantly before interaction occurs.
2. `android.permission.SYSTEM_ALERT_WINDOW`
   - **Why**: Permits Modular to present the full-screen blocking UI over third-party applications.
3. `android.permission.QUERY_ALL_PACKAGES`
   - **Why**: Allows displaying installed applications in the Whitelist Picker UI during mode creation.
4. `android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`
   - **Why**: Prevents aggressive OEM battery managers (e.g., Xiaomi MIUI, Samsung OneUI, OnePlus OxygenOS) from killing the monitoring engine.

---

## 🛡️ Emergency Whitelist (Never Blocked)

For safety and system usability, the following applications are **hardcoded as exempt**:
- **Phone / Dialer** (`com.google.android.dialer`, default system dialers)
- **Messages / SMS** (`com.google.android.apps.messaging`, default SMS app)
- **Clock / Alarms** (`com.google.android.deskclock`, system deskclock)
- **Camera** (`com.google.android.GoogleCamera`, default camera)
- **Modular App** (`com.modular.app`)

---

## 🚀 How to Build & Run

1. Open **Android Studio** (Hedgehog 2023.1.1 or newer).
2. Select **File -> Open** and navigate to this repository directory.
3. Wait for Gradle Sync to complete.
4. Connect an Android Device running **Android 8.0 (API 26) or higher**.
5. Click **Run 'app'** (`Shift + F10`).
6. Complete the initial Permission Wizard inside the app:
   - Enable **Modular Accessibility Service** in System Settings.
   - Grant **Display over other apps** permission.
   - (Recommended) Disable Battery Optimization.

---

## 🧪 Testing Checklist

- [x] **Whitelist Blocking**: Launch a non-whitelisted app (e.g., YouTube) during active Study Mode -> Instant block screen appears.
- [x] **Emergency Exemption**: Make a phone call or open the Clock app -> Allowed immediately.
- [x] **Exit Timer Interruption (Screen Lock)**: Start exit timer, turn off screen -> Unlock phone -> Timer resets to 05:00.
- [x] **Exit Timer Interruption (App Switch)**: Start exit timer, switch to another app -> Timer resets to 05:00.
- [x] **Accessibility Disconnect**: Revoke Accessibility Service while exiting -> Timer invalidates and session remains locked.

---

## ⚠️ Known Android Platform Limitations

- **Aggressive OEM Process Killers**: Devices running MIUI, ColorOS, or FuntouchOS may terminate background services unless Modular is set to "No Restrictions" under Battery Settings.
- **Boot Persistence**: After a hard device restart, Accessibility Services are re-engaged by Android, but users must unlock the keyguard once for the service to start receiving events.

---

## 🗺️ Roadmap (Post-MVP)

- Multi-schedule automation (e.g., automatically activate Study Mode Monday to Friday 9 AM - 5 PM).
- NFC tag / QR code unlock triggers.
- Device Admin lock mode for extreme focus protection.
