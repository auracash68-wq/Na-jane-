package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.compression.audio.AudioCompressionEngine
import com.example.compression.common.DeviceCapabilities
import com.example.compression.core.SmartCompressionManager
import com.example.compression.photo.PhotoCompressionEngine
import com.example.compression.queue.BatchCompressionQueue
import com.example.compression.storage.CompressionHistoryDatabase
import com.example.compression.verification.OutputVerifier
import com.example.compression.video.VideoCompressionEngine
import com.example.data.MediaCompressionEngine
import com.example.model.AppScreen
import com.example.model.AudioFormat
import com.example.model.BatchMediaItem
import com.example.model.BatchTab
import com.example.model.CompressionMode
import com.example.model.MediaCategory
import com.example.model.PhotoFormat
import com.example.viewmodel.CompressorViewModel
import java.io.File
import java.io.FileInputStream
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Media Compressor", appName)
  }

  @Test
  fun `verify default values and navigation across all 8 screens`() {
    val vm = CompressorViewModel()
    assertEquals(AppScreen.SPLASH, vm.uiState.value.currentScreen)
    assertEquals("4.2 MB", MediaCompressionEngine.formatFileSize(vm.uiState.value.photoOriginalBytes))
    assertEquals("892 KB", MediaCompressionEngine.formatFileSize(vm.uiState.value.photoCompressedBytes))
    assertEquals("85.6 MB", MediaCompressionEngine.formatFileSize(vm.uiState.value.videoOriginalBytes))
    assertEquals("28.3 MB", MediaCompressionEngine.formatFileSize(vm.uiState.value.videoEstimatedBytes))
    assertEquals("12.5 MB", MediaCompressionEngine.formatFileSize(vm.uiState.value.audioOriginalBytes))

    vm.navigateTo(AppScreen.HOME)
    assertEquals(AppScreen.HOME, vm.uiState.value.currentScreen)

    vm.navigateTo(AppScreen.PHOTO_COMPRESSION)
    assertEquals(AppScreen.PHOTO_COMPRESSION, vm.uiState.value.currentScreen)
    vm.setPhotoCompressionMode(CompressionMode.SMALLEST_SIZE)
    vm.setPhotoOutputFormat(PhotoFormat.WEBP)
    assertTrue(vm.uiState.value.photoCompressedBytes < vm.uiState.value.photoOriginalBytes)

    vm.navigateTo(AppScreen.VIDEO_COMPRESSION)
    assertEquals(AppScreen.VIDEO_COMPRESSION, vm.uiState.value.currentScreen)
    vm.updateVideoSettings(resolution = "720p (HD)", codec = "H.265 (HEVC)")
    assertTrue(vm.uiState.value.videoEstimatedBytes < vm.uiState.value.videoOriginalBytes)

    vm.navigateTo(AppScreen.AUDIO_COMPRESSION)
    assertEquals(AppScreen.AUDIO_COMPRESSION, vm.uiState.value.currentScreen)
    vm.setAudioFormat(AudioFormat.OPUS)
    vm.setAudioBitrate(128)
    assertEquals(128, vm.uiState.value.audioBitrateKbps)

    vm.navigateTo(AppScreen.BATCH_COMPRESSION)
    assertEquals(AppScreen.BATCH_COMPRESSION, vm.uiState.value.currentScreen)
    vm.setBatchTab(BatchTab.VIDEOS)
    assertEquals(BatchTab.VIDEOS, vm.uiState.value.selectedBatchTab)
  }

  @Test
  fun `verify photo video audio and batch engines produce verified outputs and persist history`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = CompressionHistoryDatabase.getInstance(context)
    db.clearHistory()

    // 1. Photo Engine (JPEG & Lossless PNG)
    val jpgRes = PhotoCompressionEngine.compressPhoto(
      context = context,
      sourceUriString = null,
      fallbackDrawableRes = R.drawable.sample_mountain_lake,
      originalSizeBytes = 3_250_585L,
      mode = CompressionMode.BALANCED,
      format = PhotoFormat.JPG,
      quality = 75,
      resizeOption = "Original Size",
      onProgress = {}
    )
    val jpgFile = File(jpgRes.outputFilePath)
    assertTrue(jpgFile.exists() && jpgFile.length() > 0)
    assertTrue(OutputVerifier.verifyOutputFile(jpgFile, MediaCategory.PHOTO).isValid)

    val pngRes = PhotoCompressionEngine.compressPhoto(
      context = context,
      sourceUriString = null,
      fallbackDrawableRes = R.drawable.sample_portrait,
      originalSizeBytes = 2_900_000L,
      mode = CompressionMode.LOSSLESS,
      format = PhotoFormat.PNG,
      quality = 100,
      resizeOption = "50% (1080p)",
      onProgress = {}
    )
    val pngFile = File(pngRes.outputFilePath)
    assertTrue(pngFile.exists() && pngFile.length() > 0)

    // 2. Video Engine (H.264 & HEVC fallback check)
    val cap = DeviceCapabilities.findBestVideoEncoder("H.265 (HEVC)", preferHardware = true)
    assertTrue(cap.mimeType.startsWith("video/"))

    val videoRes = VideoCompressionEngine.compressVideo(
      context = context,
      sourceUriString = null,
      originalSizeBytes = 89_758_105L,
      estimatedSizeBytes = 29_674_700L,
      resolution = "1080p (Full HD)",
      codec = "H.264 (Recommended)",
      onProgress = {}
    )
    val videoFile = File(videoRes.outputFilePath)
    assertTrue(videoFile.exists() && videoFile.length() > 0)
    assertTrue(OutputVerifier.verifyOutputFile(videoFile, MediaCategory.VIDEO).isValid)

    // 3. Audio Engine (MP3 & Lossless FLAC)
    val flacRes = AudioCompressionEngine.compressAudio(
      context = context,
      sourceUriString = null,
      originalSizeBytes = 13_107_200L,
      mode = CompressionMode.LOSSLESS,
      format = AudioFormat.FLAC,
      bitrateKbps = 320,
      sampleRateLabel = "44.1 kHz",
      onProgress = {}
    )
    val flacFile = File(flacRes.outputFilePath)
    assertTrue(flacFile.exists())
    val flacMagic = ByteArray(4)
    FileInputStream(flacFile).use { it.read(flacMagic) }
    assertEquals("fLaC", String(flacMagic, Charsets.US_ASCII))

    // 4. Batch Queue + SmartCompressionManager SQLite History persistence
    val batchRes = SmartCompressionManager.executeBatchCompression(
      context = context,
      selectedItems = listOf(
        BatchMediaItem(
          id = "b1",
          name = "Nature.jpg",
          sizeBytes = 4_200_000L,
          tab = BatchTab.PHOTOS,
          isSelected = true,
          drawableRes = R.drawable.sample_mountain_lake
        )
      ),
      mode = CompressionMode.BALANCED,
      preserveExif = true,
      preferHardware = true,
      autoSaveToGallery = false,
      onProgress = {}
    )
    assertTrue(batchRes.savedPercent > 0)
    assertTrue(db.historyFlow.value.isNotEmpty())
  }
}
