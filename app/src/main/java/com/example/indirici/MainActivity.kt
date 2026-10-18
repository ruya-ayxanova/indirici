package com.example.indirici

import android.content.Intent
import android.app.Activity
import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import java.io.File

class MainActivity : Activity() {
    private lateinit var status: TextView
    private var ready = false
private val COOKIE_REQ = 4242
private fun cookieFile() = File(filesDir, "cookies.txt")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(48, 96, 48, 48)

        val title = TextView(this)
        title.text = "Kervan"
        title.textSize = 28f

        val input = EditText(this)
        input.hint = "Bağlantıyı yapıştır"
        input.setSingleLine(true)

        val group = RadioGroup(this)
        val rVideo = RadioButton(this)
        rVideo.text = "Video"
        rVideo.id = 1001
        val rAudio = RadioButton(this)
        rAudio.text = "Müzik (MP3)"
        rAudio.id = 1002
        group.addView(rVideo)
        group.addView(rAudio)
        group.check(1001)

        val button = Button(this)
        button.text = "İndir"

        status = TextView(this)
        status.textSize = 16f
        status.setPadding(0, 32, 0, 0)
        status.text = "Motor hazırlanıyor, ilk açılışta bir dakika sürebilir..."

        Thread {
            try {
                YoutubeDL.getInstance().init(applicationContext)
                FFmpeg.getInstance().init(applicationContext)
                ready = true
                runOnUiThread { status.text = "Hazır. Bağlantıyı yapıştır." }
            } catch (e: Exception) {
                runOnUiThread { status.text = "Motor hatası: " + e.message }
            }
        }.start()

        button.setOnClickListener {
            val url = input.text.toString().trim()
            if (!ready) {
                status.text = "Motor henüz hazır değil, biraz bekle."
                return@setOnClickListener
            }
            if (!url.startsWith("http")) {
                status.text = "Geçerli bir bağlantı yapıştır."
                return@setOnClickListener
            }
            val audio = group.checkedRadioButtonId == 1002
            button.isEnabled = false
            status.text = "Başlıyor..."
            Thread {
                try {
                    val dir = File(getExternalFilesDir(null), "indirilen")
                    dir.mkdirs()
                    val before = dir.listFiles()?.map { it.name }?.toSet() ?: emptySet()

                    val req = YoutubeDLRequest(url)
                    req.addOption("-o", dir.absolutePath + "/%(title).80s.%(ext)s")
                    req.addOption("--no-playlist")
                    if (cookieFile().exists()) {
    req.addOption("--cookies", cookieFile().absolutePath)
}
                    if (audio) {
                        req.addOption("-x")
                        req.addOption("--audio-format", "mp3")
                    } else {
                        req.addOption("-S", "res:1080,ext:mp4:m4a")
                        req.addOption("--merge-output-format", "mp4")
                    }

                    YoutubeDL.getInstance().execute(req, null) { progress, _, _ ->
                        runOnUiThread { status.text = "İndiriliyor: %" + progress.toInt() }
                    }

                    val newFiles = dir.listFiles()?.filter { it.name !in before } ?: emptyList()
                    var saved = 0
                    for (f in newFiles) {
                        if (saveToDownloads(f)) saved++
                    }
                    runOnUiThread {
                        status.text = if (saved > 0) {
                            "Bitti. İndirilenler/Kervan klasörüne kaydedildi."
                        } else {
                            "Bitti. Dosya uygulama klasöründe: " + dir.absolutePath
                        }
                        button.isEnabled = true
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        status.text = "Hata: " + e.message
                        button.isEnabled = true
                    }
                }
            }.start()
        }

        root.addView(title)
        root.addView(input)
        root.addView(group)
        root.addView(button)
        val cookieBtn = Button(this)
cookieBtn.text = "Çerez dosyası seç"
cookieBtn.setOnClickListener {
    val i = Intent(Intent.ACTION_OPEN_DOCUMENT)
    i.addCategory(Intent.CATEGORY_OPENABLE)
    i.type = "*/*"
    startActivityForResult(i, COOKIE_REQ)
}
root.addView(cookieBtn)
        root.addView(status)
        setContentView(root)
    }
@Deprecated("Deprecated in Java")
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == COOKIE_REQ && resultCode == RESULT_OK) {
        val uri = data?.data ?: return
        try {
            contentResolver.openInputStream(uri)?.use { input ->
                cookieFile().outputStream().use { out -> input.copyTo(out) }
            }
            status.text = "Çerez dosyası yüklendi."
        } catch (e: Exception) {
            status.text = "Çerez hatası: " + e.message
        }
    }
}
    private fun saveToDownloads(f: File): Boolean {
        if (Build.VERSION.SDK_INT < 29) return false
        val values = ContentValues()
        values.put(MediaStore.Downloads.DISPLAY_NAME, f.name)
        values.put(
            MediaStore.Downloads.RELATIVE_PATH,
            Environment.DIRECTORY_DOWNLOADS + "/Kervan"
        )
        val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: return false
        contentResolver.openOutputStream(uri)?.use { out ->
            f.inputStream().use { it.copyTo(out) }
        }
        f.delete()
        return true
    }
}
