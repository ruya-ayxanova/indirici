package com.example.indirici

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout

class PinActivity : Activity() {
    private var first: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.parseColor(Ui.BG)
        window.navigationBarColor = Color.parseColor(Ui.BG)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.gravity = Gravity.CENTER
        root.setBackgroundColor(Color.parseColor(Ui.BG))
        val pad = Ui.dp(this, 24)
        root.setPadding(pad, pad, pad, pad)

        val hasPin = PinGate.hasPin(this)
        val title = Ui.text(this, if (hasPin) "PIN gir" else "Yeni 6 haneli PIN belirle", 22f, Ui.TEXT, true)
        title.gravity = Gravity.CENTER
        val msg = Ui.text(this, " ", 14f, Ui.SUB)
        msg.gravity = Gravity.CENTER

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        input.filters = arrayOf(InputFilter.LengthFilter(6))
        input.gravity = Gravity.CENTER
        input.textSize = 28f
        input.setTextColor(Color.WHITE)
        input.background = Ui.round(this, Ui.FIELD, 12)
        input.setPadding(Ui.dp(this, 14), Ui.dp(this, 12), Ui.dp(this, 14), Ui.dp(this, 12))

        root.addView(title, Ui.lp(this, 16))
        root.addView(input, Ui.lp(this, 16))
        root.addView(msg, Ui.lp(this, 0))
        setContentView(root)
        input.requestFocus()

        input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val pin = s?.toString() ?: return
                if (pin.length < 6) return
                input.setText("")
                if (!PinGate.hasPin(this@PinActivity)) {
                    val f = first
                    if (f == null) {
                        first = pin
                        title.text = "PIN'i tekrar gir"
                        msg.text = " "
                    } else if (f == pin) {
                        PinGate.setPin(this@PinActivity, pin)
                        finish()
                    } else {
                        first = null
                        title.text = "Yeni 6 haneli PIN belirle"
                        msg.text = "PIN'ler uyuşmadı, tekrar dene."
                    }
                } else {
                    val wait = PinGate.lockRemaining(this@PinActivity)
                    if (wait > 0) {
                        msg.text = "Çok fazla deneme. " + wait + " sn bekle."
                    } else if (PinGate.verify(this@PinActivity, pin)) {
                        finish()
                    } else {
                        val w = PinGate.lockRemaining(this@PinActivity)
                        msg.text = if (w > 0) "Çok fazla deneme. " + w + " sn bekle." else "Yanlış PIN."
                    }
                }
            }
        })
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }
}
