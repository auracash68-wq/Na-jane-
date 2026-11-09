package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.FlowingMulticolorWaves
import com.example.ui.components.SplashTriangleBoltLogo
import com.example.ui.theme.AppColors
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
  autoNavigate: Boolean,
  onNavigateToHome: () -> Unit,
  onNavigateToPhoto: () -> Unit,
  onNavigateToVideo: () -> Unit,
  onNavigateToAudio: () -> Unit
) {
  LaunchedEffect(autoNavigate) {
    if (autoNavigate) {
      delay(2200L)
      onNavigateToHome()
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        brush = Brush.verticalGradient(
          colors = listOf(
            Color(0xFF050E21),
            Color(0xFF0A1E4E),
            Color(0xFF0C2257),
            Color(0xFF071224)
          )
        )
      )
      .clickable { onNavigateToHome() }
      .testTag("splash_screen_root")
  ) {
    // Flowing multicolor wave graphics behind the lower-middle section
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(175.dp)
        .align(Alignment.BottomCenter)
        .padding(bottom = 165.dp)
    ) {
      Image(
        painter = painterResource(id = R.drawable.splash_wave_art),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = Modifier
          .fillMaxSize()
          .alpha(0.85f)
      )
      FlowingMulticolorWaves(
        modifier = Modifier.fillMaxSize()
      )
    }

    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .padding(horizontal = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.weight(0.14f))

      // Large centered colorful triangular Media Compressor logo with lightning symbol
      SplashTriangleBoltLogo(
        size = 138.dp,
        modifier = Modifier.testTag("splash_logo")
      )

      Spacer(modifier = Modifier.height(18.dp))

      // App Title: Media Compressor
      Text(
        text = "Media\nCompressor",
        color = AppColors.PrimaryWhite,
        fontSize = 31.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 36.sp,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Subtitle: Compress • Save • Share with Perfect Quality
      Text(
        text = "Compress • Save • Share\nwith Perfect Quality",
        color = Color(0xFFD5E1F7),
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.weight(0.32f))

      // Three small shortcut cards at the bottom: Photo, Video, Audio
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
      ) {
        SplashShortcutCard(
          label = "Photo",
          icon = Icons.Filled.Image,
          iconBoxBrush = Brush.linearGradient(
            colors = listOf(Color(0xFF9C42FF), Color(0xFF6B20E8))
          ),
          borderGlow = Color(0xFF2858B8),
          onClick = onNavigateToPhoto,
          modifier = Modifier.weight(1f),
          testTag = "splash_photo_card"
        )
        SplashShortcutCard(
          label = "Video",
          icon = Icons.Filled.PlayArrow,
          iconBoxBrush = Brush.linearGradient(
            colors = listOf(Color(0xFFFF9E22), Color(0xFFFF6A00))
          ),
          borderGlow = Color(0xFF2858B8),
          onClick = onNavigateToVideo,
          modifier = Modifier.weight(1f),
          testTag = "splash_video_card"
        )
        SplashShortcutCard(
          label = "Audio",
          icon = Icons.Filled.MusicNote,
          iconBoxBrush = Brush.linearGradient(
            colors = listOf(Color(0xFF15E67E), Color(0xFF05B85C))
          ),
          borderGlow = Color(0xFF2858B8),
          onClick = onNavigateToAudio,
          modifier = Modifier.weight(1f),
          testTag = "splash_audio_card"
        )
      }

      Spacer(modifier = Modifier.height(26.dp))

      // Bottom text: Fast • Powerful • Offline
      Text(
        text = "Fast  •  Powerful  •  Offline",
        color = Color(0xFFB8C7E0),
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(6.dp))

      // Version text: v1.0.0
      Text(
        text = "v1.0.0",
        color = AppColors.SecondaryText,
        fontSize = 11.sp,
        fontWeight = FontWeight.Normal,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
private fun SplashShortcutCard(
  label: String,
  icon: ImageVector,
  iconBoxBrush: Brush,
  borderGlow: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  testTag: String
) {
  val shape = RoundedCornerShape(18.dp)
  Column(
    modifier = modifier
      .height(94.dp)
      .clip(shape)
      .background(
        brush = Brush.verticalGradient(
          colors = listOf(
            Color(0xCC0D2352),
            Color(0xE6081636)
          )
        )
      )
      .border(1.2.dp, borderGlow, shape)
      .clickable(onClick = onClick)
      .padding(vertical = 12.dp, horizontal = 8.dp)
      .testTag(testTag),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Box(
      modifier = Modifier
        .size(34.dp)
        .clip(RoundedCornerShape(9.dp))
        .background(iconBoxBrush),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = Color.White,
        modifier = Modifier.size(20.dp)
      )
    }
    Spacer(modifier = Modifier.height(10.dp))
    Text(
      text = label,
      color = Color.White,
      fontSize = 13.sp,
      fontWeight = FontWeight.Medium
    )
  }
}
