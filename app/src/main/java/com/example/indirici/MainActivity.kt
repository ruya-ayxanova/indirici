package com.example.indirici

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView

class MainActivity : Activity() {
    private var audio = false

    private fun tile(title: String, sub: String, target: Class<*>): LinearLayout {
        val c = Ui.card(this)
        c.addView(Ui.text(this, title, 18f, Ui.TEXT, true))
        c.addView(Ui.text(this, sub, 13f, Ui.SUB))
        c.setOnClickListener { startActivity(Intent(this, target)) }
        return c
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.parseColor(Ui.BG)
        window.navigationBarColor = Color.parseColor(Ui.BG)

        val scroll = ScrollView(this)
        scroll.setBackgroundColor(Color.parseColor(Ui.BG))
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        val pad = Ui.dp(this, 20)
        root.setPadding(pad, Ui.dp(this, 32), pad, pad)
        scroll.addView(root)

        root.addView(Ui.text(this, "Kervan", 34f, Ui.TEXT, true))
        root.addView(Ui.text(this, "Video ve müzik indirici", 15f, Ui.SUB))

        val tiles = LinearLayout(this)
        tiles.orientation = LinearLayout.HORIZONTAL
        val t1 = LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
        )
        t1.rightMargin = Ui.dp(this, 6)
        val t2 = LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
        )
        t2.leftMargin = Ui.dp(this, 6)
        tiles.addView(
            tile("Siteler", "Siteyi aç, oka dokun", SitesActivity::class.java), t1
        )
        tiles.addView(
            tile("İndirilenler", "Kaydedilenleri oynat", DownloadsActivity::class.java), t2
        )
        root.addView(tiles, Ui.lp(this, 20))

        val card = Ui.card(this)
        card.addView(Ui.text(this, "Bağlantı", 14f, Ui.SUB))
        val input = EditText(this)
        input.hint = "Bağlantıyı yapıştır"
        input.setHintTextColor(Color.parseColor(Ui.SUB))
        input.setTextColor(Color.WHITE)
        input.setSingleLine(true)
        input.background = Ui.round(this, Ui.FIELD, 12)
        input.setPadding(
            Ui.dp(this, 14), Ui.dp(this, 12), Ui.dp(this, 14), Ui.dp(this, 12)
        )
        card.addView(input, Ui.lp(this, 8))

        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        val bVideo = Ui.button(this, "Video", Ui.PRIMARY)
        val bAudio = Ui.button(this, "Müzik (MP3)", Ui.FIELD)
        val left = LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
        )
        left.rightMargin = Ui.dp(this, 6)
        val right = LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
        )
        right.leftMargin = Ui.dp(this, 6)
        row.addView(bVideo, left)
        row.addView(bAudio, right)
        card.addView(row, Ui.lp(this, 12))
        bVideo.setOnClickListener {
            audio = false
            bVideo.background = Ui.round(this, Ui.PRIMARY, 14)
            bAudio.background = Ui.round(this, Ui.FIELD, 14)
        }
        bAudio.setOnClickListener {
            audio = true
            bAudio.background = Ui.round(this, Ui.PRIMARY, 14)
            bVideo.background = Ui.round(this, Ui.FIELD, 14)
        }

        val go = Ui.button(this, "İndir", Ui.ACCENT)
        card.addView(go, Ui.lp(this, 12))
        root.addView(card, Ui.lp(this, 16))

        val statusCard = Ui.card(this)
        val status = Ui.text(
            this, "Motor hazırlanıyor, ilk açılışta bir dakika sürebilir...", 15f, Ui.TEXT
        )
        status.setTextIsSelectable(true)
        statusCard.addView(status)
        val bar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
        bar.max = 100
        bar.progressTintList = ColorStateList.valueOf(Color.parseColor(Ui.ACCENT))
        bar.visibility = View.GONE
        statusCard.addView(bar, Ui.lp(this, 12))
        root.addView(statusCard, Ui.lp(this, 16))

        Engine.init(this) { err ->
            status.text = if (err == null) {
                "Hazır. Bağlantıyı yapıştır."
            } else {
                "Motor hatası: " + err
            }
        }

        go.setOnClickListener {
            val url = input.text.toString().trim()
            if (!Engine.ready) {
                status.text = "Motor henüz hazır değil, biraz bekle."
                return@setOnClickListener
            }
            if (!url.startsWith("http")) {
                status.text = "Geçerli bir bağlantı yapıştır."
                return@setOnClickListener
            }
            go.isEnabled = false
            status.text = "Başlıyor..."
            bar.progress = 0
            bar.visibility = View.VISIBLE
            Engine.download(
                this, url, audio,
                { p ->
                    status.text = "İndiriliyor: %" + p
                    bar.progress = p
                },
                { msg ->
                    status.text = msg
                    bar.visibility = View.GONE
                    go.isEnabled = true
                }
            )
        }

        setContentView(scroll)
    }
}
