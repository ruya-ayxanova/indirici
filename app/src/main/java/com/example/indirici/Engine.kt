package com.example.indirici

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import java.io.File

object Engine {
    @Volatile
    var ready = false
    private val main = Handler(Looper.getMainLooper())

    fun init(context: Context, onDone: (String?) -> Unit) {
        val app = context.applicationContext
        if (ready) {
            onDone(null)
            return
        }
        Thread {
            var err: String? = null
            synchronized(Engine) {
                try {
                    if (!ready) {
                        YoutubeDL.getInstance().init(app)
                        FFmpeg.getInstance().init(app)
                        ready = true
                    }
                } catch (e: Exception) {
                    err = e.message ?: "bilinmeyen hata"
                }
            }
            main.post { onDone(err) }
        }.start()
    }

    fun download(
        context: Context,
        url: String,
        audio: Boolean,
        onProgress: (Int) -> Unit,
        onDone: (String) -> Unit,
        hidden: Boolean = false
    ) {
        val app = context.applicationContext
        Thread {
            val message = try {
                val dir = File(app.getExternalFilesDir(null), "indirilen")
                dir.mkdirs()
                if (hidden) File(dir, ".nomedia").createNewFile()
                val before = dir.listFiles()?.map { it.name }?.toSet() ?: emptySet()

                val req = YoutubeDLRequest(url)
                req.addOption("-o", dir.absolutePath + "/%(title).80s.%(ext)s")
                req.addOption("--no-playlist")
                if (audio) {
                    req.addOption("-x")
                    req.addOption("--audio-format", "mp3")
                } else {
                    req.addOption("-S", "res:1080,ext:mp4:m4a")
                    req.addOption("--merge-output-format", "mp4")
                }

                YoutubeDL.getInstance().execute(req, null) { progress, _, _ ->
                    main.post { onProgress(progress.toInt()) }
                }

                if (hidden) {
                    "Bitti. Video uygulamanın gizli klasörüne kaydedildi."
                } else {
                    val newFiles = dir.listFiles()?.filter { it.name !in before } ?: emptyList()
                    var saved = 0
                    for (f in newFiles) {
                        if (saveToDownloads(app, f)) saved++
                    }
                    if (saved > 0) {
                        "Bitti. Indirilenler/Kervan klasörüne kaydedildi."
                    } else {
                        "Bitti. Dosya: " + dir.absolutePath
                    }
                }
            } catch (e: Exception) {
                "Hata: " + e.message
            }
            main.post { onDone(message) }
        }.start()
    }

    private fun saveToDownloads(context: Context, f: File): Boolean {
        if (Build.VERSION.SDK_INT < 29) return false
        val values = ContentValues()
        values.put(MediaStore.Downloads.DISPLAY_NAME, f.name)
        values.put(
            MediaStore.Downloads.RELATIVE_PATH,
            Environment.DIRECTORY_DOWNLOADS + "/Kervan"
        )
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: return false
        resolver.openOutputStream(uri)?.use { out ->
            f.inputStream().use { it.copyTo(out) }
        }
        f.delete()
        return true
    }
}
