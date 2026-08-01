# Currency Mobile

[![CI](https://github.com/b-scharbau/currency-mobile/actions/workflows/ci.yml/badge.svg)](https://github.com/b-scharbau/currency-mobile/actions/workflows/ci.yml)

A Kotlin Multiplatform + Compose Multiplatform mobile app for converting between currencies,
using live rates and the full currency list fetched from the
[currency-calculator](https://github.com/b-scharbau/currency-calculator) API
(`currency.bscharbau.com`).

## Structure

Standard Kotlin Multiplatform layout, one `composeApp` module targeting Android and iOS:

- `composeApp/src/commonMain` — shared code: `CurrencyApi.kt` (fetches the currency list and
  rates from `currency.bscharbau.com` via Ktor), `NetworkRetry.kt` (the retry policy — see Network
  retries below), `CurrencyRepository.kt` (combines the API with the local SQLDelight cache — see
  Persistence below), `CurrencyConverter.kt` (the pure conversion math), `Theme.kt` (brand
  colors/typography), `SignalDivider.kt` (the zigzag divider graphic), `db/DatabaseDriverFactory.kt`
  (`expect` declaration for the platform SQLite driver), and `App.kt` (the Compose UI, including
  the from/to currency pickers).
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

## Network retries

`NetworkRetry.execute()` mirrors the backend's own `FrankfurterRetry`: up to 3 attempts with
increasing backoff (300ms, 600ms, ...) for transient failures — timeouts, connection errors, 5xx
server errors, anything that isn't a definite client error. A 4xx (`ClientRequestException`, e.g.
an unknown currency code) is never retried, since retrying it would just fail the same way again.
Both `CurrencyApi.fetchCurrencies()` and `fetchRates()` go through it; `expectSuccess = true` on the
Ktor client is what makes non-2xx responses throw typed exceptions in the first place, so the retry
logic can tell a 4xx from a 5xx from a network-level failure. `NetworkRetryTest` verifies the policy
directly (retries and eventually succeeds, gives up after exhausting attempts, never retries a
real `ClientRequestException` produced by a mocked 400 response) — all using 1ms delays and
`runTest`'s virtual time, so the suite doesn't actually wait around for backoff.

## Persistence

The full currency list and the most recently fetched rate per (from, to) pair are cached locally
via [SQLDelight](https://sqldelight.github.io/sqldelight/) (`AndroidSqliteDriver` on Android,
`NativeSqliteDriver` on iOS — same generated schema/queries either way).

For rates specifically, `CurrencyRepository.rateFor()` matches the backend's own caching
philosophy (`CachedExchangeRate` / `CachedCurrencyRates`, keyed by fetch day): if the cached rate
for a pair is already from today, it's returned straight from the local database with no network
call at all; otherwise a fresh fetch runs and updates the cache. If that fetch fails, it falls back
to whatever's cached — even if stale — rather than showing an error when there's a perfectly usable
(if outdated) number already on hand.

`CurrencyRepositoryTest` (in `androidUnitTest`, not `commonTest`) verifies this against a real
in-memory SQLite database via SQLDelight's JDBC driver — Android's local unit tests run on a plain
JVM with no real Android SQLite implementation available (that needs Robolectric or an actual
device/emulator), so the JDBC driver stands in for `AndroidSqliteDriver` in tests; it's the same
generated schema and queries either way. Two tests in particular prove the point of this feature:
`usesTodaysCachedRateWithoutTouchingTheNetwork` (a mocked API that would fail the test if called at
all) and `fallsBackToAStaleCachedRateWhenAFreshFetchFails`.

The currency list follows a simpler rule via `CurrencyRepository.currencies()`: if anything is
cached at all, it's used with no network call; the API is only hit when the local cache is
completely empty (effectively: first launch, or after clearing the app's storage). Unlike rates,
there's no day-based staleness check — once cached, the list is never refreshed again on its own.

## Design

Matches the design system from the web app (`currency-calculator`'s `frontend/src/styles.css` /
bscharbau.com): the same `--paper`/`--ink`/`--signal`/`--muted`/`--line`/`--tint` color tokens
(see `BrandColors` in `Theme.kt`), Space Grotesk for headlines, IBM Plex Sans for body text, and
IBM Plex Mono for numeric values (amount, result, rate) — mirroring the web app's `.mono` usage
for figures. Fonts are bundled as static TTFs under `composeApp/src/commonMain/composeResources/
font/` (sourced from the IBM/Google Fonts and Space Grotesk upstream repos; OFL license texts in
`licenses/`), since only static per-weight files are reliably supported for text styling across
both Android and iOS in Compose Multiplatform.

The app icon combines two of the app's own motifs into one glyph: the "signal divider" zigzag from
the web design system (see `SignalDivider.kt` and `.signal-divider` in the web app's
`frontend/src/styles.css`), with its two ends terminating in arrowheads instead of rounded caps —
the same exchange-arrows idea as the in-app swap button — rather than placing both motifs side by
side, which wouldn't stay legible at launcher-icon sizes. On the brand's signal-teal background.
Generated programmatically by `graphics/gen_icons.py` (Pillow) rather than hand-drawn, covering
both the legacy per-density `ic_launcher`/`ic_launcher_round` PNGs and the API 26+ adaptive icon
foreground/background layers under `composeApp/src/androidMain/res/mipmap-*`. Re-run that script
after changing the design; `graphics/icon-512.png` is a plain reference render, not used by the
app build itself.

### Layout

`App()` measures the available space with `BoxWithConstraints` and switches between two
arrangements based on whether the window is wider than it is tall — landscape on a phone,
practically (a plain width-vs-height comparison, not an Android-specific orientation check, so it
works the same way on iOS):

- **Portrait**: hero text, then a bordered panel with `CurrencySelection` (FROM/TO/swap) stacked
  above `ConversionFields` (amount/result/rate) — one column, top to bottom.
- **Landscape**: no panel border — just a `VerticalDivider` between hero text +
  `CurrencySelection` on the left and `ConversionFields` alone on the right. `Row(Modifier.
  height(IntrinsicSize.Min))` is what makes the divider stretch to match the taller column's
  actual content height rather than the full screen height.

The switch exists because the portrait arrangement's content (especially the RATE row at the
bottom) doesn't fit within a phone's much shorter landscape height with everything stacked in one
column; splitting into two columns needs less vertical space per column instead. Verified on an
API 35 emulator in both orientations (not just reasoned about) — see below.

### Splash screen

The Android launch screen (shown before Compose's first frame renders) is styled via `AppTheme` in
`composeApp/src/androidMain/res/values/themes.xml`: `android:windowBackground` (the app's general
content background) is `@color/paper`, while `android:windowSplashScreenBackground` (the Android
12+/API 31+ splash screen specifically) is `@color/signal` — the same brand teal the launcher icon
sits on, so the launch screen reads as a deliberate brand moment rather than a blank content frame.
Both colors (`composeApp/src/androidMain/res/values/colors.xml`) are kept in sync by hand with
`BrandColors.paper`/`.signal` in `Theme.kt`. Without an explicit theme, both default to plain
system white; `AndroidManifest.xml`'s `<application>` tag points at `@style/AppTheme` instead of a
raw platform theme so this takes effect.

## Building

Requires JDK 17+ and an Android SDK (compileSdk 35). Point `local.properties` at your SDK
(`sdk.dir=/path/to/sdk`), then:

```sh
./gradlew :composeApp:assembleDebug       # builds the Android debug APK
./gradlew :composeApp:testDebugUnitTest   # runs the shared unit tests
```

### Running on an emulator

Needs KVM (`/dev/kvm`), and your user in the `kvm` group (`sudo usermod -aG kvm "$USER"`, then a
fresh login — or use `sg kvm -c "<command>"` to apply it to one command without logging out):

```sh
export ANDROID_HOME=/path/to/sdk
"$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" \
  "platform-tools" "emulator" "system-images;android-35;google_apis;x86_64"
"$ANDROID_HOME/cmdline-tools/latest/bin/avdmanager" create avd \
  --name test_phone --package "system-images;android-35;google_apis;x86_64" --device "pixel_6"
sg kvm -c "$ANDROID_HOME/emulator/emulator -avd test_phone -no-window -no-audio -gpu swiftshader_indirect" &

adb install -r -t composeApp/build/intermediates/apk/debug/composeApp-debug.apk
adb shell am start -n com.bscharbau.currencymobile/.MainActivity
```

To force landscape without physically rotating anything (useful for a headless `-no-window`
emulator): `adb shell settings put system accelerometer_rotation 0 && adb shell settings put
system user_rotation 1` (`0` for back to portrait). Screenshot with `adb exec-out screencap -p >
screenshot.png`.

### iOS

The iOS target (`iosX64`, `iosArm64`, `iosSimulatorArm64`) is declared in
`composeApp/build.gradle.kts` and `MainViewController.kt` is real, buildable KMP/Compose code —
but Kotlin/Native can only compile iOS targets on macOS with Xcode installed, so this hasn't been
built or run (this project was developed on Linux). To run it on iOS you'd need to create an
Xcode project on a Mac that embeds the `ComposeApp` framework produced by
`./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64` (or the equivalent target for a real
device), calling `MainViewController()` from a SwiftUI/UIKit wrapper.

## Known limitations (first version)

- "Today" is the device's local date (`kotlinx-datetime`, `TimeZone.currentSystemDefault()`), not
  necessarily the reference date the rate itself is quoted as of — near a day boundary in a
  timezone far from the backend's, these could disagree.
- The rate cache never expires beyond that daily check, and never gets cleaned up — it just
  accumulates one row per (from, to) pair ever viewed.
- The currency list, once cached, is never refreshed again on its own — if the backend adds or
  removes a supported currency, the app won't see it until its local storage is cleared.
- Retries are fixed at 3 attempts / 300ms initial backoff, not configurable, and every failure mode
  short of a 4xx gets the same treatment — there's no differentiation between "worth retrying
  aggressively" (a timeout) and "probably won't help" (e.g. a malformed response body).
- Amount input has no client-side validation beyond `toDoubleOrNull()`; invalid text just shows
  "Enter a valid amount" rather than proper input filtering.
