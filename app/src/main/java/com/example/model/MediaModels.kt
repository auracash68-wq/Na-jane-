package com.example.model

import com.example.R

enum class AppScreen {
  SPLASH,
  HOME,
  PHOTO_COMPRESSION,
  VIDEO_COMPRESSION,
  AUDIO_COMPRESSION,
  BATCH_COMPRESSION,
  PROCESSING,
  COMPLETE
}

enum class MediaCategory(val label: String) {
  PHOTO("Photo"),
  VIDEO("Video"),
  AUDIO("Audio"),
  BATCH("Batch")
}

enum class CompressionMode(val label: String, val shortLabel: String, val ratioFactor: Float) {
  LOSSLESS("Lossless", "Lossless", 0.72f),
  HIGH_QUALITY("High Quality", "High Quality", 0.45f),
  BALANCED("Balanced", "Balanced", 0.21f),
  SMALLEST_SIZE("Smallest Size", "Small Size", 0.14f)
}

enum class PhotoFormat(val label: String, val extension: String, val mimeType: String, val sizeMultiplier: Float) {
  JPG("JPG", "jpg", "image/jpeg", 1.0f),
  WEBP("WebP", "webp", "image/webp", 0.82f),
  AVIF("AVIF", "avif", "image/avif", 0.74f),
  PNG("PNG", "png", "image/png", 1.45f)
}

enum class AudioFormat(val label: String, val extension: String, val mimeType: String, val factor: Float) {
  MP3("MP3", "mp3", "audio/mpeg", 1.0f),
  AAC("AAC", "aac", "audio/aac", 0.88f),
  OPUS("Opus", "opus", "audio/opus", 0.75f),
  FLAC("FLAC", "flac", "audio/flac", 1.65f)
}

enum class BatchTab(val title: String) {
  PHOTOS("Photos"),
  VIDEOS("Videos"),
  AUDIOS("Audios")
}

data class BatchMediaItem(
  val id: String,
  val name: String,
  val sizeBytes: Long,
  val tab: BatchTab,
  val isSelected: Boolean = true,
  val drawableRes: Int = R.drawable.sample_mountain_lake,
  val uriString: String? = null
)

data class CompressionHistoryEntry(
  val id: String,
  val fileName: String,
  val category: MediaCategory,
  val originalBytes: Long,
  val compressedBytes: Long,
  val savedPercent: Int,
  val outputFilePath: String,
  val timestampMillis: Long
)

data class CompressionResult(
  val category: MediaCategory,
  val fileName: String,
  val originalSizeBytes: Long,
  val compressedSizeBytes: Long,
  val savedPercent: Int,
  val outputFilePath: String,
  val mimeType: String,
  val previewDrawableRes: Int = R.drawable.sample_mountain_lake,
  val previewUriString: String? = null
)
