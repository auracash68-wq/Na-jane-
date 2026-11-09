package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MediaCompressionEngine
import com.example.model.AudioFormat
import com.example.model.CompressionMode
import com.example.ui.components.CompressorTopToolbar
import com.example.ui.components.CustomDropdownField
import com.example.ui.components.PrimaryGradientButton
import com.example.ui.components.SelectableOptionChip
import com.example.ui.theme.AppColors
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun AudioCompressionScreen(
  audioOriginalBytes: Long,
  audioDurationSec: Int,
  selectedMode: CompressionMode,
  selectedFormat: AudioFormat,
  bitrateKbps: Int,
  sampleRateLabel: String,
  sampleRateDropdown: String,
  isPreviewPlaying: Boolean,
  onAudioPicked: (Uri) -> Unit,
  onTogglePreviewPlay: () -> Unit,
  onModeSelected: (CompressionMode) -> Unit,
  onFormatSelected: (AudioFormat) -> Unit,
  onBitrateChanged: (Int) -> Unit,
  onSampleRateDropdownChanged: (String) -> Unit,
  onCompressAudio: () -> Unit,
  onBackClick: () -> Unit,
  onSettingsClick: () -> Unit
) {
  val audioPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocument()
  ) { uri: Uri? ->
    if (uri != null) {
      onAudioPicked(uri)
    }
  }

  val infiniteTransition = rememberInfiniteTransition(label = "audio_wave")
  val wavePhase by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 6.283f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1600, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "phase"
  )

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(AppColors.DarkScreenBackgroundBrush)
      .statusBarsPadding()
      .navigationBarsPadding()
      .testTag("audio_compression_screen_root")
  ) {
    CompressorTopToolbar(
      title = "Audio Compression",
      onBackClick = onBackClick,
      onSettingsClick = onSettingsClick,
      backgroundColor = Color(0xFF061022)
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 16.dp)
    ) {
      Spacer(modifier = Modifier.height(10.dp))

      // Audio visualization: Purple waveform + Large circular glowing blue music icon
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(112.dp)
          .clickable {
            audioPickerLauncher.launch(arrayOf("audio/*"))
          }
          .testTag("audio_waveform_visualizer"),
        contentAlignment = Alignment.Center
      ) {
        // Purple vertical bars waveform across full width
        Canvas(
          modifier = Modifier
            .fillMaxWidth()
            .height(78.dp)
        ) {
          val barCount = 42
          val spacing = size.width / barCount
          val barWidth = (spacing * 0.46f).coerceAtLeast(3f)
          val centerIdx = barCount / 2f

          for (i in 0 until barCount) {
            val distFromCenter = abs(i - centerIdx)
            // Leave space in the middle for the circular music button
            if (distFromCenter < 4.5f) continue

            val normalizedPos = i.toFloat() / barCount.toFloat()
            val animOffset = if (isPreviewPlaying) wavePhase else 0.8f
            val envelope = sin(normalizedPos * Math.PI * 3.0 + animOffset).toFloat()
            val baseHeightFraction = (0.20f + 0.62f * abs(envelope)).coerceIn(0.16f, 0.88f)
            val barH = size.height * baseHeightFraction
            val topY = (size.height - barH) / 2f
            val leftX = i * spacing + (spacing - barWidth) / 2f

            drawRoundRect(
              brush = Brush.verticalGradient(
                colors = listOf(
                  Color(0xFFB36BFF),
                  Color(0xFF7B2CBF),
                  Color(0xFFB36BFF)
                ),
                startY = topY,
                endY = topY + barH
              ),
              topLeft = Offset(leftX, topY),
              size = Size(barWidth, barH),
              cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
          }
        }

        // Outer cyan/blue radial glow + Large circular music icon
        Box(
          modifier = Modifier
            .size(86.dp)
            .clip(CircleShape)
            .background(
              Brush.radialGradient(
                colors = listOf(
                  Color(0x661190F8),
                  Color(0x220663F2),
                  Color.Transparent
                )
              )
            ),
          contentAlignment = Alignment.Center
        ) {
          Box(
            modifier = Modifier
              .size(66.dp)
              .shadow(12.dp, CircleShape, spotColor = AppColors.BrightBlue)
              .clip(CircleShape)
              .background(
                Brush.verticalGradient(
                  colors = listOf(Color(0xFF1E9BFF), Color(0xFF0554D6))
                )
              )
              .border(1.8.dp, Color(0xFF63C5FF), CircleShape)
              .clickable { onTogglePreviewPlay() }
              .testTag("audio_center_music_button"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Filled.MusicNote,
              contentDescription = "Audio Track",
              tint = Color.White,
              modifier = Modifier.size(34.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Duration: 03:24   |   Size: 12.5 MB
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Duration: ${MediaCompressionEngine.formatDurationSeconds(audioDurationSec)}",
          color = Color(0xFFD5E0F2),
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium
        )
        Text(
          text = "   |   ",
          color = AppColors.SecondaryText,
          fontSize = 12.sp
        )
        Text(
          text = "Size: ${MediaCompressionEngine.formatFileSize(audioOriginalBytes)}",
          color = Color(0xFFD5E0F2),
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium
        )
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Compression Mode
      Text(
        text = "Compression Mode",
        color = Color.White,
        fontSize = 13.5.sp,
        fontWeight = FontWeight.SemiBold
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Segmented pill container for Lossless | High Quality | Balanced | Small Size
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(38.dp)
          .clip(RoundedCornerShape(20.dp))
          .background(Color(0xFF15223C))
          .border(1.dp, Color(0xFF25375E), RoundedCornerShape(20.dp))
          .padding(2.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        CompressionMode.entries.forEach { mode ->
          val isSelected = selectedMode == mode
          Box(
            modifier = Modifier
              .weight(1f)
              .fillMaxHeight()
              .clip(RoundedCornerShape(18.dp))
              .background(
                if (isSelected) {
                  Brush.horizontalGradient(listOf(Color(0xFF0663F2), Color(0xFF1589F7)))
                } else {
                  Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                }
              )
              .then(
                if (isSelected) {
                  Modifier.border(1.dp, Color(0xFF5EB0FF), RoundedCornerShape(18.dp))
                } else {
                  Modifier
                }
              )
              .clickable { onModeSelected(mode) }
              .testTag("audio_mode_${mode.name.lowercase()}"),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = mode.shortLabel,
              color = if (isSelected) Color.White else Color(0xFFCBD6EC),
              fontSize = 11.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              maxLines = 1
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Format: MP3, AAC, Opus, FLAC
      Text(
        text = "Format",
        color = Color.White,
        fontSize = 13.5.sp,
        fontWeight = FontWeight.SemiBold
      )

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        AudioFormat.entries.forEach { format ->
          SelectableOptionChip(
            label = format.label,
            isSelected = selectedFormat == format,
            showCheckmark = false,
            isLightSurface = false,
            isPillShape = true,
            onClick = { onFormatSelected(format) },
            modifier = Modifier.weight(1f),
            testTag = "audio_format_${format.name.lowercase()}"
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Bitrate: 192 kbps + horizontal slider + 192 kbps on right
      Text(
        text = "Bitrate: $bitrateKbps kbps",
        color = Color.White,
        fontSize = 13.5.sp,
        fontWeight = FontWeight.SemiBold
      )

      Spacer(modifier = Modifier.height(4.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Slider(
          value = bitrateKbps.toFloat(),
          onValueChange = { onBitrateChanged(it.roundToInt()) },
          valueRange = 64f..320f,
          colors = SliderDefaults.colors(
            thumbColor = Color.White,
            activeTrackColor = AppColors.BrightBlue,
            inactiveTrackColor = Color(0xFF1F3052)
          ),
          modifier = Modifier
            .weight(1f)
            .testTag("audio_bitrate_slider")
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = "$bitrateKbps kbps",
          color = Color(0xFFD5E0F2),
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Sample Rate: 44.1 kHz + Dropdown: Auto
      Text(
        text = "Sample Rate: $sampleRateLabel",
        color = Color.White,
        fontSize = 13.5.sp,
        fontWeight = FontWeight.SemiBold
      )

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Horizontal dark indicator bar on the left matching reference
        Box(
          modifier = Modifier
            .weight(1f)
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(Color(0xFF1B2B4B))
        )
        Spacer(modifier = Modifier.width(12.dp))
        CustomDropdownField(
          selectedValue = sampleRateDropdown,
          options = listOf("Auto", "44.1 kHz", "48.0 kHz", "22.05 kHz"),
          onValueChange = onSampleRateDropdownChanged,
          isLightSurface = false,
          modifier = Modifier.width(105.dp),
          testTag = "audio_sample_rate_dropdown"
        )
      }

      Spacer(modifier = Modifier.height(26.dp))

      // Bottom button: 🎵 Compress Audio (purple gradient)
      PrimaryGradientButton(
        text = "Compress Audio",
        icon = Icons.Filled.MusicNote,
        brush = AppColors.PurpleActionButtonBrush,
        onClick = onCompressAudio,
        testTag = "audio_compress_button"
      )

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}
