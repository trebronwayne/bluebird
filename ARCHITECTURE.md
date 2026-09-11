# Architecture

How Bluebird is put together: state management, data flow, tech stack, and where everything lives in the codebase.

---

## Overview

Bluebird uses a single-ViewModel, unidirectional data flow architecture. There's no navigation library and no Room database — the whole shell (Desktop, Taskbar, Start Menu, every overlay, every built-in app) reads from one `LauncherUiState`, and all persistence goes through `SharedPreferences`/`DataStore`.

```
+-----------------------------------------------------------------+
|                          MainActivity                             |
|  +----------------------+     +------------------------------+  |
|  |   SetupScreen        |     |        DesktopScreen          |  |
|  |   (OOBE Wizard)      |     |                                |  |
|  +----------------------+     |  +----------+   +-----------+  |  |
|                                |  | Desktop  |   |  Windows  |  |  |
|                                |  | (Icons,  |   |  Manager  |  |  |
|                                |  | Wallpaper|   |           |  |  |
|                                |  | Context  |   |  Floating |  |  |
|                                |  | Menu)    |   |  Windows  |  |  |
|                                |  +----------+   +-----------+  |  |
|                                |  +--------------------------+  |  |
|                                |  |         Taskbar           |  |  |
|                                |  |   Start | Apps | Tray     |  |  |
|                                |  +--------------------------+  |  |
|                                |  +--------------------------+  |  |
|                                |  |    Overlays (animated)     |  |  |
|                                |  |  Start Menu | Search       |  |  |
|                                |  |  Action Center | Power     |  |  |
|                                |  +--------------------------+  |  |
|                                +--------------------------------+  |
+-----------------------------------------------------------------+
                               |
                    +----------v-----------+
                    |   LauncherViewModel   |
                    |                       |
                    |   LauncherUiState     |
                    |   +-----------------+ |
                    |   | wallpaper       | |
                    |   | userProfile     | |
                    |   | installedApps   | |
                    |   | pinnedApps      | |
                    |   | openWindows     | |
                    |   | notifications   | |
                    |   | desktopShortcuts| |
                    |   | recycleBinItems | |
                    |   | webApps         | |
                    |   | overlayStates   | |
                    |   +-----------------+ |
                    +----------+------------+
                               |
               +---------------+---------------+
               |               |               |
     +---------v------+  +-----v------+  +----v--------+
     |  SharedPrefs   |  |  Android   |  |   System    |
     |  (persistence) |  |   APIs     |  |  Services   |
     |                |  |  File, SMS |  |  Wallpaper  |
     |  - wallpaper   |  |  Contacts  |  |  AudioMgr   |
     |  - username    |  |  CallLog   |  |  Notif.     |
     |  - pinned apps |  |  PackageMgr|  |  Listener   |
     |  - shortcuts   |  |  Media3    |  |  BatteryMgr |
     |  - recycle bin |  +------------+  +-------------+
     +----------------+
```

## Data Flow

```
User Gesture -> Composable -> ViewModel.action() -> UiState update -> Recompose
```

All state lives in a single `LauncherUiState` data class, collected as `StateFlow` by Compose. State mutations always go through `LauncherViewModel` — composables never mutate state directly. No Room database; all persistence is through `SharedPreferences` and `DataStore`, with Gson serialization for complex objects.

## Materials & Theming

Glass surfaces (Taskbar, Start Menu, Search, Action Center, Widgets, Power Menu) use real Windows 11 Mica/Acrylic via the [Haze](https://github.com/chrisbanes/haze) library's `FluentMaterials`, tinted by a muted color sampled from Bluebird's own active wallpaper (`WallpaperAccent.kt`) — not the Android system wallpaper, and not a manual opacity slider. The taskbar itself uses Mica (persistent shell chrome); everything that flies out over it uses Acrylic. Dark/light theme is always the dominant signal — the wallpaper only ever contributes a small tint on top, never enough to override it.

---

## Tech Stack

| Category | Technology | Version |
|----------|-----------|---------|
| **Language** | Kotlin | 2.0.0 |
| **UI Framework** | Jetpack Compose | BOM 2024.08.00 |
| **Architecture** | MVVM + StateFlow | — |
| **Build System** | Gradle (KTS) | 8.5.2 |
| **Navigation** | None (custom window manager) | — |
| **Media** | Media3 (ExoPlayer) | 1.4.1 |
| **Image Loading** | Coil | 2.7.0 |
| **Backdrop Blur** | Haze (Mica/Acrylic materials) | 1.6.10 |
| **JSON** | Gson | 2.11.0 |
| **Persistence** | SharedPreferences + DataStore | 1.1.1 |
| **Icons** | Fluent UI System Icons + Material Icons Extended | BOM |
| **Splash Screen** | AndroidX Core SplashScreen | 1.0.1 |
| **Min SDK** | Android 8.0 (Oreo) | API 26 |
| **Target SDK** | Android 15 | API 35 |
| **Compile SDK** | 36 | — |
| **Java Version** | 17 | — |

---

## Project Structure

```
Bluebird/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── java/io/github/norbertweb/bluebird/
│           ├── BluebirdApplication.kt
│           ├── DesktopModeHelper.kt
│           ├── LauncherViewModel.kt          # Core logic, LauncherUiState
│           ├── MainActivity.kt               # Entry point, OOBE routing, immersive mode
│           │
│           ├── browser/                      # WebView-based browser
│           │   ├── data/BrowserRepository.kt
│           │   ├── ui/BrowserScreen.kt, NewTabPage.kt
│           │   └── utils/AdBlocker.kt, DownloadHelper.kt, UrlUtils.kt
│           │
│           ├── core/filesystem/
│           │   └── BluebirdFileSystem.kt     # Real filesystem support
│           │
│           ├── data/
│           │   ├── BootReceiver.kt           # Auto-launch on boot
│           │   └── NotificationListener.kt   # System notifications
│           │
│           ├── editor/                       # Text/code editor
│           │   ├── core/EditorModels.kt, PremiumEditorState.kt
│           │   ├── highlighting/SyntaxEngine.kt
│           │   └── ui/screens/PremiumTextEditorScreen.kt
│           │
│           ├── media/
│           │   ├── MediaLibraryRepository.kt
│           │   └── PlaybackService.kt
│           │
│           ├── ui/
│           │   ├── components/
│           │   │   ├── ActionCenter.kt, StartMenu.kt, Taskbar.kt
│           │   │   ├── Desktop.kt, WindowManager.kt, WidgetsPanel.kt
│           │   │   ├── CommonComponents.kt, HazeBlurSetup.kt, WallpaperAccent.kt
│           │   │   ├── FluentDesignSystem.kt # Shared DS tokens (colors, spacing, corners)
│           │   │   ├── NotificationToast.kt, SearchOverlay.kt
│           │   │   ├── TerminalScreen.kt, WebAppManager.kt
│           │   │   └── DesktopPreferences.kt, DesktopWallpaperState.kt
│           │   ├── screens/
│           │   │   ├── AppScreens.kt         # Calculator, Calendar, Photos, TaskManager
│           │   │   ├── DesktopScreen.kt, SetupScreen.kt, SettingsScreen.kt
│           │   │   ├── FileExplorerScreen.kt, ImageViewerScreen.kt
│           │   │   ├── MediaPlayerScreen.kt, MessagesScreen.kt, RecycleBinScreen.kt
│           │   │   ├── LockScreenActivity.kt, BrowserScreen.kt
│           │   │   └── Launcherupdatesettings.kt
│           │   └── theme/Theme.kt            # bluebirdColors, dark/light Material3 themes
│           │
│           ├── wordprocessor/                # Word Impress
│           │   ├── WordImpress.kt, RichTextEngine.kt, BlockViews.kt
│           │   ├── Pagination.kt, PdfExport.kt
│           │   └── WdocIO.kt, WdocModel.kt
│           │
│           └── update/
│               ├── UpdateManager.kt          # GitHub-based update checker & downloader
│               ├── UpdateNotificationHelper.kt
│               └── Updatemodels.kt
│
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/gradle-wrapper.properties
│
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── CHANGELOG.md
├── CONTRIBUTING.md
├── ARCHITECTURE.md
├── FEATURES.md
├── BUILDING.md
├── LICENSE
├── SECURITY.md
├── README.md
└── screenshots/
```

See [FEATURES.md](FEATURES.md) for the full feature list, [BUILDING.md](BUILDING.md) for build/run instructions and permissions, and [CHANGELOG.md](CHANGELOG.md) for release history.
