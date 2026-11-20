package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = PaddyGreenPrimary,
    onPrimary = PaddyGreenOnPrimary,
    primaryContainer = PaddyGreenContainer,
    onPrimaryContainer = PaddyGreenOnContainer,
    secondary = HarvestGoldSecondary,
    onSecondary = HarvestGoldOnSecondary,
    secondaryContainer = HarvestGoldContainer,
    onSecondaryContainer = HarvestGoldOnContainer,
    tertiary = IrrigationTealTertiary,
    onTertiary = IrrigationTealOnTertiary,
    tertiaryContainer = IrrigationTealContainer,
    onTertiaryContainer = IrrigationTealOnTertiaryContainer,
    background = HuskCreamBackground,
    onBackground = ForestSlateText,
    surface = PaddyFieldSurface,
    onSurface = ForestSlateText,
    surfaceVariant = SageSurfaceVariant,
    onSurfaceVariant = MutedLeafText,
    outline = SoftBorderOutline
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPaddyPrimary,
    onPrimary = DarkPaddyOnPrimary,
    primaryContainer = DarkPaddyContainer,
    onPrimaryContainer = DarkPaddyOnContainer,
    secondary = DarkGoldSecondary,
    onSecondary = DarkGoldOnSecondary,
    secondaryContainer = DarkGoldContainer,
    onSecondaryContainer = DarkGoldOnContainer,
    tertiary = DarkTealTertiary,
    tertiaryContainer = DarkTealContainer,
    onTertiaryContainer = DarkTealOnContainer,
    background = DarkFoliageBackground,
    onBackground = DarkTextPrimary,
    surface = DarkFoliageSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkFoliageSurfaceVariant,
    onSurfaceVariant = DarkTextMuted,
    outline = DarkBorderOutline
)

val RiceShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun RiceFarmingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = RiceTypography,
        shapes = RiceShapes,
        content = content
    )
}
