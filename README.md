# Currency Mobile

A Kotlin Multiplatform + Compose Multiplatform mobile app for converting currencies. First
version: a single hardcoded JPY → EUR conversion, standing in until it's wired up to the
[currency-calculator](https://github.com/b-scharbau/currency-calculator) API's `/convert`
endpoint.

## Structure

Standard Kotlin Multiplatform layout, one `composeApp` module targeting Android and iOS:

- `composeApp/src/commonMain` — shared code: `CurrencyConverter.kt` (the hardcoded conversion
  logic) and `App.kt` (the Compose UI — a single screen with an amount field and the converted
  result).
- `composeApp/src/androidMain` — `MainActivity.kt`, the Android entry point.
- `composeApp/src/iosMain` — `MainViewController.kt`, exposing the shared Compose UI as a
  `UIViewController` for iOS.
- `composeApp/src/commonTest` — unit tests for the conversion logic.

## Building

Requires JDK 17+ and an Android SDK (compileSdk 35). Point `local.properties` at your SDK
(`sdk.dir=/path/to/sdk`), then:

```sh
./gradlew :composeApp:assembleDebug       # builds the Android debug APK
./gradlew :composeApp:testDebugUnitTest   # runs the shared unit tests
```

### iOS

The iOS target (`iosX64`, `iosArm64`, `iosSimulatorArm64`) is declared in
`composeApp/build.gradle.kts` and `MainViewController.kt` is real, buildable KMP/Compose code —
but Kotlin/Native can only compile iOS targets on macOS with Xcode installed, so this hasn't been
built or run (this project was developed on Linux). To run it on iOS you'd need to create an
Xcode project on a Mac that embeds the `ComposeApp` framework produced by
`./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64` (or the equivalent target for a real
device), calling `MainViewController()` from a SwiftUI/UIKit wrapper.

## Known limitations (first version)

- The JPY → EUR rate is hardcoded (`CurrencyConverter.kt`), not fetched live.
- Only one currency pair; no currency selection UI yet.
- Not yet wired up to the currency-calculator backend API.
