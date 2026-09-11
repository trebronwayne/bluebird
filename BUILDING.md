# Building & Running Bluebird

## Prerequisites

- Android Studio **Hedgehog (2023.1.1)** or newer
- JDK **17** or newer
- Android device or emulator running **Android 8.0+ (API 26+)**
- Gradle **8.5.2**

## Clone & Open

```bash
git clone https://github.com/norbert-web/bluebird.git
cd bluebird
```

Open in Android Studio (**File → Open**, select the `Bluebird/` folder), wait for Gradle sync, then run on a device or emulator.

## Set as Default Launcher

After installing, Android will prompt you to select a default launcher — choose **Bluebird → Always**. If not prompted: **Settings → Apps → Default Apps → Home App → Bluebird**.

---

## Building from Source

### Debug

```bash
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

### Release

1. Create a keystore (first time only):

```bash
keytool -genkey -v -keystore io.github.norbertweb.bluebird-release.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias io.github.norbertweb.bluebird
```

2. Configure signing in `app/build.gradle.kts`:

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("../io.github.norbertweb.bluebird-release.jks")
            storePassword = System.getenv("KEYSTORE_PASS")
            keyAlias = "io.github.norbertweb.bluebird"
            keyPassword = System.getenv("KEY_PASS")
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

3. Build:

```bash
KEYSTORE_PASS=yourpassword KEY_PASS=yourkeypass ./gradlew assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`

### Install / Test

```bash
./gradlew installDebug                 # install debug build
adb install -r path/to/app.apk         # or install any APK manually
./gradlew test                         # unit tests
./gradlew connectedTest                # instrumented tests (needs device/emulator)
```

---

## Configuration

All user preferences live in `SharedPreferences` under `launcher_prefs_v2` — no config file; everything is set through the in-app **Settings** and **OOBE wizard**.

**Reset to defaults:**
```bash
adb shell pm clear io.github.norbertweb.bluebird
```
Or: **Android Settings → Apps → Bluebird → Storage → Clear Data**

**Publishing your own fork?** Change the package name in `app/build.gradle.kts` (`namespace`/`applicationId`), the `FileProvider` authority in `AndroidManifest.xml`, and rename the `io/github/norbertweb/bluebird/` package directory.

---

## Permissions

| Permission | Why it's needed |
|-----------|-----------------|
| `QUERY_ALL_PACKAGES` | List installed apps in Start Menu |
| `RECEIVE_BOOT_COMPLETED` | Auto-start as home screen after reboot |
| `SET_WALLPAPER` / `SET_WALLPAPER_HINTS` | Sync a chosen home wallpaper to the system |
| `READ_MEDIA_IMAGES` / `_VIDEO` / `_AUDIO` / `_VISUAL_USER_SELECTED` | Browse media in File Explorer and Media Player |
| `READ_EXTERNAL_STORAGE` / `WRITE_EXTERNAL_STORAGE` | Filesystem access on older Android versions |
| `MANAGE_EXTERNAL_STORAGE` | Full filesystem access for File Explorer (manual grant, API 30+) |
| `REORDER_TASKS` / `EXPAND_STATUS_BAR` | Launcher-level window/status-bar behavior |
| `CHANGE_CONFIGURATION` | System UI configuration changes |
| `INTERNET` / `ACCESS_NETWORK_STATE` / `ACCESS_WIFI_STATE` | Browser, App Store, web apps, update checker |
| `POST_NOTIFICATIONS` | Show system and toast notifications |
| `BIND_NOTIFICATION_LISTENER_SERVICE` | Read live notifications via system service (manual grant) |
| `FOREGROUND_SERVICE` / `FOREGROUND_SERVICE_MEDIA_PLAYBACK` | Background media playback |
| `BATTERY_STATS` | Battery level in status bar |
| `WRITE_SETTINGS` | Adjust screen brightness (manual grant) |
| `DOWNLOAD_WITHOUT_NOTIFICATION` | Browser downloads |
| `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` | Weather widget |
| `CAMERA` | Reserved — no in-app camera capture currently uses this |
| `RECORD_AUDIO` | Reserved — not currently used by any feature |

> `MANAGE_EXTERNAL_STORAGE` and **Notification Listener** access must be granted manually in Android Settings; the OOBE wizard links directly to both.

> `CAMERA` and `RECORD_AUDIO` are currently unused by any implemented feature and are under review for removal in a future release.

---

## Known Limitations

Most of these aren't Bluebird-specific bugs — they're restrictions Android places on any third-party app, launcher or not.

| Limitation | Reason | Workaround |
|-----------|--------|-----------|
| **Wi-Fi can't be toggled programmatically** | Removed in Android 10 (API 29) by Google | Tapping Wi-Fi opens system Wi-Fi Settings |
| **Bluetooth toggle** | Deprecated for third-party apps in Android 13 | Tapping opens system Bluetooth Settings |
| **MANAGE_EXTERNAL_STORAGE** | Requires manual grant on Android 11+ | OOBE wizard provides a direct link |
| **Notification Listener** | Requires manual grant in system settings | OOBE wizard opens the settings screen |
| **File deletions are permanent** | Recycle Bin tracks metadata but `File.delete()` can't be reliably undone on all devices | Items appear in Recycle Bin with restore option |
| **Screen brightness control** | Requires `WRITE_SETTINGS`, granted manually | Settings → Display → System permissions |
| **Wallpaper on lock screen** | The Android system lock screen is separate from Bluebird's in-app lock screen | Use the in-app lock (Start → Power → Lock) |
| **Web apps run in a sandboxed WebView** | Not native apps, limited to WebView-supported APIs | Use native Android apps for anything outside WebView's scope |
