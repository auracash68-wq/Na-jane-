package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.MediaCompressionEngine
import com.example.ui.components.CompressorTopToolbar
import com.example.ui.components.CustomDropdownField
import com.example.ui.components.PrimaryGradientButton
import com.example.ui.theme.AppColors
import kotlinx.coroutines.delay

@Composable
fun VideoCompressionScreen(
  videoOriginalBytes: Long,
  videoEstimatedBytes: Long,
  savedPercent: Int,
  resolution: String,
  codec: String,
  bitrate: String,
  fps: String,
  audioQuality: String,
  durationSec: Int,
  currentPosSec: Int,
  isPlaying: Boolean,
  onTogglePlay: () -> Unit,
  onSeekTo: (Int) -> Unit,
  onVideoPicked: (Uri) -> Unit,
  onResolutionChange: (String) -> Unit,
  onCodecChange: (String) -> Unit,
  onBitrateChange: (String) -> Unit,
  onFpsChange: (String) -> Unit,
  onAudioQualityChange: (String) -> Unit,
  onCompressVideo: () -> Unit,
  onBackClick: () -> Unit,
  onSettingsClick: () -> Unit
) {
  val videoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    if (uri != null) {
      onVideoPicked(uri)
    }
  }

  LaunchedEffect(isPlaying, currentPosSec, durationSec) {
    if (isPlaying) {
      delay(1000L)
      val next = if (currentPosSec + 1 >= durationSec) 0 else currentPosSec + 1
      onSeekTo(next)
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(AppColors.DarkScreenBackgroundBrush)
      .statusBarsPadding()
      .navigationBarsPadding()
      .testTag("video_compression_screen_root")
  ) {
    CompressorTopToolbar(
      title = "Video Compression",
      onBackClick = onBackClick,
      onSettingsClick = onSettingsClick,
      backgroundColor = Color(0xFF061022)
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
    ) {
      // Large landscape video preview
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(205.dp)
          .clickable {
            videoPickerLauncher.launch(
              PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
            )
          }
          .testTag("video_preview_box")
      ) {
        Image(
          painter = painterResource(id = R.drawable.sample_video_coast),
          contentDescription = "Video Thumbnail",
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )

        // Bottom dark gradient overlay for timeline & controls
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .align(Alignment.BottomCenter)
            .background(
              Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color(0xCC040B1A))
              )
            )
        )

        // Central Play Button
        Box(
          modifier = Modifier
            .size(58.dp)
            .align(Alignment.Center)
            .clip(CircleShape)
            .background(Color(0xAA061020))
            .border(1.5.dp, Color.White.copy(alpha = 0.7f), CircleShape)
            .clickable { onTogglePlay() }
            .testTag("video_center_play_button"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
            contentDescription = if (isPlaying) "Pause" else "Play",
            tint = Color.White,
            modifier = Modifier.size(32.dp)
          )
        }

        // Bottom playback bar: Play icon, Duration: 00:00 / 00:30, Fullscreen control
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(28.dp)
                  .clip(CircleShape)
                  .background(Color(0x99122444))
                  .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                  .clickable { onTogglePlay() },
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                  contentDescription = "Play toggle",
                  tint = Color.White,
                  modifier = Modifier.size(16.dp)
                )
              }
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "${MediaCompressionEngine.formatDurationSeconds(currentPosSec)} / ${MediaCompressionEngine.formatDurationSeconds(durationSec)}",
                color = Color.White,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium
              )
            }

            Icon(
              imageVector = Icons.Filled.Fullscreen,
              contentDescription = "Fullscreen",
              tint = Color.White,
              modifier = Modifier
                .size(22.dp)
                .clickable {
                  videoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                  )
                }
            )
          }

          Spacer(modifier = Modifier.height(6.dp))

          // Playback timeline bar
          val progressFraction = if (durationSec > 0) {
            (currentPosSec.toFloat() / durationSec.toFloat()).coerceIn(0.08f, 1f)
          } else {
            0.15f
          }
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(3.dp)
              .clip(RoundedCornerShape(2.dp))
              .background(Color.White.copy(alpha = 0.28f))
          ) {
            Box(
              modifier = Modifier
                .fillMaxWidth(progressFraction)
                .height(3.dp)
                .background(AppColors.BrightBlue)
            )
          }
        }
      }

      // Settings rows with dropdown controls
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        VideoSettingDropdownRow(
          label = "Resolution",
          selectedValue = resolution,
          options = listOf("1080p (Full HD)", "720p (HD)", "480p (SD)", "4K (UHD)"),
          onValueChange = onResolutionChange,
          testTag = "video_resolution_dropdown"
        )

        VideoSettingDropdownRow(
          label = "Codec",
          selectedValue = codec,
          options = listOf("H.264 (Recommended)", "H.265 (HEVC)", "VP9", "AV1"),
          onValueChange = onCodecChange,
          testTag = "video_codec_dropdown"
        )

        VideoSettingDropdownRow(
          label = "Bitrate",
          selectedValue = bitrate,
          options = listOf("Auto (Balanced)", "Low (Smallest)", "High (Best Quality)"),
          onValueChange = onBitrateChange,
          showSignalBarsIcon = true,
          testTag = "video_bitrate_dropdown"
        )

        VideoSettingDropdownRow(
          label = "FPS",
          selectedValue = fps,
          options = listOf("Auto", "60 FPS", "30 FPS", "24 FPS"),
          onValueChange = onFpsChange,
          testTag = "video_fps_dropdown"
        )

        VideoSettingDropdownRow(
          label = "Audio Quality",
          selectedValue = audioQuality,
          options = listOf("High", "Medium", "Low"),
          onValueChange = onAudioQualityChange,
          testTag = "video_audio_quality_dropdown"
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Information card: Original Size: 85.6 MB / Estimated Size: 28.3 MB (≈67%↓)
        val infoCardShape = RoundedCornerShape(14.dp)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(infoCardShape)
            .background(
              Brush.horizontalGradient(
                colors = listOf(AppColors.VideoInfoCardStart, AppColors.VideoInfoCardEnd)
              )
            )
            .border(1.dp, AppColors.VideoInfoCardBorder, infoCardShape)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag("video_info_card"),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = "Size Estimate Info",
            tint = Color(0xFFBBA4FF),
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "Original Size: ${MediaCompressionEngine.formatFileSize(videoOriginalBytes)}",
              color = Color.White,
              fontSize = 12.5.sp,
              fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "Estimated Size: ${MediaCompressionEngine.formatFileSize(videoEstimatedBytes)} (≈${savedPercent}%↓)",
              color = Color(0xFFE5DCFF),
              fontSize = 12.5.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Bottom button: ▶ Compress Video
        PrimaryGradientButton(
          text = "Compress Video",
          icon = Icons.Filled.PlayArrow,
          brush = AppColors.BlueActionButtonBrush,
          onClick = onCompressVideo,
          testTag = "video_compress_button"
        )
      }
    }
  }
}

@Composable
private fun VideoSettingDropdownRow(
  label: String,
  selectedValue: String,
  options: List<String>,
  onValueChange: (String) -> Unit,
  showSignalBarsIcon: Boolean = false,
  testTag: String
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(
      text = label,
      color = Color.White,
      fontSize = 13.sp,
      fontWeight = FontWeight.SemiBold,
      modifier = Modifier.weight(0.42f)
    )
    CustomDropdownField(
      selectedValue = selectedValue,
      options = options,
      onValueChange = onValueChange,
      isLightSurface = false,
      showSignalBarsIcon = showSignalBarsIcon,
      modifier = Modifier.weight(0.58f),
      testTag = testTag
    )
  }
}
