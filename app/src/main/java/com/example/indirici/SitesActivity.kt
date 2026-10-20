package com.example.indirici

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class SitesActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val scroll = ScrollView(this)
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(48, 96, 48, 48)
        scroll.addView(root)

        header(root, "Siteler", 28f)
        header(root, "Favoriler", 18f)
        site(root, "YouTube", "https://m.youtube.com")
        site(root, "Facebook", "https://m.facebook.com")
        site(root, "Instagram", "https://www.instagram.com")
        site(root, "TikTok", "https://www.tiktok.com")
        site(root, "Vimeo", "https://vimeo.com")
        site(root, "YouTube Music", "https://music.youtube.com")
        site(root, "Pinterest", "https://www.pinterest.com")

        header(root, "Eğlence", 18f)
        site(root, "TED", "https://www.ted.com")
        site(root, "Snapchat", "https://www.snapchat.com")

        val adult = LinearLayout(this)
        adult.orientation = LinearLayout.VERTICAL
        adult.visibility = View.GONE
        site(adult, "XVideos", "https://www.xvideos.com")
        site(adult, "XNXX", "https://www.xnxx.com")
        site(adult, "Pornhub", "https://www.pornhub.com")
        site(adult, "Eporner", "https://www.eporner.com")
        site(adult, "xHamster", "https://xhamster.com")
        site(adult, "RedTube", "https://www.redtube.com")
        site(adult, "YouPorn", "https://www.youporn.com")
        site(adult, "Spankbang", "https://spankbang.com")
        site(adult, "Thumbzilla", "https://www.thumbzilla.com")
        site(adult, "tube8", "https://www.tube8.com")
        site(adult, "TXXX", "https://txxx.com")

        val toggle = Button(this)
        toggle.text = "18+ siteleri göster"
        toggle.setOnClickListener {
            if (adult.visibility == View.GONE) {
                adult.visibility = View.VISIBLE
                toggle.text = "18+ siteleri gizle"
            } else {
                adult.visibility = View.GONE
                toggle.text = "18+ siteleri göster"
            }
        }
        header(root, "18+", 18f)
        root.addView(toggle)
        root.addView(adult)

        setContentView(scroll)
    }

    private fun header(parent: LinearLayout, text: String, size: Float) {
        val t = TextView(this)
        t.text = text
        t.textSize = size
        t.setPadding(0, 32, 0, 8)
        parent.addView(t)
    }

    private fun site(parent: LinearLayout, name: String, url: String) {
        val b = Button(this)
        b.text = name
        b.setOnClickListener {
            val i = Intent(this, BrowserActivity::class.java)
            i.putExtra("url", url)
            startActivity(i)
        }
        parent.addView(b)
    }
}
