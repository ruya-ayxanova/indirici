package com.example.indirici

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView

class SitesActivity : Activity() {
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

        root.addView(Ui.text(this, "Siteler", 30f, Ui.TEXT, true))
        root.addView(Ui.text(this, "Bir site aç, videonun yanındaki oka dokun", 14f, Ui.SUB))

        section(root, "FAVORİLER")
        site(root, "YouTube", "https://m.youtube.com", "#E62117")
        site(root, "Facebook", "https://m.facebook.com", "#1877F2")
        site(root, "Instagram", "https://www.instagram.com", "#C13584")
        site(root, "TikTok", "https://www.tiktok.com", "#2A2A2A")
        site(root, "Vimeo", "https://vimeo.com", "#1AB7EA")
        site(root, "YouTube Music", "https://music.youtube.com", "#E62117")
        site(root, "Pinterest", "https://www.pinterest.com", "#BD081C")

        section(root, "EĞLENCE")
        site(root, "TED", "https://www.ted.com", "#E62B1E")
        site(root, "Snapchat", "https://www.snapchat.com", "#C9B800")

        section(root, "18+")
        val adult = LinearLayout(this)
        adult.orientation = LinearLayout.VERTICAL
        adult.visibility = View.GONE
        val c = Ui.PRIMARY
        site(adult, "XVideos", "https://www.xvideos.com", c)
        site(adult, "XNXX", "https://www.xnxx.com", c)
        site(adult, "Pornhub", "https://www.pornhub.com", c)
        site(adult, "Eporner", "https://www.eporner.com", c)
        site(adult, "xHamster", "https://xhamster.com", c)
        site(adult, "RedTube", "https://www.redtube.com", c)
        site(adult, "YouPorn", "https://www.youporn.com", c)
        site(adult, "Spankbang", "https://spankbang.com", c)
        site(adult, "Thumbzilla", "https://www.thumbzilla.com", c)
        site(adult, "tube8", "https://www.tube8.com", c)
        site(adult, "TXXX", "https://txxx.com", c)

        val toggle = Ui.button(this, "18+ siteleri göster", Ui.FIELD)
        toggle.setOnClickListener {
            if (adult.visibility == View.GONE) {
                adult.visibility = View.VISIBLE
                toggle.text = "18+ siteleri gizle"
            } else {
                adult.visibility = View.GONE
                toggle.text = "18+ siteleri göster"
            }
        }
        root.addView(toggle, Ui.lp(this, 8))
        root.addView(adult)

        setContentView(scroll)
    }

    private fun section(parent: LinearLayout, title: String) {
        val t = Ui.text(this, title, 13f, Ui.SUB, true)
        parent.addView(t, Ui.lp(this, 24))
    }

    private fun site(parent: LinearLayout, name: String, url: String, color: String) {
        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.gravity = Gravity.CENTER_VERTICAL
        row.background = Ui.round(this, Ui.CARD, 16)
        row.setPadding(
            Ui.dp(this, 14), Ui.dp(this, 12), Ui.dp(this, 14), Ui.dp(this, 12)
        )

        val badge = Ui.text(this, name.substring(0, 1).uppercase(), 18f, "#FFFFFF", true)
        badge.gravity = Gravity.CENTER
        badge.background = Ui.circle(color)
        row.addView(badge, LinearLayout.LayoutParams(Ui.dp(this, 40), Ui.dp(this, 40)))

        val label = Ui.text(this, name, 17f, Ui.TEXT, true)
        val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        lp.leftMargin = Ui.dp(this, 14)
        row.addView(label, lp)
        row.addView(Ui.text(this, "›", 22f, Ui.SUB))

        row.setOnClickListener {
            val i = Intent(this, BrowserActivity::class.java)
            i.putExtra("url", url)
            startActivity(i)
        }
        parent.addView(row, Ui.lp(this, 8))
    }
}
