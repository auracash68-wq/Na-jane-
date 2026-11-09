package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.MediaCompressionEngine
import com.example.model.CompressionMode
import com.example.model.PhotoFormat
import com.example.ui.components.CompressorTopToolbar
import com.example.ui.components.CustomDropdownField
import com.example.ui.components.PrimaryGradientButton
import com.example.ui.components.SelectableOptionChip
import com.example.ui.theme.AppColors
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoCompressionScreen(
  photoUriString: String?,
  photoDrawableRes: Int,
  originalSizeBytes: Long,
  compressedSizeBytes: Long,
  selectedMode: CompressionMode,
  selectedFormat: PhotoFormat,
  quality: Int,
  resizeOption: String,
  comparisonFraction: Float,
  onComparisonFractionChange: (Float) -> Unit,
  onPhotoSelected: (Uri) -> Unit,
  onModeSelected: (CompressionMode) -> Unit,
  onFormatSelected: (PhotoFormat) -> Unit,
  onQualityChanged: (Int) -> Unit,
  onResizeSelected: (String) -> Unit,
  onCompressNow: () -> Unit,
  onBackClick: () -> Unit,
  onSettingsClick: () -> Unit
) {
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    if (uri != null) {
      onPhotoSelected(uri)
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF071630))
      .statusBarsPadding()
      .navigationBarsPadding()
      .testTag("photo_compression_screen_root")
  ) {
    // Top Toolbar
    CompressorTopToolbar(
      title = "Photo Compression",
      onBackClick = onBackClick,
      onSettingsClick = onSettingsClick,
      backgroundColor = Color(0xFF071630)
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .background(AppColors.LightPanelBackground)
        .verticalScroll(rememberScrollState())
    ) {
      // Image preview with before/after comparison divider & circular drag handle
      BoxWithConstraints(
        modifier = Modifier
          .fillMaxWidth()
          .height(215.dp)
          .clickable {
            photoPickerLauncher.launch(
              PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
          }
          .pointerInput(Unit) {
            detectHorizontalDragGestures { change, dragAmount ->
              change.consume()
              val deltaFraction = dragAmount / size.width.toFloat().coerceAtLeast(1f)
              onComparisonFractionChange(comparisonFraction + deltaFraction)
            }
          }
          .testTag("photo_preview_comparison_box")
      ) {
        val boxWidthPx = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        val density = LocalDensity.current
        val dividerX = (boxWidthPx * comparisonFraction).roundToInt()

        if (!photoUriString.isNullOrBlank()) {
          AsyncImage(
            model = Uri.parse(photoUriString),
            contentDescription = "Selected Photo Preview",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
        } else {
          Image(
            painter = painterResource(id = photoDrawableRes),
            contentDescription = "Photo Preview",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
        }

        // Bottom gradient scrim for clear text legibility
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(74.dp)
            .align(Alignment.BottomCenter)
            .background(
              Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color(0xBB040D1E))
              )
            )
        )

        // Vertical comparison line
        Box(
          modifier = Modifier
            .offset { IntOffset(dividerX - with(density) { 1.dp.roundToPx() }, 0) }
            .width(1.8.dp)
            .fillMaxHeight()
            .background(Color.White.copy(alpha = 0.92f))
        )

        // Circular drag handle with "< >"
        Box(
          modifier = Modifier
            .offset {
              IntOffset(
                x = dividerX - with(density) { 15.dp.roundToPx() },
                y = with(density) { 142.dp.roundToPx() }
              )
            }
            .size(30.dp)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(Color.White),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Filled.Code,
            contentDescription = "Comparison Handle",
            tint = AppColors.PrimaryBlue,
            modifier = Modifier.size(18.dp)
          )
        }

        // Left label: Original (4.2 MB)
        Column(
          modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(start = 14.dp, bottom = 12.dp)
        ) {
          Text(
            text = "Original",
            color = Color.White,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "(${MediaCompressionEngine.formatFileSize(originalSizeBytes)})",
            color = Color(0xFFE0EAFA),
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium
          )
        }

        // Right label: Compressed (892 KB)
        Column(
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(end = 14.dp, bottom = 12.dp),
          horizontalAlignment = Alignment.End
        ) {
          Text(
            text = "Compressed",
            color = Color.White,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End
          )
          Text(
            text = "(${MediaCompressionEngine.formatFileSize(compressedSizeBytes)})",
            color = Color(0xFFE0EAFA),
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End
          )
        }
      }

      // Light blue & white controls card container
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 14.dp)
      ) {
        // Compression Mode
        Row(
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Compression Mode",
            color = AppColors.DarkNavyHeading,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.width(5.dp))
          Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = "Compression Mode Info",
            tint = Color(0xFF47628C),
            modifier = Modifier.size(15.dp)
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
          CompressionMode.entries.forEach { mode ->
            SelectableOptionChip(
              label = mode.label,
              isSelected = selectedMode == mode,
              showCheckmark = true,
              isLightSurface = true,
              onClick = { onModeSelected(mode) },
              modifier = Modifier.weight(1f),
              testTag = "photo_mode_${mode.name.lowercase()}"
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Output Format
        Text(
          text = "Output Format",
          color = AppColors.DarkNavyHeading,
          fontSize = 13.5.sp,
          fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          PhotoFormat.entries.forEach { format ->
            SelectableOptionChip(
              label = format.label,
              isSelected = selectedFormat == format,
              showCheckmark = true,
              isLightSurface = true,
              onClick = { onFormatSelected(format) },
              modifier = Modifier.weight(1f),
              testTag = "photo_format_${format.name.lowercase()}"
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quality section: Quality: 75% ... 75%
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Quality: $quality%",
            color = AppColors.DarkNavyHeading,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "$quality%",
            color = AppColors.DarkNavyHeading,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
          )
        }

        Slider(
          value = quality.toFloat(),
          onValueChange = { onQualityChanged(it.roundToInt()) },
          valueRange = 10f..100f,
          colors = SliderDefaults.colors(
            thumbColor = AppColors.PrimaryBlue,
            activeTrackColor = AppColors.PrimaryBlue,
            inactiveTrackColor = Color(0xFFC2D6F2)
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("photo_quality_slider")
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Resize (Optional)
        Text(
          text = "Resize (Optional)",
          color = AppColors.DarkNavyHeading,
          fontSize = 13.5.sp,
          fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        CustomDropdownField(
          selectedValue = resizeOption,
          options = listOf(
            "Original Size",
            "75% (1440p)",
            "50% (1080p)",
            "25% (720p)"
          ),
          onValueChange = onResizeSelected,
          isLightSurface = true,
          testTag = "photo_resize_dropdown"
        )

        Spacer(modifier = Modifier.height(22.dp))

        // Bottom primary button: ⚡ Compress Now
        PrimaryGradientButton(
          text = "Compress Now",
          icon = Icons.Filled.FlashOn,
          brush = AppColors.BlueActionButtonBrush,
          onClick = onCompressNow,
          testTag = "photo_compress_now_button"
        )

        Spacer(modifier = Modifier.height(12.dp))
      }
    }
  }
}
