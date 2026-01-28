package com.example.smarthomecontrol

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.smarthomecontrol.databinding.ActivityVideoPlayBinding

class VideoPlayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVideoPlayBinding
    private var gesture: Gesture? = null
    private var playCount = 0
    private val minPlays = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gestureName = intent.getStringExtra(MainActivity.EXTRA_GESTURE_NAME)
        gesture = Gesture.getByName(gestureName ?: "")

        if (gesture == null) {
            Toast.makeText(this, getString(R.string.error_gesture_not_found), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupUI()
        setupVideoView()
        setupButtons()
    }

    private fun setupUI() {
        gesture?.let {
            binding.gestureNameText.text = it.displayName
        }
        updatePlayCountText()
    }

    private fun setupVideoView() {
        gesture?.let { g ->
            // Video files should be placed in res/raw folder
            // Named as: lighton.mp4, lightoff.mp4, etc.
            val videoResId = resources.getIdentifier(
                g.videoFileName,
                "raw",
                packageName
            )

            if (videoResId != 0) {
                val videoUri = Uri.parse("android.resource://$packageName/$videoResId")
                binding.videoView.setVideoURI(videoUri)

                binding.videoView.setOnPreparedListener { mediaPlayer ->
                    mediaPlayer.isLooping = false
                    binding.videoView.start()
                }

                binding.videoView.setOnCompletionListener {
                    onVideoCompleted()
                }
            } else {
                // If video file not found, show message
                Toast.makeText(
                    this,
                    getString(R.string.error_video_not_found, g.videoFileName),
                    Toast.LENGTH_LONG
                ).show()

                // For demo purposes, simulate video completion after delay
                binding.videoView.postDelayed({
                    onVideoCompleted()
                }, 2000)
            }
        }
    }

    private fun onVideoCompleted() {
        playCount++
        updatePlayCountText()

        if (playCount >= minPlays) {
            binding.practiceButton.isEnabled = true
        }
    }

    private fun updatePlayCountText() {
        binding.playCountText.text = getString(R.string.play_count_format, playCount, minPlays)
    }

    private fun setupButtons() {
        binding.replayButton.setOnClickListener {
            binding.videoView.seekTo(0)
            binding.videoView.start()
        }

        binding.practiceButton.setOnClickListener {
            gesture?.let { g ->
                val intent = Intent(this, RecordActivity::class.java).apply {
                    putExtra(MainActivity.EXTRA_GESTURE_NAME, g.name)
                }
                startActivity(intent)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (binding.videoView.isPlaying) {
            binding.videoView.pause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.videoView.stopPlayback()
    }
}
