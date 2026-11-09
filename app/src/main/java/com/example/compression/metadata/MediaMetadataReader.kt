package com.example.compression.metadata

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import com.example.model.MediaCategory
import java.io.InputStream
import kotlin.math.abs

data class PhotoMetadata(
  val fileName: String,
  val fileSizeBytes: Long,
  val width: Int,
  val height: Int,
  val mimeType: String,
  val hasAlpha: Boolean,
  val orientationDegrees: Int
)

data class VideoMetadata(
  val fileName: String,
  val fileSizeBytes: Long,
  val durationMs: Long,
  val width: Int,
  val height: Int,
  val rotationDegrees: Int,
  val videoCodec: String,
  val videoBitrateBps: Long,
  val frameRateFps: Float,
  val hasAudioTrack: Boolean,
  val audioCodec: String,
  val audioBitrateBps: Int,
  val audioSampleRateHz: Int,
  val audioChannels: Int
)

data class AudioMetadata(
  val fileName: String,
  val fileSizeBytes: Long,
  val durationMs: Long,
  val mimeType: String,
  val bitrateKbps: Int,
  val sampleRateHz: Int,
  val channels: Int,
  val waveformPeaks: List<Float>
)

object MediaMetadataReader {

  fun detectCategory(context: Context, uri: Uri, fallbackName: String = ""): MediaCategory {
    val mime = try {
      context.contentResolver.getType(uri) ?: ""
    } catch (_: Exception) {
      ""
    }.lowercase()
    val name = fallbackName.lowercase()
    return when {
      mime.startsWith("image/") ||
        name.endsWith(".jpg") || name.endsWith(".jpeg") ||
        name.endsWith(".png") || name.endsWith(".webp") ||
        name.endsWith(".avif") || name.endsWith(".heic") ||
        name.endsWith(".bmp") -> MediaCategory.PHOTO
      mime.startsWith("video/") ||
        name.endsWith(".mp4") || name.endsWith(".mkv") ||
        name.endsWith(".mov") || name.endsWith(".webm") -> MediaCategory.VIDEO
      mime.startsWith("audio/") ||
        name.endsWith(".mp3") || name.endsWith(".aac") ||
        name.endsWith(".m4a") || name.endsWith(".opus") ||
        name.endsWith(".ogg") || name.endsWith(".flac") ||
        name.endsWith(".wav") -> MediaCategory.AUDIO
      else -> MediaCategory.PHOTO
    }
  }

  fun queryDisplayNameAndSize(context: Context, uri: Uri): Pair<String, Long> {
    var name = "Selected_Media"
    var size = 0L
    try {
      context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
        if (cursor.moveToFirst()) {
          if (nameIdx >= 0) {
            cursor.getString(nameIdx)?.let { if (it.isNotBlank()) name = it }
          }
          if (sizeIdx >= 0 && !cursor.isNull(sizeIdx)) {
            size = cursor.getLong(sizeIdx)
          }
        }
      }
      if (size <= 0L) {
        context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { afd ->
          if (afd.length > 0) size = afd.length
        }
      }
    } catch (_: Exception) {
    }
    return name to size
  }

  fun readPhotoMetadata(
    context: Context,
    uri: Uri?,
    fallbackDrawableRes: Int,
    fallbackSizeBytes: Long = 4_404_019L
  ): PhotoMetadata {
    if (uri != null) {
      val (name, rawSize) = queryDisplayNameAndSize(context, uri)
      val size = if (rawSize > 0L) rawSize else fallbackSizeBytes
      val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
      var orientation = 0
      try {
        context.contentResolver.openInputStream(uri)?.use { stream ->
          BitmapFactory.decodeStream(stream, null, opts)
        }
        context.contentResolver.openInputStream(uri)?.use { stream ->
          orientation = readJpegExifOrientationDegrees(stream)
        }
      } catch (_: Exception) {
      }
      val mime = opts.outMimeType ?: context.contentResolver.getType(uri) ?: "image/jpeg"
      val hasAlpha = mime.contains("png", ignoreCase = true) ||
        mime.contains("webp", ignoreCase = true) ||
        mime.contains("avif", ignoreCase = true)
      return PhotoMetadata(
        fileName = name,
        fileSizeBytes = size,
        width = opts.outWidth.coerceAtLeast(800),
        height = opts.outHeight.coerceAtLeast(430),
        mimeType = mime,
        hasAlpha = hasAlpha,
        orientationDegrees = orientation
      )
    }

    val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    try {
      BitmapFactory.decodeResource(context.resources, fallbackDrawableRes, opts)
    } catch (_: Exception) {
    }
    return PhotoMetadata(
      fileName = "Mountain_Lake.jpg",
      fileSizeBytes = fallbackSizeBytes,
      width = opts.outWidth.coerceAtLeast(800),
      height = opts.outHeight.coerceAtLeast(430),
      mimeType = "image/jpeg",
      hasAlpha = false,
      orientationDegrees = 0
    )
  }

  fun readVideoMetadata(
    context: Context,
    uri: Uri?,
    fallbackSizeBytes: Long = 89_758_105L
  ): VideoMetadata {
    if (uri == null) {
      return VideoMetadata(
        fileName = "Coastal_Sunset.mp4",
        fileSizeBytes = fallbackSizeBytes,
        durationMs = 30_000L,
        width = 1920,
        height = 1080,
        rotationDegrees = 0,
        videoCodec = "H.264 / AVC",
        videoBitrateBps = 22_000_000L,
        frameRateFps = 30f,
        hasAudioTrack = true,
        audioCodec = "AAC",
        audioBitrateBps = 192_000,
        audioSampleRateHz = 48_000,
        audioChannels = 2
      )
    }

    val (name, rawSize) = queryDisplayNameAndSize(context, uri)
    val validSize = if (rawSize > 0L) rawSize else fallbackSizeBytes
    val retriever = MediaMetadataRetriever()
    var durationMs = 30_000L
    var width = 1920
    var height = 1080
    var rotation = 0
    var bitrate = 12_000_000L
    var hasAudio = true

    try {
      retriever.setDataSource(context, uri)
      durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 30_000L
      width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 1920
      height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 1080
      rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull() ?: 0
      bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toLongOrNull()
        ?: ((validSize * 8L * 1000L) / durationMs.coerceAtLeast(1000L))
      hasAudio = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO) != null
    } catch (_: Exception) {
    } finally {
      try {
        retriever.release()
      } catch (_: Exception) {
      }
    }

    var videoCodec = "H.264 / AVC"
    var fps = 30f
    var audioCodec = "AAC"
    var audioSampleRate = 44100
    var audioChannels = 2

    val extractor = MediaExtractor()
    try {
      extractor.setDataSource(context, uri, null)
      for (i in 0 until extractor.trackCount) {
        val format = extractor.getTrackFormat(i)
        val mime = format.getString(MediaFormat.KEY_MIME) ?: continue
        if (mime.startsWith("video/")) {
          videoCodec = when {
            mime.contains("hevc", ignoreCase = true) -> "H.265 / HEVC"
            mime.contains("av01", ignoreCase = true) -> "AV1"
            mime.contains("vp9", ignoreCase = true) -> "VP9"
            else -> "H.264 / AVC"
          }
          if (format.containsKey(MediaFormat.KEY_FRAME_RATE)) {
            fps = try {
              format.getInteger(MediaFormat.KEY_FRAME_RATE).toFloat()
            } catch (_: Exception) {
              format.getFloat(MediaFormat.KEY_FRAME_RATE)
            }
          }
        } else if (mime.startsWith("audio/")) {
          hasAudio = true
          audioCodec = when {
            mime.contains("opus", ignoreCase = true) -> "Opus"
            mime.contains("flac", ignoreCase = true) -> "FLAC"
            mime.contains("mpeg", ignoreCase = true) -> "MP3"
            else -> "AAC"
          }
          if (format.containsKey(MediaFormat.KEY_SAMPLE_RATE)) {
            audioSampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
          }
          if (format.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) {
            audioChannels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
          }
        }
      }
    } catch (_: Exception) {
    } finally {
      try {
        extractor.release()
      } catch (_: Exception) {
      }
    }

    return VideoMetadata(
      fileName = name,
      fileSizeBytes = validSize,
      durationMs = durationMs.coerceAtLeast(1000L),
      width = width,
      height = height,
      rotationDegrees = rotation,
      videoCodec = videoCodec,
      videoBitrateBps = bitrate,
      frameRateFps = fps.coerceIn(15f, 120f),
      hasAudioTrack = hasAudio,
      audioCodec = audioCodec,
      audioBitrateBps = 192_000,
      audioSampleRateHz = audioSampleRate,
      audioChannels = audioChannels
    )
  }

  fun readAudioMetadata(
    context: Context,
    uri: Uri?,
    fallbackSizeBytes: Long = 13_107_200L
  ): AudioMetadata {
    if (uri == null) {
      return AudioMetadata(
        fileName = "Synthwave_Track.mp3",
        fileSizeBytes = fallbackSizeBytes,
        durationMs = 204_000L, // 03:24
        mimeType = "audio/mpeg",
        bitrateKbps = 320,
        sampleRateHz = 44100,
        channels = 2,
        waveformPeaks = generateDefaultWaveformPeaks(42)
      )
    }

    val (name, rawSize) = queryDisplayNameAndSize(context, uri)
    val validSize = if (rawSize > 0L) rawSize else fallbackSizeBytes
    val retriever = MediaMetadataRetriever()
    var durationMs = 204_000L
    var bitrateBps = 192_000
    var mime = context.contentResolver.getType(uri) ?: "audio/mpeg"

    try {
      retriever.setDataSource(context, uri)
      durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 204_000L
      bitrateBps = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toIntOrNull()
        ?: (((validSize * 8L * 1000L) / durationMs.coerceAtLeast(1000L)).toInt())
      retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)?.let { mime = it }
    } catch (_: Exception) {
    } finally {
      try {
        retriever.release()
      } catch (_: Exception) {
      }
    }

    var sampleRate = 44100
    var channels = 2
    val extractor = MediaExtractor()
    try {
      extractor.setDataSource(context, uri, null)
      for (i in 0 until extractor.trackCount) {
        val format = extractor.getTrackFormat(i)
        val trackMime = format.getString(MediaFormat.KEY_MIME) ?: continue
        if (trackMime.startsWith("audio/")) {
          if (format.containsKey(MediaFormat.KEY_SAMPLE_RATE)) {
            sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
          }
          if (format.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) {
            channels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
          }
          break
        }
      }
    } catch (_: Exception) {
    } finally {
      try {
        extractor.release()
      } catch (_: Exception) {
      }
    }

    val peaks = sampleWaveformPeaksFromUri(context, uri, 42)
    return AudioMetadata(
      fileName = name,
      fileSizeBytes = validSize,
      durationMs = durationMs.coerceAtLeast(1000L),
      mimeType = mime,
      bitrateKbps = (bitrateBps / 1000).coerceIn(32, 1411),
      sampleRateHz = sampleRate,
      channels = channels.coerceIn(1, 2),
      waveformPeaks = peaks
    )
  }

  private fun sampleWaveformPeaksFromUri(context: Context, uri: Uri, barCount: Int): List<Float> {
    return try {
      context.contentResolver.openInputStream(uri)?.use { stream ->
        val bytes = ByteArray(8192)
        val read = stream.read(bytes)
        if (read <= 64) return generateDefaultWaveformPeaks(barCount)
        val step = (read / barCount).coerceAtLeast(1)
        List(barCount) { idx ->
          var sum = 0f
          val start = (idx * step).coerceAtMost(read - 1)
          val end = ((idx + 1) * step).coerceAtMost(read)
          for (j in start until end) {
            sum += abs(bytes[j].toInt()) / 128f
          }
          val avg = sum / (end - start).coerceAtLeast(1)
          avg.coerceIn(0.18f, 0.92f)
        }
      } ?: generateDefaultWaveformPeaks(barCount)
    } catch (_: Exception) {
      generateDefaultWaveformPeaks(barCount)
    }
  }

  private fun generateDefaultWaveformPeaks(count: Int): List<Float> {
    return List(count) { i ->
      val norm = i.toFloat() / count.toFloat()
      val v = abs(kotlin.math.sin(norm * Math.PI * 3.0)).toFloat()
      (0.20f + 0.65f * v).coerceIn(0.18f, 0.90f)
    }
  }

  /**
   * Minimal dependency-free JPEG EXIF orientation parser so image orientation
   * is always respected even without third-party native EXIF libraries.
   */
  fun readJpegExifOrientationDegrees(input: InputStream): Int {
    return try {
      val header = ByteArray(64 * 1024)
      val len = input.read(header)
      if (len < 14) return 0
      // Check JPEG SOI 0xFFD8
      if ((header[0].toInt() and 0xFF) != 0xFF || (header[1].toInt() and 0xFF) != 0xD8) return 0
      var offset = 2
      while (offset + 4 < len) {
        if ((header[offset].toInt() and 0xFF) != 0xFF) break
        val marker = header[offset + 1].toInt() and 0xFF
        if (marker == 0xD9 || marker == 0xDA) break
        val segLen = ((header[offset + 2].toInt() and 0xFF) shl 8) or (header[offset + 3].toInt() and 0xFF)
        if (segLen < 2 || offset + 2 + segLen > len) break
        if (marker == 0xE1 && segLen >= 14) {
          // Check "Exif\0\0"
          val isExif = header[offset + 4] == 'E'.code.toByte() &&
            header[offset + 5] == 'x'.code.toByte() &&
            header[offset + 6] == 'i'.code.toByte() &&
            header[offset + 7] == 'f'.code.toByte()
          if (isExif) {
            val tiffStart = offset + 10
            val littleEndian = header[tiffStart] == 'I'.code.toByte()
            val ifdOffset = readInt32(header, tiffStart + 4, littleEndian)
            val dirStart = tiffStart + ifdOffset
            if (dirStart + 2 <= len) {
              val entries = readInt16(header, dirStart, littleEndian)
              for (e in 0 until entries) {
                val entryOffset = dirStart + 2 + e * 12
                if (entryOffset + 12 > len) break
                val tag = readInt16(header, entryOffset, littleEndian)
                if (tag == 0x0112) {
                  val orientationVal = readInt16(header, entryOffset + 8, littleEndian)
                  return when (orientationVal) {
                    3 -> 180
                    6 -> 90
                    8 -> 270
                    else -> 0
                  }
                }
              }
            }
          }
        }
        offset += 2 + segLen
      }
      0
    } catch (_: Exception) {
      0
    }
  }

  private fun readInt16(buf: ByteArray, pos: Int, littleEndian: Boolean): Int {
    val b0 = buf[pos].toInt() and 0xFF
    val b1 = buf[pos + 1].toInt() and 0xFF
    return if (littleEndian) (b1 shl 8) or b0 else (b0 shl 8) or b1
  }

  private fun readInt32(buf: ByteArray, pos: Int, littleEndian: Boolean): Int {
    val b0 = buf[pos].toInt() and 0xFF
    val b1 = buf[pos + 1].toInt() and 0xFF
    val b2 = buf[pos + 2].toInt() and 0xFF
    val b3 = buf[pos + 3].toInt() and 0xFF
    return if (littleEndian) {
      (b3 shl 24) or (b2 shl 16) or (b1 shl 8) or b0
    } else {
      (b0 shl 24) or (b1 shl 16) or (b2 shl 8) or b3
    }
  }
}
