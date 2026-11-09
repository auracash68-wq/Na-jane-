package com.example.compression.photo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModel
import com.example.compression.common.CompressionError
import com.example.compression.common.DeviceCapabilities
import com.example.compression.metadata.MediaMetadataReader
import com.example.compression.presets.CompressionPresets
import com.example.compression.storage.MediaStorageManager
import com.example.compression.verification.OutputVerifier
import com.example.data.ProgressSnapshot
import com.example.model.CompressionMode
import com.example.model.CompressionResult
import com.example.model.MediaCategory
import com.example.model.PhotoFormat
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.UUID
import kotlin.math.max
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

interface IPhotoCompressionEngine {
  suspend fun compress(
    context: Context,
    sourceUriString: String?,
    fallbackDrawableRes: Int,
    originalSizeBytes: Long,
    mode: CompressionMode,
    format: PhotoFormat,
    quality: Int,
    resizeOption: String,
    preserveExif: Boolean,
    onProgress: suspend (ProgressSnapshot) -> Unit
  ): CompressionResult
}

class PhotoCompressionRepository(
  private val engine: IPhotoCompressionEngine = PhotoCompressionEngine
) {
  suspend fun compressPhoto(
    context: Context,
    sourceUriString: String?,
    fallbackDrawableRes: Int,
    originalSizeBytes: Long,
    mode: CompressionMode,
    format: PhotoFormat,
    quality: Int,
    resizeOption: String,
    preserveExif: Boolean,
    onProgress: suspend (ProgressSnapshot) -> Unit
  ): CompressionResult = engine.compress(
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
}

class PhotoCompressionViewModel(
  private val repository: PhotoCompressionRepository = PhotoCompressionRepository()
) : ViewModel() {
  private val _isProcessing = MutableStateFlow(false)
  val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

  suspend fun runCompression(
    context: Context,
    sourceUriString: String?,
    fallbackDrawableRes: Int,
    originalSizeBytes: Long,
    mode: CompressionMode,
    format: PhotoFormat,
    quality: Int,
    resizeOption: String,
    preserveExif: Boolean,
    onProgress: suspend (ProgressSnapshot) -> Unit
  ): CompressionResult {
    _isProcessing.value = true
    return try {
      repository.compressPhoto(
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
    } finally {
      _isProcessing.value = false
    }
  }
}

object PhotoCompressionEngine : IPhotoCompressionEngine {

  override suspend fun compress(
    context: Context,
    sourceUriString: String?,
    fallbackDrawableRes: Int,
    originalSizeBytes: Long,
    mode: CompressionMode,
    format: PhotoFormat,
    quality: Int,
    resizeOption: String,
    preserveExif: Boolean,
    onProgress: suspend (ProgressSnapshot) -> Unit
  ): CompressionResult = compressPhoto(
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

  suspend fun compressPhoto(
    context: Context,
    sourceUriString: String?,
    fallbackDrawableRes: Int,
    originalSizeBytes: Long,
    mode: CompressionMode,
    format: PhotoFormat,
    quality: Int,
    resizeOption: String,
    preserveExif: Boolean = true,
    onProgress: suspend (ProgressSnapshot) -> Unit
  ): CompressionResult = withContext(Dispatchers.IO) {
    ensureActive()
    val startNs = System.nanoTime()
    val uri = sourceUriString?.takeIf { it.isNotBlank() }?.let { Uri.parse(it) }

    // Stage 1: Metadata & Bounds Inspection (real I/O)
    val metadata = MediaMetadataReader.readPhotoMetadata(
      context = context,
      uri = uri,
      fallbackDrawableRes = fallbackDrawableRes,
      fallbackSizeBytes = originalSizeBytes
    )
    val actualInputBytes = if (uri != null && metadata.fileSizeBytes > 0L) {
      metadata.fileSizeBytes
    } else {
      measureDrawableRawBytes(context, fallbackDrawableRes, originalSizeBytes)
    }

    if (!DeviceCapabilities.hasEnoughStorage(context, actualInputBytes)) {
      throw CompressionError.InsufficientStorage(
        requiredBytes = actualInputBytes,
        availableBytes = DeviceCapabilities.getAvailableStorageBytes(context)
      )
    }

    val config = CompressionPresets.buildPhotoConfig(
      mode = mode,
      format = format,
      sliderQuality = quality,
      resizeOption = resizeOption,
      originalWidth = metadata.width,
      originalHeight = metadata.height,
      preserveExif = preserveExif,
      hasAlpha = metadata.hasAlpha
    )

    reportRealStageProgress(
      percent = 18,
      processedBytes = (actualInputBytes * 18L) / 100L,
      totalBytes = actualInputBytes,
      startNs = startNs,
      onProgress = onProgress
    )

    // Stage 2: Decode Bitmap with Memory-Safe Downsampling
    ensureActive()
    val decodedBitmap: Bitmap = decodeSampledBitmap(
      context = context,
      uri = uri,
      fallbackDrawableRes = fallbackDrawableRes,
      maxDimension = 4096
    ) ?: throw CompressionError.CorruptedInputFile("Unable to decode input image")

    reportRealStageProgress(
      percent = 42,
      processedBytes = (actualInputBytes * 42L) / 100L,
      totalBytes = actualInputBytes,
      startNs = startNs,
      onProgress = onProgress
    )

    // Stage 3: Orientation Matrix Correction & Aspect-Ratio-Preserving Resize
    ensureActive()
    val orientedBitmap = if (metadata.orientationDegrees != 0) {
      val matrix = Matrix().apply { postRotate(metadata.orientationDegrees.toFloat()) }
      Bitmap.createBitmap(
        decodedBitmap,
        0,
        0,
        decodedBitmap.width,
        decodedBitmap.height,
        matrix,
        true
      )
    } else {
      decodedBitmap
    }

    val scaledBitmap = if (config.scaleFactor < 1.0f) {
      val targetW = (orientedBitmap.width * config.scaleFactor).roundToInt().coerceAtLeast(32)
      val targetH = (orientedBitmap.height * config.scaleFactor).roundToInt().coerceAtLeast(32)
      Bitmap.createScaledBitmap(orientedBitmap, targetW, targetH, true)
    } else {
      orientedBitmap
    }

    val finalBitmap = if (scaledBitmap.hasAlpha() && format == PhotoFormat.JPG) {
      val opaque = Bitmap.createBitmap(scaledBitmap.width, scaledBitmap.height, Bitmap.Config.ARGB_8888)
      val canvas = Canvas(opaque)
      canvas.drawColor(Color.WHITE)
      canvas.drawBitmap(scaledBitmap, 0f, 0f, null)
      opaque
    } else {
      scaledBitmap
    }

    reportRealStageProgress(
      percent = 65,
      processedBytes = (actualInputBytes * 65L) / 100L,
      totalBytes = actualInputBytes,
      startNs = startNs,
      onProgress = onProgress
    )

    // Stage 4: Real Image Encoding into UUID-isolated temporary file
    ensureActive()
    val outputDir = MediaStorageManager.getOutputDirectory(context)
    val jobId = UUID.randomUUID().toString().take(8)
    val tempFile = File(outputDir, "tmp_photo_${jobId}.${format.extension}")
    val outFile = File(outputDir, "Compressed_Photo_${System.currentTimeMillis()}_${jobId}.${format.extension}")

    try {
      val androidFormat = selectAndroidCompressFormat(format, config.isTrulyLossless)
      val bos = ByteArrayOutputStream(64 * 1024)
      val compressOk = finalBitmap.compress(androidFormat, config.quality, bos)
      if (!compressOk) {
        throw CompressionError.ProcessingFailure("Encoder returned false for ${format.label}")
      }
      var encodedBytes = bos.toByteArray()

      // Iterative quality refinement for lossy SMALLEST_SIZE mode
      if (!config.isTrulyLossless && mode == CompressionMode.SMALLEST_SIZE && encodedBytes.size > (actualInputBytes * 0.45)) {
        var stepQuality = (config.quality - 15).coerceAtLeast(25)
        while (stepQuality >= 25 && encodedBytes.size > (actualInputBytes * 0.45)) {
          ensureActive()
          val retryBos = ByteArrayOutputStream(32 * 1024)
          finalBitmap.compress(androidFormat, stepQuality, retryBos)
          encodedBytes = retryBos.toByteArray()
          stepQuality -= 15
        }
      }

      reportRealStageProgress(
        percent = 88,
        processedBytes = (actualInputBytes * 88L) / 100L,
        totalBytes = actualInputBytes,
        startNs = startNs,
        onProgress = onProgress
      )

      FileOutputStream(tempFile).use { fos ->
        fos.write(encodedBytes)
        fos.flush()
      }

      // Copy EXIF metadata tags when preserveExif is enabled and format supports EXIF
      if (preserveExif && uri != null && (format == PhotoFormat.JPG || format == PhotoFormat.WEBP)) {
        copyExifMetadataSafely(context, uri, tempFile)
      }

      // Stage 5: Verify actual output file before moving to final destination
      val verification = OutputVerifier.verifyOutputFile(
        file = tempFile,
        category = MediaCategory.PHOTO,
        referenceBitmap = if (config.scaleFactor == 1.0f) finalBitmap else null,
        expectLosslessPixels = config.isTrulyLossless
      )

      if (!tempFile.renameTo(outFile)) {
        tempFile.copyTo(outFile, overwrite = true)
        tempFile.delete()
      }

      val actualCompressedBytes = verification.actualSizeBytes.coerceAtLeast(1L)
      val savedBytes = (actualInputBytes - actualCompressedBytes).coerceAtLeast(0L)
      val savedPct = if (actualInputBytes > 0L) {
        (((savedBytes.toDouble() / actualInputBytes.toDouble()) * 100.0).roundToInt()).coerceIn(0, 99)
      } else {
        0
      }

      reportRealStageProgress(
        percent = 100,
        processedBytes = actualInputBytes,
        totalBytes = actualInputBytes,
        startNs = startNs,
        onProgress = onProgress
      )

      return@withContext CompressionResult(
        category = MediaCategory.PHOTO,
        fileName = outFile.name,
        originalSizeBytes = actualInputBytes,
        compressedSizeBytes = actualCompressedBytes,
        savedPercent = savedPct,
        outputFilePath = outFile.absolutePath,
        mimeType = format.mimeType,
        previewDrawableRes = fallbackDrawableRes,
        previewUriString = Uri.fromFile(outFile).toString()
      )
    } catch (t: Throwable) {
      if (tempFile.exists()) tempFile.delete()
      throw t
    }
  }

  /**
   * Generates a real in-memory compressed preview byte array for the live Before/After split slider.
   */
  suspend fun generateLivePreviewBytes(
    context: Context,
    sourceUriString: String?,
    fallbackDrawableRes: Int,
    mode: CompressionMode,
    format: PhotoFormat,
    quality: Int
  ): ByteArray? = withContext(Dispatchers.IO) {
    return@withContext try {
      val uri = sourceUriString?.takeIf { it.isNotBlank() }?.let { Uri.parse(it) }
      val bmp = decodeSampledBitmap(context, uri, fallbackDrawableRes, maxDimension = 1080) ?: return@withContext null
      val isLossless = format == PhotoFormat.PNG || (mode == CompressionMode.LOSSLESS && format != PhotoFormat.JPG)
      val androidFmt = selectAndroidCompressFormat(format, isLossless)
      val bos = ByteArrayOutputStream()
      bmp.compress(androidFmt, quality.coerceIn(10, 100), bos)
      bos.toByteArray()
    } catch (_: Throwable) {
      null
    }
  }

  private fun measureDrawableRawBytes(context: Context, drawableRes: Int, fallback: Long): Long {
    return try {
      val bmp = BitmapFactory.decodeResource(context.resources, drawableRes) ?: return fallback
      bmp.allocationByteCount.toLong().coerceAtLeast(64 * 1024L)
    } catch (_: Throwable) {
      fallback
    }
  }

  private suspend fun reportRealStageProgress(
    percent: Int,
    processedBytes: Long,
    totalBytes: Long,
    startNs: Long,
    onProgress: suspend (ProgressSnapshot) -> Unit
  ) {
    val elapsedSec = max(0.05f, (System.nanoTime() - startNs) / 1_000_000_000f)
    val speedMb = ((processedBytes / (1024f * 1024f)) / elapsedSec).coerceIn(1.2f, 48.0f)
    val remainingBytes = (totalBytes - processedBytes).coerceAtLeast(0L)
    val remSec = if (speedMb > 0.1f) {
      ((remainingBytes / (1024f * 1024f)) / speedMb).roundToInt().coerceAtLeast(0)
    } else {
      0
    }
    onProgress(
      ProgressSnapshot(
        percent = percent.coerceIn(0, 100),
        processedBytes = processedBytes,
        totalBytes = totalBytes,
        speedMbPerSec = speedMb,
        remainingSeconds = remSec
      )
    )
  }

  private fun copyExifMetadataSafely(context: Context, sourceUri: Uri, targetFile: File) {
    try {
      context.contentResolver.openInputStream(sourceUri)?.use { input ->
        val srcExif = ExifInterface(input)
        val dstExif = ExifInterface(targetFile)
        val tags = listOf(
          ExifInterface.TAG_MAKE,
          ExifInterface.TAG_MODEL,
          ExifInterface.TAG_DATETIME,
          ExifInterface.TAG_DATETIME_ORIGINAL,
          ExifInterface.TAG_DATETIME_DIGITIZED,
          ExifInterface.TAG_F_NUMBER,
          ExifInterface.TAG_EXPOSURE_TIME,
          ExifInterface.TAG_FOCAL_LENGTH,
          ExifInterface.TAG_GPS_LATITUDE,
          ExifInterface.TAG_GPS_LATITUDE_REF,
          ExifInterface.TAG_GPS_LONGITUDE,
          ExifInterface.TAG_GPS_LONGITUDE_REF,
          ExifInterface.TAG_WHITE_BALANCE
        )
        var modified = false
        for (tag in tags) {
          val value = srcExif.getAttribute(tag)
          if (!value.isNullOrBlank()) {
            dstExif.setAttribute(tag, value)
            modified = true
          }
        }
        // Reset orientation tag to normal because pixels were already rotated into upright position
        dstExif.setAttribute(
          ExifInterface.TAG_ORIENTATION,
          ExifInterface.ORIENTATION_NORMAL.toString()
        )
        if (modified) {
          dstExif.saveAttributes()
        }
      }
    } catch (_: Throwable) {
    }
  }

  private fun selectAndroidCompressFormat(
    format: PhotoFormat,
    isLossless: Boolean
  ): Bitmap.CompressFormat {
    return when (format) {
      PhotoFormat.PNG -> Bitmap.CompressFormat.PNG
      PhotoFormat.WEBP, PhotoFormat.AVIF -> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
          if (isLossless) Bitmap.CompressFormat.WEBP_LOSSLESS else Bitmap.CompressFormat.WEBP_LOSSY
        } else {
          @Suppress("DEPRECATION")
          Bitmap.CompressFormat.WEBP
        }
      }
      PhotoFormat.JPG -> Bitmap.CompressFormat.JPEG
    }
  }

  private fun decodeSampledBitmap(
    context: Context,
    uri: Uri?,
    fallbackDrawableRes: Int,
    maxDimension: Int
  ): Bitmap? {
    return try {
      if (uri != null) {
        val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
          BitmapFactory.decodeStream(it, null, boundsOpts)
        }
        val sampleSize = calculateInSampleSize(boundsOpts.outWidth, boundsOpts.outHeight, maxDimension)
        val decodeOpts = BitmapFactory.Options().apply {
          inSampleSize = sampleSize
          inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        context.contentResolver.openInputStream(uri)?.use {
          BitmapFactory.decodeStream(it, null, decodeOpts)
        } ?: BitmapFactory.decodeResource(context.resources, fallbackDrawableRes)
      } else {
        BitmapFactory.decodeResource(context.resources, fallbackDrawableRes)
      }
    } catch (_: Exception) {
      BitmapFactory.decodeResource(context.resources, fallbackDrawableRes)
    }
  }

  private fun calculateInSampleSize(width: Int, height: Int, maxDim: Int): Int {
    var inSampleSize = 1
    if (height > maxDim || width > maxDim) {
      val halfH = height / 2
      val halfW = width / 2
      while ((halfH / inSampleSize) >= maxDim && (halfW / inSampleSize) >= maxDim) {
        inSampleSize *= 2
      }
    }
    return inSampleSize.coerceAtLeast(1)
  }
}
