package com.example.compression.common

sealed class CompressionError(
  val userMessage: String,
  val technicalDetails: String
) : Exception(userMessage) {

  class UnsupportedFile(formatOrName: String) : CompressionError(
    userMessage = "Unsupported media file ($formatOrName). Please choose a supported photo, video, or audio file.",
    technicalDetails = "Unsupported input format or MIME type: $formatOrName"
  )

  class UnsupportedCodec(codecName: String) : CompressionError(
    userMessage = "Codec $codecName is not supported on this device. Falling back or select H.264 / AAC.",
    technicalDetails = "Codec unavailable in MediaCodecList: $codecName"
  )

  class EncoderUnavailable(mimeType: String) : CompressionError(
    userMessage = "Hardware/software encoder for $mimeType is unavailable on this device.",
    technicalDetails = "No compatible MediaCodec encoder registered for $mimeType"
  )

  class DecoderUnavailable(mimeType: String) : CompressionError(
    userMessage = "Cannot decode input media ($mimeType) on this device.",
    technicalDetails = "Decoder initialization failed for $mimeType"
  )

  class InsufficientStorage(requiredBytes: Long, availableBytes: Long) : CompressionError(
    userMessage = "Insufficient storage space to complete compression.",
    technicalDetails = "Required=$requiredBytes bytes, Available=$availableBytes bytes"
  )

  class PermissionDenied(permissionOrUri: String) : CompressionError(
    userMessage = "Storage or file access permission was denied.",
    technicalDetails = "SecurityException or revoked URI permission: $permissionOrUri"
  )

  class CorruptedInputFile(reason: String) : CompressionError(
    userMessage = "The selected file appears to be corrupted or unreadable.",
    technicalDetails = "Input validation/decoding failed: $reason"
  )

  class ProcessingFailure(reason: String, cause: Throwable? = null) : CompressionError(
    userMessage = "Compression failed: $reason",
    technicalDetails = "Processing exception: ${cause?.message ?: reason}"
  )

  object CancelledOperation : CompressionError(
    userMessage = "Compression was cancelled.",
    technicalDetails = "Job cancelled by user request"
  )

  class OutputVerificationFailure(reason: String) : CompressionError(
    userMessage = "Compressed output verification failed ($reason). Original file was kept untouched.",
    technicalDetails = "OutputVerifier rejected generated file: $reason"
  )
}
