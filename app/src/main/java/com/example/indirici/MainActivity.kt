package com.example.indirici

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
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

        val sites = Button(this)
        sites.text = "Siteler"
        sites.setOnClickListener {
            startActivity(Intent(this, SitesActivity::class.java))
        }

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

        val status = TextView(this)
        status.textSize = 16f
        status.setPadding(0, 32, 0, 0)
        status.text = "Motor hazırlanıyor, ilk açılışta bir dakika sürebilir..."

        Engine.init(this) { err ->
            status.text = if (err == null) {
                "Hazır. Bağlantıyı yapıştır."
            } else {
                "Motor hatası: " + err
            }
        }

        button.setOnClickListener {
            val url = input.text.toString().trim()
            if (!Engine.ready) {
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
            Engine.download(
                this, url, audio,
                { p -> status.text = "İndiriliyor: %" + p },
                { msg ->
                    status.text = msg
                    button.isEnabled = true
                }
            )
        }

        root.addView(title)
        root.addView(sites)
        root.addView(input)
        root.addView(group)
        root.addView(button)
        root.addView(status)
        setContentView(root)
    }
}
