<div align="center">

# Bluebird

### A full Windows 11 desktop experience — on Android.

[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B-brightgreen?style=flat-square&logo=android)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-7F52FF?style=flat-square&logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.08-4285F4?style=flat-square&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)](LICENSE)
[![Release](https://img.shields.io/badge/Release-v2.0-success?style=flat-square)](https://github.com/norbert-web/bluebird/releases)

<br/>

<p align="center">
  <img src="screenshots/desktop.png" width="32%">
  <img src="screenshots/startmenu.png" width="32%">
  <img src="screenshots/explorer.png" width="32%">
</p>

</div>

---

Bluebird turns your phone into a Windows 11 desktop — a real home screen launcher, not a themed wallpaper. Every window, icon, and menu is built from scratch in Jetpack Compose: floating windows you can drag and stack, a real file system underneath, a taskbar that tracks what's actually running, and Mica/Acrylic materials tinted by your own wallpaper.

Most desktop-style launchers are designed for landscape or tablets and fall apart the moment someone opens them one-handed in portrait — which is how most people actually hold their phone. Bluebird's icon grid, windows, and text scale to the device's real size and density, so it looks intentional on any screen. It's also light: ~27MB, smooth on 2GB RAM devices.

**Highlights:**

- Floating, draggable, resizable windows with a real taskbar and Start Menu
- A genuine file explorer, terminal, and rich-text word processor (Word Impress)
- A built-in BPK manager for installing custom HTML/CSS/JS web apps as first-class desktop programs
- Live wallpapers, desktop particle effects, and wallpaper-driven Mica/Acrylic theming
- 

See **[FEATURES.md](FEATURES.md)** for the full list.

---

## Get Started

```bash
git clone https://github.com/norbert-web/bluebird.git
cd bluebird
./gradlew installDebug
```

Then set Bluebird as your default launcher when prompted. Full build/signing instructions, permissions, and known limitations: **[BUILDING.md](BUILDING.md)**.

**[Download the latest release →](https://github.com/norbert-web/bluebird/releases/latest)**

---

## Documentation

| | |
|---|---|
| **[FEATURES.md](FEATURES.md)** | Full feature list, section by section |
| **[ARCHITECTURE.md](ARCHITECTURE.md)** | State management, data flow, tech stack, project structure |
| **[BUILDING.md](BUILDING.md)** | Build/run instructions, permissions, known limitations |
| **[CHANGELOG.md](CHANGELOG.md)** | Release history |
| **[CONTRIBUTING.md](CONTRIBUTING.md)** | Dev setup, code style, PR process |
| **[SECURITY.md](SECURITY.md)** | Reporting vulnerabilities |

---

## Contributing

Contributions are welcome — see **[CONTRIBUTING.md](CONTRIBUTING.md)** for setup and guidelines. Quick version: fork, branch, keep composables small and stateless (mutations go through `LauncherViewModel`), open a PR.

## Credits

Built with [Jetpack Compose](https://developer.android.com/jetpack/compose), [Media3](https://developer.android.com/media/media3), [Coil](https://coil-kt.github.io/coil/), [Haze](https://github.com/chrisbanes/haze), and [Fluent UI System Icons](https://github.com/microsoft/fluentui-system-icons). Design language inspired by [Windows 11](https://www.microsoft.com/en-us/windows/windows-11).

<a href="https://github.com/norbert-web/bluebird/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=norbert-web/bluebird" />
</a>

## License

MIT — see [LICENSE](LICENSE).

## Contact

[Issues](https://github.com/norbert-web/bluebird/issues) · [Discussions](https://github.com/norbert-web/bluebird/discussions) · trebronwayne@gmail.com

<div align="center">

<br/>

Bluebird · [Back to top](#bluebird)

</div>
