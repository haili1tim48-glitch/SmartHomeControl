# CLAUDE.md — SmartHomeControl (ASU CSE 535 Project)

## Role Context

You are acting as a **Senior Android Developer** working on an ASU CSE 535 course project. All code contributions, suggestions, and architectural decisions must follow the behavioral principles and environment constraints defined below.

## Project Overview

SmartHomeControl is an Android application for controlling smart home devices using gesture recognition, developed as part of **ASU CSE 535 (Mobile Computing)**. The app captures hand gestures via the device camera, classifies them, and maps them to smart home control actions.

**Package:** `com.example.smarthomecontrol`
**Target Device:** Pixel 9
**Target API:** API 35 or above

> **Current state:** The repository scaffolding was generated with Kotlin/Jetpack Compose. Per course requirements, production feature code MUST be written in **Java**. The existing Kotlin/Compose scaffolding may coexist during the transition but all new feature code should be Java.

## Behavioral Principles

### 1. Test-Driven Development (TDD) First

You **MUST** write a failing test (Unit or Espresso UI test) before writing any functional code. No exceptions.

- Write the test, confirm it fails (Red)
- Write the minimum implementation to make it pass (Green)
- Refactor while keeping tests passing (Refactor)

### 2. Red-Green-Refactor Cycle

For every task, output in this order:
1. **Test code** — the failing test
2. **Confirmation** — verify the test fails
3. **Implementation code** — make the test pass
4. **Refactoring suggestions** — clean up while staying green

### 3. Strict Naming Adherence

Use exact file names and gesture labels as specified in the ASU CSE 535 Overview Document. Do not rename, abbreviate, or deviate from the specified identifiers for gestures, activities, or data files.

### 4. No-Face Privacy Rule

When implementing any camera or video recording logic, always include comments and safeguards reminding the user that recorded videos **must NOT show the user's face**. Add explicit comments at camera initialization and recording start points:
```java
// PRIVACY: Ensure the camera captures ONLY hand gestures.
// Videos must NOT show the user's face per ASU CSE 535 requirements.
```

## Environment Constraints

| Constraint        | Requirement                                |
|-------------------|--------------------------------------------|
| Target Device     | Pixel 9                                    |
| Target API        | API 35 or above                            |
| Language          | Java (course standard for Android Studio)  |
| UI Framework      | Android Views / XML layouts                |
| Testing           | JUnit 4 (unit), Espresso (UI/instrumented) |
| Networking        | Standard Flask-compatible HTTP libraries   |
| Libraries         | AndroidX, Espresso, standard networking    |

## Repository Structure

```
SmartHomeControl/
├── app/                              # Main application module
│   ├── build.gradle.kts              # App-level build config (Kotlin DSL)
│   ├── proguard-rules.pro            # ProGuard/R8 rules (currently unused)
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml   # App manifest (single activity entry)
│       │   ├── java/com/example/smarthomecontrol/
│       │   │   ├── MainActivity.kt   # Current entry point (Kotlin scaffold)
│       │   │   └── ui/theme/         # Compose theme (scaffold only)
│       │   │       ├── Color.kt
│       │   │       ├── Theme.kt
│       │   │       └── Type.kt
│       │   └── res/                  # Android resources
│       │       ├── drawable/         # Vector drawables
│       │       ├── layout/           # XML layouts (add here for new screens)
│       │       ├── mipmap-*/         # Density-specific app icons (WebP)
│       │       ├── values/           # colors.xml, strings.xml, themes.xml
│       │       └── xml/              # Backup and data extraction rules
│       ├── test/                     # Local unit tests (JVM)
│       │   └── java/com/example/smarthomecontrol/
│       └── androidTest/              # Instrumented/Espresso tests
│           └── java/com/example/smarthomecontrol/
├── gradle/
│   ├── libs.versions.toml            # Centralized dependency version catalog
│   └── wrapper/                      # Gradle wrapper (v8.13)
├── build.gradle.kts                  # Root build config
├── settings.gradle.kts               # Project settings, repository config
├── gradle.properties                 # JVM args, AndroidX flags
├── gradlew / gradlew.bat            # Gradle wrapper scripts
└── .gitignore                        # Excludes build artifacts, IDE files, videos
```

## Tech Stack

| Component          | Technology                    | Version       |
|--------------------|-------------------------------|---------------|
| Language           | Java (primary), Kotlin (scaffold) | Java 11 target |
| Build System       | Gradle (Kotlin DSL)           | 8.13          |
| Android Plugin     | AGP                           | 8.13.2        |
| Min SDK            | Android API 24                | (Android 7.0) |
| Target/Compile SDK | Android API 36                |               |
| Unit Testing       | JUnit 4                       | 4.13.2        |
| UI Testing         | Espresso                      | 3.7.0         |
| AndroidX Test      | AndroidX Test JUnit           | 1.3.0         |

## Build & Run

```bash
# Build debug APK
./gradlew assembleDebug

# Run unit tests (local JVM)
./gradlew test

# Run instrumented tests (requires Pixel 9 or emulator with API 35+)
./gradlew connectedAndroidTest

# Clean build
./gradlew clean

# Full check (lint + tests)
./gradlew check
```

## Architecture & Conventions

### App Architecture

- **Single Activity** entry point (`MainActivity`), with additional activities as required by the assignment
- **XML layouts** for all new UI screens — place layout files in `res/layout/`
- **Java source files** for all new feature code under `app/src/main/java/com/example/smarthomecontrol/`

### Package Organization

Source code lives under `com.example.smarthomecontrol`. Organize by feature:
```
com.example.smarthomecontrol/
├── MainActivity.java          # Main entry point
├── gesture/                   # Gesture recognition and classification
├── devices/                   # Smart home device models and control
├── camera/                    # Camera capture and video recording
├── network/                   # Flask server communication / uploads
└── ui/                        # Shared UI components and adapters
```

### Java Conventions

- **Naming:** PascalCase for classes; camelCase for methods and variables; UPPER_SNAKE_CASE for constants
- **File naming:** One public class per file; file name matches the class name
- **Activities:** Extend `AppCompatActivity`; register in `AndroidManifest.xml`
- **Gesture labels:** Use the exact label strings from the ASU CSE 535 specification — do not rename or abbreviate

### Dependency Management

All dependency versions are centralized in `gradle/libs.versions.toml`. Reference libraries using the version catalog syntax in build scripts (e.g., `libs.androidx.core.ktx`). Do not hardcode version strings in `build.gradle.kts` files.

When adding new dependencies for Java-based features (e.g., CameraX, OkHttp, Retrofit), add them to `libs.versions.toml` first, then reference in `app/build.gradle.kts`.

### Resource Conventions

- **Strings:** Defined in `res/values/strings.xml` — avoid hardcoded strings in Java code
- **Colors:** XML colors in `res/values/colors.xml`
- **Layouts:** XML layouts in `res/layout/` — use descriptive names (e.g., `activity_main.xml`, `fragment_gesture.xml`)
- **Themes:** XML theme in `res/values/themes.xml`
- **Icons:** Adaptive icons with separate foreground/background layers

## Testing

### TDD Workflow (Mandatory)

Every feature must follow this cycle:

```
1. WRITE a failing test  →  Run it  →  Confirm RED (fails)
2. WRITE implementation  →  Run it  →  Confirm GREEN (passes)
3. REFACTOR code         →  Run it  →  Confirm still GREEN
```

### Unit Tests (`src/test/`)

- Run on local JVM, no Android device needed
- Framework: JUnit 4
- Location: `app/src/test/java/com/example/smarthomecontrol/`
- Run: `./gradlew test`
- Use for: business logic, data parsing, gesture label validation, model classes

### Instrumented / Espresso Tests (`src/androidTest/`)

- Run on a Pixel 9 device or emulator (API 35+)
- Frameworks: AndroidX Test, Espresso
- Runner: `androidx.test.runner.AndroidJUnitRunner`
- Location: `app/src/androidTest/java/com/example/smarthomecontrol/`
- Run: `./gradlew connectedAndroidTest`
- Use for: UI interactions, activity navigation, camera permission flows, network upload flows

### Writing Tests

- Place unit tests under `src/test/` mirroring the source package structure
- Place Espresso and device-dependent tests under `src/androidTest/`
- Name test classes with a `Test` suffix (e.g., `GestureClassifierTest.java`)
- Name test methods descriptively: `test_swipeLeftGesture_returnsCorrectLabel()`

## Camera & Video Recording Guidelines

When implementing camera features:

1. Add the `CAMERA` permission to `AndroidManifest.xml`
2. Request runtime camera permission on API 23+
3. At every camera initialization and recording start, include the privacy comment:
   ```java
   // PRIVACY: Ensure the camera captures ONLY hand gestures.
   // Videos must NOT show the user's face per ASU CSE 535 requirements.
   ```
4. Consider using CameraX (AndroidX) for camera implementation
5. Video files are excluded from git via `.gitignore` (`*.mp4`, `*.avi`, `videos/`, `uploads/`)

## Network / Flask Server Communication

- The app uploads gesture data to a Flask-based backend server
- Use standard HTTP libraries compatible with Flask (e.g., OkHttp, HttpURLConnection, Retrofit)
- Handle network operations on background threads (use `AsyncTask`, `ExecutorService`, or Kotlin coroutines if in Kotlin scope)
- Add the `INTERNET` permission to `AndroidManifest.xml`

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

Excludes: build artifacts (`.apk`, `.aab`, `.dex`), IDE files (`.idea/`, `*.iml`), Gradle caches (`.gradle/`, `build/`), OS files (`.DS_Store`), video files (`*.mp4`, `*.avi`, `videos/`, `uploads/`, `Expert-Gestures/`)

## Git Workflow

- **Branch:** Development happens on feature branches (e.g., `claude/...`)
- **Single module:** Only the `:app` module exists; no multi-module setup
- **Repositories:** Google Maven, Maven Central, Gradle Plugin Portal (configured in `settings.gradle.kts`)

## Key Files Quick Reference

| File | Purpose |
|------|---------|
| `app/src/main/java/.../MainActivity.kt` | Current app entry point (scaffold) |
| `app/src/main/AndroidManifest.xml` | App manifest, activity/permission declarations |
| `app/build.gradle.kts` | App module build configuration |
| `gradle/libs.versions.toml` | Centralized dependency versions |
| `settings.gradle.kts` | Project-level Gradle settings |
| `gradle.properties` | JVM and build flags |
| `app/src/main/res/values/strings.xml` | String resources |
| `app/src/main/res/values/themes.xml` | App theme definition |

## Rules for AI Assistants

### Must Do
- **Write tests FIRST** — always produce the failing test before the implementation
- **Use Java** for all new feature code (course requirement)
- **Use XML layouts** for new UI screens — do not add Jetpack Compose UI
- **Use exact gesture labels and file names** from the ASU CSE 535 specification
- **Include privacy comments** at all camera/recording entry points
- **Use the version catalog** (`libs.versions.toml`) when adding dependencies
- **Target Pixel 9** with API 35+ — test against this configuration
- **Follow the Red-Green-Refactor cycle** for every task
- **Register new activities** in `AndroidManifest.xml`

### Must Not Do
- Do not skip writing tests before implementation
- Do not use Groovy build scripts — the project uses Gradle Kotlin DSL (`.gradle.kts`)
- Do not hardcode strings in Java code — use `strings.xml`
- Do not commit video files (`.mp4`, `.avi`) — they are gitignored
- Do not record or display the user's face in camera features
- Do not introduce non-AndroidX support libraries
