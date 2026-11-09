package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object AppColors {
  // Primary reference palette
  val MainDarkBackground = Color(0xFF071224)
  val SecondaryDarkBlue = Color(0xFF0C1F54)
  val PrimaryBlue = Color(0xFF0663F2)
  val BrightBlue = Color(0xFF1190F8)
  val LightSurface = Color(0xFFDEE8FA)
  val LightBlue = Color(0xFFAFCFF6)
  val Green = Color(0xFF05CD6A)
  val DarkGreen = Color(0xFF116B39)
  val Orange = Color(0xFFFA9615)
  val Purple = Color(0xFF6B20E8)
  val SecondaryText = Color(0xFF9598AD)
  val PrimaryWhite = Color(0xFFFFFFFF)

  // Refined component colors sampled from the 8 reference panels
  val DeepNavyTop = Color(0xFF050E1F)
  val DeepNavyCenter = Color(0xFF091B42)
  val DeepNavyBottom = Color(0xFF061126)

  val LightPanelBackground = Color(0xFFE4EEFC)
  val LightCardSurface = Color(0xFFF3F7FE)
  val LightChipUnselected = Color(0xFFD5E3F8)
  val LightDivider = Color(0xFFCBD9F0)
  val DarkNavyHeading = Color(0xFF0E1B33)
  val DarkNavySubtext = Color(0xFF4F5E7B)

  // Home cards
  val PhotoCardStart = Color(0xFF0568F5)
  val PhotoCardEnd = Color(0xFF1597FF)

  val VideoCardStart = Color(0xFF22D47B)
  val VideoCardEnd = Color(0xFF06B85D)

  val AudioCardStart = Color(0xFFFF781F)
  val AudioCardEnd = Color(0xFFFA9B17)

  // Quick tools & Smart Compression
  val QuickToolCardBg = Color(0xFF121F3D)
  val QuickToolCardBorder = Color(0xFF1F325C)
  val QuickToolIconBg = Color(0xFF1B2C52)

  val SmartBannerStart = Color(0xFF0B3835)
  val SmartBannerMid = Color(0xFF102A52)
  val SmartBannerEnd = Color(0xFF282469)
  val SmartBannerBorder = Color(0xFF31489C)

  // Pro badge
  val ProBadgeBg = Color(0xFF3A3019)
  val ProBadgeBorder = Color(0xFF6B531E)
  val ProBadgeGold = Color(0xFFFFCC33)

  // Dark controls (Video & Audio screens)
  val DarkControlSurface = Color(0xFF14213D)
  val DarkControlBorder = Color(0xFF22355E)
  val DarkChipUnselected = Color(0xFF17233E)

  // Video Info Card
  val VideoInfoCardStart = Color(0xFF3E218B)
  val VideoInfoCardEnd = Color(0xFF4A25AA)
  val VideoInfoCardBorder = Color(0xFF6B42E0)

  // Audio Purple Accents
  val AudioPurpleGlow = Color(0xFF8B3DFF)
  val AudioPurpleButtonStart = Color(0xFF9B4DFF)
  val AudioPurpleButtonEnd = Color(0xFF6B20E8)
  val CyanAccent = Color(0xFF21D4FD)

  // Gradients
  val SplashBackgroundBrush = Brush.verticalGradient(
    colors = listOf(
      Color(0xFF050E21),
      Color(0xFF0B2154),
      Color(0xFF071224)
    )
  )

  val DarkScreenBackgroundBrush = Brush.verticalGradient(
    colors = listOf(
      Color(0xFF061022),
      Color(0xFF091731),
      Color(0xFF071224)
    )
  )

  val PhotoCardBrush = Brush.horizontalGradient(
    colors = listOf(PhotoCardStart, PhotoCardEnd)
  )

  val VideoCardBrush = Brush.horizontalGradient(
    colors = listOf(VideoCardStart, VideoCardEnd)
  )

  val AudioCardBrush = Brush.horizontalGradient(
    colors = listOf(AudioCardStart, AudioCardEnd)
  )

  val BlueActionButtonBrush = Brush.horizontalGradient(
    colors = listOf(Color(0xFF0663F2), Color(0xFF1589F7))
  )

  val PurpleActionButtonBrush = Brush.horizontalGradient(
    colors = listOf(AudioPurpleButtonStart, AudioPurpleButtonEnd)
  )

  val GreenShareButtonBrush = Brush.horizontalGradient(
    colors = listOf(Color(0xFF1AD968), Color(0xFF05C46B))
  )
}
