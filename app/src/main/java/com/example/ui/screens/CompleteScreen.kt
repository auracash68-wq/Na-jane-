package com.example.ui.screens

import android.net.Uri
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.MediaCompressionEngine
import com.example.model.CompressionResult
import com.example.ui.components.PrimaryGradientButton
import com.example.ui.theme.AppColors

@Composable
fun CompleteScreen(
  result: CompressionResult,
  onOpenFileClick: () -> Unit,
  onShareClick: () -> Unit,
  onDoneClick: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(
            Color(0xFF061624),
            Color(0xFF071224),
            Color(0xFF050E1E)
          )
        )
      )
      .statusBarsPadding()
      .navigationBarsPadding()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 22.dp)
      .testTag("complete_screen_root"),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Spacer(modifier = Modifier.height(34.dp))

    // Large glowing green success circle with green checkmark icon
    Box(
      modifier = Modifier
        .size(110.dp)
        .testTag("complete_success_badge"),
      contentAlignment = Alignment.Center
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        // Outer green neon aura
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(
              Color(0x5505CD6A),
              Color(0x2205CD6A),
              Color.Transparent
            )
          ),
          radius = size.minDimension * 0.52f
        )

        // Inner dark green circle fill
        drawCircle(
          color = Color(0xFF092A26),
          radius = size.minDimension * 0.36f
        )

        // Glowing green ring
        drawCircle(
          brush = Brush.linearGradient(
            colors = listOf(Color(0xFF28F78D), Color(0xFF05CD6A))
          ),
          radius = size.minDimension * 0.36f,
          style = Stroke(width = 5.5.dp.toPx())
        )
      }

      Icon(
        imageVector = Icons.Filled.Check,
        contentDescription = "Compression Complete",
        tint = AppColors.Green,
        modifier = Modifier.size(44.dp)
      )
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Heading: Compression Complete!
    Text(
      text = "Compression Complete!",
      color = Color.White,
      fontSize = 21.sp,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Savings information: You saved 72% space \n (3.1 MB → 0.9 MB)
    Text(
      text = "You saved ${result.savedPercent}% space",
      color = Color(0xFFE2ECFA),
      fontSize = 14.sp,
      fontWeight = FontWeight.Medium,
      textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(3.dp))
    Text(
      text = "(${MediaCompressionEngine.formatFileSize(result.originalSizeBytes)} → ${MediaCompressionEngine.formatFileSize(result.compressedSizeBytes)})",
      color = Color(0xFFD5E2F6),
      fontSize = 13.5.sp,
      fontWeight = FontWeight.SemiBold,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(22.dp))

    // Large rounded preview image
    val previewShape = RoundedCornerShape(16.dp)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(155.dp)
        .clip(previewShape)
        .border(1.5.dp, Color(0xFF1F3868), previewShape)
        .clickable { onOpenFileClick() }
        .testTag("complete_preview_image")
    ) {
      if (!result.previewUriString.isNullOrBlank()) {
        AsyncImage(
          model = Uri.parse(result.previewUriString),
          contentDescription = "Compressed Preview",
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )
      } else {
        Image(
          painter = painterResource(id = result.previewDrawableRes),
          contentDescription = "Compressed Preview",
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )
      }
    }

    Spacer(modifier = Modifier.height(26.dp))

    // 1. Open File (Blue gradient button with folder icon)
    PrimaryGradientButton(
      text = "Open File",
      icon = Icons.Filled.Folder,
      brush = AppColors.BlueActionButtonBrush,
      onClick = onOpenFileClick,
      testTag = "complete_open_file_button"
    )

    Spacer(modifier = Modifier.height(12.dp))

    // 2. Share (Green gradient button with share icon)
    PrimaryGradientButton(
      text = "Share",
      icon = Icons.Filled.Share,
      brush = AppColors.GreenShareButtonBrush,
      onClick = onShareClick,
      testTag = "complete_share_button"
    )

    Spacer(modifier = Modifier.height(12.dp))

    // 3. Done (Dark transparent outlined button)
    val doneShape = RoundedCornerShape(25.dp)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
        .clip(doneShape)
        .background(Color(0xFF0C1830))
        .border(1.2.dp, Color(0xFF3E527A), doneShape)
        .clickable(onClick = onDoneClick)
        .testTag("complete_done_button"),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "Done",
        color = Color.White,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold
      )
    }

    Spacer(modifier = Modifier.height(20.dp))
  }
}
