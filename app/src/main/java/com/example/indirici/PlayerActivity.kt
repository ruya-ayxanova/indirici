package com.example.indirici

import android.app.Activity
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView

class PlayerActivity : Activity() {
    private lateinit var video: VideoView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.BLACK
        window.navigationBarColor = Color.BLACK
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val root = FrameLayout(this)
        root.setBackgroundColor(Color.BLACK)

        video = VideoView(this)
        root.addView(
            video,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
                Gravity.CENTER
            )
        )

        val title = Ui.text(this, intent.getStringExtra("name") ?: "", 14f, "#FFFFFF")
        title.setPadding(Ui.dp(this, 16), Ui.dp(this, 12), Ui.dp(this, 16), 0)
        root.addView(
            title,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.TOP
            )
        )

        val controller = MediaController(this)
        controller.setAnchorView(video)
        video.setMediaController(controller)
        video.setOnErrorListener { _, _, _ ->
            Toast.makeText(this, "Bu dosya oynatılamadı", Toast.LENGTH_LONG).show()
            true
        }
        video.setVideoURI(Uri.parse(intent.getStringExtra("uri") ?: ""))
        video.setOnPreparedListener { it.start() }

        setContentView(root)
    }

    override fun onPause() {
        super.onPause()
        video.pause()
    }
}
