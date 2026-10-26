package com.example.indirici

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class BrowserActivity : Activity() {
    private lateinit var web: WebView
    private lateinit var root: FrameLayout
    private lateinit var fab: TextView
    private lateinit var input: EditText
    private lateinit var lockBtn: TextView
    private val handler = Handler(Looper.getMainLooper())
    private var busy = false
    private var hidden = false

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
        window.statusBarColor = Color.parseColor(Ui.BG)
        window.navigationBarColor = Color.parseColor(Ui.BG)
        hidden = intent.getBooleanExtra("hidden", false)

        root = FrameLayout(this)
        root.setBackgroundColor(Color.parseColor(Ui.BG))

        val col = LinearLayout(this)
        col.orientation = LinearLayout.VERTICAL

        val bar = LinearLayout(this)
        bar.orientation = LinearLayout.HORIZONTAL
        bar.gravity = Gravity.CENTER_VERTICAL
        bar.setBackgroundColor(Color.parseColor(Ui.BG))
        bar.setPadding(Ui.dp(this, 8), Ui.dp(this, 28), Ui.dp(this, 8), Ui.dp(this, 8))

        input = EditText(this)
        input.setSingleLine(true)
        input.hint = "Ara veya adres yaz"
        input.setHintTextColor(Color.parseColor(Ui.SUB))
        input.setTextColor(Color.WHITE)
        input.textSize = 15f
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.imeOptions = EditorInfo.IME_ACTION_GO
        input.background = Ui.round(this, Ui.FIELD, 12)
        input.setPadding(Ui.dp(this, 14), Ui.dp(this, 10), Ui.dp(this, 14), Ui.dp(this, 10))
        input.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                go()
                true
            } else {
                false
            }
        }
        bar.addView(
            input,
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        )

        lockBtn = TextView(this)
        lockBtn.textSize = 20f
        lockBtn.gravity = Gravity.CENTER
        lockBtn.setOnClickListener {
            hidden = !hidden
            updateLock()
            Toast.makeText(
                this,
                if (hidden) "Gizli indirme açık: videolar galeride görünmez."
                else "Normal indirme: videolar Indirilenler/Kervan klasörüne kaydedilir.",
                Toast.LENGTH_LONG
            ).show()
        }
        val lp = LinearLayout.LayoutParams(Ui.dp(this, 44), Ui.dp(this, 44))
        lp.leftMargin = Ui.dp(this, 8)
        bar.addView(lockBtn, lp)
        updateLock()

        web = WebView(this)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                if (!input.hasFocus() && url != null) input.setText(url)
            }
        }

        col.addView(
            bar,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
        col.addView(web, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
        root.addView(
            col,
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

    private fun updateLock() {
        lockBtn.text = if (hidden) "🔒" else "🔓"
        lockBtn.background = Ui.round(this, if (hidden) Ui.PRIMARY else Ui.FIELD, 12)
    }

    private fun go() {
        val q = input.text.toString().trim()
        if (q.isEmpty()) return
        val url = if (q.startsWith("http://") || q.startsWith("https://")) {
            q
        } else if (!q.contains(" ") && q.contains(".")) {
            "https://" + q
        } else {
            "https://www.google.com/search?q=" + Uri.encode(q)
        }
        web.loadUrl(url)
        input.clearFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(input.windowToken, 0)
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
        fab.y = (web.top + top * d + 8 * d).coerceIn(0f, maxY)
        fab.visibility = View.VISIBLE
    }

    private fun startDownload(audio: Boolean) {
        if (busy) return
        val u = web.url ?: return
        busy = true
        fab.text = "…"
        Toast.makeText(this, "İndirme başlıyor...", Toast.LENGTH_SHORT).show()
        val hide = hidden
        Engine.init(this) { err ->
            if (err != null) {
                finishDownload("Motor hatası: " + err)
            } else {
                Engine.download(
                    this, u, audio,
                    { p -> fab.text = p.toString() + "%" },
                    { msg -> finishDownload(msg) },
                    hide
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
        PinGate.check(this)
        handler.post(poll)
    }

    override fun onPause() {
        super.onPause()
        PinGate.touch()
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
