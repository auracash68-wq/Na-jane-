package com.example.compression.common

import android.app.ActivityManager
import android.content.Context
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.os.Build
import android.os.Environment
import android.os.StatFs

data class EncoderCapability(
  val mimeType: String,
  val codecName: String,
  val isHardwareAccelerated: Boolean,
  val maxSupportedWidth: Int,
  val maxSupportedHeight: Int
)

object DeviceCapabilities {

  fun findBestVideoEncoder(
    preferredCodecLabel: String,
    preferHardware: Boolean = true
  ): EncoderCapability {
    val requestedMime = when {
      preferredCodecLabel.contains("H.265", ignoreCase = true) ||
        preferredCodecLabel.contains("HEVC", ignoreCase = true) -> "video/hevc"
      preferredCodecLabel.contains("AV1", ignoreCase = true) -> "video/av01"
      preferredCodecLabel.contains("VP9", ignoreCase = true) -> "video/x-vnd.on2.vp9"
      else -> "video/avc"
    }

    val specific = queryVideoEncoderForMime(requestedMime, preferHardware)
    if (specific != null) return specific

    // Safe fallback to H.264 / AVC when HEVC or AV1 hardware/software encoder is absent
    return queryVideoEncoderForMime("video/avc", preferHardware)
      ?: EncoderCapability(
        mimeType = "video/avc",
        codecName = "OMX.google.h264.encoder",
        isHardwareAccelerated = false,
        maxSupportedWidth = 1920,
        maxSupportedHeight = 1080
      )
  }

  private fun queryVideoEncoderForMime(
    mimeType: String,
    preferHardware: Boolean
  ): EncoderCapability? {
    return try {
      val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
      val encoders = codecList.codecInfos
        .filter { it.isEncoder && it.supportedTypes.any { t -> t.equals(mimeType, ignoreCase = true) } }

      if (encoders.isEmpty()) return null

      val chosen = if (preferHardware) {
        encoders.firstOrNull { isHardwareCodec(it) } ?: encoders.first()
      } else {
        encoders.firstOrNull { !isHardwareCodec(it) } ?: encoders.first()
      }

      val caps = try {
        chosen.getCapabilitiesForType(mimeType)?.videoCapabilities
      } catch (_: Exception) {
        null
      }

      EncoderCapability(
        mimeType = mimeType,
        codecName = chosen.name,
        isHardwareAccelerated = isHardwareCodec(chosen),
        maxSupportedWidth = caps?.supportedWidths?.upper ?: 3840,
        maxSupportedHeight = caps?.supportedHeights?.upper ?: 2160
      )
    } catch (_: Throwable) {
      EncoderCapability(
        mimeType = "video/avc",
        codecName = "Standard-AVC-Encoder",
        isHardwareAccelerated = true,
        maxSupportedWidth = 3840,
        maxSupportedHeight = 2160
      )
    }
  }

  fun hasAudioEncoder(mimeType: String): Boolean {
    return try {
      val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
      codecList.codecInfos.any { info ->
        info.isEncoder && info.supportedTypes.any { it.equals(mimeType, ignoreCase = true) }
      }
    } catch (_: Throwable) {
      true
    }
  }

  private fun isHardwareCodec(info: MediaCodecInfo): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      return info.isHardwareAccelerated
    }
    val name = info.name.lowercase()
    return !name.startsWith("omx.google.") &&
      !name.startsWith("c2.android.") &&
      !name.contains(".sw.")
  }

  fun getAvailableStorageBytes(context: Context): Long {
    return try {
      val dir = context.cacheDir ?: Environment.getDataDirectory()
      val stat = StatFs(dir.absolutePath)
      stat.availableBlocksLong * stat.blockSizeLong
    } catch (_: Throwable) {
      512L * 1024L * 1024L
    }
  }

  fun hasEnoughStorage(context: Context, estimatedOutputBytes: Long): Boolean {
    val available = getAvailableStorageBytes(context)
    val safetyMargin = 10L * 1024L * 1024L // 10 MB buffer
    return available > (estimatedOutputBytes + safetyMargin)
  }

  fun recommendedBatchConcurrency(context: Context): Int {
    return try {
      val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
      val memInfo = ActivityManager.MemoryInfo()
      am?.getMemoryInfo(memInfo)
      val totalRamMb = (memInfo.totalMem / (1024L * 1024L)).toInt()
      val cores = Runtime.getRuntime().availableProcessors()
      when {
        memInfo.lowMemory || totalRamMb in 1..3072 -> 1
        cores >= 6 && totalRamMb >= 6144 -> 2
        else -> 1
      }
    } catch (_: Throwable) {
      1
    }
  }
}
