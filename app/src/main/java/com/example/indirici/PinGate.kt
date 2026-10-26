package com.example.indirici

import android.app.Activity
import android.content.Context
import android.content.Intent
import java.security.MessageDigest
import java.util.UUID

object PinGate {
    private const val PREF = "pin"
    private const val TIMEOUT = 15000L
    private var unlocked = false
    private var lastPause = 0L

    private fun prefs(c: Context) = c.getSharedPreferences(PREF, Context.MODE_PRIVATE)

    fun hasPin(c: Context) = prefs(c).getString("hash", null) != null

    private fun hash(pin: String, salt: String): String {
        val d = MessageDigest.getInstance("SHA-256").digest((salt + pin).toByteArray())
        return d.joinToString("") { "%02x".format(it) }
    }

    fun setPin(c: Context, pin: String) {
        val salt = UUID.randomUUID().toString()
        prefs(c).edit().putString("salt", salt).putString("hash", hash(pin, salt)).apply()
        unlock()
    }

    private fun unlock() {
        unlocked = true
        lastPause = System.currentTimeMillis()
    }

    fun lockRemaining(c: Context): Int {
        val until = prefs(c).getLong("lockUntil", 0L)
        return ((until - System.currentTimeMillis() + 999) / 1000).toInt().coerceAtLeast(0)
    }

    fun verify(c: Context, pin: String): Boolean {
        if (lockRemaining(c) > 0) return false
        val p = prefs(c)
        val ok = hash(pin, p.getString("salt", "") ?: "") == p.getString("hash", "")
        if (ok) {
            p.edit().putInt("fails", 0).apply()
            unlock()
        } else {
            val f = p.getInt("fails", 0) + 1
            if (f >= 5) {
                p.edit().putInt("fails", 0)
                    .putLong("lockUntil", System.currentTimeMillis() + 30000L).apply()
            } else {
                p.edit().putInt("fails", f).apply()
            }
        }
        return ok
    }

    fun touch() {
        lastPause = System.currentTimeMillis()
    }

    fun check(a: Activity) {
        if (!unlocked || System.currentTimeMillis() - lastPause > TIMEOUT) {
            unlocked = false
            a.startActivity(Intent(a, PinActivity::class.java))
        }
    }
}
