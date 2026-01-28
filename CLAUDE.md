# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Development Commands

```bash
# Build the project
./gradlew build

# Build debug APK
./gradlew assembleDebug

# Install debug APK on connected device/emulator
./gradlew installDebug

# Run unit tests (local JVM)
./gradlew test

# Run a single unit test class
./gradlew test --tests "com.example.smarthomecontrol.ExampleUnitTest"

# Run instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Clean build artifacts
./gradlew clean
```

## Architecture

- **Single-module Android app** (`app/`) using Kotlin and Jetpack Compose
- **Single Activity pattern**: `MainActivity` extends `ComponentActivity`, all UI is built with Compose (no XML layouts)
- **Material3 theming** with dynamic color support (Android 12+), defined in `ui/theme/`
- **Package**: `com.example.smarthomecontrol`

## Key Configuration

- **Compile/Target SDK**: 36, **Min SDK**: 24
- **Kotlin**: 2.0.21 with Compose compiler plugin
- **JVM target**: Java 11
- **Dependency versions** managed via Gradle version catalog at `gradle/libs.versions.toml`
- **Compose BOM**: 2024.09.00 (controls all Compose library versions)

## Source Layout

```
app/src/main/java/com/example/smarthomecontrol/
├── MainActivity.kt          # Entry point, sets up Compose content
└── ui/theme/                 # Material3 theme (Color, Theme, Type)

app/src/test/                 # Local JVM unit tests (JUnit 4)
app/src/androidTest/          # Instrumented tests (AndroidJUnit4 + Espresso + Compose UI testing)
```
