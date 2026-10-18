package com.example.indirici

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import java.io.File
import java.net.URI

class BrowserActivity : Activity() {
    private lateinit var web: WebView
    private lateinit var urlBox: EditText
    private lateinit var info: TextView
    private lateinit var root: FrameLayout
    private lateinit var page: LinearLayout
    private var customView: View? = null
    private var customCallback: WebChromeClient.CustomViewCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        root = FrameLayout(this)
        page = LinearLayout(this)
        page.orientation = LinearLayout.VERTICAL
        page.setPadding(0, 96, 0, 0)

        val bar = LinearLayout(this)
        bar.orientation = LinearLayout.HORIZONTAL
        urlBox = EditText(this)
        urlBox.setSingleLine(true)
        urlBox.setText("https://www.instagram.com/")
        bar.addView(urlBox, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        val go = Button(this)
        go.text = "Git"
        bar.addView(go)

        web = WebView(this)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.userAgentString =
            "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(web, true)
        web.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                if (url != null) urlBox.setText(url)
            }
        }
        web.webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                if (view == null) return
                customView = view
                customCallback = callback
                page.visibility = View.GONE
                root.addView(view, FrameLayout.LayoutParams(-1, -1))
            }
            override fun onHideCustomView() {
                customView?.let { root.removeView(it) }
                customView = null
                page.visibility = View.VISIBLE
                customCallback?.onCustomViewHidden()
            }
        }

        info = TextView(this)
        info.textSize = 14f
        info.text = "Instagram'a giriş yap, sonra çerezleri kaydet."

        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        val saveBtn = Button(this)
        saveBtn.text = "Çerezleri kaydet"
        val sendBtn = Button(this)
        sendBtn.text = "İndirmeye gönder"
        row.addView(saveBtn, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        row.addView(sendBtn, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        page.addView(bar)
        page.addView(web, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        page.addView(info)
        page.addView(row)
        root.addView(page)
        setContentView(root)

        fun open() {
            var u = urlBox.text.toString().trim()
            if (u.isEmpty()) return
            if (!u.startsWith("http")) u = "https://" + u
            web.loadUrl(u)
        }
        go.setOnClickListener { open() }
        saveBtn.setOnClickListener { saveCookies() }
        sendBtn.setOnClickListener {
            val i = Intent()
            i.putExtra("url", web.url ?: "")
            setResult(RESULT_OK, i)
            finish()
        }

        open()
    }

    private fun saveCookies() {
        val url = web.url ?: return
        val host = URI(url).host ?: return
        val parts = host.split(".")
        val domain = "." + (if (parts.size >= 2) parts.takeLast(2).joinToString(".") else host)
        val raw = CookieManager.getInstance().getCookie(url)
        if (raw.isNullOrEmpty()) {
            info.text = "Bu sitede çerez yok, önce giriş yap."
            return
        }
        val expiry = System.currentTimeMillis() / 1000 + 31536000
        val file = File(filesDir, "cookies.txt")
        val old = if (file.exists()) {
            file.readLines().filter { it.isNotBlank() && !it.startsWith("#") && !it.startsWith(domain) }
        } else emptyList()
        val sb = StringBuilder("# Netscape HTTP Cookie File\n")
        old.forEach { sb.append(it).append("\n") }
        for (c in raw.split(";")) {
            val kv = c.trim().split("=", limit = 2)
            if (kv.size == 2) {
                sb.append(domain).append("\tTRUE\t/\tTRUE\t").append(expiry)
                    .append("\t").append(kv[0]).append("\t").append(kv[1]).append("\n")
            }
        }
        file.writeText(sb.toString())
        info.text = "Çerezler kaydedildi: " + domain
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (customView != null) {
            web.webChromeClient?.onHideCustomView()
        } else if (web.canGoBack()) {
            web.goBack()
        } else {
            finish()
        }
    }
}
