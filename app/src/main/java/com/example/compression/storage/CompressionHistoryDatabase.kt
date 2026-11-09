package com.example.compression.storage

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.model.CompressionHistoryEntry
import com.example.model.MediaCategory
import java.io.File
import java.io.FileInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class CompressionHistoryDatabase private constructor(context: Context) :
  SQLiteOpenHelper(context.applicationContext, DB_NAME, null, DB_VERSION) {

  private val appContext = context.applicationContext
  private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

  private val _historyFlow = MutableStateFlow<List<CompressionHistoryEntry>>(emptyList())
  val historyFlow: StateFlow<List<CompressionHistoryEntry>> = _historyFlow.asStateFlow()

  init {
    _historyFlow.value = queryAllEntriesInternal()
  }

  override fun onCreate(db: SQLiteDatabase) {
    db.execSQL(
      """
      CREATE TABLE IF NOT EXISTS $TABLE_HISTORY (
        id TEXT PRIMARY KEY,
        file_name TEXT NOT NULL,
        output_file_name TEXT NOT NULL,
        category TEXT NOT NULL,
        compression_mode TEXT NOT NULL,
        output_format TEXT NOT NULL,
        original_bytes INTEGER NOT NULL,
        compressed_bytes INTEGER NOT NULL,
        saved_percent INTEGER NOT NULL,
        output_file_path TEXT NOT NULL,
        status TEXT NOT NULL,
        timestamp_millis INTEGER NOT NULL
      )
      """.trimIndent()
    )
  }

  override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    db.execSQL("DROP TABLE IF EXISTS $TABLE_HISTORY")
    onCreate(db)
  }

  suspend fun insertEntry(
    entry: CompressionHistoryEntry,
    modeLabel: String = "Balanced",
    formatLabel: String = "JPG",
    status: String = "COMPLETED"
  ) = withContext(Dispatchers.IO) {
    val values = ContentValues().apply {
      put("id", entry.id)
      put("file_name", entry.fileName)
      put("output_file_name", File(entry.outputFilePath).name.ifBlank { entry.fileName })
      put("category", entry.category.name)
      put("compression_mode", modeLabel)
      put("output_format", formatLabel)
      put("original_bytes", entry.originalBytes)
      put("compressed_bytes", entry.compressedBytes)
      put("saved_percent", entry.savedPercent)
      put("output_file_path", entry.outputFilePath)
      put("status", status)
      put("timestamp_millis", entry.timestampMillis)
    }
    writableDatabase.insertWithOnConflict(
      TABLE_HISTORY,
      null,
      values,
      SQLiteDatabase.CONFLICT_REPLACE
    )
    _historyFlow.value = queryAllEntriesInternal()
  }

  suspend fun clearHistory() = withContext(Dispatchers.IO) {
    writableDatabase.delete(TABLE_HISTORY, null, null)
    _historyFlow.value = emptyList()
  }

  private fun queryAllEntriesInternal(): List<CompressionHistoryEntry> {
    val list = mutableListOf<CompressionHistoryEntry>()
    try {
      readableDatabase.query(
        TABLE_HISTORY,
        null,
        null,
        null,
        null,
        null,
        "timestamp_millis DESC",
        "100"
      )?.use { cursor ->
        val idIdx = cursor.getColumnIndexOrThrow("id")
        val nameIdx = cursor.getColumnIndexOrThrow("file_name")
        val catIdx = cursor.getColumnIndexOrThrow("category")
        val origIdx = cursor.getColumnIndexOrThrow("original_bytes")
        val compIdx = cursor.getColumnIndexOrThrow("compressed_bytes")
        val pctIdx = cursor.getColumnIndexOrThrow("saved_percent")
        val pathIdx = cursor.getColumnIndexOrThrow("output_file_path")
        val timeIdx = cursor.getColumnIndexOrThrow("timestamp_millis")

        while (cursor.moveToNext()) {
          val catName = cursor.getString(catIdx)
          val category = MediaCategory.entries.firstOrNull { it.name == catName } ?: MediaCategory.PHOTO
          list.add(
            CompressionHistoryEntry(
              id = cursor.getString(idIdx),
              fileName = cursor.getString(nameIdx),
              category = category,
              originalBytes = cursor.getLong(origIdx),
              compressedBytes = cursor.getLong(compIdx),
              savedPercent = cursor.getInt(pctIdx),
              outputFilePath = cursor.getString(pathIdx),
              timestampMillis = cursor.getLong(timeIdx)
            )
          )
        }
      }
    } catch (_: Exception) {
    }
    return list
  }

  // Persistent Settings Accessors
  var preserveExifMetadata: Boolean
    get() = prefs.getBoolean("preserve_exif", true)
    set(value) = prefs.edit().putBoolean("preserve_exif", value).apply()

  var hardwareAcceleration: Boolean
    get() = prefs.getBoolean("hardware_accel", true)
    set(value) = prefs.edit().putBoolean("hardware_accel", value).apply()

  var autoSaveToGallery: Boolean
    get() = prefs.getBoolean("auto_save_gallery", true)
    set(value) = prefs.edit().putBoolean("auto_save_gallery", value).apply()

  var defaultCompressionMode: String
    get() = prefs.getString("default_mode", "Balanced") ?: "Balanced"
    set(value) = prefs.edit().putString("default_mode", value).apply()

  var defaultPhotoFormat: String
    get() = prefs.getString("default_photo_format", "JPG") ?: "JPG"
    set(value) = prefs.edit().putString("default_photo_format", value).apply()

  companion object {
    private const val DB_NAME = "media_compressor_history.db"
    private const val DB_VERSION = 1
    private const val TABLE_HISTORY = "compression_history"
    private const val PREFS_NAME = "media_compressor_settings"

    @Volatile
    private var instance: CompressionHistoryDatabase? = null

    fun getInstance(context: Context): CompressionHistoryDatabase {
      return instance ?: synchronized(this) {
        instance ?: CompressionHistoryDatabase(context).also { instance = it }
      }
    }
  }
}

object MediaStorageManager {

  fun getOutputDirectory(context: Context): File {
    return File(context.cacheDir, "compressed_output").apply { mkdirs() }
  }

  fun cleanupAbandonedTempFiles(context: Context) {
    try {
      val dir = getOutputDirectory(context)
      val cutoff = System.currentTimeMillis() - 24L * 3600L * 1000L
      dir.listFiles()?.forEach { file ->
        if (file.name.startsWith("tmp_") && file.lastModified() < cutoff) {
          file.delete()
        }
      }
    } catch (_: Exception) {
    }
  }

  /**
   * Exports a verified compressed file to Android MediaStore without ever overwriting
   * the user's original source file.
   */
  suspend fun exportToMediaStoreIfEnabled(
    context: Context,
    sourceFile: File,
    mimeType: String,
    category: MediaCategory
  ): String? = withContext(Dispatchers.IO) {
    if (!sourceFile.exists() || sourceFile.length() <= 0L) return@withContext null
    try {
      val resolver = context.contentResolver
      val collection = when (category) {
        MediaCategory.PHOTO, MediaCategory.BATCH -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        MediaCategory.VIDEO -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        MediaCategory.AUDIO -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
      }
      val values = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, sourceFile.name)
        put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          val relPath = when (category) {
            MediaCategory.PHOTO, MediaCategory.BATCH -> Environment.DIRECTORY_PICTURES + "/MediaCompressor"
            MediaCategory.VIDEO -> Environment.DIRECTORY_MOVIES + "/MediaCompressor"
            MediaCategory.AUDIO -> Environment.DIRECTORY_MUSIC + "/MediaCompressor"
          }
          put(MediaStore.MediaColumns.RELATIVE_PATH, relPath)
          put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
      }
      val uri = resolver.insert(collection, values) ?: return@withContext null
      resolver.openOutputStream(uri)?.use { out ->
        FileInputStream(sourceFile).use { input ->
          input.copyTo(out, bufferSize = 32 * 1024)
        }
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val doneValues = ContentValues().apply {
          put(MediaStore.MediaColumns.IS_PENDING, 0)
        }
        resolver.update(uri, doneValues, null, null)
      }
      uri.toString()
    } catch (_: Exception) {
      null
    }
  }
}
