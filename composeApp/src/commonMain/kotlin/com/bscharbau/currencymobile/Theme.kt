package com.bscharbau.currencymobile

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
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

// Brand tokens, matching frontend/src/styles.css on the web app (currency-calculator / bscharbau.com).
object BrandColors {
    val paper = Color(0xFFF7F6F3)
    val ink = Color(0xFF14202B)
    val signal = Color(0xFF2B6E68)
    val muted = Color(0xFF6B7680)
    val line = Color(0xFFD8D3C7)
    val tint = Color(0xFFEDF3F1)
    val error = Color(0xFFA8402A)
}

@Composable
fun brandColorScheme() = lightColorScheme(
    primary = BrandColors.signal,
    onPrimary = BrandColors.paper,
    background = BrandColors.paper,
    onBackground = BrandColors.ink,
    surface = BrandColors.paper,
    onSurface = BrandColors.ink,
    surfaceVariant = BrandColors.tint,
    onSurfaceVariant = BrandColors.muted,
    outline = BrandColors.line,
    error = BrandColors.error,
)

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
    MaterialTheme(
        colorScheme = brandColorScheme(),
        typography = brandTypography(),
        content = content,
    )
}
