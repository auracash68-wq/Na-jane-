package com.example.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.compression.core.SmartCompressionManager
import com.example.compression.metadata.MediaMetadataReader
import com.example.model.AudioFormat
import com.example.model.BatchMediaItem
import com.example.model.CompressionMode
import com.example.model.CompressionResult
import com.example.model.PhotoFormat
import java.io.File
import java.util.Locale
import kotlin.math.roundToInt

data class ProgressSnapshot(
  val percent: Int,
  val processedBytes: Long,
  val totalBytes: Long,
  val speedMbPerSec: Float,
  val remainingSeconds: Int
)

object MediaCompressionEngine {

  fun formatFileSize(bytes: Long): String {
    if (bytes <= 0L) return "0 KB"
    val mb = bytes.toDouble() / (1024.0 * 1024.0)
    return if (mb >= 1.0) {
      String.format(Locale.US, "%.1f MB", mb)
    } else {
      val kb = (bytes.toDouble() / 1024.0).roundToInt().coerceAtLeast(1)
      "$kb KB"
    }
  }

  fun formatDurationSeconds(totalSeconds: Int): String {
    val mins = (totalSeconds / 60).coerceAtLeast(0)
    val secs = (totalSeconds % 60).coerceAtLeast(0)
    return String.format(Locale.US, "%02d:%02d", mins, secs)
  }

  fun formatHoursMinutesSeconds(totalSeconds: Int): String {
    val hrs = (totalSeconds / 3600).coerceAtLeast(0)
    val mins = ((totalSeconds % 3600) / 60).coerceAtLeast(0)
    val secs = (totalSeconds % 60).coerceAtLeast(0)
    return String.format(Locale.US, "%02d:%02d:%02d", hrs, mins, secs)
  }

  fun queryUriFileInfo(context: Context, uri: Uri): Pair<String, Long> {
    return MediaMetadataReader.queryDisplayNameAndSize(context, uri)
  }

  fun queryMediaDurationSeconds(context: Context, uri: Uri, defaultSec: Int): Int {
    val videoMeta = MediaMetadataReader.readVideoMetadata(context, uri)
    val sec = (videoMeta.durationMs / 1000L).toInt()
    return if (sec > 0) sec else defaultSec
  }

  fun estimatePhotoCompressedBytes(
    originalBytes: Long,
    mode: CompressionMode,
    format: PhotoFormat,
    quality: Int,
    resizeOption: String,
    isDefaultReferencePhoto: Boolean = false
  ): Long {
    return SmartCompressionManager.estimatePhotoSize(
      originalBytes = originalBytes,
      mode = mode,
      format = format,
      quality = quality,
      resizeOption = resizeOption,
      isDefaultReferencePhoto = isDefaultReferencePhoto
    )
  }

  fun estimateVideoCompressedBytes(
    originalBytes: Long,
    resolution: String,
    codec: String,
    bitrate: String,
    fps: String,
    audioQuality: String
  ): Long {
    return SmartCompressionManager.estimateVideoSize(
      originalBytes = originalBytes,
      durationSec = 30,
      resolution = resolution,
      codec = codec,
      bitrate = bitrate,
      fps = fps,
      audioQuality = audioQuality
    )
  }

  fun estimateAudioCompressedBytes(
    originalBytes: Long,
    mode: CompressionMode,
    format: AudioFormat,
    bitrateKbps: Int,
    sampleRate: String
  ): Long {
    return SmartCompressionManager.estimateAudioSize(
      originalBytes = originalBytes,
      durationSec = 204,
      mode = mode,
      format = format,
      bitrateKbps = bitrateKbps,
      sampleRate = sampleRate
    )
  }

  suspend fun compressPhotoReal(
    context: Context,
    sourceUriString: String?,
    fallbackDrawableRes: Int,
    originalSizeBytes: Long,
    mode: CompressionMode,
    format: PhotoFormat,
    quality: Int,
    resizeOption: String,
    preserveExif: Boolean = true,
    autoSaveToGallery: Boolean = true,
    onProgress: suspend (ProgressSnapshot) -> Unit
  ): CompressionResult {
    return SmartCompressionManager.executePhotoCompression(
      context = context,
      sourceUriString = sourceUriString,
      fallbackDrawableRes = fallbackDrawableRes,
      originalSizeBytes = originalSizeBytes,
      mode = mode,
      format = format,
      quality = quality,
      resizeOption = resizeOption,
      preserveExif = preserveExif,
      autoSaveToGallery = autoSaveToGallery,
      onProgress = onProgress
    )
  }

  suspend fun compressVideoReal(
    context: Context,
    sourceUriString: String?,
    originalSizeBytes: Long,
    estimatedSizeBytes: Long,
    resolution: String,
    codec: String,
    bitrate: String = "Auto (Balanced)",
    fps: String = "Auto",
    audioQuality: String = "High",
    preferHardware: Boolean = true,
    autoSaveToGallery: Boolean = true,
    onProgress: suspend (ProgressSnapshot) -> Unit
  ): CompressionResult {
    return SmartCompressionManager.executeVideoCompression(
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
      autoSaveToGallery = autoSaveToGallery,
      onProgress = onProgress
    )
  }

  suspend fun compressAudioReal(
    context: Context,
    sourceUriString: String?,
    originalSizeBytes: Long,
    mode: CompressionMode,
    format: AudioFormat,
    bitrateKbps: Int,
    sampleRateLabel: String,
    autoSaveToGallery: Boolean = true,
    onProgress: suspend (ProgressSnapshot) -> Unit
  ): CompressionResult {
    return SmartCompressionManager.executeAudioCompression(
      context = context,
      sourceUriString = sourceUriString,
      originalSizeBytes = originalSizeBytes,
      mode = mode,
      format = format,
      bitrateKbps = bitrateKbps,
      sampleRateLabel = sampleRateLabel,
      autoSaveToGallery = autoSaveToGallery,
      onProgress = onProgress
    )
  }

  suspend fun compressBatchReal(
    context: Context,
    selectedItems: List<BatchMediaItem>,
    mode: CompressionMode,
    preserveExif: Boolean = true,
    preferHardware: Boolean = true,
    autoSaveToGallery: Boolean = true,
    onProgress: suspend (ProgressSnapshot) -> Unit
  ): CompressionResult {
    return SmartCompressionManager.executeBatchCompression(
      context = context,
      selectedItems = selectedItems,
      mode = mode,
      preserveExif = preserveExif,
      preferHardware = preferHardware,
      autoSaveToGallery = autoSaveToGallery,
      onProgress = onProgress
    )
  }

  fun openCompressedFile(context: Context, filePath: String, mimeType: String) {
    try {
      val file = File(filePath)
      if (!file.exists()) {
        Toast.makeText(context, "Compressed file ready at: ${file.name}", Toast.LENGTH_SHORT).show()
        return
      }
      val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
      val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mimeType)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      val chooser = Intent.createChooser(intent, "Open Compressed File").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(chooser)
    } catch (_: Exception) {
      Toast.makeText(context, "Saved to: ${File(filePath).name}", Toast.LENGTH_SHORT).show()
    }
  }

  fun shareCompressedFile(context: Context, filePath: String, mimeType: String) {
    try {
      val file = File(filePath)
      if (!file.exists()) {
        Toast.makeText(context, "File not found to share", Toast.LENGTH_SHORT).show()
        return
      }
      val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
      val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, "Compressed with Media Compressor (${formatFileSize(file.length())})")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }
      val chooser = Intent.createChooser(shareIntent, "Share Compressed Media").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(chooser)
    } catch (e: Exception) {
      Toast.makeText(context, "Unable to launch Share sheet: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
  }
}
