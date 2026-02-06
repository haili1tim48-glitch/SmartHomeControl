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

    // Recording related
    private lateinit var tvTimer: TextView
    private lateinit var btnRecord: Button
    private lateinit var tvPracticeCount: TextView
    private lateinit var btnUpload: Button
    private var videoCapture: VideoCapture<Recorder>? = null
    private var currentRecording: Recording? = null
    private var countDownTimer: CountDownTimer? = null
    private var currentLensFacing: Int = CameraSelector.LENS_FACING_FRONT
    private var currentCount = 1

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

        val btnBack = findViewById<Button>(R.id.btn_back_to_expert)
        btnBack.setOnClickListener {
            finish()
        }

        btnRetryPermission.setOnClickListener {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        btnRecord.setOnClickListener {
            startRecordingWithCountdown()
        }

        btnUpload.setOnClickListener {
            uploadVideos()
        }

        checkCameraPermission()
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                onCameraPermissionGranted()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
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
                    .also {
                        it.setSurfaceProvider(viewFinder.getSurfaceProvider())
                    }

                // Setup video capture with recorder (no audio)
                val recorder = Recorder.Builder()
                    .setQualitySelector(QualitySelector.from(Quality.HD))
                    .build()
                videoCapture = VideoCapture.withOutput(recorder)

                cameraProvider?.unbindAll()

                // Try front camera first, fallback to back camera
                if (!tryBindCamera(preview, CameraSelector.LENS_FACING_FRONT)) {
                    Log.w(TAG, "Front camera not available, trying back camera")
                    if (!tryBindCamera(preview, CameraSelector.LENS_FACING_BACK)) {
                        Log.e(TAG, "No camera available on this device")
                        showCameraError("No camera available")
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Failed to get camera provider", e)
                e.printStackTrace()
                showCameraError("Camera initialization failed: ${e.localizedMessage}")
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun tryBindCamera(preview: Preview, lensFacing: Int): Boolean {
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        return try {
            // Check if the camera is available
            val hasCamera = cameraProvider?.hasCamera(cameraSelector) ?: false
            if (!hasCamera) {
                Log.d(TAG, "Camera with lens facing $lensFacing not available")
                return false
            }

            cameraProvider?.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                videoCapture
            )
            currentLensFacing = lensFacing
            Log.i(TAG, "Successfully bound to camera with lens facing $lensFacing")
            hidePermissionError()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to bind camera with lens facing $lensFacing", e)
            e.printStackTrace()
            false
        }
    }

    private fun startRecordingWithCountdown() {
        val videoCapture = videoCapture ?: run {
            Log.e(TAG, "VideoCapture is not initialized")
            return
        }

        // Disable record button and show timer
        btnRecord.isEnabled = false
        btnRecord.alpha = 0.5f
        tvTimer.visibility = View.VISIBLE
        tvTimer.text = "5"

        // Create output file: [GESTURE]_PRACTICE_[COUNT]_Liang.mp4
        val sanitizedGesture = gestureLabel.replace(" ", "_").uppercase()
        val fileName = "${sanitizedGesture}_PRACTICE_${currentCount}_Liang.mp4"
        val outputFile = File(getExternalFilesDir(null), fileName)

        Log.i(TAG, "Recording will be saved to: ${outputFile.absolutePath}")

        // Prepare recording with NO audio
        val fileOutputOptions = FileOutputOptions.Builder(outputFile).build()
        val pendingRecording = videoCapture.output
            .prepareRecording(this, fileOutputOptions)
            // Note: NOT calling .withAudioEnabled() to disable audio

        // Start recording
        currentRecording = pendingRecording.start(ContextCompat.getMainExecutor(this)) { event ->
            when (event) {
                is VideoRecordEvent.Start -> {
                    Log.i(TAG, "Recording started")
                }
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

        // Start 5-second countdown
        countDownTimer = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = (millisUntilFinished / 1000 + 1).toInt()
                tvTimer.text = secondsRemaining.toString()
                Log.d(TAG, "Countdown: $secondsRemaining")
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

        // Update UI
        tvTimer.text = "Done!"
        tvTimer.postDelayed({
            tvTimer.visibility = View.INVISIBLE

            // Increment count after recording is saved
            currentCount++

            if (currentCount <= 3) {
                // Update practice count text and re-enable record button
                tvPracticeCount.text = "Practice: $currentCount / 3"
                btnRecord.isEnabled = true
                btnRecord.alpha = 1.0f
            } else {
                // All 3 recordings complete
                tvPracticeCount.text = "Practice: Complete!"
                Toast.makeText(this, "All 3 videos recorded!", Toast.LENGTH_LONG).show()

                // Hide record button, show upload button
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

    // Upload progress tracking
    private val uploadSuccessCount = AtomicInteger(0)
    private val uploadErrorOccurred = AtomicBoolean(false)

    private fun uploadVideos() {
        val uploadUrl = "http://10.0.2.2:5000/upload"

        // Reset counters
        uploadSuccessCount.set(0)
        uploadErrorOccurred.set(false)

        // Get video files from app's external files directory
        val videoDir = getExternalFilesDir(null)
        Log.d("UploadDebug", "Video directory path: ${videoDir?.absolutePath}")

        if (videoDir == null || !videoDir.exists()) {
            Log.e(TAG, "Video directory not found")
            Toast.makeText(this, "Video directory not found", Toast.LENGTH_SHORT).show()
            return
        }

        // List all files in directory for debugging
        val allFiles = videoDir.listFiles()
        Log.d("UploadDebug", "All files in directory: ${allFiles?.map { it.name }}")

        // Filter files ending with _Liang.mp4
        val videoFiles = videoDir.listFiles { file ->
            file.isFile && file.name.endsWith("_Liang.mp4")
        }?.sortedBy { it.name }?.toList() ?: emptyList()

        Log.d("UploadDebug", "Filtered video files: ${videoFiles.map { it.name }}")

        if (videoFiles.size < 3) {
            Log.e(TAG, "Expected 3 videos, found ${videoFiles.size}")
            Toast.makeText(this, "Not all videos found (${videoFiles.size}/3)", Toast.LENGTH_SHORT).show()
            return
        }

        val filesToUpload = videoFiles.take(3)
        val totalFiles = filesToUpload.size

        Log.i(TAG, "Found $totalFiles videos to upload")
        Toast.makeText(this, "Starting upload of $totalFiles videos...", Toast.LENGTH_SHORT).show()

        // Disable upload button during upload
        btnUpload.isEnabled = false
        btnUpload.alpha = 0.5f

        // Create OkHttpClient
        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()

        // Upload each file separately using indexed loop
        for (index in filesToUpload.indices) {
            val videoFile = filesToUpload[index]
            val fileNumber = index + 1

            Log.d("UploadDebug", "Starting upload: ${videoFile.name} (file $fileNumber of $totalFiles)")

            // Build multipart request for single file
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "video",
                    videoFile.name,
                    videoFile.asRequestBody("video/mp4".toMediaType())
                )
                .build()

            val request = Request.Builder()
                .url(uploadUrl)
                .post(requestBody)
                .build()

            // Capture file info for callback
            val fileName = videoFile.name
            val fileNum = fileNumber

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("UploadDebug", "Upload FAILED for $fileName: ${e.message}", e)

                    if (uploadErrorOccurred.compareAndSet(false, true)) {
                        runOnUiThread {
                            btnUpload.isEnabled = true
                            btnUpload.alpha = 1.0f
                            Toast.makeText(
                                this@PracticeActivity,
                                "Upload failed for video #$fileNum ($fileName): ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    Log.d("UploadDebug", "Server response for $fileName: ${response.code}")

                    if (response.isSuccessful) {
                        val currentSuccess = uploadSuccessCount.incrementAndGet()
                        Log.d("UploadDebug", "Successfully sent: $fileName (progress: $currentSuccess / $totalFiles)")

                        if (currentSuccess == totalFiles && !uploadErrorOccurred.get()) {
                            runOnUiThread {
                                Log.i(TAG, "All $totalFiles videos uploaded successfully!")
                                Toast.makeText(
                                    this@PracticeActivity,
                                    "All 3 videos uploaded successfully!",
                                    Toast.LENGTH_LONG
                                ).show()
                                finish()
                            }
                        }
                    } else {
                        Log.e("UploadDebug", "Server error for $fileName: ${response.code}")

                        if (uploadErrorOccurred.compareAndSet(false, true)) {
                            runOnUiThread {
                                btnUpload.isEnabled = true
                                btnUpload.alpha = 1.0f
                                Toast.makeText(
                                    this@PracticeActivity,
                                    "Upload failed for video #$fileNum ($fileName): Server error ${response.code}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
            })
        }

        Log.d("UploadDebug", "All $totalFiles upload requests have been enqueued")
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        currentRecording?.stop()
        cameraProvider?.unbindAll()
        cameraExecutor.shutdown()
    }
}
