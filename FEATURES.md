# Features

Full feature list, grouped by the part of the shell each one lives in. Where useful, a section notes *how* something is actually implemented (real Android APIs vs. simulated behavior) — Bluebird aims to be a real, functioning environment, not a set of screens that only look like one.

---

## Desktop Environment

| Feature | Description |
|---------|-------------|
| **Floating Windows** | Every app opens in a draggable, focusable window with minimize/maximize/close |
| **Mica & Acrylic materials** | Real backdrop blur (via Haze), tinted by a muted color sampled from Bluebird's own active wallpaper |
| **Multi-window management** | Stack and switch between multiple open windows |
| **Snap layout picker** | Windows 11–style snap layout overlay for quick window positioning |
| **Desktop icons** | System icons (This PC, Recycle Bin, Settings, Network), file shortcuts, and app shortcuts, using Fluent UI System Icons |
| **Right-click context menu** | Long-press desktop → New Folder, New Text File, Personalize, Refresh, Display Settings |
| **Desktop shortcuts** | Drag any file from File Explorer → Create Shortcut; long-press app in Start Menu → Add to Desktop |
| **Scrollable desktop** | Desktop scrolls when icons exceed the screen area |
| **Live wallpapers** | Aurora, Nebula, Waves, and Bokeh animated wallpapers |
| **Desktop particle animations** | Snow, Bubbles, Stars, Rain, Hearts, Confetti, Fireflies, Leaves, Matrix, and Sakura |
| **Built-in gradient wallpapers** | Blue Bloom, Sunset Purple, Forest Green, Deep Space, Aurora, and more |
| **Custom wallpaper** | Pick any image from Gallery for home screen and lock screen separately — stays inside Bluebird, doesn't touch the Android system wallpaper |
| **Wallpaper persistence** | Custom images copied to internal storage — survive app restarts |
| **Wallpaper slideshow mode** | Automatically rotate through a selected set of wallpapers |
| **Double-tap taskbar toggle** | Double-tap empty desktop space to hide/show the taskbar (full immersive mode) |
| **Adaptive canvas scaling** | Icon grid, windows, and text scale to the device's actual size/density, so the desktop looks correctly proportioned in both portrait and landscape |
| **Dark / Light / For You / Special themes** | Full theme switching, live and persistent, with an 8-color accent picker |

## Taskbar

| Feature | Description |
|---------|-------------|
| **Windows 11 taskbar** | Centered Start button, pinned apps, running window indicators, system tray |
| **Pin / unpin apps** | Long-press any app icon in Start Menu or taskbar to pin/unpin |
| **Running window badges** | Dot indicator below any app with an open window |
| **Window switcher** | Tap a running window's taskbar icon to focus or restore it |
| **System tray** | Battery level, Wi-Fi indicator, Bluetooth, clock, date — Fluent-style tray glyphs |
| **Notification badge** | Badge count on Action Center button when there are unread notifications |
| **Toast notifications** | Transient, Windows-style toast popups for incoming notifications and system events |

## Start Menu

| Feature | Description |
|---------|-------------|
| **App grid** | Pinned apps + all built-in apps in a 6-column icon grid |
| **All Apps view** | Scroll through every installed app on the device |
| **Real-time search** | Type to filter installed apps instantly |
| **User profile** | Real username + avatar (from OOBE setup) shown in bottom bar |
| **App context menu** | Long-press any app → Open / Add to Desktop / Pin to Taskbar |
| **Power menu** | Sleep, Lock, Restart, Shut Down options |

## File Explorer (Real Filesystem)

A genuine file manager, not a themed shortcut list — backed by `BluebirdFileSystem`, sitting directly on top of Android's `File` API. Every folder, file size, and modified date shown is real.

| Feature | Description |
|---------|-------------|
| **True filesystem browsing** | Real files, real sizes, real dates |
| **Breadcrumb navigation** | Clickable path segments |
| **List & Grid views** | Toggle between detail list and thumbnail grid |
| **Sort options** | Name, Date, Size, or Type — ascending or descending |
| **Hidden files toggle** | Show/hide dotfiles |
| **File search** | Filter files in the current directory by name |
| **File operations** | Copy, Cut, Paste, Rename, Delete (to Recycle Bin) |
| **Create desktop shortcut** | Right-click any file/folder → Create Desktop Shortcut |
| **Quick Access panel** | Desktop, Downloads, Documents, Pictures, Music, Movies, DCIM, Storage |
| **Storage progress bar** | Used/free internal storage in left panel |
| **Advanced copy/move engine** | Shared between Desktop and File Explorer, with live progress, speed/time estimates, and cancel support |

## Bluebird App Store & Web Apps

Bluebird's installer mimics a real Windows-style software install instead of just launching a bookmark. A web app's HTML/CSS/JS assets get packaged into a zip with the right internal structure, then renamed to Bluebird's own `.bpk` package extension — opening one triggers a real "install" flow (icon, name, permissions) rather than just opening a URL.

| Feature | Description |
|---------|-------------|
| **Bluebird App Store** | Browse and install apps directly inside the launcher |
| **Custom HTML/CSS/JS app installation** | Package your own lightweight web-based apps as a `.bpk` installer |
| **Install from URL** | Install any web app directly from a URL |
| **Web App Manager / Viewer** | Manage installed web apps; dedicated runtime window separate from the main browser |
| **Custom icon / favicon support** | Installed web apps show their own icon on the desktop and Start Menu |

## Word Impress — Word Processor

| Feature | Description |
|---------|-------------|
| **Rich text editing** | Full rich-text engine (`RichTextEngine`) with block-based document model |
| **Pagination** | Real page-based layout with automatic pagination as you type |
| **PDF export** | Export any document directly to PDF |
| **Native `.wdoc` format** | Saved/loaded through a dedicated `WdocIO` / `WdocModel` pipeline |

## Terminal

A real command surface, not a decorative black box — commands run through Bluebird's own shell bridge, in a standard floating window like any other app.

## Text Editor

A lightweight code/text editor for quick edits — Notepad++ rather than a full IDE. Syntax highlighting (`SyntaxEngine`), multi-tab state, line numbers, find/replace.

## Image Viewer

| Feature | Description |
|---------|-------------|
| **Full-screen viewer** | Immersive full-screen mode |
| **Pinch-to-zoom** | Multi-touch pinch zoom, pan, double-tap to zoom 2.5x |
| **Swipe navigation** | Move between images in the same folder |
| **Rotate, share, delete to bin, set as wallpaper** | Standard viewer actions |
| **Image info panel** | File name, size, date modified, path |

## Media Player

| Feature | Description |
|---------|-------------|
| **Media3 (ExoPlayer) playback** | MP3, WAV, OGG, FLAC, AAC, M4A, MP4, MKV, AVI, MOV, WebM |
| **Background playback service** | Dedicated `PlaybackService` for uninterrupted playback |
| **Auto-playlist** | Loads all media in the same folder via `MediaLibraryRepository` |
| **Playback controls** | Play/Pause, Previous, Next, Shuffle, Repeat (Off / All / One) |
| **Playlist panel** | Sidebar with all tracks, now-playing indicator, equalizer animation |

## Phone & Messages

| Feature | Description |
|---------|-------------|
| **Real contacts & call log** | Reads from `ContactsContract` / `CallLog` |
| **Numeric keypad** | Triggers `ACTION_CALL` intent |
| **Real SMS threads** | Reads from `Telephony.Sms.Conversations`, sends via `ACTION_SENDTO` |
| **Contact resolution** | Phone numbers resolved to contact names automatically |

## Recycle Bin

All files deleted from File Explorer land here — restorable, permanently deletable, or bulk-emptied, with original path/deletion date/size shown per item.

## Settings

Settings are exposed through `LauncherUiState` across these categories:

| Section | Highlights |
|---------|-----------|
| **Appearance** | Theme, text scale, icon size, taskbar position, layout, animation speed, corner radius, wallpaper slideshow |
| **System** | Launch on boot, snap layouts, clipboard history, Do Not Disturb, Focus Assist, game mode, battery saver |
| **Sound** | Media/ringtone/notification/alarm volume, system sounds, haptics |
| **Network** | Wi-Fi/Bluetooth toggles, data saver, hotspot, VPN, custom DNS |
| **Accessibility** | High contrast, larger/bold text, reduce motion, magnification, captions |
| **Privacy & Security** | Screen lock, permissions, usage diagnostics, biometric unlock |
| **Time & Language** | 24-hour clock, timezone, date format, first day of week |

## Notifications

Live system notifications via `NotificationListenerService`, grouped in Action Center with app name/title/body/timestamp, plus Windows-style toast popups for incoming notifications.

## First-Launch Setup (OOBE)

Welcome screen → per-permission grant buttons → username → profile picture, with every step skippable and revisitable later in Settings.

## Lock Screen

Custom wallpaper (separate from home), live clock, user avatar, tap to unlock.

## Gestures & Undo

Swipe up/down, double-tap, pinch, and standard navigation gestures are supported across the shell. Reversible actions surface a clear, human-readable undo affordance.

---

## Built-in Apps

Every app below runs as its own floating window inside Bluebird's window manager — none are separate installable APKs, which is what makes the multi-window experience possible.

| App | Description |
|-----|-------------|
| **File Explorer** | Real filesystem browser |
| **Settings** | Full settings panel |
| **Phone** | Real dialer with contacts and call log |
| **Messages** | Real SMS reader and sender |
| **Media Player** | Audio/video player with playlist, built on Media3 |
| **Image Viewer / Photos** | Full-screen viewer and gallery |
| **Recycle Bin** | Deleted files manager |
| **Browser** | WebView-based browser with ad blocking |
| **Calculator / Calendar** | Standard utility apps |
| **Task Manager** | Running processes viewer |
| **Text Editor** | Syntax-highlighted in-launcher editor |
| **Word Impress** | Rich-text word processor with pagination and PDF export |
| **Bluebird Store** | Built-in app store for custom web apps |
| **Terminal** | On-device shell access |
| **Web App Manager / Viewer** | Manage and run installed web apps |
