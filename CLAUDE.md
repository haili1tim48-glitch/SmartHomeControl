# CLAUDE.md — SmartHomeControl

## Project Overview

SmartHomeControl is an Android application for controlling smart home devices using gesture recognition. It is built with Kotlin and Jetpack Compose, targeting modern Android devices (API 24+). The project is in its early stage — foundational scaffolding is in place with a single-activity Compose architecture, Material 3 theming, and test infrastructure ready for feature development.

**Package:** `com.example.smarthomecontrol`

## Repository Structure

```
SmartHomeControl/
├── app/                              # Main application module
│   ├── build.gradle.kts              # App-level build config
│   ├── proguard-rules.pro            # ProGuard/R8 rules (currently unused)
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml   # App manifest (single activity)
│       │   ├── java/com/example/smarthomecontrol/
│       │   │   ├── MainActivity.kt   # Entry point, Compose host
│       │   │   └── ui/theme/         # Material 3 theme definitions
│       │   │       ├── Color.kt      # Color palette (light/dark)
│       │   │       ├── Theme.kt      # Theme provider composable
│       │   │       └── Type.kt       # Typography styles
│       │   └── res/                  # Android resources
│       │       ├── drawable/         # Vector drawables (launcher icons)
│       │       ├── mipmap-*/         # Density-specific app icons (WebP)
│       │       ├── values/           # colors.xml, strings.xml, themes.xml
│       │       └── xml/              # Backup and data extraction rules
│       ├── test/                     # Local unit tests (JVM)
│       └── androidTest/              # Instrumented tests (device/emulator)
├── gradle/
│   ├── libs.versions.toml            # Centralized dependency version catalog
│   └── wrapper/                      # Gradle wrapper (v8.13)
├── build.gradle.kts                  # Root build config (plugin declarations)
├── settings.gradle.kts               # Project settings, repository config
├── gradle.properties                 # JVM args, AndroidX flags
├── gradlew / gradlew.bat            # Gradle wrapper scripts
└── .gitignore                        # Excludes build artifacts, IDE files
```

## Tech Stack

| Component         | Technology                     | Version      |
|-------------------|--------------------------------|--------------|
| Language          | Kotlin                         | 2.0.21       |
| JVM Target        | Java                           | 11           |
| UI Framework      | Jetpack Compose                | BOM 2024.09.00 |
| Design System     | Material Design 3              | latest       |
| Build System      | Gradle (Kotlin DSL)            | 8.13         |
| Android Plugin    | AGP                            | 8.13.2       |
| Min SDK           | Android API 24                 | (Android 7.0) |
| Target/Compile SDK| Android API 36                 |              |
| Unit Testing      | JUnit 4                        | 4.13.2       |
| UI Testing        | Espresso                       | 3.7.0        |
| Compose Testing   | Compose UI Test JUnit4         | (via BOM)    |

## Build & Run

```bash
# Build debug APK
./gradlew assembleDebug

# Run unit tests (local JVM)
./gradlew test

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Clean build
./gradlew clean

# Full check (lint + tests)
./gradlew check
```

No CI/CD pipeline is configured yet.

## Architecture & Conventions

### App Architecture

- **Single Activity:** `MainActivity` is the sole entry point, using `ComponentActivity` with Compose
- **Compose-only UI:** No XML layouts; all UI is declarative Compose
- **Edge-to-edge:** `enableEdgeToEdge()` is enabled for modern display support
- **Scaffold-based layout:** Uses Material 3 `Scaffold` as the root layout container

### Package Organization

Source code lives under `com.example.smarthomecontrol`:
- Root package: Activities and top-level components
- `ui/theme/`: Theme definitions (Color, Theme, Typography)
- Future packages should follow feature-based organization (e.g., `gesture/`, `devices/`, `network/`)

### Kotlin & Compose Conventions

- **Naming:** PascalCase for classes and `@Composable` functions; camelCase for regular functions and properties
- **Composables:** Accept a `modifier: Modifier = Modifier` parameter; use `@Preview` annotations for development previews
- **Theme:** Use `SmartHomeControlTheme` wrapper composable; supports light/dark mode and dynamic colors (Android 12+)
- **File naming:** Matches the primary class/composable name within the file

### Dependency Management

All dependency versions are centralized in `gradle/libs.versions.toml`. Reference libraries using the version catalog syntax in build scripts (e.g., `libs.androidx.core.ktx`). Do not hardcode version strings in `build.gradle.kts` files.

### Resource Conventions

- **Strings:** Defined in `res/values/strings.xml` — avoid hardcoded strings in Kotlin code
- **Colors:** XML colors in `res/values/colors.xml` for non-Compose contexts; Compose colors in `ui/theme/Color.kt`
- **Themes:** XML theme in `res/values/themes.xml` (for system/manifest); Compose theme in `ui/theme/Theme.kt`
- **Icons:** Adaptive icons with separate foreground/background layers; density-specific variants in mipmap directories

## Testing

### Unit Tests (`src/test/`)

- Run on local JVM, no Android dependencies needed
- Framework: JUnit 4
- Location: `app/src/test/java/com/example/smarthomecontrol/`
- Run: `./gradlew test`

### Instrumented Tests (`src/androidTest/`)

- Run on a device or emulator
- Frameworks: AndroidX Test, Espresso, Compose UI Test
- Runner: `androidx.test.runner.AndroidJUnitRunner`
- Location: `app/src/androidTest/java/com/example/smarthomecontrol/`
- Run: `./gradlew connectedAndroidTest`

### Writing Tests

- Place unit tests alongside the source structure under `src/test/`
- Place device-dependent or UI tests under `src/androidTest/`
- Use Compose testing APIs (`createComposeRule`, `onNodeWithText`, etc.) for composable tests

## Configuration Details

### gradle.properties

- JVM heap: 2048 MB (`-Xmx2048m`)
- AndroidX enabled (`android.useAndroidX=true`)
- Non-transitive R classes enabled for smaller APKs
- Kotlin official code style enforced

### ProGuard/R8

- Code shrinking is **disabled** in all build types (`minifyEnabled = false`)
- `proguard-rules.pro` exists with placeholder rules for future use

### .gitignore

Excludes: build artifacts (`.apk`, `.aab`, `.dex`), IDE files (`.idea/`, `*.iml`), Gradle caches (`.gradle/`, `build/`), OS files (`.DS_Store`), and media files (`*.mp4`, `*.avi`, `videos/`, `uploads/`)

## Git Workflow

- **Branch:** Development happens on feature branches (e.g., `claude/...`)
- **Single module:** Only the `:app` module exists; no multi-module setup
- **Remote repositories:** Google Maven, Maven Central, Gradle Plugin Portal (configured in `settings.gradle.kts`)

## Key Files Quick Reference

| File | Purpose |
|------|---------|
| `app/src/main/java/.../MainActivity.kt` | App entry point, Compose host |
| `app/src/main/java/.../ui/theme/Theme.kt` | Material 3 theme provider |
| `app/src/main/java/.../ui/theme/Color.kt` | Color definitions (light/dark palettes) |
| `app/src/main/java/.../ui/theme/Type.kt` | Typography configuration |
| `app/src/main/AndroidManifest.xml` | App manifest, activity declarations |
| `app/build.gradle.kts` | App module build configuration |
| `gradle/libs.versions.toml` | Centralized dependency versions |
| `settings.gradle.kts` | Project-level Gradle settings |
| `gradle.properties` | JVM and build flags |

## Notes for AI Assistants

- This is a Kotlin-first Android project — do not introduce Java source files
- All UI must use Jetpack Compose — do not add XML layouts or View-based UI code
- Use the version catalog (`libs.versions.toml`) when adding new dependencies
- Follow Material 3 design patterns and use the existing theme system
- The project uses Gradle Kotlin DSL (`.gradle.kts`) — do not use Groovy build scripts
- Keep the single-activity, Compose-based architecture; add new screens as composable functions, not new activities
- When adding features, follow feature-based package organization under `com.example.smarthomecontrol`
- Ensure backward compatibility with API 24 (Android 7.0) minimum
