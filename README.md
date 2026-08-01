# Currency Mobile

A Kotlin Multiplatform + Compose Multiplatform mobile app for converting between JPY and EUR,
bidirectionally, using live rates fetched from the
[currency-calculator](https://github.com/b-scharbau/currency-calculator) API
(`currency.bscharbau.com`).

## Structure

Standard Kotlin Multiplatform layout, one `composeApp` module targeting Android and iOS:

- `composeApp/src/commonMain` — shared code: `CurrencyApi.kt` (fetches rates from
  `currency.bscharbau.com/currency?code=` via Ktor), `CurrencyConverter.kt` (the pure conversion
  math and direction handling), `Theme.kt` (brand colors/typography), `SignalDivider.kt` (the
  zigzag divider graphic), and `App.kt` (the Compose UI).
- `composeApp/src/androidMain` — `MainActivity.kt`, the Android entry point.
- `composeApp/src/iosMain` — `MainViewController.kt`, exposing the shared Compose UI as a
  `UIViewController` for iOS.
- `composeApp/src/commonTest` — unit tests for the conversion logic and API response parsing
  (via Ktor's `MockEngine` — no live network calls in the test suite).

## Design

Matches the design system from the web app (`currency-calculator`'s `frontend/src/styles.css` /
bscharbau.com): the same `--paper`/`--ink`/`--signal`/`--muted`/`--line`/`--tint` color tokens
(see `BrandColors` in `Theme.kt`), Space Grotesk for headlines, IBM Plex Sans for body text, and
IBM Plex Mono for numeric values (amount, result, rate) — mirroring the web app's `.mono` usage
for figures. Fonts are bundled as static TTFs under `composeApp/src/commonMain/composeResources/
font/` (sourced from the IBM/Google Fonts and Space Grotesk upstream repos; OFL license texts in
`licenses/`), since only static per-weight files are reliably supported for text styling across
both Android and iOS in Compose Multiplatform.

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

- Only the JPY/EUR pair (swappable in either direction); no arbitrary currency selection UI yet.
- No caching — a fresh `/currency?code=` request is made every time the direction changes (the
  web frontend's approach), rather than persisting rates across app launches.
- No retry logic on network failure — a failed request just shows an error message.
