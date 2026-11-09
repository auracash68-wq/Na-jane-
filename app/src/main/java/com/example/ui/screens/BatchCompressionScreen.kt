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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.MediaCompressionEngine
import com.example.model.BatchMediaItem
import com.example.model.BatchTab
import com.example.model.CompressionMode
import com.example.ui.components.CompressorTopToolbar
import com.example.ui.components.CustomDropdownField
import com.example.ui.components.PrimaryGradientButton
import com.example.ui.theme.AppColors

@Composable
fun BatchCompressionScreen(
  selectedTab: BatchTab,
  allBatchItems: List<BatchMediaItem>,
  applyToAllMode: CompressionMode,
  onTabSelected: (BatchTab) -> Unit,
  onToggleItemSelection: (String) -> Unit,
  onAddBatchMedia: (List<Uri>) -> Unit,
  onApplyToAllModeChange: (CompressionMode) -> Unit,
  onStartBatchCompression: () -> Unit,
  onBackClick: () -> Unit,
  onSettingsClick: () -> Unit
) {
  val multiVisualMediaPicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickMultipleVisualMedia(15)
  ) { uris: List<Uri> ->
    if (uris.isNotEmpty()) {
      onAddBatchMedia(uris)
    }
  }

  val multiAudioDocumentPicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenMultipleDocuments()
  ) { uris: List<Uri> ->
    if (uris.isNotEmpty()) {
      onAddBatchMedia(uris)
    }
  }

  val launchBatchPickerForCurrentTab = {
    when (selectedTab) {
      BatchTab.PHOTOS -> multiVisualMediaPicker.launch(
        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
      )
      BatchTab.VIDEOS -> multiVisualMediaPicker.launch(
        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
      )
      BatchTab.AUDIOS -> multiAudioDocumentPicker.launch(arrayOf("audio/*"))
    }
  }

  val photosCount = allBatchItems.count { it.tab == BatchTab.PHOTOS }
  val videosCount = allBatchItems.count { it.tab == BatchTab.VIDEOS }
  val audiosCount = allBatchItems.count { it.tab == BatchTab.AUDIOS }

  val visibleItems = allBatchItems.filter { it.tab == selectedTab }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF071630))
      .statusBarsPadding()
      .navigationBarsPadding()
      .testTag("batch_compression_screen_root")
  ) {
    // Top Toolbar
    CompressorTopToolbar(
      title = "Batch Compression",
      onBackClick = onBackClick,
      onSettingsClick = onSettingsClick,
      backgroundColor = Color(0xFF071630)
    )

    // Category Tabs: Photos (5) | Videos (2) | Audios (3)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      BatchCategoryTabPill(
        label = "Photos ($photosCount)",
        isSelected = selectedTab == BatchTab.PHOTOS,
        onClick = { onTabSelected(BatchTab.PHOTOS) },
        modifier = Modifier.weight(1f),
        testTag = "batch_tab_photos"
      )
      BatchCategoryTabPill(
        label = "Videos ($videosCount)",
        isSelected = selectedTab == BatchTab.VIDEOS,
        onClick = { onTabSelected(BatchTab.VIDEOS) },
        modifier = Modifier.weight(1f),
        testTag = "batch_tab_videos"
      )
      BatchCategoryTabPill(
        label = "Audios ($audiosCount)",
        isSelected = selectedTab == BatchTab.AUDIOS,
        onClick = { onTabSelected(BatchTab.AUDIOS) },
        modifier = Modifier.weight(1f),
        testTag = "batch_tab_audios"
      )
    }

    Spacer(modifier = Modifier.height(4.dp))

    // Light blue rounded sheet container for the file list and bottom action bar
    Column(
      modifier = Modifier
        .fillMaxSize()
        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
        .background(AppColors.LightPanelBackground)
        .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
      // File list
      LazyColumn(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(visibleItems, key = { it.id }) { item ->
          BatchFileRowCard(
            item = item,
            onToggleSelect = { onToggleItemSelection(item.id) },
            onThumbnailClick = launchBatchPickerForCurrentTab
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      HorizontalDivider(color = Color(0xFFCBD9F0), thickness = 1.dp)

      Spacer(modifier = Modifier.height(12.dp))

      // Apply to All + Balanced dropdown
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = "Apply to All",
          color = AppColors.DarkNavyHeading,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold
        )

        CustomDropdownField(
          selectedValue = applyToAllMode.label,
          options = CompressionMode.entries.map { it.label },
          onValueChange = { chosenLabel ->
            val mode = CompressionMode.entries.firstOrNull { it.label == chosenLabel }
              ?: CompressionMode.BALANCED
            onApplyToAllModeChange(mode)
          },
          isLightSurface = true,
          modifier = Modifier.width(142.dp),
          testTag = "batch_apply_all_dropdown"
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Primary button: ⚡ Start Batch Compression
      PrimaryGradientButton(
        text = "Start Batch Compression",
        icon = Icons.Filled.FlashOn,
        brush = AppColors.BlueActionButtonBrush,
        onClick = onStartBatchCompression,
        testTag = "batch_start_button"
      )

      Spacer(modifier = Modifier.height(6.dp))
    }
  }
}

@Composable
private fun BatchCategoryTabPill(
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  testTag: String
) {
  val shape = RoundedCornerShape(18.dp)
  val bgColor = if (isSelected) Color.White else Color(0xFF152546)
  val textColor = if (isSelected) AppColors.PrimaryBlue else Color(0xFFD6E2F8)

  Box(
    modifier = modifier
      .height(36.dp)
      .clip(shape)
      .background(bgColor)
      .then(
        if (!isSelected) {
          Modifier.border(1.dp, Color(0xFF283E6B), shape)
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
      fontSize = 12.sp,
      fontWeight = FontWeight.Bold,
      maxLines = 1
    )
  }
}

@Composable
private fun BatchFileRowCard(
  item: BatchMediaItem,
  onToggleSelect: () -> Unit,
  onThumbnailClick: () -> Unit
) {
  val cardShape = RoundedCornerShape(14.dp)
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(cardShape)
      .background(Color(0xFFF3F8FF))
      .border(1.dp, Color(0xFFD6E4F9), cardShape)
      .clickable { onToggleSelect() }
      .padding(horizontal = 10.dp, vertical = 8.dp)
      .testTag("batch_item_${item.name}"),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Thumbnail
    Box(
      modifier = Modifier
        .width(64.dp)
        .height(46.dp)
        .clip(RoundedCornerShape(9.dp))
        .clickable { onThumbnailClick() }
    ) {
      if (!item.uriString.isNullOrBlank()) {
        AsyncImage(
          model = Uri.parse(item.uriString),
          contentDescription = item.name,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )
      } else {
        Image(
          painter = painterResource(id = item.drawableRes),
          contentDescription = item.name,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )
      }
    }

    Spacer(modifier = Modifier.width(12.dp))

    // File name and File size
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.Center
    ) {
      Text(
        text = item.name,
        color = AppColors.DarkNavyHeading,
        fontSize = 13.5.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = MediaCompressionEngine.formatFileSize(item.sizeBytes),
        color = AppColors.DarkNavySubtext,
        fontSize = 11.5.sp,
        fontWeight = FontWeight.Medium
      )
    }

    // Blue circular selection checkbox
    Box(
      modifier = Modifier
        .size(24.dp)
        .clip(CircleShape)
        .background(if (item.isSelected) AppColors.PrimaryBlue else Color(0xFFC5D7F2))
        .clickable { onToggleSelect() }
        .testTag("batch_checkbox_${item.name}"),
      contentAlignment = Alignment.Center
    ) {
      if (item.isSelected) {
        Icon(
          imageVector = Icons.Filled.Check,
          contentDescription = "Selected",
          tint = Color.White,
          modifier = Modifier.size(15.dp)
        )
      }
    }
  }
}
