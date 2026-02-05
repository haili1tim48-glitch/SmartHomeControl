package com.example.smarthomecontrol

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.VideoView
import androidx.activity.ComponentActivity

class ExpertVideoActivity : ComponentActivity() {

    private var gestureLabel: String = ""
    private lateinit var videoView: VideoView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expert_video)

        gestureLabel = intent.getStringExtra(GestureConstants.EXTRA_GESTURE_LABEL) ?: ""

        val tvGestureLabel = findViewById<TextView>(R.id.tv_gesture_label)
        tvGestureLabel.text = gestureLabel

        videoView = findViewById(R.id.videoViewExpert)
        videoView.visibility = View.VISIBLE

        val btnBack = findViewById<Button>(R.id.btn_back_to_main)
        btnBack.setOnClickListener {
            finish()
        }

        val btnPractice = findViewById<Button>(R.id.btn_practice)
        btnPractice.setOnClickListener {
            val intent = Intent(this, PracticeActivity::class.java)
            intent.putExtra(GestureConstants.EXTRA_GESTURE_LABEL, gestureLabel)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        playExpertVideo()
    }

    private fun playExpertVideo() {
        val rawName = GestureConstants.videoResourceMap[gestureLabel]
        if (rawName != null) {
            val resId = resources.getIdentifier(rawName.lowercase(), "raw", packageName)
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
