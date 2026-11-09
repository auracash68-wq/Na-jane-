package com.example

import com.example.compression.audio.AudioCompressionRepository
import com.example.compression.estimation.SmartSizeEstimator
import com.example.compression.metadata.MediaMetadataReader
import com.example.compression.photo.PhotoCompressionRepository
import com.example.compression.presets.CompressionPresets
import com.example.compression.verification.OutputVerifier
import com.example.compression.video.VideoCompressionRepository
import com.example.data.MediaCompressionEngine
import com.example.model.AppScreen
import com.example.model.AudioFormat
import com.example.model.BatchTab
import com.example.model.CompressionMode
import com.example.model.MediaCategory
import com.example.model.PhotoFormat
import com.example.viewmodel.CompressorViewModel
import java.io.ByteArrayInputStream
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun verifyPhotoPresetsAndLosslessRules() {
    val pngCfg = CompressionPresets.buildPhotoConfig(
      mode = CompressionMode.BALANCED,
      format = PhotoFormat.PNG,
      sliderQuality = 60,
      resizeOption = "Original Size",
      originalWidth = 1920,
      originalHeight = 1080,
      preserveExif = true,
      hasAlpha = true
    )
    assertTrue(pngCfg.isTrulyLossless)
    assertEquals(100, pngCfg.quality)
    assertTrue(pngCfg.preserveAlpha)

    val jpgCfg = CompressionPresets.buildPhotoConfig(
      mode = CompressionMode.LOSSLESS,
      format = PhotoFormat.JPG,
      sliderQuality = 75,
      resizeOption = "50% (1080p)",
      originalWidth = 2000,
      originalHeight = 1000,
      preserveExif = true,
      hasAlpha = true
    )
    assertFalse(jpgCfg.isTrulyLossless)
    assertFalse(jpgCfg.preserveAlpha)
    assertEquals(1000, jpgCfg.targetWidth)
    assertEquals(500, jpgCfg.targetHeight)
  }

  @Test
  fun verifyVideoAndAudioPresetsAndSizeEstimation() {
    val videoCfg = CompressionPresets.buildVideoConfig(
      resolutionLabel = "1080p (Full HD)",
      codecLabel = "H.265 (HEVC)",
      bitrateLabel = "Auto (Balanced)",
      fpsLabel = "30 FPS",
      audioQualityLabel = "High",
      originalWidth = 3840,
      originalHeight = 2160,
      originalFps = 60f,
      originalBitrateBps = 25_000_000L
    )
    assertEquals(1920, videoCfg.targetWidth)
    assertEquals(1080, videoCfg.targetHeight)
    assertEquals(30, videoCfg.targetFps)

    val flacCfg = CompressionPresets.buildAudioConfig(
      mode = CompressionMode.LOSSLESS,
      format = AudioFormat.FLAC,
      bitrateKbps = 192,
      sampleRateDropdown = "Auto",
      originalSampleRateHz = 44100,
      originalChannels = 2
    )
    assertTrue(flacCfg.isTrulyLossless)
    assertEquals(44100, flacCfg.sampleRateHz)
    assertEquals(2, flacCfg.channels)

    val mp3Cfg = CompressionPresets.buildAudioConfig(
      mode = CompressionMode.BALANCED,
      format = AudioFormat.MP3,
      bitrateKbps = 192,
      sampleRateDropdown = "48.0 kHz",
      originalSampleRateHz = 44100,
      originalChannels = 2
    )
    assertFalse(mp3Cfg.isTrulyLossless)
    assertEquals(48000, mp3Cfg.sampleRateHz)

    val photoEst = SmartSizeEstimator.estimatePhotoSize(
      originalBytes = 4_404_019L,
      mode = CompressionMode.BALANCED,
      format = PhotoFormat.JPG,
      quality = 75,
      resizeOption = "Original Size",
      isDefaultReferencePhoto = true
    )
    assertEquals("892 KB", MediaCompressionEngine.formatFileSize(photoEst.estimatedBytes))
  }

  @Test
  fun verifyIndependentRepositoriesAndOutputVerifier() {
    val photoRepo = PhotoCompressionRepository()
    val videoRepo = VideoCompressionRepository()
    val audioRepo = AudioCompressionRepository()
    assertNotNull(photoRepo)
    assertNotNull(videoRepo)
    assertNotNull(audioRepo)

    val tempVideo = File.createTempFile("test_vid_", ".mp4")
    try {
      val ftyp = byteArrayOf(
        0x00, 0x00, 0x00, 0x18,
        'f'.code.toByte(), 't'.code.toByte(), 'y'.code.toByte(), 'p'.code.toByte(),
        'm'.code.toByte(), 'p'.code.toByte(), '4'.code.toByte(), '2'.code.toByte()
      )
      tempVideo.writeBytes(ftyp)
      val report = OutputVerifier.verifyOutputFile(tempVideo, MediaCategory.VIDEO)
      assertTrue(report.isValid)
    } finally {
      tempVideo.delete()
    }

    val orientation = MediaMetadataReader.readJpegExifOrientationDegrees(ByteArrayInputStream(ByteArray(4)))
    assertEquals(0, orientation)

    val vm = CompressorViewModel()
    assertEquals(AppScreen.SPLASH, vm.uiState.value.currentScreen)
    vm.navigateTo(AppScreen.BATCH_COMPRESSION)
    vm.setBatchTab(BatchTab.AUDIOS)
    assertEquals(BatchTab.AUDIOS, vm.uiState.value.selectedBatchTab)
  }
}
