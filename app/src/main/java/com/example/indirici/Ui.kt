package com.example.indirici

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

object Ui {
    const val BG = "#0F1230"
    const val CARD = "#1B1F52"
    const val FIELD = "#272B66"
    const val PRIMARY = "#3F42C0"
    const val ACCENT = "#E0283C"
    const val TEXT = "#FFFFFF"
    const val SUB = "#A9ADD9"

    fun dp(c: Context, v: Int): Int = (v * c.resources.displayMetrics.density).toInt()

    fun round(c: Context, color: String, radius: Int): GradientDrawable {
        val g = GradientDrawable()
        g.setColor(Color.parseColor(color))
        g.cornerRadius = dp(c, radius).toFloat()
        return g
    }

    fun circle(color: String): GradientDrawable {
        val g = GradientDrawable()
        g.shape = GradientDrawable.OVAL
        g.setColor(Color.parseColor(color))
        return g
    }

    fun text(c: Context, s: String, size: Float, color: String, bold: Boolean = false): TextView {
        val t = TextView(c)
        t.text = s
        t.textSize = size
        t.setTextColor(Color.parseColor(color))
        if (bold) t.setTypeface(null, Typeface.BOLD)
        return t
    }

    fun button(c: Context, s: String, color: String): Button {
        val b = Button(c)
        b.text = s
        b.isAllCaps = false
        b.textSize = 16f
        b.setTextColor(Color.WHITE)
        b.setTypeface(null, Typeface.BOLD)
        b.background = round(c, color, 14)
        b.stateListAnimator = null
        b.setPadding(dp(c, 16), dp(c, 12), dp(c, 16), dp(c, 12))
        return b
    }

    fun card(c: Context): LinearLayout {
        val l = LinearLayout(c)
        l.orientation = LinearLayout.VERTICAL
        l.background = round(c, CARD, 20)
        l.setPadding(dp(c, 16), dp(c, 16), dp(c, 16), dp(c, 16))
        return l
    }

    fun lp(c: Context, top: Int = 0): LinearLayout.LayoutParams {
        val p = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        p.topMargin = dp(c, top)
        return p
    }
}
