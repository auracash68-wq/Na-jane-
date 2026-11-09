package com.example

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.MediaCompressionEngine
import com.example.model.AppScreen
import com.example.ui.components.HistoryModalDialog
import com.example.ui.components.SettingsModalDialog
import com.example.ui.screens.AudioCompressionScreen
import com.example.ui.screens.BatchCompressionScreen
import com.example.ui.screens.CompleteScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PhotoCompressionScreen
import com.example.ui.screens.ProcessingScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.VideoCompressionScreen
import com.example.ui.theme.AppColors
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.CompressorViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.dark(AppColors.MainDarkBackground.toArgb()),
      navigationBarStyle = SystemBarStyle.dark(AppColors.MainDarkBackground.toArgb())
    )
    setContent {
      MyApplicationTheme {
        val view = LocalView.current
        if (!view.isInEditMode) {
          SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
              isAppearanceLightStatusBars = false
              isAppearanceLightNavigationBars = false
            }
          }
        }
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = AppColors.MainDarkBackground
        ) {
          MediaCompressorApp()
        }
      }
    }
  }
}

@Composable
fun MediaCompressorApp(
  viewModel: CompressorViewModel = viewModel()
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val context = LocalContext.current
  var isInitialColdLaunch by remember { mutableStateOf(true) }

  androidx.compose.runtime.LaunchedEffect(Unit) {
    viewModel.initializeWithContext(context)
  }

  if (uiState.currentScreen != AppScreen.SPLASH && uiState.currentScreen != AppScreen.HOME) {
    BackHandler {
      viewModel.navigateBack()
    }
  } else if (uiState.currentScreen == AppScreen.HOME) {
    BackHandler {
      isInitialColdLaunch = false
      viewModel.navigateTo(AppScreen.SPLASH)
    }
  }

  Crossfade(
    targetState = uiState.currentScreen,
    animationSpec = tween(durationMillis = 220),
    label = "screen_transition"
  ) { screen ->
    when (screen) {
      AppScreen.SPLASH -> {
        SplashScreen(
          autoNavigate = isInitialColdLaunch,
          onNavigateToHome = {
            isInitialColdLaunch = false
            viewModel.navigateTo(AppScreen.HOME)
          },
          onNavigateToPhoto = {
            isInitialColdLaunch = false
            viewModel.navigateTo(AppScreen.PHOTO_COMPRESSION)
          },
          onNavigateToVideo = {
            isInitialColdLaunch = false
            viewModel.navigateTo(AppScreen.VIDEO_COMPRESSION)
          },
          onNavigateToAudio = {
            isInitialColdLaunch = false
            viewModel.navigateTo(AppScreen.AUDIO_COMPRESSION)
          }
        )
      }

      AppScreen.HOME -> {
        HomeScreen(
          onNavigateToPhoto = { viewModel.navigateTo(AppScreen.PHOTO_COMPRESSION) },
          onNavigateToVideo = { viewModel.navigateTo(AppScreen.VIDEO_COMPRESSION) },
          onNavigateToAudio = { viewModel.navigateTo(AppScreen.AUDIO_COMPRESSION) },
          onNavigateToBatch = { viewModel.navigateTo(AppScreen.BATCH_COMPRESSION) },
          onOpenHistory = { viewModel.setShowHistoryDialog(true) },
          onOpenSettings = { viewModel.setShowSettingsDialog(true) },
          onNavigateToSplash = {
            isInitialColdLaunch = false
            viewModel.navigateTo(AppScreen.SPLASH)
          }
        )
      }

      AppScreen.PHOTO_COMPRESSION -> {
        BackHandler { viewModel.navigateTo(AppScreen.HOME) }
        PhotoCompressionScreen(
          photoUriString = uiState.photoUriString,
          photoDrawableRes = uiState.photoDrawableRes,
          originalSizeBytes = uiState.photoOriginalBytes,
          compressedSizeBytes = uiState.photoCompressedBytes,
          selectedMode = uiState.photoCompressionMode,
          selectedFormat = uiState.photoOutputFormat,
          quality = uiState.photoQuality,
          resizeOption = uiState.photoResizeOption,
          comparisonFraction = uiState.photoComparisonFraction,
          onComparisonFractionChange = viewModel::setPhotoComparisonFraction,
          onPhotoSelected = { uri -> viewModel.onPhotoPicked(context, uri) },
          onModeSelected = viewModel::setPhotoCompressionMode,
          onFormatSelected = viewModel::setPhotoOutputFormat,
          onQualityChanged = viewModel::setPhotoQuality,
          onResizeSelected = viewModel::setPhotoResizeOption,
          onCompressNow = { viewModel.startPhotoCompression(context) },
          onBackClick = { viewModel.navigateTo(AppScreen.HOME) },
          onSettingsClick = { viewModel.setShowSettingsDialog(true) }
        )
      }

      AppScreen.VIDEO_COMPRESSION -> {
        BackHandler { viewModel.navigateTo(AppScreen.HOME) }
        VideoCompressionScreen(
          videoOriginalBytes = uiState.videoOriginalBytes,
          videoEstimatedBytes = uiState.videoEstimatedBytes,
          savedPercent = viewModel.videoSavedPercentage(),
          resolution = uiState.videoResolution,
          codec = uiState.videoCodec,
          bitrate = uiState.videoBitrate,
          fps = uiState.videoFps,
          audioQuality = uiState.videoAudioQuality,
          durationSec = uiState.videoDurationSec,
          currentPosSec = uiState.videoCurrentPosSec,
          isPlaying = uiState.isVideoPlayingPreview,
          onTogglePlay = viewModel::toggleVideoPlayback,
          onSeekTo = viewModel::seekVideoPreview,
          onVideoPicked = { uri -> viewModel.onVideoPicked(context, uri) },
          onResolutionChange = { viewModel.updateVideoSettings(resolution = it) },
          onCodecChange = { viewModel.updateVideoSettings(codec = it) },
          onBitrateChange = { viewModel.updateVideoSettings(bitrate = it) },
          onFpsChange = { viewModel.updateVideoSettings(fps = it) },
          onAudioQualityChange = { viewModel.updateVideoSettings(audioQuality = it) },
          onCompressVideo = { viewModel.startVideoCompression(context) },
          onBackClick = { viewModel.navigateTo(AppScreen.HOME) },
          onSettingsClick = { viewModel.setShowSettingsDialog(true) }
        )
      }

      AppScreen.AUDIO_COMPRESSION -> {
        BackHandler { viewModel.navigateTo(AppScreen.HOME) }
        AudioCompressionScreen(
          audioOriginalBytes = uiState.audioOriginalBytes,
          audioDurationSec = uiState.audioDurationSec,
          selectedMode = uiState.audioCompressionMode,
          selectedFormat = uiState.audioFormat,
          bitrateKbps = uiState.audioBitrateKbps,
          sampleRateLabel = uiState.audioSampleRate,
          sampleRateDropdown = uiState.audioSampleRateDropdown,
          isPreviewPlaying = uiState.isAudioPreviewPlaying,
          onAudioPicked = { uri -> viewModel.onAudioPicked(context, uri) },
          onTogglePreviewPlay = { viewModel.toggleAudioPreviewPlayback(context) },
          onModeSelected = viewModel::setAudioCompressionMode,
          onFormatSelected = viewModel::setAudioFormat,
          onBitrateChanged = viewModel::setAudioBitrate,
          onSampleRateDropdownChanged = viewModel::setAudioSampleRateDropdown,
          onCompressAudio = { viewModel.startAudioCompression(context) },
          onBackClick = { viewModel.navigateTo(AppScreen.HOME) },
          onSettingsClick = { viewModel.setShowSettingsDialog(true) }
        )
      }

      AppScreen.BATCH_COMPRESSION -> {
        BackHandler { viewModel.navigateTo(AppScreen.HOME) }
        BatchCompressionScreen(
          selectedTab = uiState.selectedBatchTab,
          allBatchItems = uiState.batchItems,
          applyToAllMode = uiState.batchApplyToAllMode,
          onTabSelected = viewModel::setBatchTab,
          onToggleItemSelection = viewModel::toggleBatchItemSelection,
          onAddBatchMedia = { uris -> viewModel.onBatchMediaPicked(context, uris) },
          onApplyToAllModeChange = viewModel::setBatchApplyToAllMode,
          onStartBatchCompression = { viewModel.startBatchCompression(context) },
          onBackClick = { viewModel.navigateTo(AppScreen.HOME) },
          onSettingsClick = { viewModel.setShowSettingsDialog(true) }
        )
      }

      AppScreen.PROCESSING -> {
        BackHandler { viewModel.cancelActiveCompression() }
        ProcessingScreen(
          percent = uiState.processingPercent,
          processedBytes = uiState.processingProcessedBytes,
          totalBytes = uiState.processingTotalBytes,
          speedMbPerSec = uiState.processingSpeedMbPerSec,
          remainingSeconds = uiState.processingRemainingSeconds,
          onCancelClick = viewModel::cancelActiveCompression
        )
      }

      AppScreen.COMPLETE -> {
        BackHandler { viewModel.navigateTo(AppScreen.HOME) }
        CompleteScreen(
          result = uiState.lastCompressionResult,
          onOpenFileClick = {
            MediaCompressionEngine.openCompressedFile(
              context = context,
              filePath = uiState.lastCompressionResult.outputFilePath,
              mimeType = uiState.lastCompressionResult.mimeType
            )
          },
          onShareClick = {
            MediaCompressionEngine.shareCompressedFile(
              context = context,
              filePath = uiState.lastCompressionResult.outputFilePath,
              mimeType = uiState.lastCompressionResult.mimeType
            )
          },
          onDoneClick = { viewModel.navigateTo(AppScreen.HOME) }
        )
      }
    }
  }

  if (uiState.showHistoryDialog) {
    HistoryModalDialog(
      entries = uiState.historyEntries,
      onClearHistory = { viewModel.clearCompressionHistory(context) },
      onDismiss = { viewModel.setShowHistoryDialog(false) }
    )
  }

  if (uiState.showSettingsDialog) {
    SettingsModalDialog(
      preserveExif = uiState.preserveExifMetadata,
      hardwareAccel = uiState.hardwareAcceleration,
      autoSave = uiState.autoSaveToGallery,
      onToggleExif = { viewModel.togglePreserveExif(context) },
      onToggleHardware = { viewModel.toggleHardwareAcceleration(context) },
      onToggleAutoSave = { viewModel.toggleAutoSaveToGallery(context) },
      onClearHistory = { viewModel.clearCompressionHistory(context) },
      onDismiss = { viewModel.setShowSettingsDialog(false) }
    )
  }
}
