package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MediaCompressionEngine
import com.example.model.CompressionHistoryEntry
import com.example.ui.theme.AppColors
import com.example.ui.theme.AppDimens
import com.example.ui.theme.AppTypography

@Composable
fun CompressorTopToolbar(
  title: String,
  onBackClick: () -> Unit,
  onSettingsClick: () -> Unit,
  backgroundColor: Color = Color(0xFF071630),
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(AppDimens.ToolbarHeight)
      .background(backgroundColor)
      .padding(horizontal = 8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    IconButton(
      onClick = onBackClick,
      modifier = Modifier
        .size(48.dp)
        .testTag("toolbar_back_button")
    ) {
      Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
        contentDescription = "Back",
        tint = AppColors.PrimaryWhite,
        modifier = Modifier.size(22.dp)
      )
    }

    Text(
      text = title,
      style = AppTypography.ToolbarTitle,
      modifier = Modifier
        .weight(1f)
        .padding(start = 4.dp),
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )

    IconButton(
      onClick = onSettingsClick,
      modifier = Modifier
        .size(48.dp)
        .testTag("toolbar_settings_button")
    ) {
      Icon(
        imageVector = Icons.Filled.Settings,
        contentDescription = "Settings",
        tint = AppColors.PrimaryWhite,
        modifier = Modifier.size(22.dp)
      )
    }
  }
}

@Composable
fun SplashTriangleBoltLogo(
  size: Dp = 128.dp,
  modifier: Modifier = Modifier
) {
  Canvas(modifier = modifier.size(size)) {
    val w = this.size.width
    val h = this.size.height

    // Outer ambient glow
    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(
          Color(0x551190F8),
          Color(0x286B20E8),
          Color.Transparent
        ),
        center = Offset(w * 0.52f, h * 0.52f),
        radius = w * 0.65f
      ),
      radius = w * 0.65f,
      center = Offset(w * 0.52f, h * 0.52f)
    )

    // Rounded play-triangle path
    val triPath = Path().apply {
      moveTo(w * 0.22f, h * 0.16f)
      cubicTo(w * 0.22f, h * 0.06f, w * 0.32f, h * 0.04f, w * 0.42f, h * 0.10f)
      lineTo(w * 0.84f, h * 0.39f)
      cubicTo(w * 0.94f, h * 0.46f, w * 0.94f, h * 0.56f, w * 0.84f, h * 0.63f)
      lineTo(w * 0.42f, h * 0.90f)
      cubicTo(w * 0.32f, h * 0.96f, w * 0.22f, h * 0.94f, w * 0.22f, h * 0.84f)
      close()
    }

    // Multi-color 3D gradient fill matching reference: top-left orange -> pink/purple -> right cyan/blue
    drawPath(
      path = triPath,
      brush = Brush.linearGradient(
        colors = listOf(
          Color(0xFFFFA126),
          Color(0xFFFF3877),
          Color(0xFF7B2BFF),
          Color(0xFF00D2FF)
        ),
        start = Offset(w * 0.18f, h * 0.12f),
        end = Offset(w * 0.88f, h * 0.78f)
      )
    )

    // Secondary diagonal sheen from top-right cyan
    drawPath(
      path = triPath,
      brush = Brush.linearGradient(
        colors = listOf(
          Color.Transparent,
          Color(0x6600E5FF),
          Color(0x991190F8)
        ),
        start = Offset(w * 0.25f, h * 0.75f),
        end = Offset(w * 0.88f, h * 0.45f)
      )
    )

    // Glossy inner rim highlight
    drawPath(
      path = triPath,
      brush = Brush.linearGradient(
        colors = listOf(
          Color(0xAAFFFFFF),
          Color(0x33FFFFFF),
          Color(0x8800E5FF)
        ),
        start = Offset(w * 0.2f, h * 0.1f),
        end = Offset(w * 0.9f, h * 0.9f)
      ),
      style = Stroke(width = w * 0.025f, join = StrokeJoin.Round)
    )

    // White lightning bolt in center
    val boltPath = Path().apply {
      moveTo(w * 0.54f, h * 0.28f)
      lineTo(w * 0.36f, h * 0.54f)
      lineTo(w * 0.49f, h * 0.54f)
      lineTo(w * 0.43f, h * 0.75f)
      lineTo(w * 0.65f, h * 0.47f)
      lineTo(w * 0.51f, h * 0.47f)
      close()
    }

    drawPath(
      path = boltPath,
      color = Color.White
    )
  }
}

@Composable
fun FlowingMulticolorWaves(
  modifier: Modifier = Modifier
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // Wave 1: Magenta -> Purple -> Blue -> Cyan -> Green ribbon wave
    val wave1 = Path().apply {
      moveTo(0f, h * 0.42f)
      cubicTo(
        w * 0.26f, h * 0.88f,
        w * 0.48f, h * 0.05f,
        w * 0.76f, h * 0.22f
      )
      cubicTo(
        w * 0.88f, h * 0.30f,
        w * 0.95f, h * 0.55f,
        w, h * 0.68f
      )
      lineTo(w, h * 0.88f)
      cubicTo(
        w * 0.85f, h * 0.68f,
        w * 0.68f, h * 0.35f,
        w * 0.46f, h * 0.56f
      )
      cubicTo(
        w * 0.25f, h * 0.78f,
        w * 0.12f, h * 0.68f,
        0f, h * 0.56f
      )
      close()
    }

    drawPath(
      path = wave1,
      brush = Brush.horizontalGradient(
        colors = listOf(
          Color(0xDDFF2CDF),
          Color(0xCC7928CA),
          Color(0xDD00D2FF),
          Color(0xEE05CD6A)
        )
      )
    )

    // Wave 2: Secondary translucent cyan/blue crest
    val wave2 = Path().apply {
      moveTo(0f, h * 0.64f)
      cubicTo(
        w * 0.28f, h * 0.92f,
        w * 0.55f, h * 0.18f,
        w * 0.82f, h * 0.38f
      )
      cubicTo(
        w * 0.92f, h * 0.46f,
        w * 0.97f, h * 0.62f,
        w, h * 0.74f
      )
    }

    drawPath(
      path = wave2,
      brush = Brush.horizontalGradient(
        colors = listOf(
          Color(0xFFFF55E8),
          Color(0xFF47B5FF),
          Color(0xFF00F5A0)
        )
      ),
      style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
    )

    // Top glowing edge line
    val topEdge = Path().apply {
      moveTo(0f, h * 0.42f)
      cubicTo(
        w * 0.26f, h * 0.88f,
        w * 0.48f, h * 0.05f,
        w * 0.76f, h * 0.22f
      )
      cubicTo(
        w * 0.88f, h * 0.30f,
        w * 0.95f, h * 0.55f,
        w, h * 0.68f
      )
    }
    drawPath(
      path = topEdge,
      brush = Brush.horizontalGradient(
        colors = listOf(
          Color(0xFFFF85F3),
          Color(0xFF70E4FF),
          Color(0xFF5CFF9D)
        )
      ),
      style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
    )
  }
}

@Composable
fun SelectableOptionChip(
  label: String,
  isSelected: Boolean,
  showCheckmark: Boolean,
  isLightSurface: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  isPillShape: Boolean = false,
  testTag: String = label
) {
  val shape = if (isPillShape) RoundedCornerShape(18.dp) else RoundedCornerShape(10.dp)
  val bgBrush = when {
    isSelected -> Brush.horizontalGradient(
      colors = listOf(Color(0xFF0663F2), Color(0xFF1589F7))
    )
    isLightSurface -> Brush.horizontalGradient(
      colors = listOf(Color(0xFFD5E3F8), Color(0xFFD5E3F8))
    )
    else -> Brush.horizontalGradient(
      colors = listOf(Color(0xFF1A2744), Color(0xFF1A2744))
    )
  }
  val textColor = when {
    isSelected -> Color.White
    isLightSurface -> AppColors.DarkNavyHeading
    else -> Color(0xFFD5DEEF)
  }

  Box(
    modifier = modifier
      .height(36.dp)
      .clip(shape)
      .background(bgBrush)
      .then(
        if (!isSelected && !isLightSurface) {
          Modifier.border(1.dp, Color(0xFF283A63), shape)
        } else if (isSelected && !isLightSurface) {
          Modifier.border(1.dp, Color(0xFF4DA3FF), shape)
        } else {
          Modifier
        }
      )
      .clickable(onClick = onClick)
      .testTag(testTag),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      color = textColor,
      fontSize = 11.5.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
      maxLines = 1
    )

    if (isSelected && showCheckmark) {
      Icon(
        imageVector = Icons.Filled.Check,
        contentDescription = null,
        tint = Color.White,
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(top = 3.dp, end = 4.dp)
          .size(9.dp)
      )
    }
  }
}

@Composable
fun CustomDropdownField(
  selectedValue: String,
  options: List<String>,
  onValueChange: (String) -> Unit,
  isLightSurface: Boolean,
  modifier: Modifier = Modifier,
  showSignalBarsIcon: Boolean = false,
  testTag: String = "dropdown_field"
) {
  var expanded by remember { mutableStateOf(false) }
  val shape = RoundedCornerShape(10.dp)
  val bgColor = if (isLightSurface) Color(0xFFD8E6FA) else Color(0xFF14213D)
  val borderColor = if (isLightSurface) Color(0xFFC5D7F2) else Color(0xFF23365E)
  val textColor = if (isLightSurface) AppColors.DarkNavyHeading else Color.White
  val iconTint = if (isLightSurface) AppColors.DarkNavyHeading else Color(0xFFB0BDD6)

  Box(modifier = modifier) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(38.dp)
        .clip(shape)
        .background(bgColor)
        .border(1.dp, borderColor, shape)
        .clickable { expanded = true }
        .padding(horizontal = 12.dp)
        .testTag(testTag),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(
        text = selectedValue,
        color = textColor,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.weight(1f)
      )
      Spacer(modifier = Modifier.width(4.dp))
      Icon(
        imageVector = if (showSignalBarsIcon) Icons.Filled.SignalCellularAlt else Icons.Filled.KeyboardArrowDown,
        contentDescription = "Select option",
        tint = iconTint,
        modifier = Modifier.size(18.dp)
      )
    }

    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      modifier = Modifier.background(if (isLightSurface) Color.White else Color(0xFF162544))
    ) {
      options.forEach { option ->
        DropdownMenuItem(
          text = {
            Text(
              text = option,
              color = if (isLightSurface) AppColors.DarkNavyHeading else Color.White,
              fontSize = 13.sp,
              fontWeight = if (option == selectedValue) FontWeight.Bold else FontWeight.Normal
            )
          },
          onClick = {
            onValueChange(option)
            expanded = false
          }
        )
      }
    }
  }
}

@Composable
fun PrimaryGradientButton(
  text: String,
  icon: ImageVector,
  brush: Brush,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  testTag: String = "primary_action_button"
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(50.dp)
      .shadow(elevation = 6.dp, shape = RoundedCornerShape(25.dp), spotColor = AppColors.PrimaryBlue)
      .clip(RoundedCornerShape(25.dp))
      .background(brush)
      .clickable(onClick = onClick)
      .testTag(testTag),
    contentAlignment = Alignment.Center
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = Color.White,
        modifier = Modifier.size(20.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = text,
        style = AppTypography.ActionButtonText
      )
    }
  }
}

@Composable
fun HistoryModalDialog(
  entries: List<CompressionHistoryEntry>,
  onClearHistory: () -> Unit = {},
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = Color(0xFF101F3E),
    titleContentColor = Color.White,
    textContentColor = Color(0xFFD0DCF0),
    title = {
      Text("Compression History", fontWeight = FontWeight.Bold, fontSize = 18.sp)
    },
    text = {
      if (entries.isEmpty()) {
        Text(
          "No files compressed in this session yet. Compress a photo, video, audio, or batch to view history here.",
          fontSize = 13.sp,
          color = AppColors.SecondaryText
        )
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          entries.take(6).forEach { entry ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF192B52))
                .padding(10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(entry.fileName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(
                  "${MediaCompressionEngine.formatFileSize(entry.originalBytes)} → ${MediaCompressionEngine.formatFileSize(entry.compressedBytes)}",
                  color = AppColors.SecondaryText,
                  fontSize = 11.sp
                )
              }
              Text(
                "-${entry.savedPercent}%",
                color = AppColors.Green,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
              )
            }
          }
        }
      }
    },
    dismissButton = {
      if (entries.isNotEmpty()) {
        TextButton(onClick = onClearHistory) {
          Text("Clear History", color = AppColors.Orange, fontWeight = FontWeight.SemiBold)
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Close", color = AppColors.BrightBlue, fontWeight = FontWeight.SemiBold)
      }
    }
  )
}

@Composable
fun SettingsModalDialog(
  preserveExif: Boolean,
  hardwareAccel: Boolean,
  autoSave: Boolean,
  onToggleExif: () -> Unit,
  onToggleHardware: () -> Unit,
  onToggleAutoSave: () -> Unit,
  onClearHistory: () -> Unit = {},
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = Color(0xFF101F3E),
    titleContentColor = Color.White,
    textContentColor = Color.White,
    title = {
      Text("Compression Settings", fontWeight = FontWeight.Bold, fontSize = 18.sp)
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SettingsSwitchRow(
          title = "Preserve EXIF Metadata",
          subtitle = "Keep camera and date tags on images",
          checked = preserveExif,
          onCheckedChange = { onToggleExif() }
        )
        SettingsSwitchRow(
          title = "Hardware Acceleration",
          subtitle = "Use MediaCodec GPU encoding when supported",
          checked = hardwareAccel,
          onCheckedChange = { onToggleHardware() }
        )
        SettingsSwitchRow(
          title = "Auto-Save Output",
          subtitle = "Save compressed copies to MediaStore & cache",
          checked = autoSave,
          onCheckedChange = { onToggleAutoSave() }
        )
      }
    },
    dismissButton = {
      TextButton(onClick = onClearHistory) {
        Text("Clear History", color = AppColors.Orange, fontWeight = FontWeight.SemiBold)
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Done", color = AppColors.BrightBlue, fontWeight = FontWeight.SemiBold)
      }
    }
  )
}

@Composable
private fun SettingsSwitchRow(
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(title, color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
      Text(subtitle, color = AppColors.SecondaryText, fontSize = 11.sp)
    }
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors = SwitchDefaults.colors(
        checkedThumbColor = Color.White,
        checkedTrackColor = AppColors.PrimaryBlue
      )
    )
  }
}
