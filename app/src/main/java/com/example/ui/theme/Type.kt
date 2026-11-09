package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object AppDimens {
  val ScreenHorizontalPadding = 16.dp
  val ToolbarHeight = 52.dp
  val CardGap = 12.dp
  val SmallGap = 8.dp
  val LargeCardRadius = 18.dp
  val MediumCardRadius = 14.dp
  val SmallCardRadius = 10.dp
  val PillRadius = 24.dp
  val ActionButtonHeight = 50.dp
  val ActionButtonRadius = 25.dp
  val DropdownHeight = 40.dp
  val ModeChipHeight = 36.dp
}

object AppTypography {
  val SplashTitle = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Bold,
    fontSize = 30.sp,
    lineHeight = 34.sp,
    color = AppColors.PrimaryWhite
  )

  val ScreenTitle = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Bold,
    fontSize = 21.sp,
    lineHeight = 26.sp,
    color = AppColors.PrimaryWhite
  )

  val ToolbarTitle = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.SemiBold,
    fontSize = 18.sp,
    lineHeight = 22.sp,
    color = AppColors.PrimaryWhite
  )

  val CardTitle = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Bold,
    fontSize = 16.sp,
    lineHeight = 20.sp,
    color = AppColors.PrimaryWhite
  )

  val SectionHeaderLight = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Bold,
    fontSize = 13.5.sp,
    lineHeight = 18.sp,
    color = AppColors.DarkNavyHeading
  )

  val SectionHeaderDark = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.SemiBold,
    fontSize = 13.5.sp,
    lineHeight = 18.sp,
    color = AppColors.PrimaryWhite
  )

  val BodyText = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    color = AppColors.SecondaryText
  )

  val LabelText = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    color = AppColors.PrimaryWhite
  )

  val SupportingText = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Normal,
    fontSize = 10.5.sp,
    lineHeight = 14.sp,
    color = AppColors.SecondaryText
  )

  val ActionButtonText = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.SemiBold,
    fontSize = 15.sp,
    lineHeight = 20.sp,
    color = AppColors.PrimaryWhite
  )
}

val Typography = Typography(
  bodyLarge = AppTypography.BodyText,
  titleLarge = AppTypography.ScreenTitle,
  labelSmall = AppTypography.SupportingText
)
