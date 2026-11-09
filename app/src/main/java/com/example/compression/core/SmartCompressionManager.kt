package com.example.compression.core

import android.content.Context
import com.example.compression.audio.AudioCompressionEngine
import com.example.compression.estimation.SmartSizeEstimator
import com.example.compression.photo.PhotoCompressionEngine
import com.example.compression.queue.BatchCompressionQueue
import com.example.compression.storage.CompressionHistoryDatabase
import com.example.compression.storage.MediaStorageManager
import com.example.compression.video.VideoCompressionEngine
import com.example.data.ProgressSnapshot
import com.example.model.AudioFormat
import com.example.model.BatchMediaItem
import com.example.model.CompressionHistoryEntry
import com.example.model.CompressionMode
import com.example.model.CompressionResult
import java.io.File

object SmartCompressionManager {

  fun initializeSession(context: Context) {
    MediaStorageManager.cleanupAbandonedTempFiles(context)
  }

  fun estimatePhotoSize(
    originalBytes: Long,
    mode: CompressionMode,
    format: com.example.model.PhotoFormat,
    quality: Int,
    resizeOption: String,
    isDefaultReferencePhoto: Boolean = false
  ): Long {
    return SmartSizeEstimator.estimatePhotoSize(
      originalBytes = originalBytes,
      mode = mode,
      format = format,
      quality = quality,
      resizeOption = resizeOption,
      isDefaultReferencePhoto = isDefaultReferencePhoto
    ).estimatedBytes
  }

  fun estimateVideoSize(
    originalBytes: Long,
    durationSec: Int,
    resolution: String,
    codec: String,
    bitrate: String,
    fps: String,
    audioQuality: String
  ): Long {
    return SmartSizeEstimator.estimateVideoSize(
      originalBytes = originalBytes,
      durationSec = durationSec,
      resolution = resolution,
      codec = codec,
      bitrate = bitrate,
      fps = fps,
      audioQuality = audioQuality
    ).estimatedBytes
  }

  fun estimateAudioSize(
    originalBytes: Long,
    durationSec: Int,
    mode: CompressionMode,
    format: AudioFormat,
    bitrateKbps: Int,
    sampleRate: String
  ): Long {
    return SmartSizeEstimator.estimateAudioSize(
      originalBytes = originalBytes,
      durationSec = durationSec,
      mode = mode,
      format = format,
      bitrateKbps = bitrateKbps,
      sampleRate = sampleRate
    ).estimatedBytes
  }

  suspend fun executePhotoCompression(
    context: Context,
    sourceUriString: String?,
    fallbackDrawableRes: Int,
    originalSizeBytes: Long,
    mode: CompressionMode,
    format: com.example.model.PhotoFormat,
    quality: Int,
    resizeOption: String,
    preserveExif: Boolean,
    autoSaveToGallery: Boolean,
    onProgress: suspend (ProgressSnapshot) -> Unit
  ): CompressionResult {
    val result = PhotoCompressionEngine.compressPhoto(
      context = context,
      sourceUriString = sourceUriString,
      fallbackDrawableRes = fallbackDrawableRes,
      originalSizeBytes = originalSizeBytes,
      mode = mode,
      format = format,
      quality = quality,
      resizeOption = resizeOption,
      preserveExif = preserveExif,
      onProgress = onProgress
    )
    finalizeAndRecordResult(
      context = context,
      result = result,
      modeLabel = mode.label,
      formatLabel = format.label,
      autoSaveToGallery = autoSaveToGallery
    )
    return result
  }

  suspend fun executeVideoCompression(
    context: Context,
    sourceUriString: String?,
    originalSizeBytes: Long,
    estimatedSizeBytes: Long,
    resolution: String,
    codec: String,
    bitrate: String,
    fps: String,
    audioQuality: String,
    preferHardware: Boolean,
    autoSaveToGallery: Boolean,
    onProgress: suspend (ProgressSnapshot) -> Unit
  ): CompressionResult {
    val result = VideoCompressionEngine.compressVideo(
      context = context,
      sourceUriString = sourceUriString,
      originalSizeBytes = originalSizeBytes,
      estimatedSizeBytes = estimatedSizeBytes,
      resolution = resolution,
      codec = codec,
      bitrate = bitrate,
      fps = fps,
      audioQuality = audioQuality,
      preferHardware = preferHardware,
      onProgress = onProgress
    )
    finalizeAndRecordResult(
      context = context,
      result = result,
      modeLabel = resolution,
      formatLabel = "MP4",
      autoSaveToGallery = autoSaveToGallery
    )
    return result
  }

  suspend fun executeAudioCompression(
    context: Context,
    sourceUriString: String?,
    originalSizeBytes: Long,
    mode: CompressionMode,
    format: AudioFormat,
    bitrateKbps: Int,
    sampleRateLabel: String,
    autoSaveToGallery: Boolean,
    onProgress: suspend (ProgressSnapshot) -> Unit
  ): CompressionResult {
    val result = AudioCompressionEngine.compressAudio(
      context = context,
      sourceUriString = sourceUriString,
      originalSizeBytes = originalSizeBytes,
      mode = mode,
      format = format,
      bitrateKbps = bitrateKbps,
      sampleRateLabel = sampleRateLabel,
      onProgress = onProgress
    )
    finalizeAndRecordResult(
      context = context,
      result = result,
      modeLabel = mode.label,
      formatLabel = format.label,
      autoSaveToGallery = autoSaveToGallery
    )
    return result
  }

  suspend fun executeBatchCompression(
    context: Context,
    selectedItems: List<BatchMediaItem>,
    mode: CompressionMode,
    preserveExif: Boolean,
    preferHardware: Boolean,
    autoSaveToGallery: Boolean,
    onProgress: suspend (ProgressSnapshot) -> Unit
  ): CompressionResult {
    val summary = BatchCompressionQueue.processBatch(
      context = context,
      items = selectedItems,
      mode = mode,
      preserveExif = preserveExif,
      preferHardware = preferHardware,
      onProgress = onProgress
    )
    finalizeAndRecordResult(
      context = context,
      result = summary.primaryResult,
      modeLabel = mode.label,
      formatLabel = "BATCH",
      autoSaveToGallery = autoSaveToGallery
    )
    return summary.primaryResult
  }

  private suspend fun finalizeAndRecordResult(
    context: Context,
    result: CompressionResult,
    modeLabel: String,
    formatLabel: String,
    autoSaveToGallery: Boolean
  ) {
    if (autoSaveToGallery && result.outputFilePath.isNotBlank()) {
      MediaStorageManager.exportToMediaStoreIfEnabled(
        context = context,
        sourceFile = File(result.outputFilePath),
        mimeType = result.mimeType,
        category = result.category
      )
    }

    val db = CompressionHistoryDatabase.getInstance(context)
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
    db.insertEntry(
      entry = entry,
      modeLabel = modeLabel,
      formatLabel = formatLabel,
      status = "COMPLETED"
    )
  }
}
