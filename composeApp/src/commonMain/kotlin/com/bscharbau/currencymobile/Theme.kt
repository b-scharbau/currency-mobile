package com.bscharbau.currencymobile

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.bscharbau.currencymobile.resources.Res
import com.bscharbau.currencymobile.resources.ibm_plex_mono_medium
import com.bscharbau.currencymobile.resources.ibm_plex_mono_regular
import com.bscharbau.currencymobile.resources.ibm_plex_sans_medium
import com.bscharbau.currencymobile.resources.ibm_plex_sans_regular
import com.bscharbau.currencymobile.resources.ibm_plex_sans_semibold
import com.bscharbau.currencymobile.resources.space_grotesk_bold
import com.bscharbau.currencymobile.resources.space_grotesk_medium
import com.bscharbau.currencymobile.resources.space_grotesk_regular
import org.jetbrains.compose.resources.Font

data class BrandPalette(
    val paper: Color,
    val ink: Color,
    val signal: Color,
    val muted: Color,
    val line: Color,
    val tint: Color,
    val error: Color,
)

// Matching frontend/src/styles.css on the web app (currency-calculator / bscharbau.com).
val LightBrandPalette = BrandPalette(
    paper = Color(0xFFF7F6F3),
    ink = Color(0xFF14202B),
    signal = Color(0xFF2B6E68),
    muted = Color(0xFF6B7680),
    line = Color(0xFFD8D3C7),
    tint = Color(0xFFEDF3F1),
    error = Color(0xFFA8402A),
)

// The web app has no dark mode yet, so there's no upstream palette to mirror here — this is this
// app's own design, keeping the same hues as the light palette: paper/ink swapped, and signal/
// error brightened so they still meet contrast against the dark background.
val DarkBrandPalette = BrandPalette(
    paper = Color(0xFF14202B),
    ink = Color(0xFFF7F6F3),
    signal = Color(0xFF4FA89F),
    muted = Color(0xFF8E99A3),
    line = Color(0xFF2A3540),
    tint = Color(0xFF1B252E),
    error = Color(0xFFE2624B),
)

private val LocalBrandPalette = staticCompositionLocalOf { LightBrandPalette }

// Preserves the `BrandColors.xxx` call-site syntax used throughout the app — each property
// resolves to whichever palette CurrencyMobileTheme provided for the current system theme, so
// existing call sites need no changes to become theme-aware.
object BrandColors {
    val paper: Color @Composable get() = LocalBrandPalette.current.paper
    val ink: Color @Composable get() = LocalBrandPalette.current.ink
    val signal: Color @Composable get() = LocalBrandPalette.current.signal
    val muted: Color @Composable get() = LocalBrandPalette.current.muted
    val line: Color @Composable get() = LocalBrandPalette.current.line
    val tint: Color @Composable get() = LocalBrandPalette.current.tint
    val error: Color @Composable get() = LocalBrandPalette.current.error
}

private fun brandColorScheme(palette: BrandPalette, darkTheme: Boolean): ColorScheme = if (darkTheme) {
    darkColorScheme(
        primary = palette.signal,
        onPrimary = palette.paper,
        background = palette.paper,
        onBackground = palette.ink,
        surface = palette.paper,
        onSurface = palette.ink,
        surfaceVariant = palette.tint,
        onSurfaceVariant = palette.muted,
        outline = palette.line,
        error = palette.error,
    )
} else {
    lightColorScheme(
        primary = palette.signal,
        onPrimary = palette.paper,
        background = palette.paper,
        onBackground = palette.ink,
        surface = palette.paper,
        onSurface = palette.ink,
        surfaceVariant = palette.tint,
        onSurfaceVariant = palette.muted,
        outline = palette.line,
        error = palette.error,
    )
}

@Composable
fun spaceGroteskFamily() = FontFamily(
    Font(Res.font.space_grotesk_regular, FontWeight.Normal),
    Font(Res.font.space_grotesk_medium, FontWeight.Medium),
    Font(Res.font.space_grotesk_bold, FontWeight.Bold),
)

@Composable
fun ibmPlexSansFamily() = FontFamily(
    Font(Res.font.ibm_plex_sans_regular, FontWeight.Normal),
    Font(Res.font.ibm_plex_sans_medium, FontWeight.Medium),
    Font(Res.font.ibm_plex_sans_semibold, FontWeight.SemiBold),
)

@Composable
fun ibmPlexMonoFamily() = FontFamily(
    Font(Res.font.ibm_plex_mono_regular, FontWeight.Normal),
    Font(Res.font.ibm_plex_mono_medium, FontWeight.Medium),
)

@Composable
fun brandTypography(): Typography {
    val display = spaceGroteskFamily()
    val body = ibmPlexSansFamily()
    return Typography(
        headlineSmall = TextStyle(fontFamily = display, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 34.sp),
        titleLarge = TextStyle(fontFamily = display, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp),
        titleMedium = TextStyle(fontFamily = body, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 24.sp),
        bodyLarge = TextStyle(fontFamily = body, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
        bodyMedium = TextStyle(fontFamily = body, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
        labelLarge = TextStyle(fontFamily = body, fontWeight = FontWeight.Medium, fontSize = 15.sp, lineHeight = 20.sp),
        labelMedium = TextStyle(fontFamily = body, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.8.sp),
    )
}

@Composable
fun CurrencyMobileTheme(content: @Composable () -> Unit) {
    val darkTheme = isSystemInDarkTheme()
    val palette = if (darkTheme) DarkBrandPalette else LightBrandPalette
    CompositionLocalProvider(LocalBrandPalette provides palette) {
        MaterialTheme(
            colorScheme = brandColorScheme(palette, darkTheme),
            typography = brandTypography(),
            content = content,
        )
    }
}
