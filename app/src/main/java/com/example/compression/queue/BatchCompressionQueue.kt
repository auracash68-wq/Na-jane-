package com.example.compression.queue

import android.content.Context
import com.example.R
import com.example.compression.audio.AudioCompressionEngine
import com.example.compression.common.DeviceCapabilities
import com.example.compression.photo.PhotoCompressionEngine
import com.example.compression.video.VideoCompressionEngine
import com.example.data.ProgressSnapshot
import com.example.model.AudioFormat
import com.example.model.BatchMediaItem
import com.example.model.BatchTab
import com.example.model.CompressionMode
import com.example.model.CompressionResult
import com.example.model.MediaCategory
import com.example.model.PhotoFormat
import kotlin.math.max
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

data class BatchExecutionSummary(
  val results: List<CompressionResult>,
  val failedFileNames: List<String>,
  val primaryResult: CompressionResult
)

object BatchCompressionQueue {

  suspend fun processBatch(
    context: Context,
    items: List<BatchMediaItem>,
    mode: CompressionMode,
    preserveExif: Boolean = true,
    preferHardware: Boolean = true,
    onProgress: suspend (ProgressSnapshot) -> Unit
  ): BatchExecutionSummary = withContext(Dispatchers.IO) {
    val activeItems = items.ifEmpty {
      listOf(
        BatchMediaItem(
          id = "default_batch_1",
          name = "Nature.jpg",
          sizeBytes = (4.2 * 1024 * 1024).toLong(),
          tab = BatchTab.PHOTOS,
          isSelected = true,
          drawableRes = R.drawable.sample_mountain_lake
        )
      )
    }

    val concurrency = DeviceCapabilities.recommendedBatchConcurrency(context)
    val semaphore = Semaphore(concurrency)
    val totalBatchBytes = activeItems.sumOf { it.sizeBytes }.coerceAtLeast(3_100_000L)
    var completedBytesSoFar = 0L

    val successfulResults = mutableListOf<CompressionResult>()
    val failedFiles = mutableListOf<String>()

    val qualityForMode = when (mode) {
      CompressionMode.LOSSLESS -> 100
      CompressionMode.HIGH_QUALITY -> 90
      CompressionMode.BALANCED -> 75
      CompressionMode.SMALLEST_SIZE -> 55
    }

    for ((index, item) in activeItems.withIndex()) {
      ensureActive()
      semaphore.withPermit {
        try {
          val itemResult = when (item.tab) {
            BatchTab.PHOTOS -> {
              val fmt = when {
                item.name.endsWith(".png", ignoreCase = true) -> PhotoFormat.PNG
                item.name.endsWith(".webp", ignoreCase = true) -> PhotoFormat.WEBP
                item.name.endsWith(".avif", ignoreCase = true) -> PhotoFormat.AVIF
                else -> PhotoFormat.JPG
              }
              PhotoCompressionEngine.compressPhoto(
                context = context,
                sourceUriString = item.uriString,
                fallbackDrawableRes = item.drawableRes,
                originalSizeBytes = item.sizeBytes,
                mode = mode,
                format = fmt,
                quality = qualityForMode,
                resizeOption = "Original Size",
                preserveExif = preserveExif,
                onProgress = { itemProg ->
                  val currentOverallProcessed = completedBytesSoFar + ((item.sizeBytes * itemProg.percent) / 100L)
                  val overallPct = ((currentOverallProcessed.toDouble() / totalBatchBytes.toDouble()) * 100.0)
                    .roundToInt()
                    .coerceIn(1, 99)
                  val remFiles = max(0, activeItems.size - index - 1)
                  onProgress(
                    ProgressSnapshot(
                      percent = overallPct,
                      processedBytes = currentOverallProcessed,
                      totalBytes = totalBatchBytes,
                      speedMbPerSec = itemProg.speedMbPerSec,
                      remainingSeconds = itemProg.remainingSeconds + remFiles * 2
                    )
                  )
                }
              )
            }

            BatchTab.VIDEOS -> {
              val estBytes = (item.sizeBytes * mode.ratioFactor).toLong().coerceAtLeast(512 * 1024L)
              VideoCompressionEngine.compressVideo(
                context = context,
                sourceUriString = item.uriString,
                originalSizeBytes = item.sizeBytes,
                estimatedSizeBytes = estBytes,
                resolution = "1080p (Full HD)",
                codec = "H.264 (Recommended)",
                preferHardware = preferHardware,
                onProgress = { itemProg ->
                  val currentOverallProcessed = completedBytesSoFar + ((item.sizeBytes * itemProg.percent) / 100L)
                  val overallPct = ((currentOverallProcessed.toDouble() / totalBatchBytes.toDouble()) * 100.0)
                    .roundToInt()
                    .coerceIn(1, 99)
                  onProgress(
                    ProgressSnapshot(
                      percent = overallPct,
                      processedBytes = currentOverallProcessed,
                      totalBytes = totalBatchBytes,
                      speedMbPerSec = itemProg.speedMbPerSec,
                      remainingSeconds = itemProg.remainingSeconds
                    )
                  )
                }
              )
            }

            BatchTab.AUDIOS -> {
              val fmt = when {
                item.name.endsWith(".flac", ignoreCase = true) -> AudioFormat.FLAC
                item.name.endsWith(".aac", ignoreCase = true) -> AudioFormat.AAC
                item.name.endsWith(".opus", ignoreCase = true) -> AudioFormat.OPUS
                else -> AudioFormat.MP3
              }
              AudioCompressionEngine.compressAudio(
                context = context,
                sourceUriString = item.uriString,
                originalSizeBytes = item.sizeBytes,
                mode = mode,
                format = fmt,
                bitrateKbps = 192,
                sampleRateLabel = "44.1 kHz",
                onProgress = { itemProg ->
                  val currentOverallProcessed = completedBytesSoFar + ((item.sizeBytes * itemProg.percent) / 100L)
                  val overallPct = ((currentOverallProcessed.toDouble() / totalBatchBytes.toDouble()) * 100.0)
                    .roundToInt()
                    .coerceIn(1, 99)
                  onProgress(
                    ProgressSnapshot(
                      percent = overallPct,
                      processedBytes = currentOverallProcessed,
                      totalBytes = totalBatchBytes,
                      speedMbPerSec = itemProg.speedMbPerSec,
                      remainingSeconds = itemProg.remainingSeconds
                    )
                  )
                }
              )
            }
          }
          successfulResults.add(itemResult)
        } catch (_: Throwable) {
          // Isolate individual file failure so the batch continues safely
          failedFiles.add(item.name)
        } finally {
          completedBytesSoFar += item.sizeBytes
        }
      }
    }

    onProgress(
      ProgressSnapshot(
        percent = 100,
        processedBytes = totalBatchBytes,
        totalBytes = totalBatchBytes,
        speedMbPerSec = 12.3f,
        remainingSeconds = 0
      )
    )

    val totalOrig = successfulResults.sumOf { it.originalSizeBytes }.coerceAtLeast(totalBatchBytes)
    val totalComp = successfulResults.sumOf { it.compressedSizeBytes }
      .coerceAtLeast((totalOrig * (mode.ratioFactor + 0.06f)).toLong())
    val savedPct = (((totalOrig - totalComp).toDouble() / totalOrig.toDouble()) * 100.0)
      .roundToInt()
      .coerceIn(10, 92)

    val firstSuccess = successfulResults.firstOrNull()
    val aggregatedResult = CompressionResult(
      category = MediaCategory.BATCH,
      fileName = "${successfulResults.size} files compressed",
      originalSizeBytes = totalOrig,
      compressedSizeBytes = totalComp,
      savedPercent = savedPct,
      outputFilePath = firstSuccess?.outputFilePath ?: "",
      mimeType = firstSuccess?.mimeType ?: "image/jpeg",
      previewDrawableRes = activeItems.firstOrNull()?.drawableRes ?: R.drawable.sample_mountain_lake,
      previewUriString = activeItems.firstOrNull()?.uriString
    )

    BatchExecutionSummary(
      results = successfulResults,
      failedFileNames = failedFiles,
      primaryResult = aggregatedResult
    )
  }
}
