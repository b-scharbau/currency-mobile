# Currency Mobile

A Kotlin Multiplatform + Compose Multiplatform mobile app for converting between currencies,
using live rates and the full currency list fetched from the
[currency-calculator](https://github.com/b-scharbau/currency-calculator) API
(`currency.bscharbau.com`).

## Structure

Standard Kotlin Multiplatform layout, one `composeApp` module targeting Android and iOS:

- `composeApp/src/commonMain` — shared code: `CurrencyApi.kt` (fetches the currency list and
  rates from `currency.bscharbau.com` via Ktor), `CurrencyRepository.kt` (combines the API with
  the local SQLDelight cache — see Persistence below), `CurrencyConverter.kt` (the pure conversion
  math), `Theme.kt` (brand colors/typography), `SignalDivider.kt` (the zigzag divider graphic),
  `db/DatabaseDriverFactory.kt` (`expect` declaration for the platform SQLite driver), and
  `App.kt` (the Compose UI, including the from/to currency pickers).
- `composeApp/src/commonMain/sqldelight` — the SQL schema (`Currency.sq`, `Rate.sq`) SQLDelight
  generates the typed `AppDatabase` API from.
- `composeApp/src/androidMain` — `MainActivity.kt` (constructs the database with
  `AndroidSqliteDriver` and passes the repository into `App()`) and the `actual
  DatabaseDriverFactory`.
- `composeApp/src/iosMain` — `MainViewController.kt` (same, with `NativeSqliteDriver`), exposing
  the shared Compose UI as a `UIViewController` for iOS.
- `composeApp/src/commonTest` — unit tests for the conversion logic and API response parsing
  (via Ktor's `MockEngine` — no live network calls in the test suite).
- `composeApp/src/androidUnitTest` — `CurrencyRepositoryTest.kt`, exercising the repository
  against a real in-memory SQLite database (see Persistence below).

## Persistence

The full currency list and the most recently fetched rate per (from, to) pair are cached locally
via [SQLDelight](https://sqldelight.github.io/sqldelight/) (`AndroidSqliteDriver` on Android,
`NativeSqliteDriver` on iOS — same generated schema/queries either way). `CurrencyRepository`
wraps this with a "show cached, then refresh" pattern: on launch and on every from/to change, the
cached value (if any) is shown immediately, then a fresh fetch runs in the background and updates
both the UI and the cache on success — but silently keeps showing the cached value if the refresh
fails, rather than replacing a working display with an error.

`CurrencyRepositoryTest` (in `androidUnitTest`, not `commonTest`) verifies this against a real
in-memory SQLite database via SQLDelight's JDBC driver — Android's local unit tests run on a plain
JVM with no real Android SQLite implementation available (that needs Robolectric or an actual
device/emulator), so the JDBC driver stands in for `AndroidSqliteDriver` in tests; it's the same
generated schema and queries either way. One test in particular
(`cachedRateSurvivesAFailedRefresh`) directly proves the point of this feature: a rate fetched
successfully is still readable from the cache after a subsequent refresh attempt fails.

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

- The cache never expires or gets cleaned up — it just accumulates one row per (from, to) pair
  ever viewed, with no TTL or "last updated" staleness indicator shown in the UI.
- No retry logic on network failure — a failed request falls back to the cache if there's a hit,
  or shows an error message if there isn't; either way, there's no automatic retry.
- Amount input has no client-side validation beyond `toDoubleOrNull()`; invalid text just shows
  "Enter a valid amount" rather than proper input filtering.
