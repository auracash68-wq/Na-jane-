package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppColors
import com.example.ui.theme.AppDimens

@Composable
fun HomeScreen(
  onNavigateToPhoto: () -> Unit,
  onNavigateToVideo: () -> Unit,
  onNavigateToAudio: () -> Unit,
  onNavigateToBatch: () -> Unit,
  onOpenHistory: () -> Unit,
  onOpenSettings: () -> Unit,
  onNavigateToSplash: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(AppColors.DarkScreenBackgroundBrush)
      .statusBarsPadding()
      .navigationBarsPadding()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = AppDimens.ScreenHorizontalPadding)
      .testTag("home_screen_root")
  ) {
    Spacer(modifier = Modifier.height(6.dp))

    // Top row: Hamburger menu on the left, Small gold Pro badge on the right
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = onNavigateToSplash,
        modifier = Modifier
          .size(40.dp)
          .testTag("home_menu_button")
      ) {
        Icon(
          imageVector = Icons.Filled.Menu,
          contentDescription = "Menu",
          tint = AppColors.PrimaryWhite,
          modifier = Modifier.size(24.dp)
        )
      }

      // Gold Pro badge
      Row(
        modifier = Modifier
          .clip(RoundedCornerShape(16.dp))
          .background(AppColors.ProBadgeBg)
          .border(1.dp, AppColors.ProBadgeBorder, RoundedCornerShape(16.dp))
          .clickable { onOpenSettings() }
          .padding(horizontal = 11.dp, vertical = 5.dp)
          .testTag("home_pro_badge"),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Filled.WorkspacePremium,
          contentDescription = "Pro",
          tint = AppColors.ProBadgeGold,
          modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "Pro",
          color = AppColors.ProBadgeGold,
          fontSize = 11.5.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Title: Media Compressor
    Text(
      text = "Media Compressor",
      color = AppColors.PrimaryWhite,
      fontSize = 22.sp,
      fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(3.dp))

    // Subtitle: Professional Media Compression Tool
    Text(
      text = "Professional Media Compression Tool",
      color = AppColors.SecondaryText,
      fontSize = 12.sp,
      fontWeight = FontWeight.Normal
    )

    Spacer(modifier = Modifier.height(14.dp))

    // CARD 1 — BLUE: Photo Compressor
    MainCompressorFeatureCard(
      title = "Photo\nCompressor",
      subtitle = "Compress Images\n(JPG, PNG, WebP, AVIF)",
      brush = AppColors.PhotoCardBrush,
      iconContent = {
        Box(
          modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(2.5.dp, Color.White, RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.12f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Filled.Image,
            contentDescription = "Photo Compressor",
            tint = Color.White,
            modifier = Modifier.size(30.dp)
          )
        }
      },
      onClick = onNavigateToPhoto,
      testTag = "card_photo_compressor"
    )

    Spacer(modifier = Modifier.height(12.dp))

    // CARD 2 — GREEN: Video Compressor
    MainCompressorFeatureCard(
      title = "Video\nCompressor",
      subtitle = "Reduce Video Size\n(MP4, WebM, etc.)",
      brush = AppColors.VideoCardBrush,
      iconContent = {
        Box(
          modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = "Video Compressor",
            tint = AppColors.VideoCardEnd,
            modifier = Modifier.size(30.dp)
          )
        }
      },
      onClick = onNavigateToVideo,
      testTag = "card_video_compressor"
    )

    Spacer(modifier = Modifier.height(12.dp))

    // CARD 3 — ORANGE: Audio Compressor
    MainCompressorFeatureCard(
      title = "Audio\nCompressor",
      subtitle = "Optimize Audio\n(MP3, AAC, Opus, FLAC)",
      brush = AppColors.AudioCardBrush,
      iconContent = {
        Box(
          modifier = Modifier.size(48.dp),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Filled.MusicNote,
            contentDescription = "Audio Compressor",
            tint = Color.White,
            modifier = Modifier.size(44.dp)
          )
        }
      },
      onClick = onNavigateToAudio,
      testTag = "card_audio_compressor"
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Quick Tools Section
    Text(
      text = "Quick Tools",
      color = AppColors.PrimaryWhite,
      fontSize = 14.sp,
      fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(10.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      QuickToolSquareCard(
        label = "Batch",
        icon = Icons.Filled.Inventory2,
        iconTint = Color(0xFF9E7BFF),
        onClick = onNavigateToBatch,
        modifier = Modifier.weight(1f),
        testTag = "quick_tool_batch"
      )
      QuickToolSquareCard(
        label = "History",
        icon = Icons.Filled.History,
        iconTint = Color(0xFF63A1FF),
        onClick = onOpenHistory,
        modifier = Modifier.weight(1f),
        testTag = "quick_tool_history"
      )
      QuickToolSquareCard(
        label = "Settings",
        icon = Icons.Filled.Settings,
        iconTint = Color(0xFFA58BFF),
        onClick = onOpenSettings,
        modifier = Modifier.weight(1f),
        testTag = "quick_tool_settings"
      )
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Bottom promotional card: Smart Compression - Save up to 80% space!
    val smartShape = RoundedCornerShape(26.dp)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(58.dp)
        .clip(smartShape)
        .background(
          brush = Brush.horizontalGradient(
            colors = listOf(
              AppColors.SmartBannerStart,
              AppColors.SmartBannerMid,
              AppColors.SmartBannerEnd
            )
          )
        )
        .border(1.2.dp, AppColors.SmartBannerBorder, smartShape)
        .clickable(onClick = onNavigateToPhoto)
        .padding(horizontal = 16.dp)
        .testTag("smart_compression_card"),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Filled.Eco,
        contentDescription = "Smart Compression",
        tint = AppColors.Green,
        modifier = Modifier.size(28.dp)
      )
      Spacer(modifier = Modifier.width(12.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "Smart Compression",
          color = Color.White,
          fontSize = 13.5.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Save up to 80% space!",
          color = Color(0xFFD0DCF5),
          fontSize = 11.5.sp,
          fontWeight = FontWeight.Normal
        )
      }
      Box(
        modifier = Modifier
          .size(30.dp)
          .clip(CircleShape)
          .background(Color(0xFF282B78))
          .border(1.dp, Color(0xFF4F5BD5), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowForward,
          contentDescription = "Go",
          tint = Color(0xFFB8C7FF),
          modifier = Modifier.size(16.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}

@Composable
private fun MainCompressorFeatureCard(
  title: String,
  subtitle: String,
  brush: Brush,
  iconContent: @Composable () -> Unit,
  onClick: () -> Unit,
  testTag: String
) {
  val shape = RoundedCornerShape(18.dp)
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(104.dp)
      .clip(shape)
      .background(brush)
      .clickable(onClick = onClick)
      .padding(horizontal = 18.dp, vertical = 14.dp)
      .testTag(testTag),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier.width(56.dp),
      contentAlignment = Alignment.CenterStart
    ) {
      iconContent()
    }

    Spacer(modifier = Modifier.width(12.dp))

    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.Center
    ) {
      Text(
        text = title,
        color = Color.White,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 19.sp
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = subtitle,
        color = Color.White.copy(alpha = 0.92f),
        fontSize = 11.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 14.sp
      )
    }

    Icon(
      imageVector = Icons.AutoMirrored.Filled.ArrowForward,
      contentDescription = "Open",
      tint = Color.White,
      modifier = Modifier.size(20.dp)
    )
  }
}

@Composable
private fun QuickToolSquareCard(
  label: String,
  icon: ImageVector,
  iconTint: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  testTag: String
) {
  val shape = RoundedCornerShape(14.dp)
  Column(
    modifier = modifier
      .height(74.dp)
      .clip(shape)
      .background(AppColors.QuickToolCardBg)
      .border(1.dp, AppColors.QuickToolCardBorder, shape)
      .clickable(onClick = onClick)
      .padding(vertical = 10.dp, horizontal = 8.dp)
      .testTag(testTag),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Icon(
      imageVector = icon,
      contentDescription = label,
      tint = iconTint,
      modifier = Modifier.size(24.dp)
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = label,
      color = Color(0xFFE1E9F8),
      fontSize = 11.5.sp,
      fontWeight = FontWeight.Medium
    )
  }
}
