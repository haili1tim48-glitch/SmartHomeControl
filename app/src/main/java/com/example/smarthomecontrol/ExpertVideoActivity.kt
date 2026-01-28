package com.example.smarthomecontrol

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.VideoView
import androidx.activity.ComponentActivity

class ExpertVideoActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expert_video)

        val gestureLabel = intent.getStringExtra(GestureConstants.EXTRA_GESTURE_LABEL) ?: ""

        val tvGestureLabel = findViewById<TextView>(R.id.tv_gesture_label)
        tvGestureLabel.text = gestureLabel

        val btnBack = findViewById<Button>(R.id.btn_back_to_main)
        btnBack.setOnClickListener {
            finish()
        }

        val videoView = findViewById<VideoView>(R.id.videoViewExpert)
        val rawName = GestureConstants.videoResourceMap[gestureLabel]
        if (rawName != null) {
            val resId = resources.getIdentifier(rawName, "raw", packageName)
            if (resId != 0) {
                val uri = Uri.parse("android.resource://$packageName/$resId")
                videoView.setVideoURI(uri)
                videoView.setOnPreparedListener { mp ->
                    mp.isLooping = true
                }
                videoView.start()
            }
        }
    }
}
