<p align="center"> 
  <img src="https://img.shields.io/badge/Android-8.0%2B-3DDC84?style=flat-square&logo=android" alt="Android">
  
  <img src="https://img.shields.io/badge/License-Apache%202.0-blue?style=flat-square" alt="License">
  <a href="https://github.com/soe1hom-arch/calcduo/releases/latest"><img src="https://img.shields.io/badge/Release-1.6.3-7B1FA2?style=flat-square&logo=github" alt="Release"></a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin">
  <img src="https://img.shields.io/badge/Material%20Design%203-0066CC?style=for-the-badge&logo=materialdesign&logoColor=white" alt="Material Design 3">
</p>

<h1 align="center">CalcDuo</h1>
<p align="center"><em>Premium Dual Calculator for Android</em></p>

<p align="center">
  Two calculators in one view — standard & scientific modes.<br>
  Built with Kotlin & Material Design 3.
</p>

---

## Features

| | |
|---|---|
| **Dual Display** | Two calculators on screen at once |
| **Standard Mode** | AC, ⌫, %, ÷, ×, −, +, ±, ., = |
| **Scientific Mode** | sin, cos, tan, log, ln, √, x², 1/x, xʸ, π, e |
| **Smart Engine** | Parentheses, operator precedence, error handling |
| **History** | Saved calculations per calculator |
| **Memory** | MC / MR / M+ / M− with indicator |
| **Notes** | Built-in notepad |
| **Themes** | System, Light, Dark, Grey |
| **Haptic** | Optional key vibration |
| **Offline** | No internet needed |

---

## Keyboard

### Standard

`AC` `∨` `⌫` `÷` / `7` `8` `9` `×` / `4` `5` `6` `−` / `1` `2` `3` `+` / `±` `0` `.` `=`

### Scientific

`sin` `cos` `tan` `÷` / `log` `ln` `√` `x²` / `1/x` `xʸ` `π` `e` / `(` `)` `%` `∧`

Tap **∨** to expand, **∧** to collapse.

---

## Download

| Channel | Link |
|---|---|
| **Google Play** | *(Coming soon)* |
| **GitHub Releases** | [Releases page](https://github.com/soe1hom-arch/calcduo/releases/latest) |
| **CI Build** | [Actions tab](https://github.com/soe1hom-arch/calcduo/actions) |

---

## Build

```bash
git clone https://github.com/soe1hom-arch/calcduo.git
cd calcduo
./gradlew assembleDebug          # debug APK
# ./gradlew assembleRelease      # signed release (requires keystore)
```

### Local Release Build

To generate a keystore and set up for local release builds:

```bash
# Passwords via env vars (recommended for scripting)
KEY_PASSWORD="your_password" STORE_PASSWORD="your_password" \
  ./scripts/setup-release.sh

# Or run interactively (passwords hidden)
./scripts/setup-release.sh
```

> **Security:** Never hardcode passwords in scripts or commit them to the repository.

### CI / GitHub Secrets

Push to `main` → auto debug build. Tag `v*` → signed release.

| Secret | Purpose |
|---|---|
| `KEYSTORE_BASE64` | Base64-encoded keystore file |
| `KEYSTORE_PASSWORD` | Keystore password |
| `KEY_ALIAS` | Key alias |
| `KEY_PASSWORD` | Key password |
| `PAT_TOKEN` | GitHub token for release creation *(optional — only for `build-release.yml`)* |

---

## Requirements

| | |
|---|---|
| **Android** | 8.0+ (API 26) |
| **Size** | ~5 MB |
| **Permissions** | Vibrate (haptic only) |

---

## Tech Stack

Kotlin · ViewModel · StateFlow · Material Design 3 · ViewBinding · Gradle KTS · GitHub Actions

---

## Privacy

No data collected. No internet access required. Fully offline.

---

## License

```
Copyright 2016 The Android Open Source Project
Copyright 2026 soe1hom-arch
Apache License 2.0
```
