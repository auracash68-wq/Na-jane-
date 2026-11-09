package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val MediaCompressorColorScheme = darkColorScheme(
  primary = AppColors.PrimaryBlue,
  secondary = AppColors.BrightBlue,
  tertiary = AppColors.Green,
  background = AppColors.MainDarkBackground,
  surface = AppColors.SecondaryDarkBlue,
  onPrimary = AppColors.PrimaryWhite,
  onSecondary = AppColors.PrimaryWhite,
  onTertiary = AppColors.PrimaryWhite,
  onBackground = AppColors.PrimaryWhite,
  onSurface = AppColors.PrimaryWhite
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = MediaCompressorColorScheme,
    typography = Typography,
    content = content
  )
}
