package com.example.indirici

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File

data class MediaItem(
    val name: String,
    val uri: Uri,
    val size: Long,
    val isAudio: Boolean
)

object Library {
    fun list(context: Context): List<MediaItem> {
        val out = ArrayList<MediaItem>()

        val dir = File(context.getExternalFilesDir(null), "indirilen")
        dir.listFiles()?.forEach { f ->
            out.add(MediaItem(f.name, Uri.fromFile(f), f.length(), isAudio(f.name)))
        }

        if (Build.VERSION.SDK_INT >= 29) {
            val proj = arrayOf(
                MediaStore.Downloads._ID,
                MediaStore.Downloads.DISPLAY_NAME,
                MediaStore.Downloads.SIZE
            )
            val sel = MediaStore.Downloads.RELATIVE_PATH + " LIKE ?"
            val args = arrayOf(Environment.DIRECTORY_DOWNLOADS + "/Kervan%")
            val cursor = context.contentResolver.query(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                proj, sel, args,
                MediaStore.MediaColumns.DATE_ADDED + " DESC"
            )
            cursor?.use { c ->
                while (c.moveToNext()) {
                    val id = c.getLong(0)
                    val name = c.getString(1) ?: continue
                    val size = c.getLong(2)
                    val uri = ContentUris.withAppendedId(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI, id
                    )
                    out.add(MediaItem(name, uri, size, isAudio(name)))
                }
            }
        }
        return out
    }

    fun delete(context: Context, item: MediaItem): Boolean {
        return try {
            if (item.uri.scheme == "file") {
                File(item.uri.path ?: "").delete()
            } else {
                context.contentResolver.delete(item.uri, null, null) > 0
            }
        } catch (e: Exception) {
            false
        }
    }

    fun formatSize(b: Long): String {
        val mb = b / 1048576.0
        return if (mb >= 1) "%.1f MB".format(mb) else (b / 1024).toString() + " KB"
    }

    private fun isAudio(n: String): Boolean {
        val s = n.lowercase()
        return s.endsWith(".mp3") || s.endsWith(".m4a") || s.endsWith(".opus") ||
            s.endsWith(".ogg") || s.endsWith(".wav")
    }
}
