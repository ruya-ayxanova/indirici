package com.example.indirici

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast

class BrowserActivity : Activity() {
    private lateinit var web: WebView
    private lateinit var root: FrameLayout
    private lateinit var fab: TextView
    private val handler = Handler(Looper.getMainLooper())
    private var busy = false

    private val js = "(function(){var v=document.querySelector('video');" +
        "if(!v){return '';}var r=v.getBoundingClientRect();" +
        "if(r.width<120||r.height<80){return '';}" +
        "return r.top+','+r.right;})()"

    private val poll = object : Runnable {
        override fun run() {
            web.evaluateJavascript(js) { result -> showButton(result) }
            handler.postDelayed(this, 1000)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        root = FrameLayout(this)
        web = WebView(this)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.webViewClient = WebViewClient()
        root.addView(
            web,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        val d = resources.displayMetrics.density
        val size = (56 * d).toInt()
        fab = TextView(this)
        fab.text = "⬇"
        fab.textSize = 20f
        fab.setTextColor(Color.WHITE)
        fab.gravity = Gravity.CENTER
        val bg = GradientDrawable()
        bg.shape = GradientDrawable.OVAL
        bg.setColor(Color.parseColor("#A01828"))
        fab.background = bg
        fab.visibility = View.GONE
        fab.setOnClickListener { startDownload(false) }
        fab.setOnLongClickListener {
            startDownload(true)
            true
        }
        root.addView(fab, FrameLayout.LayoutParams(size, size))

        setContentView(root)
        web.loadUrl(intent.getStringExtra("url") ?: "https://www.google.com")
    }

    private fun showButton(result: String?) {
        val s = result?.trim('"') ?: ""
        val parts = s.split(",")
        if (s.isEmpty() || parts.size < 2) {
            if (!busy) fab.visibility = View.GONE
            return
        }
        val top = parts[0].toFloatOrNull() ?: return
        val right = parts[1].toFloatOrNull() ?: return
        val d = resources.displayMetrics.density
        val size = (56 * d)
        val maxX = maxOf(root.width - size, 0f)
        val maxY = maxOf(root.height - size, 0f)
        fab.x = (right * d - size - 8 * d).coerceIn(0f, maxX)
        fab.y = (top * d + 8 * d).coerceIn(0f, maxY)
        fab.visibility = View.VISIBLE
    }

    private fun startDownload(audio: Boolean) {
        if (busy) return
        val u = web.url ?: return
        busy = true
        fab.text = "…"
        Toast.makeText(this, "İndirme başlıyor...", Toast.LENGTH_SHORT).show()
        Engine.init(this) { err ->
            if (err != null) {
                finishDownload("Motor hatası: " + err)
            } else {
                Engine.download(
                    this, u, audio,
                    { p -> fab.text = p.toString() + "%" },
                    { msg -> finishDownload(msg) }
                )
            }
        }
    }

    private fun finishDownload(msg: String) {
        busy = false
        fab.text = "⬇"
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        handler.post(poll)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(poll)
    }

    override fun onDestroy() {
        super.onDestroy()
        web.destroy()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (web.canGoBack()) web.goBack() else super.onBackPressed()
    }
}
