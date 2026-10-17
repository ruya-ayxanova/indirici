package com.example.indirici

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
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

        val button = Button(this)
        button.text = "İndir"

        val status = TextView(this)
        status.textSize = 16f
        status.setPadding(0, 32, 0, 0)

        button.setOnClickListener {
            val url = input.text.toString().trim()
            if (!url.startsWith("http")) {
                status.text = "Geçerli bir bağlantı yapıştır"
                return@setOnClickListener
            }
            try {
                val name = Uri.parse(url).lastPathSegment ?: "kervan_indirme"
                val req = DownloadManager.Request(Uri.parse(url))
                req.setTitle(name)
                req.setNotificationVisibility(
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                )
                req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name)
                val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                dm.enqueue(req)
                status.text = "İndirme başladı. Bildirimden takip edebilirsin."
            } catch (e: Exception) {
                status.text = "Hata: " + e.message
            }
        }

        root.addView(title)
        root.addView(input)
        root.addView(button)
        root.addView(status)
        setContentView(root)
    }
}
