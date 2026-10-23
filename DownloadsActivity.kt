package com.example.indirici

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView

class DownloadsActivity : Activity() {
    private lateinit var list: LinearLayout

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

        root.addView(Ui.text(this, "İndirilenler", 30f, Ui.TEXT, true))
        root.addView(Ui.text(this, "Dokun ve oynat", 14f, Ui.SUB))

        list = LinearLayout(this)
        list.orientation = LinearLayout.VERTICAL
        root.addView(list, Ui.lp(this, 16))

        setContentView(scroll)
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        list.removeAllViews()
        val items = Library.list(this)
        if (items.isEmpty()) {
            list.addView(Ui.text(this, "Henüz indirilen dosya yok.", 16f, Ui.SUB))
            return
        }
        for (item in items) {
            list.addView(row(item), Ui.lp(this, 8))
        }
    }

    private fun row(item: MediaItem): LinearLayout {
        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.gravity = Gravity.CENTER_VERTICAL
        row.background = Ui.round(this, Ui.CARD, 16)
        row.setPadding(
            Ui.dp(this, 14), Ui.dp(this, 12), Ui.dp(this, 8), Ui.dp(this, 12)
        )

        val badge = Ui.text(
            this, if (item.isAudio) "♪" else "▶", 18f, "#FFFFFF", true
        )
        badge.gravity = Gravity.CENTER
        badge.background = Ui.circle(if (item.isAudio) Ui.ACCENT else Ui.PRIMARY)
        row.addView(badge, LinearLayout.LayoutParams(Ui.dp(this, 44), Ui.dp(this, 44)))

        val col = LinearLayout(this)
        col.orientation = LinearLayout.VERTICAL
        val name = Ui.text(this, item.name, 15f, Ui.TEXT, true)
        name.maxLines = 2
        name.ellipsize = TextUtils.TruncateAt.END
        col.addView(name)
        col.addView(Ui.text(this, Library.formatSize(item.size), 12f, Ui.SUB))
        val colLp = LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
        )
        colLp.leftMargin = Ui.dp(this, 14)
        row.addView(col, colLp)

        val trash = Ui.text(this, "🗑", 20f, Ui.SUB)
        trash.setPadding(Ui.dp(this, 12), Ui.dp(this, 8), Ui.dp(this, 12), Ui.dp(this, 8))
        trash.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Silinsin mi?")
                .setMessage(item.name)
                .setPositiveButton("Sil") { _, _ ->
                    Library.delete(this, item)
                    refresh()
                }
                .setNegativeButton("Vazgeç", null)
                .show()
        }
        row.addView(trash)

        row.setOnClickListener {
            val i = Intent(this, PlayerActivity::class.java)
            i.putExtra("uri", item.uri.toString())
            i.putExtra("name", item.name)
            startActivity(i)
        }
        return row
    }
}
