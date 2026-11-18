package com.hermesmobile.app.ui.theme

import android.app.Activity
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.Colors
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController
import top.yukonga.miuix.kmp.theme.ThemePaletteStyle

enum class Accent(val label: String) {
    Dynamic("Wallpaper"),
    MiuBlue("Biru MIUI"),
    Violet("Ungu"),
    Green("Hijau"),
    Orange("Oranye"),
    ;

    fun seedColor(): Color? = when (this) {
        Dynamic, MiuBlue -> null
        Violet -> Color(0xFF6750A4)
        Green -> Color(0xFF356A35)
        Orange -> Color(0xFF8B4F23)
    }

    companion object {
        fun fromId(id: String?): Accent = entries.firstOrNull { it.name == id } ?: Dynamic
    }
}

private val LiquidShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

private val LiquidTypography = Typography(
    headlineSmall = TextStyle(fontSize = 25.sp, lineHeight = 31.sp, fontWeight = FontWeight.Normal),
    titleLarge = TextStyle(fontSize = 21.sp, lineHeight = 27.sp, fontWeight = FontWeight.Medium),
    titleMedium = TextStyle(fontSize = 17.sp, lineHeight = 23.sp, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
    labelMedium = TextStyle(fontSize = 12.sp, lineHeight = 17.sp, fontWeight = FontWeight.Medium),
)

private fun Colors.toMaterial(dark: Boolean): ColorScheme {
    val base = if (dark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        tertiary = tertiaryContainer,
        onTertiary = onTertiaryContainer,
        tertiaryContainer = tertiaryContainer,
        onTertiaryContainer = onTertiaryContainer,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariantSummary,
        surfaceTint = primary,
        error = error,
        onError = onError,
        errorContainer = errorContainer,
        onErrorContainer = onErrorContainer,
        outline = outline,
        outlineVariant = dividerLine,
        surfaceContainer = surfaceContainer,
        surfaceContainerHigh = surfaceContainerHigh,
        surfaceContainerHighest = surfaceContainerHighest,
    )
}

@Composable
fun LiquidTheme(
    accentColor: Accent = Accent.Dynamic,
    darkTheme: Boolean? = null,
    content: @Composable () -> Unit,
) {
    val dark = darkTheme ?: isSystemInDarkTheme()
    val context = LocalContext.current
    val mode = if (accentColor == Accent.MiuBlue) {
        if (dark) ColorSchemeMode.Dark else ColorSchemeMode.Light
    } else {
        if (dark) ColorSchemeMode.MonetDark else ColorSchemeMode.MonetLight
    }
    val keyColor = if (accentColor == Accent.Dynamic || accentColor == Accent.MiuBlue) null else accentColor.seedColor()
    val controller = androidx.compose.runtime.remember(accentColor, dark) {
        ThemeController(
            colorSchemeMode = mode,
            keyColor = keyColor,
            paletteStyle = ThemePaletteStyle.Neutral,
            isDark = dark,
        )
    }
    SideEffect {
        (context as? Activity)?.window?.let { window ->
            WindowCompat.getInsetsController(window, window.decorView).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }
    MiuixTheme(controller = controller) {
        val scheme = MiuixTheme.colorScheme
        MaterialTheme(
            colorScheme = scheme.toMaterial(dark),
            typography = LiquidTypography,
            shapes = LiquidShapes,
            content = content,
        )
    }
}

object LiquidMotion {
    fun <T> bouncy(): SpringSpec<T> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow,
    )

    fun <T> gentle(): SpringSpec<T> = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessVeryLow,
    )

    fun <T> snappy(): SpringSpec<T> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )

    fun <T> press(): SpringSpec<T> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium,
    )
}
