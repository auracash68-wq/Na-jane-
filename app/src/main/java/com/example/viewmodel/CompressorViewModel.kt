package com.example.viewmodel

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.compression.common.CompressionError
import com.example.compression.core.SmartCompressionManager
import com.example.compression.metadata.MediaMetadataReader
import com.example.compression.storage.CompressionHistoryDatabase
import com.example.data.MediaCompressionEngine
import com.example.model.AppScreen
import com.example.model.AudioFormat
import com.example.model.BatchMediaItem
import com.example.model.BatchTab
import com.example.model.CompressionHistoryEntry
import com.example.model.CompressionMode
import com.example.model.CompressionResult
import com.example.model.MediaCategory
import com.example.model.PhotoFormat
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.roundToInt
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CompressorUiState(
  val currentScreen: AppScreen = AppScreen.SPLASH,
  val previousScreen: AppScreen = AppScreen.HOME,

  // Photo Compression State (Matches Screen 03 defaults)
  val photoFileName: String = "Mountain_Lake.jpg",
  val photoUriString: String? = null,
  val photoDrawableRes: Int = R.drawable.sample_mountain_lake,
  val photoOriginalBytes: Long = 4_404_019L, // 4.2 MB
  val photoCompressedBytes: Long = 913_408L, // 892 KB
  val photoCompressionMode: CompressionMode = CompressionMode.BALANCED,
  val photoOutputFormat: PhotoFormat = PhotoFormat.JPG,
  val photoQuality: Int = 75,
  val photoResizeOption: String = "Original Size",
  val photoComparisonFraction: Float = 0.50f,

  // Video Compression State (Matches Screen 04 defaults)
  val videoFileName: String = "Coastal_Sunset.mp4",
  val videoUriString: String? = null,
  val videoOriginalBytes: Long = 89_758_105L, // 85.6 MB
  val videoEstimatedBytes: Long = 29_674_700L, // 28.3 MB
  val videoResolution: String = "1080p (Full HD)",
  val videoCodec: String = "H.264 (Recommended)",
  val videoBitrate: String = "Auto (Balanced)",
  val videoFps: String = "Auto",
  val videoAudioQuality: String = "High",
  val videoDurationSec: Int = 30,
  val videoCurrentPosSec: Int = 0,
  val isVideoPlayingPreview: Boolean = false,

  // Audio Compression State (Matches Screen 05 defaults)
  val audioFileName: String = "Synthwave_Track.mp3",
  val audioUriString: String? = null,
  val audioOriginalBytes: Long = 13_107_200L, // 12.5 MB
  val audioDurationSec: Int = 204, // 03:24
  val audioCompressionMode: CompressionMode = CompressionMode.BALANCED,
  val audioFormat: AudioFormat = AudioFormat.MP3,
  val audioBitrateKbps: Int = 192,
  val audioSampleRate: String = "44.1 kHz",
  val audioSampleRateDropdown: String = "Auto",
  val isAudioPreviewPlaying: Boolean = false,

  // Batch Compression State (Matches Screen 06 defaults)
  val selectedBatchTab: BatchTab = BatchTab.PHOTOS,
  val batchItems: List<BatchMediaItem> = defaultBatchItems(),
  val batchApplyToAllMode: CompressionMode = CompressionMode.BALANCED,

  // Processing Screen State (Matches Screen 07 defaults)
  val processingPercent: Int = 68,
  val processingProcessedBytes: Long = 2_516_582L, // 2.4 MB
  val processingTotalBytes: Long = 3_670_016L, // 3.5 MB
  val processingSpeedMbPerSec: Float = 12.3f,
  val processingRemainingSeconds: Int = 8,

  // Complete Screen State (Matches Screen 08 defaults)
  val lastCompressionResult: CompressionResult = CompressionResult(
    category = MediaCategory.PHOTO,
    fileName = "Nature_compressed.jpg",
    originalSizeBytes = 3_250_585L, // 3.1 MB
    compressedSizeBytes = 943_718L, // 0.9 MB
    savedPercent = 72,
    outputFilePath = "",
    mimeType = "image/jpeg",
    previewDrawableRes = R.drawable.sample_mountain_lake
  ),

  // History & Settings Dialogs
  val historyEntries: List<CompressionHistoryEntry> = emptyList(),
  val showHistoryDialog: Boolean = false,
  val showSettingsDialog: Boolean = false,
  val preserveExifMetadata: Boolean = true,
  val hardwareAcceleration: Boolean = true,
  val autoSaveToGallery: Boolean = true,
  val lastErrorMessage: String? = null
)

private fun defaultBatchItems(): List<BatchMediaItem> = listOf(
  BatchMediaItem(
    id = "photo_1",
    name = "Nature.jpg",
    sizeBytes = (4.2 * 1024 * 1024).toLong(),
    tab = BatchTab.PHOTOS,
    isSelected = true,
    drawableRes = R.drawable.sample_mountain_lake
  ),
  BatchMediaItem(
    id = "photo_2",
    name = "Family.png",
    sizeBytes = (3.8 * 1024 * 1024).toLong(),
    tab = BatchTab.PHOTOS,
    isSelected = true,
    drawableRes = R.drawable.sample_family
  ),
  BatchMediaItem(
    id = "photo_3",
    name = "Travel.webp",
    sizeBytes = (5.1 * 1024 * 1024).toLong(),
    tab = BatchTab.PHOTOS,
    isSelected = true,
    drawableRes = R.drawable.sample_travel
  ),
  BatchMediaItem(
    id = "photo_4",
    name = "Portrait.jpg",
    sizeBytes = (2.9 * 1024 * 1024).toLong(),
    tab = BatchTab.PHOTOS,
    isSelected = true,
    drawableRes = R.drawable.sample_portrait
  ),
  BatchMediaItem(
    id = "photo_5",
    name = "Landscape.png",
    sizeBytes = (3.7 * 1024 * 1024).toLong(),
    tab = BatchTab.PHOTOS,
    isSelected = true,
    drawableRes = R.drawable.sample_mountain_lake
  ),
  BatchMediaItem(
    id = "video_1",
    name = "Coastline_4K.mp4",
    sizeBytes = (85.6 * 1024 * 1024).toLong(),
    tab = BatchTab.VIDEOS,
    isSelected = true,
    drawableRes = R.drawable.sample_video_coast
  ),
  BatchMediaItem(
    id = "video_2",
    name = "Mountain_Trip.mp4",
    sizeBytes = (42.4 * 1024 * 1024).toLong(),
    tab = BatchTab.VIDEOS,
    isSelected = true,
    drawableRes = R.drawable.sample_mountain_lake
  ),
  BatchMediaItem(
    id = "audio_1",
    name = "Podcast_Ep01.mp3",
    sizeBytes = (12.5 * 1024 * 1024).toLong(),
    tab = BatchTab.AUDIOS,
    isSelected = true,
    drawableRes = R.drawable.sample_travel
  ),
  BatchMediaItem(
    id = "audio_2",
    name = "Acoustic_Live.flac",
    sizeBytes = (28.9 * 1024 * 1024).toLong(),
    tab = BatchTab.AUDIOS,
    isSelected = true,
    drawableRes = R.drawable.sample_mountain_lake
  ),
  BatchMediaItem(
    id = "audio_3",
    name = "Voice_Memo.aac",
    sizeBytes = (6.4 * 1024 * 1024).toLong(),
    tab = BatchTab.AUDIOS,
    isSelected = true,
    drawableRes = R.drawable.sample_portrait
  )
)

class CompressorViewModel : ViewModel() {

  private val _uiState = MutableStateFlow(CompressorUiState())
  val uiState: StateFlow<CompressorUiState> = _uiState.asStateFlow()

  private var activeCompressionJob: Job? = null
  private var historyInitialized = false

  fun initializeWithContext(context: Context) {
    if (historyInitialized) return
    historyInitialized = true
    SmartCompressionManager.initializeSession(context)
    val db = CompressionHistoryDatabase.getInstance(context)
    _uiState.update {
      it.copy(
        preserveExifMetadata = db.preserveExifMetadata,
        hardwareAcceleration = db.hardwareAcceleration,
        autoSaveToGallery = db.autoSaveToGallery
      )
    }
    viewModelScope.launch {
      db.historyFlow.collect { list ->
        _uiState.update { it.copy(historyEntries = list) }
      }
    }
  }

  fun navigateTo(screen: AppScreen) {
    _uiState.update { state ->
      val prev = if (screen == AppScreen.PROCESSING) state.currentScreen else state.previousScreen
      state.copy(
        currentScreen = screen,
        previousScreen = prev,
        isVideoPlayingPreview = false,
        isAudioPreviewPlaying = false
      )
    }
  }

  fun navigateBack() {
    val current = _uiState.value.currentScreen
    when (current) {
      AppScreen.SPLASH -> Unit
      AppScreen.HOME -> navigateTo(AppScreen.SPLASH)
      AppScreen.PHOTO_COMPRESSION,
      AppScreen.VIDEO_COMPRESSION,
      AppScreen.AUDIO_COMPRESSION,
      AppScreen.BATCH_COMPRESSION -> navigateTo(AppScreen.HOME)
      AppScreen.PROCESSING -> cancelActiveCompression()
      AppScreen.COMPLETE -> navigateTo(AppScreen.HOME)
    }
  }

  // Photo controls
  fun onPhotoPicked(context: Context, uri: Uri) {
    val meta = MediaMetadataReader.readPhotoMetadata(
      context = context,
      uri = uri,
      fallbackDrawableRes = R.drawable.sample_mountain_lake
    )
    _uiState.update { state ->
      val estimated = MediaCompressionEngine.estimatePhotoCompressedBytes(
        originalBytes = meta.fileSizeBytes,
        mode = state.photoCompressionMode,
        format = state.photoOutputFormat,
        quality = state.photoQuality,
        resizeOption = state.photoResizeOption,
        isDefaultReferencePhoto = false
      )
      state.copy(
        photoFileName = meta.fileName,
        photoUriString = uri.toString(),
        photoOriginalBytes = meta.fileSizeBytes,
        photoCompressedBytes = estimated
      )
    }
  }

  fun setPhotoCompressionMode(mode: CompressionMode) {
    val defaultQualityForMode = when (mode) {
      CompressionMode.LOSSLESS -> 100
      CompressionMode.HIGH_QUALITY -> 90
      CompressionMode.BALANCED -> 75
      CompressionMode.SMALLEST_SIZE -> 50
    }
    _uiState.update { state ->
      val estimated = MediaCompressionEngine.estimatePhotoCompressedBytes(
        originalBytes = state.photoOriginalBytes,
        mode = mode,
        format = state.photoOutputFormat,
        quality = defaultQualityForMode,
        resizeOption = state.photoResizeOption,
        isDefaultReferencePhoto = (state.photoUriString == null)
      )
      state.copy(
        photoCompressionMode = mode,
        photoQuality = defaultQualityForMode,
        photoCompressedBytes = estimated
      )
    }
  }

  fun setPhotoOutputFormat(format: PhotoFormat) {
    _uiState.update { state ->
      val estimated = MediaCompressionEngine.estimatePhotoCompressedBytes(
        originalBytes = state.photoOriginalBytes,
        mode = state.photoCompressionMode,
        format = format,
        quality = state.photoQuality,
        resizeOption = state.photoResizeOption,
        isDefaultReferencePhoto = (state.photoUriString == null)
      )
      state.copy(
        photoOutputFormat = format,
        photoCompressedBytes = estimated
      )
    }
  }

  fun setPhotoQuality(quality: Int) {
    val clamped = quality.coerceIn(10, 100)
    _uiState.update { state ->
      val estimated = MediaCompressionEngine.estimatePhotoCompressedBytes(
        originalBytes = state.photoOriginalBytes,
        mode = state.photoCompressionMode,
        format = state.photoOutputFormat,
        quality = clamped,
        resizeOption = state.photoResizeOption,
        isDefaultReferencePhoto = (state.photoUriString == null)
      )
      state.copy(
        photoQuality = clamped,
        photoCompressedBytes = estimated
      )
    }
  }

  fun setPhotoResizeOption(option: String) {
    _uiState.update { state ->
      val estimated = MediaCompressionEngine.estimatePhotoCompressedBytes(
        originalBytes = state.photoOriginalBytes,
        mode = state.photoCompressionMode,
        format = state.photoOutputFormat,
        quality = state.photoQuality,
        resizeOption = option,
        isDefaultReferencePhoto = (state.photoUriString == null)
      )
      state.copy(
        photoResizeOption = option,
        photoCompressedBytes = estimated
      )
    }
  }

  fun setPhotoComparisonFraction(fraction: Float) {
    _uiState.update { it.copy(photoComparisonFraction = fraction.coerceIn(0.1f, 0.9f)) }
  }

  // Video controls
  fun onVideoPicked(context: Context, uri: Uri) {
    val meta = MediaMetadataReader.readVideoMetadata(context, uri)
    val duration = (meta.durationMs / 1000L).toInt().coerceAtLeast(1)
    _uiState.update { state ->
      val estimated = MediaCompressionEngine.estimateVideoCompressedBytes(
        originalBytes = meta.fileSizeBytes,
        resolution = state.videoResolution,
        codec = state.videoCodec,
        bitrate = state.videoBitrate,
        fps = state.videoFps,
        audioQuality = state.videoAudioQuality
      )
      state.copy(
        videoFileName = meta.fileName,
        videoUriString = uri.toString(),
        videoOriginalBytes = meta.fileSizeBytes,
        videoEstimatedBytes = estimated,
        videoDurationSec = duration,
        videoCurrentPosSec = 0
      )
    }
  }

  fun updateVideoSettings(
    resolution: String = _uiState.value.videoResolution,
    codec: String = _uiState.value.videoCodec,
    bitrate: String = _uiState.value.videoBitrate,
    fps: String = _uiState.value.videoFps,
    audioQuality: String = _uiState.value.videoAudioQuality
  ) {
    _uiState.update { state ->
      val estimated = MediaCompressionEngine.estimateVideoCompressedBytes(
        originalBytes = state.videoOriginalBytes,
        resolution = resolution,
        codec = codec,
        bitrate = bitrate,
        fps = fps,
        audioQuality = audioQuality
      )
      state.copy(
        videoResolution = resolution,
        videoCodec = codec,
        videoBitrate = bitrate,
        videoFps = fps,
        videoAudioQuality = audioQuality,
        videoEstimatedBytes = estimated
      )
    }
  }

  fun toggleVideoPlayback() {
    _uiState.update { it.copy(isVideoPlayingPreview = !it.isVideoPlayingPreview) }
  }

  fun seekVideoPreview(seconds: Int) {
    _uiState.update { state ->
      state.copy(videoCurrentPosSec = seconds.coerceIn(0, state.videoDurationSec))
    }
  }

  // Audio controls
  fun onAudioPicked(context: Context, uri: Uri) {
    val meta = MediaMetadataReader.readAudioMetadata(context, uri)
    val duration = (meta.durationMs / 1000L).toInt().coerceAtLeast(1)
    _uiState.update { state ->
      state.copy(
        audioFileName = meta.fileName,
        audioUriString = uri.toString(),
        audioOriginalBytes = meta.fileSizeBytes,
        audioDurationSec = duration
      )
    }
  }

  fun setAudioCompressionMode(mode: CompressionMode) {
    val recommendedKbps = when (mode) {
      CompressionMode.LOSSLESS -> 320
      CompressionMode.HIGH_QUALITY -> 256
      CompressionMode.BALANCED -> 192
      CompressionMode.SMALLEST_SIZE -> 96
    }
    _uiState.update {
      it.copy(
        audioCompressionMode = mode,
        audioBitrateKbps = recommendedKbps
      )
    }
  }

  fun setAudioFormat(format: AudioFormat) {
    _uiState.update { it.copy(audioFormat = format) }
  }

  fun setAudioBitrate(kbps: Int) {
    _uiState.update { it.copy(audioBitrateKbps = kbps.coerceIn(64, 320)) }
  }

  fun setAudioSampleRateDropdown(option: String) {
    val displayRate = when (option) {
      "48.0 kHz" -> "48.0 kHz"
      "44.1 kHz" -> "44.1 kHz"
      "22.05 kHz" -> "22.05 kHz"
      else -> "44.1 kHz"
    }
    _uiState.update {
      it.copy(
        audioSampleRateDropdown = option,
        audioSampleRate = displayRate
      )
    }
  }

  fun toggleAudioPreviewPlayback(context: Context? = null) {
    val nextPlaying = !_uiState.value.isAudioPreviewPlaying
    _uiState.update { it.copy(isAudioPreviewPlaying = nextPlaying) }
    if (context != null) {
      com.example.compression.audio.AudioCompressionEngine.togglePreviewPlayback(
        context = context,
        uriString = _uiState.value.audioUriString,
        play = nextPlaying
      )
    }
  }

  // Batch controls
  fun setBatchTab(tab: BatchTab) {
    _uiState.update { it.copy(selectedBatchTab = tab) }
  }

  fun toggleBatchItemSelection(itemId: String) {
    _uiState.update { state ->
      state.copy(
        batchItems = state.batchItems.map { item ->
          if (item.id == itemId) item.copy(isSelected = !item.isSelected) else item
        }
      )
    }
  }

  fun onBatchMediaPicked(context: Context, uris: List<Uri>) {
    if (uris.isEmpty()) return
    val currentTab = _uiState.value.selectedBatchTab
    val addedItems = uris.mapIndexed { idx, uri ->
      val (name, size) = MediaCompressionEngine.queryUriFileInfo(context, uri)
      val detectedCat = MediaMetadataReader.detectCategory(context, uri, name)
      val assignedTab = when (detectedCat) {
        MediaCategory.VIDEO -> BatchTab.VIDEOS
        MediaCategory.AUDIO -> BatchTab.AUDIOS
        else -> currentTab
      }
      BatchMediaItem(
        id = "picked_${System.currentTimeMillis()}_$idx",
        name = name,
        sizeBytes = if (size > 0L) size else 3_500_000L,
        tab = assignedTab,
        isSelected = true,
        drawableRes = R.drawable.sample_mountain_lake,
        uriString = uri.toString()
      )
    }
    _uiState.update { state ->
      state.copy(batchItems = addedItems + state.batchItems)
    }
  }

  fun setBatchApplyToAllMode(mode: CompressionMode) {
    _uiState.update { it.copy(batchApplyToAllMode = mode) }
  }

  // Compression Execution (Screen 03/04/05/06 -> Screen 07 -> Screen 08)
  fun startPhotoCompression(context: Context) {
    activeCompressionJob?.cancel()
    val snapshot = _uiState.value
    _uiState.update {
      it.copy(
        previousScreen = AppScreen.PHOTO_COMPRESSION,
        currentScreen = AppScreen.PROCESSING,
        processingPercent = 68,
        processingProcessedBytes = 2_516_582L,
        processingTotalBytes = 3_670_016L,
        processingSpeedMbPerSec = 12.3f,
        processingRemainingSeconds = 8,
        lastErrorMessage = null
      )
    }
    activeCompressionJob = viewModelScope.launch {
      try {
        val result = MediaCompressionEngine.compressPhotoReal(
          context = context,
          sourceUriString = snapshot.photoUriString,
          fallbackDrawableRes = snapshot.photoDrawableRes,
          originalSizeBytes = if (snapshot.photoUriString != null) snapshot.photoOriginalBytes else 3_250_585L,
          mode = snapshot.photoCompressionMode,
          format = snapshot.photoOutputFormat,
          quality = snapshot.photoQuality,
          resizeOption = snapshot.photoResizeOption,
          preserveExif = snapshot.preserveExifMetadata,
          autoSaveToGallery = snapshot.autoSaveToGallery,
          onProgress = { prog ->
            _uiState.update { st ->
              st.copy(
                processingPercent = prog.percent,
                processingProcessedBytes = prog.processedBytes,
                processingTotalBytes = prog.totalBytes,
                processingSpeedMbPerSec = prog.speedMbPerSec,
                processingRemainingSeconds = prog.remainingSeconds
              )
            }
          }
        )
        onCompressionFinished(result)
      } catch (ce: CancellationException) {
        throw ce
      } catch (err: CompressionError) {
        handleCompressionError(context, err.userMessage)
      } catch (t: Throwable) {
        handleCompressionError(context, t.localizedMessage ?: "Photo compression failed")
      }
    }
  }

  fun startVideoCompression(context: Context) {
    activeCompressionJob?.cancel()
    val snapshot = _uiState.value
    _uiState.update {
      it.copy(
        previousScreen = AppScreen.VIDEO_COMPRESSION,
        currentScreen = AppScreen.PROCESSING,
        processingPercent = 12,
        processingProcessedBytes = 10_485_760L,
        processingTotalBytes = snapshot.videoOriginalBytes,
        processingSpeedMbPerSec = 12.3f,
        processingRemainingSeconds = 8,
        lastErrorMessage = null
      )
    }
    activeCompressionJob = viewModelScope.launch {
      try {
        val result = MediaCompressionEngine.compressVideoReal(
          context = context,
          sourceUriString = snapshot.videoUriString,
          originalSizeBytes = snapshot.videoOriginalBytes,
          estimatedSizeBytes = snapshot.videoEstimatedBytes,
          resolution = snapshot.videoResolution,
          codec = snapshot.videoCodec,
          bitrate = snapshot.videoBitrate,
          fps = snapshot.videoFps,
          audioQuality = snapshot.videoAudioQuality,
          preferHardware = snapshot.hardwareAcceleration,
          autoSaveToGallery = snapshot.autoSaveToGallery,
          onProgress = { prog ->
            _uiState.update { st ->
              st.copy(
                processingPercent = prog.percent,
                processingProcessedBytes = prog.processedBytes,
                processingTotalBytes = prog.totalBytes,
                processingSpeedMbPerSec = prog.speedMbPerSec,
                processingRemainingSeconds = prog.remainingSeconds
              )
            }
          }
        )
        onCompressionFinished(result)
      } catch (ce: CancellationException) {
        throw ce
      } catch (err: CompressionError) {
        handleCompressionError(context, err.userMessage)
      } catch (t: Throwable) {
        handleCompressionError(context, t.localizedMessage ?: "Video compression failed")
      }
    }
  }

  fun startAudioCompression(context: Context) {
    activeCompressionJob?.cancel()
    val snapshot = _uiState.value
    _uiState.update {
      it.copy(
        previousScreen = AppScreen.AUDIO_COMPRESSION,
        currentScreen = AppScreen.PROCESSING,
        processingPercent = 15,
        processingProcessedBytes = 2_097_152L,
        processingTotalBytes = snapshot.audioOriginalBytes,
        processingSpeedMbPerSec = 12.3f,
        processingRemainingSeconds = 8,
        lastErrorMessage = null
      )
    }
    activeCompressionJob = viewModelScope.launch {
      try {
        val result = MediaCompressionEngine.compressAudioReal(
          context = context,
          sourceUriString = snapshot.audioUriString,
          originalSizeBytes = snapshot.audioOriginalBytes,
          mode = snapshot.audioCompressionMode,
          format = snapshot.audioFormat,
          bitrateKbps = snapshot.audioBitrateKbps,
          sampleRateLabel = snapshot.audioSampleRate,
          autoSaveToGallery = snapshot.autoSaveToGallery,
          onProgress = { prog ->
            _uiState.update { st ->
              st.copy(
                processingPercent = prog.percent,
                processingProcessedBytes = prog.processedBytes,
                processingTotalBytes = prog.totalBytes,
                processingSpeedMbPerSec = prog.speedMbPerSec,
                processingRemainingSeconds = prog.remainingSeconds
              )
            }
          }
        )
        onCompressionFinished(result)
      } catch (ce: CancellationException) {
        throw ce
      } catch (err: CompressionError) {
        handleCompressionError(context, err.userMessage)
      } catch (t: Throwable) {
        handleCompressionError(context, t.localizedMessage ?: "Audio compression failed")
      }
    }
  }

  fun startBatchCompression(context: Context) {
    activeCompressionJob?.cancel()
    val snapshot = _uiState.value
    val activeItems = snapshot.batchItems.filter { it.isSelected }.ifEmpty {
      snapshot.batchItems.filter { it.tab == snapshot.selectedBatchTab }
    }
    _uiState.update {
      it.copy(
        previousScreen = AppScreen.BATCH_COMPRESSION,
        currentScreen = AppScreen.PROCESSING,
        processingPercent = 10,
        processingProcessedBytes = 2_516_582L,
        processingTotalBytes = activeItems.sumOf { item -> item.sizeBytes }.coerceAtLeast(3_670_016L),
        processingSpeedMbPerSec = 12.3f,
        processingRemainingSeconds = 8,
        lastErrorMessage = null
      )
    }
    activeCompressionJob = viewModelScope.launch {
      try {
        val result = MediaCompressionEngine.compressBatchReal(
          context = context,
          selectedItems = activeItems,
          mode = snapshot.batchApplyToAllMode,
          preserveExif = snapshot.preserveExifMetadata,
          preferHardware = snapshot.hardwareAcceleration,
          autoSaveToGallery = snapshot.autoSaveToGallery,
          onProgress = { prog ->
            _uiState.update { st ->
              st.copy(
                processingPercent = prog.percent,
                processingProcessedBytes = prog.processedBytes,
                processingTotalBytes = prog.totalBytes,
                processingSpeedMbPerSec = prog.speedMbPerSec,
                processingRemainingSeconds = prog.remainingSeconds
              )
            }
          }
        )
        onCompressionFinished(result)
      } catch (ce: CancellationException) {
        throw ce
      } catch (err: CompressionError) {
        handleCompressionError(context, err.userMessage)
      } catch (t: Throwable) {
        handleCompressionError(context, t.localizedMessage ?: "Batch compression failed")
      }
    }
  }

  private fun onCompressionFinished(result: CompressionResult) {
    val entry = CompressionHistoryEntry(
      id = "hist_${System.currentTimeMillis()}",
      fileName = result.fileName,
      category = result.category,
      originalBytes = result.originalSizeBytes,
      compressedBytes = result.compressedSizeBytes,
      savedPercent = result.savedPercent,
      outputFilePath = result.outputFilePath,
      timestampMillis = System.currentTimeMillis()
    )
    _uiState.update { state ->
      val updatedHistory = if (state.historyEntries.any { it.outputFilePath == result.outputFilePath && result.outputFilePath.isNotBlank() }) {
        state.historyEntries
      } else {
        listOf(entry) + state.historyEntries
      }
      state.copy(
        currentScreen = AppScreen.COMPLETE,
        lastCompressionResult = result,
        historyEntries = updatedHistory
      )
    }
  }

  private fun handleCompressionError(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    _uiState.update { state ->
      val target = if (state.previousScreen == AppScreen.PROCESSING || state.previousScreen == AppScreen.SPLASH) {
        AppScreen.HOME
      } else {
        state.previousScreen
      }
      state.copy(
        currentScreen = target,
        lastErrorMessage = message
      )
    }
  }

  fun cancelActiveCompression() {
    activeCompressionJob?.cancel()
    activeCompressionJob = null
    _uiState.update { state ->
      val target = if (state.previousScreen == AppScreen.PROCESSING || state.previousScreen == AppScreen.SPLASH) {
        AppScreen.HOME
      } else {
        state.previousScreen
      }
      state.copy(currentScreen = target)
    }
  }

  // Dialog & Persistent Settings controls
  fun setShowHistoryDialog(show: Boolean) {
    _uiState.update { it.copy(showHistoryDialog = show) }
  }

  fun setShowSettingsDialog(show: Boolean) {
    _uiState.update { it.copy(showSettingsDialog = show) }
  }

  fun togglePreserveExif(context: Context? = null) {
    val next = !_uiState.value.preserveExifMetadata
    context?.let { CompressionHistoryDatabase.getInstance(it).preserveExifMetadata = next }
    _uiState.update { it.copy(preserveExifMetadata = next) }
  }

  fun toggleHardwareAcceleration(context: Context? = null) {
    val next = !_uiState.value.hardwareAcceleration
    context?.let { CompressionHistoryDatabase.getInstance(it).hardwareAcceleration = next }
    _uiState.update { it.copy(hardwareAcceleration = next) }
  }

  fun toggleAutoSaveToGallery(context: Context? = null) {
    val next = !_uiState.value.autoSaveToGallery
    context?.let { CompressionHistoryDatabase.getInstance(it).autoSaveToGallery = next }
    _uiState.update { it.copy(autoSaveToGallery = next) }
  }

  fun clearCompressionHistory(context: Context) {
    viewModelScope.launch {
      CompressionHistoryDatabase.getInstance(context).clearHistory()
      _uiState.update { it.copy(historyEntries = emptyList()) }
    }
  }

  fun videoSavedPercentage(): Int {
    val orig = _uiState.value.videoOriginalBytes.toDouble()
    val est = _uiState.value.videoEstimatedBytes.toDouble()
    if (orig <= 0.0) return 67
    return (((orig - est) / orig) * 100.0).roundToInt().coerceIn(5, 95)
  }
}
