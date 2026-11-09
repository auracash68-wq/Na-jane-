package com.example.ui.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MediaCompressionEngine
import com.example.ui.theme.AppColors
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ProcessingScreen(
  percent: Int,
  processedBytes: Long,
  totalBytes: Long,
  speedMbPerSec: Float,
  remainingSeconds: Int,
  onCancelClick: () -> Unit
) {
  val infiniteTransition = rememberInfiniteTransition(label = "processing_wave")
  val phase by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 6.283f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1400, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "wave_phase"
  )

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(
            Color(0xFF050E20),
            Color(0xFF081838),
            Color(0xFF061024)
          )
        )
      )
      .statusBarsPadding()
      .navigationBarsPadding()
      .padding(horizontal = 24.dp)
      .testTag("processing_screen_root"),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Spacer(modifier = Modifier.weight(0.16f))

    // Large circular blue-to-green progress ring with 68% in center
    Box(
      modifier = Modifier
        .size(210.dp)
        .testTag("processing_progress_ring"),
      contentAlignment = Alignment.Center
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = 14.dp.toPx()
        val glowStroke = 26.dp.toPx()
        val diameter = size.minDimension - glowStroke
        val topLeft = Offset(glowStroke / 2f, glowStroke / 2f)
        val arcSize = Size(diameter, diameter)

        // Ambient radial glow behind ring
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(
              Color(0x3800E5A3),
              Color(0x180663F2),
              Color.Transparent
            )
          ),
          radius = size.minDimension * 0.52f
        )

        // Unfilled background track ring
        drawArc(
          color = Color(0xFF142444),
          startAngle = 0f,
          sweepAngle = 360f,
          useCenter = false,
          topLeft = topLeft,
          size = arcSize,
          style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        val sweep = (percent.coerceIn(1, 100) / 100f) * 360f

        // Soft outer neon glow along active arc
        drawArc(
          brush = Brush.sweepGradient(
            colors = listOf(
              Color(0x550663F2),
              Color(0x6600D4FF),
              Color(0x7705CD6A),
              Color(0x550663F2)
            )
          ),
          startAngle = -100f,
          sweepAngle = sweep,
          useCenter = false,
          topLeft = topLeft,
          size = arcSize,
          style = Stroke(width = glowStroke, cap = StrokeCap.Round)
        )

        // Crisp foreground blue-to-green progress arc
        drawArc(
          brush = Brush.sweepGradient(
            colors = listOf(
              Color(0xFF0663F2),
              Color(0xFF00D4FF),
              Color(0xFF05CD6A),
              Color(0xFF0663F2)
            )
          ),
          startAngle = -100f,
          sweepAngle = sweep,
          useCenter = false,
          topLeft = topLeft,
          size = arcSize,
          style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
      }

      // Percentage text: 68%
      Text(
        text = buildAnnotatedString {
          withStyle(SpanStyle(fontSize = 42.sp, fontWeight = FontWeight.Bold, color = Color.White)) {
            append("$percent")
          }
          withStyle(SpanStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)) {
            append("%")
          }
        },
        textAlign = TextAlign.Center
      )
    }

    Spacer(modifier = Modifier.height(18.dp))

    // Status: Compressing...
    Text(
      text = "Compressing...",
      color = Color.White,
      fontSize = 18.sp,
      fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(4.dp))

    // Progress details: (2.4 MB / 3.5 MB)
    Text(
      text = "(${MediaCompressionEngine.formatFileSize(processedBytes)} / ${MediaCompressionEngine.formatFileSize(totalBytes)})",
      color = Color(0xFFD0DCF0),
      fontSize = 13.5.sp,
      fontWeight = FontWeight.Medium
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Animated purple processing waveform
    Canvas(
      modifier = Modifier
        .fillMaxWidth()
        .height(66.dp)
        .testTag("processing_purple_waveform")
    ) {
      val w = size.width
      val h = size.height
      val baselineY = h * 0.88f
      val pointsCount = 64

      val linePath = Path()
      val fillPath = Path()

      for (i in 0..pointsCount) {
        val t = i.toFloat() / pointsCount.toFloat()
        val x = t * w
        // Multi-peak mountain waveform matching Screen 07
        val peak1 = kotlin.math.exp(-((t - 0.14f) * (t - 0.14f)) / 0.003f) * 0.72f
        val peak2 = kotlin.math.exp(-((t - 0.28f) * (t - 0.28f)) / 0.004f) * 0.52f
        val peak3 = kotlin.math.exp(-((t - 0.56f) * (t - 0.56f)) / 0.008f) * 0.35f
        val peak4 = kotlin.math.exp(-((t - 0.83f) * (t - 0.83f)) / 0.0025f) * 0.78f
        val ripple = (sin(t * 22f + phase) * 0.08f + cos(t * 13f - phase) * 0.06f)
        val amp = (peak1 + peak2 + peak3 + peak4 + ripple + 0.12f).coerceIn(0.06f, 0.85f)
        val y = baselineY - amp * (h * 0.78f)

        if (i == 0) {
          linePath.moveTo(x, y)
          fillPath.moveTo(x, baselineY)
          fillPath.lineTo(x, y)
        } else {
          linePath.lineTo(x, y)
          fillPath.lineTo(x, y)
        }
      }
      fillPath.lineTo(w, baselineY)
      fillPath.close()

      drawPath(
        path = fillPath,
        brush = Brush.verticalGradient(
          colors = listOf(
            Color(0x667B2BFF),
            Color(0x224815AA),
            Color.Transparent
          ),
          startY = 0f,
          endY = baselineY
        )
      )

      drawPath(
        path = linePath,
        brush = Brush.horizontalGradient(
          colors = listOf(
            Color(0xFF5E60CE),
            Color(0xFF9B5DE5),
            Color(0xFF7B2BFF),
            Color(0xFF5E60CE)
          )
        ),
        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
      )
    }

    Spacer(modifier = Modifier.height(18.dp))

    // Speed indicator (⚡ Speed: 12.3 MB/s) & Time Left indicator (🕒 Time Left: 00:00:08)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Filled.FlashOn,
          contentDescription = "Speed",
          tint = AppColors.Orange,
          modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
          Text(
            text = "Speed",
            color = AppColors.SecondaryText,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Normal
          )
          Text(
            text = String.format(Locale.US, "%.1f MB/s", speedMbPerSec),
            color = Color(0xFF4BA8FF),
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Filled.AccessTime,
          contentDescription = "Time Left",
          tint = AppColors.BrightBlue,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
          Text(
            text = "Time Left",
            color = AppColors.SecondaryText,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Normal
          )
          Text(
            text = MediaCompressionEngine.formatHoursMinutesSeconds(remainingSeconds),
            color = Color.White,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }

    Spacer(modifier = Modifier.weight(0.24f))

    // Bottom: Outlined rounded Cancel button with pause icon
    val cancelShape = RoundedCornerShape(25.dp)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(50.dp)
        .clip(cancelShape)
        .background(Color(0xFF0E1D3A))
        .border(
          width = 1.2.dp,
          brush = Brush.horizontalGradient(
            colors = listOf(Color(0xFF4E6899), Color(0xFF334973))
          ),
          shape = cancelShape
        )
        .clickable(onClick = onCancelClick)
        .testTag("processing_cancel_button"),
      contentAlignment = Alignment.Center
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        Icon(
          imageVector = Icons.Filled.Pause,
          contentDescription = "Cancel",
          tint = Color.White,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Cancel",
          color = Color.White,
          fontSize = 15.sp,
          fontWeight = FontWeight.SemiBold
        )
      }
    }

    Spacer(modifier = Modifier.height(22.dp))
  }
}
