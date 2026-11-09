package com.example.compression.presets

import com.example.model.AudioFormat
import com.example.model.CompressionMode
import com.example.model.PhotoFormat

data class PhotoEncodingConfig(
  val mode: CompressionMode,
  val format: PhotoFormat,
  val quality: Int,
  val isTrulyLossless: Boolean,
  val scaleFactor: Float,
  val targetWidth: Int?,
  val targetHeight: Int?,
  val preserveExif: Boolean,
  val preserveAlpha: Boolean
)

data class VideoEncodingConfig(
  val mode: CompressionMode,
  val resolutionLabel: String,
  val targetWidth: Int,
  val targetHeight: Int,
  val codecLabel: String,
  val videoBitrateBps: Int,
  val targetFps: Int,
  val audioBitrateBps: Int,
  val isNoRecompressionPassThrough: Boolean
)

data class AudioEncodingConfig(
  val mode: CompressionMode,
  val format: AudioFormat,
  val bitrateKbps: Int,
  val sampleRateHz: Int,
  val channels: Int,
  val isTrulyLossless: Boolean
)

object CompressionPresets {

  fun buildPhotoConfig(
    mode: CompressionMode,
    format: PhotoFormat,
    sliderQuality: Int,
    resizeOption: String,
    originalWidth: Int,
    originalHeight: Int,
    preserveExif: Boolean,
    hasAlpha: Boolean
  ): PhotoEncodingConfig {
    // Never claim lossy compression is lossless:
    // PNG is always lossless; WebP in LOSSLESS mode uses WEBP_LOSSLESS.
    val isTrulyLossless = format == PhotoFormat.PNG ||
      (mode == CompressionMode.LOSSLESS && (format == PhotoFormat.WEBP || format == PhotoFormat.AVIF))

    val effectiveQuality = when {
      format == PhotoFormat.PNG -> 100 // PNG is lossless deflate, quality slider does not degrade pixels
      mode == CompressionMode.LOSSLESS -> 100
      mode == CompressionMode.HIGH_QUALITY -> maxOf(sliderQuality, 88)
      mode == CompressionMode.SMALLEST_SIZE -> minOf(sliderQuality, 55)
      else -> sliderQuality.coerceIn(10, 100)
    }

    val scaleFactor = when {
      resizeOption.contains("75%") -> 0.75f
      resizeOption.contains("50%") -> 0.50f
      resizeOption.contains("25%") -> 0.25f
      else -> 1.0f // Never silently reduce image resolution
    }

    val targetW = (originalWidth * scaleFactor).toInt().coerceAtLeast(1)
    val targetH = (originalHeight * scaleFactor).toInt().coerceAtLeast(1)

    return PhotoEncodingConfig(
      mode = mode,
      format = format,
      quality = effectiveQuality,
      isTrulyLossless = isTrulyLossless,
      scaleFactor = scaleFactor,
      targetWidth = if (scaleFactor < 1.0f) targetW else null,
      targetHeight = if (scaleFactor < 1.0f) targetH else null,
      preserveExif = preserveExif,
      preserveAlpha = hasAlpha && format != PhotoFormat.JPG
    )
  }

  fun buildVideoConfig(
    resolutionLabel: String,
    codecLabel: String,
    bitrateLabel: String,
    fpsLabel: String,
    audioQualityLabel: String,
    originalWidth: Int,
    originalHeight: Int,
    originalFps: Float,
    originalBitrateBps: Long
  ): VideoEncodingConfig {
    val isLandscape = originalWidth >= originalHeight
    val (targetW, targetH) = when {
      resolutionLabel.startsWith("4K") -> if (isLandscape) 3840 to 2160 else 2160 to 3840
      resolutionLabel.startsWith("2K") -> if (isLandscape) 2560 to 1440 else 1440 to 2560
      resolutionLabel.startsWith("1080p") -> if (isLandscape) 1920 to 1080 else 1080 to 1920
      resolutionLabel.startsWith("720p") -> if (isLandscape) 1280 to 720 else 720 to 1280
      resolutionLabel.startsWith("480p") -> if (isLandscape) 854 to 480 else 480 to 854
      else -> originalWidth to originalHeight
    }

    // Preserve aspect ratio accurately
    val aspect = originalWidth.toFloat() / originalHeight.toFloat().coerceAtLeast(1f)
    val adjustedW = (targetW / 2) * 2
    val adjustedH = ((adjustedW / aspect).toInt() / 2) * 2

    val baseBitrate = when {
      adjustedW >= 3840 -> 14_000_000
      adjustedW >= 2560 -> 8_000_000
      adjustedW >= 1920 -> 4_500_000
      adjustedW >= 1280 -> 2_400_000
      else -> 1_200_000
    }

    val codecEfficiency = when {
      codecLabel.contains("H.265") || codecLabel.contains("HEVC") -> 0.72f
      codecLabel.contains("AV1") || codecLabel.contains("VP9") -> 0.75f
      else -> 1.0f
    }

    val bitrateMultiplier = when {
      bitrateLabel.contains("Low", ignoreCase = true) -> 0.65f
      bitrateLabel.contains("High", ignoreCase = true) -> 1.35f
      else -> 1.0f
    }

    val targetVideoBitrate = (baseBitrate * codecEfficiency * bitrateMultiplier)
      .toInt()
      .coerceAtMost(originalBitrateBps.toInt().coerceAtLeast(1_500_000))

    val targetFps = when {
      fpsLabel.startsWith("60") -> minOf(60, originalFps.toInt().coerceAtLeast(30))
      fpsLabel.startsWith("30") -> minOf(30, originalFps.toInt().coerceAtLeast(24))
      fpsLabel.startsWith("24") -> 24
      else -> originalFps.toInt().coerceIn(24, 60) // Avoid unnecessary FPS reduction
    }

    val audioBps = when (audioQualityLabel) {
      "Low" -> 96_000
      "Medium" -> 128_000
      else -> 192_000
    }

    return VideoEncodingConfig(
      mode = CompressionMode.BALANCED,
      resolutionLabel = resolutionLabel,
      targetWidth = adjustedW.coerceAtLeast(320),
      targetHeight = adjustedH.coerceAtLeast(240),
      codecLabel = codecLabel,
      videoBitrateBps = targetVideoBitrate,
      targetFps = targetFps,
      audioBitrateBps = audioBps,
      isNoRecompressionPassThrough = false
    )
  }

  fun buildAudioConfig(
    mode: CompressionMode,
    format: AudioFormat,
    bitrateKbps: Int,
    sampleRateDropdown: String,
    originalSampleRateHz: Int,
    originalChannels: Int
  ): AudioEncodingConfig {
    // FLAC is truly lossless; MP3, AAC, Opus are lossy perceptual codecs
    val isTrulyLossless = format == AudioFormat.FLAC

    val targetSampleRate = when {
      sampleRateDropdown.startsWith("48") -> 48000
      sampleRateDropdown.startsWith("44.1") -> 44100
      sampleRateDropdown.startsWith("22.05") -> 22050
      else -> originalSampleRateHz.coerceIn(22050, 48000) // Avoid unnecessary sample-rate conversion
    }

    val effectiveBitrate = when {
      format == AudioFormat.FLAC -> 700 // Lossless FLAC typical bitrate
      mode == CompressionMode.LOSSLESS -> 320
      mode == CompressionMode.HIGH_QUALITY -> maxOf(bitrateKbps, 256)
      mode == CompressionMode.SMALLEST_SIZE -> minOf(bitrateKbps, 96)
      else -> bitrateKbps.coerceIn(64, 320)
    }

    return AudioEncodingConfig(
      mode = mode,
      format = format,
      bitrateKbps = effectiveBitrate,
      sampleRateHz = targetSampleRate,
      channels = originalChannels.coerceIn(1, 2), // Preserve stereo by default
      isTrulyLossless = isTrulyLossless
    )
  }
}
