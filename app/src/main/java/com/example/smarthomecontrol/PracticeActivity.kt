package com.example.smarthomecontrol

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicBoolean

class PracticeActivity : ComponentActivity() {

    companion object {
        private const val TAG = "PracticeActivity"
    }

    private var gestureLabel: String = ""
    private lateinit var viewFinder: PreviewView
    private lateinit var cameraExecutor: ExecutorService
    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var permissionErrorLayout: LinearLayout
    private lateinit var tvErrorMessage: TextView
    private lateinit var btnRetryPermission: Button

    private lateinit var tvTimer: TextView
    private lateinit var btnRecord: Button
    private lateinit var tvPracticeCount: TextView
    private lateinit var btnUpload: Button
    private var videoCapture: VideoCapture<Recorder>? = null
    private var currentRecording: Recording? = null
    private var countDownTimer: CountDownTimer? = null
    private var currentLensFacing: Int = CameraSelector.LENS_FACING_FRONT
    private var currentCount = 1

    // Using AtomicInteger/AtomicBoolean to avoid race conditions in async upload callbacks
    private val clipsUploaded = AtomicInteger(0)
    private val uploadFailed = AtomicBoolean(false)

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                hidePermissionError()
                onCameraPermissionGranted()
            } else {
                Log.w(TAG, "Camera permission denied by user")
                showPermissionError("Camera permission is required to use this feature")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_practice)

        gestureLabel = intent.getStringExtra(GestureConstants.EXTRA_GESTURE_LABEL) ?: ""

        viewFinder = findViewById(R.id.viewFinder)
        cameraExecutor = Executors.newSingleThreadExecutor()
        permissionErrorLayout = findViewById(R.id.permissionErrorLayout)
        tvErrorMessage = findViewById(R.id.tvErrorMessage)
        btnRetryPermission = findViewById(R.id.btnRetryPermission)
        tvTimer = findViewById(R.id.tv_timer)
        btnRecord = findViewById(R.id.btn_record)
        tvPracticeCount = findViewById(R.id.tv_practice_count)
        btnUpload = findViewById(R.id.btn_upload)

        findViewById<Button>(R.id.btn_back_to_expert).setOnClickListener { finish() }

        btnRetryPermission.setOnClickListener {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        btnRecord.setOnClickListener { startRecordingWithCountdown() }

        btnUpload.setOnClickListener { uploadClips() }

        checkCameraPermission()
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            onCameraPermissionGranted()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun onCameraPermissionGranted() {
        startCamera()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder()
                    .build()
                    .also { it.setSurfaceProvider(viewFinder.getSurfaceProvider()) }

                val recorder = Recorder.Builder()
                    .setQualitySelector(QualitySelector.from(Quality.HD))
                    .build()
                videoCapture = VideoCapture.withOutput(recorder)

                cameraProvider?.unbindAll()

                // Front camera preferred; fall back to rear if unavailable
                if (!tryBindCamera(preview, CameraSelector.LENS_FACING_FRONT)) {
                    Log.w(TAG, "Front camera not available, trying back camera")
                    if (!tryBindCamera(preview, CameraSelector.LENS_FACING_BACK)) {
                        Log.e(TAG, "No camera available on this device")
                        showCameraError("No camera available")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get camera provider", e)
                showCameraError("Camera initialization failed: ${e.localizedMessage}")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun tryBindCamera(preview: Preview, lensFacing: Int): Boolean {
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        return try {
            if (cameraProvider?.hasCamera(cameraSelector) != true) {
                Log.d(TAG, "Camera with lens facing $lensFacing not available")
                return false
            }

            cameraProvider?.bindToLifecycle(this, cameraSelector, preview, videoCapture)
            currentLensFacing = lensFacing
            Log.i(TAG, "Successfully bound to camera with lens facing $lensFacing")
            hidePermissionError()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to bind camera with lens facing $lensFacing", e)
            false
        }
    }

    private fun startRecordingWithCountdown() {
        val videoCapture = videoCapture ?: run {
            Log.e(TAG, "VideoCapture is not initialized")
            return
        }

        btnRecord.isEnabled = false
        btnRecord.alpha = 0.5f
        tvTimer.visibility = View.VISIBLE
        tvTimer.text = "5"

        val sanitizedGesture = gestureLabel.replace(" ", "_").uppercase()
        val clipFile = File(
            getExternalFilesDir(null),
            "${sanitizedGesture}_PRACTICE_${currentCount}_Liang.mp4"
        )

        Log.i(TAG, "Recording will be saved to: ${clipFile.absolutePath}")

        // Audio intentionally disabled — gesture recognition only needs video
        val pendingRecording = videoCapture.output
            .prepareRecording(this, FileOutputOptions.Builder(clipFile).build())

        currentRecording = pendingRecording.start(ContextCompat.getMainExecutor(this)) { event ->
            when (event) {
                is VideoRecordEvent.Start -> Log.i(TAG, "Recording started")
                is VideoRecordEvent.Finalize -> {
                    if (event.hasError()) {
                        Log.e(TAG, "Recording error: ${event.error}, cause: ${event.cause}")
                        currentRecording?.close()
                        currentRecording = null
                    } else {
                        Log.i(TAG, "Recording saved: ${event.outputResults.outputUri}")
                    }
                }
            }
        }

        countDownTimer = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = (millisUntilFinished / 1000 + 1).toInt()
                tvTimer.text = secondsRemaining.toString()
            }

            override fun onFinish() {
                stopRecording()
            }
        }.start()
    }

    private fun stopRecording() {
        countDownTimer?.cancel()
        countDownTimer = null

        currentRecording?.stop()
        currentRecording = null

        Log.i(TAG, "Recording $currentCount stopped")

        tvTimer.text = "Done!"
        tvTimer.postDelayed({
            tvTimer.visibility = View.INVISIBLE
            currentCount++

            if (currentCount <= 3) {
                tvPracticeCount.text = "Practice: $currentCount / 3"
                btnRecord.isEnabled = true
                btnRecord.alpha = 1.0f
            } else {
                tvPracticeCount.text = "Practice: Complete!"
                Toast.makeText(this, "Recording complete: 3/3 clips saved.", Toast.LENGTH_LONG)
                    .show()
                btnRecord.visibility = View.GONE
                btnUpload.visibility = View.VISIBLE
            }
        }, 1500)
    }

    private fun showPermissionError(message: String) {
        tvErrorMessage.text = message
        permissionErrorLayout.visibility = View.VISIBLE
        btnRetryPermission.visibility = View.VISIBLE
    }

    private fun showCameraError(message: String) {
        tvErrorMessage.text = message
        permissionErrorLayout.visibility = View.VISIBLE
        btnRetryPermission.visibility = View.GONE
    }

    private fun hidePermissionError() {
        permissionErrorLayout.visibility = View.GONE
    }

    private fun uploadClips() {
        val uploadUrl = "http://10.0.2.2:5000/upload"
        val gestureName = gestureLabel.replace(" ", "_").uppercase()

        clipsUploaded.set(0)
        uploadFailed.set(false)

        val clipDir = getExternalFilesDir(null)
        Log.d(TAG, "Clip directory: ${clipDir?.absolutePath}, gesture: $gestureName")

        if (clipDir == null || !clipDir.exists()) {
            Log.e(TAG, "Clip directory not found")
            Toast.makeText(this, "Clip directory not found.", Toast.LENGTH_SHORT).show()
            return
        }

        val gesturePrefix = "${gestureName}_PRACTICE_"
        val gestureClips = clipDir.listFiles { file ->
            file.isFile &&
                file.name.startsWith(gesturePrefix) &&
                file.name.endsWith("_Liang.mp4")
        }?.sortedBy { it.name }?.toList() ?: emptyList()

        Log.d(TAG, "Matched clips: ${gestureClips.map { it.name }}")

        if (gestureClips.size < 3) {
            Log.e(TAG, "Expected 3 clips for '$gestureName', found ${gestureClips.size}")
            Toast.makeText(
                this,
                "Missing clips for $gestureName (${gestureClips.size}/3).",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val clipsToUpload = gestureClips.take(3)
        val totalClips = clipsToUpload.size

        Log.i(TAG, "Uploading $totalClips clips")
        Toast.makeText(this, "Uploading $totalClips clips...", Toast.LENGTH_SHORT).show()

        btnUpload.isEnabled = false
        btnUpload.alpha = 0.5f

        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()

        for ((clipIndex, clip) in clipsToUpload.withIndex()) {
            val clipNumber = clipIndex + 1
            val clipName = clip.name

            Log.d(TAG, "Enqueuing upload: $clipName ($clipNumber/$totalClips)")

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "video",
                    clipName,
                    clip.asRequestBody("video/mp4".toMediaType())
                )
                .build()

            val request = Request.Builder()
                .url(uploadUrl)
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e(TAG, "Upload failed for $clipName: ${e.message}", e)

                    if (uploadFailed.compareAndSet(false, true)) {
                        runOnUiThread {
                            btnUpload.isEnabled = true
                            btnUpload.alpha = 1.0f
                            Toast.makeText(
                                this@PracticeActivity,
                                "Upload failed: clip #$clipNumber — ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        val completed = clipsUploaded.incrementAndGet()
                        Log.d(TAG, "Uploaded $clipName ($completed/$totalClips)")

                        if (completed == totalClips && !uploadFailed.get()) {
                            // Clean up local clips to prevent stale files in future sessions
                            clipsToUpload.forEach { it.delete() }

                            runOnUiThread {
                                Toast.makeText(
                                    this@PracticeActivity,
                                    "Upload complete: $totalClips/$totalClips clips received.",
                                    Toast.LENGTH_LONG
                                ).show()
                                finish()
                            }
                        }
                    } else {
                        Log.e(
                            TAG,
                            "Server error for $clipName: ${response.code} — " +
                                "${response.body?.string()}"
                        )

                        if (uploadFailed.compareAndSet(false, true)) {
                            runOnUiThread {
                                btnUpload.isEnabled = true
                                btnUpload.alpha = 1.0f
                                Toast.makeText(
                                    this@PracticeActivity,
                                    "Upload failed: clip #$clipNumber — server error ${response.code}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        currentRecording?.stop()
        cameraProvider?.unbindAll()
        cameraExecutor.shutdown()
    }
}
