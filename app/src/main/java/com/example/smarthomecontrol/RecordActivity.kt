package com.example.smarthomecontrol

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.MediaRecorder
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.smarthomecontrol.databinding.ActivityRecordBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

class RecordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecordBinding
    private var gesture: Gesture? = null
    private var practiceNumber = 1

    // Camera2 API components
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var mediaRecorder: MediaRecorder? = null
    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null
    private val cameraOpenCloseLock = Semaphore(1)

    private var isRecording = false
    private var videoFile: File? = null
    private var previewSize: Size? = null

    private val surfaceTextureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            openCamera()
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = true
        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
    }

    private val stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraOpenCloseLock.release()
            cameraDevice = camera
            startPreview()
        }

        override fun onDisconnected(camera: CameraDevice) {
            cameraOpenCloseLock.release()
            camera.close()
            cameraDevice = null
        }

        override fun onError(camera: CameraDevice, error: Int) {
            cameraOpenCloseLock.release()
            camera.close()
            cameraDevice = null
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gestureName = intent.getStringExtra(MainActivity.EXTRA_GESTURE_NAME)
        gesture = Gesture.getByName(gestureName ?: "")

        if (gesture == null) {
            Toast.makeText(this, getString(R.string.error_gesture_not_found), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupUI()
        setupButtons()
        checkPermissions()
    }

    private fun setupUI() {
        gesture?.let {
            binding.gestureNameText.text = getString(R.string.recording_gesture, it.displayName)
        }
        updatePracticeCountText()
        binding.statusText.text = getString(R.string.status_ready)
    }

    private fun updatePracticeCountText() {
        binding.practiceCountText.text = getString(R.string.practice_count_format, practiceNumber)
    }

    private fun setupButtons() {
        binding.recordButton.setOnClickListener {
            if (!isRecording) {
                startRecording()
            }
        }

        binding.uploadButton.setOnClickListener {
            videoFile?.let { file ->
                uploadVideo(file)
            }
        }
    }

    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                if (binding.textureView.isAvailable) {
                    openCamera()
                }
            } else {
                Toast.makeText(this, getString(R.string.error_permissions_required), Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        startBackgroundThread()
        if (binding.textureView.isAvailable) {
            openCamera()
        } else {
            binding.textureView.surfaceTextureListener = surfaceTextureListener
        }
    }

    override fun onPause() {
        closeCamera()
        stopBackgroundThread()
        super.onPause()
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("CameraBackground").also { it.start() }
        backgroundHandler = Handler(backgroundThread!!.looper)
    }

    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        try {
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        try {
            // Find front-facing camera
            val cameraId = cameraManager.cameraIdList.find { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                facing == CameraCharacteristics.LENS_FACING_FRONT
            } ?: cameraManager.cameraIdList[0]

            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)

            // Choose preview size
            previewSize = map?.getOutputSizes(SurfaceTexture::class.java)
                ?.filter { it.width <= 1920 && it.height <= 1080 }
                ?.maxByOrNull { it.width * it.height }
                ?: Size(1280, 720)

            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw RuntimeException("Time out waiting to lock camera opening.")
            }

            cameraManager.openCamera(cameraId, stateCallback, backgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
            Toast.makeText(this, getString(R.string.error_camera_access), Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun closeCamera() {
        try {
            cameraOpenCloseLock.acquire()
            captureSession?.close()
            captureSession = null
            cameraDevice?.close()
            cameraDevice = null
            mediaRecorder?.release()
            mediaRecorder = null
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera closing.", e)
        } finally {
            cameraOpenCloseLock.release()
        }
    }

    private fun startPreview() {
        val texture = binding.textureView.surfaceTexture ?: return
        texture.setDefaultBufferSize(previewSize?.width ?: 1280, previewSize?.height ?: 720)
        val surface = Surface(texture)

        try {
            val previewRequestBuilder = cameraDevice?.createCaptureRequest(
                CameraDevice.TEMPLATE_PREVIEW
            )?.apply {
                addTarget(surface)
            } ?: return

            cameraDevice?.createCaptureSession(
                listOf(surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        if (cameraDevice == null) return
                        captureSession = session
                        try {
                            session.setRepeatingRequest(
                                previewRequestBuilder.build(),
                                null,
                                backgroundHandler
                            )
                        } catch (e: CameraAccessException) {
                            e.printStackTrace()
                        }
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        Toast.makeText(
                            this@RecordActivity,
                            getString(R.string.error_camera_config),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                backgroundHandler
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun setupMediaRecorder() {
        gesture?.let { g ->
            val fileName = "${g.name}_PRACTICE_${practiceNumber}_LIANG.mp4"
            videoFile = File(getExternalFilesDir(null), fileName)

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setVideoSource(MediaRecorder.VideoSource.SURFACE)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setOutputFile(videoFile?.absolutePath)
                setVideoEncodingBitRate(10000000)
                setVideoFrameRate(30)
                setVideoSize(previewSize?.width ?: 1280, previewSize?.height ?: 720)
                setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                prepare()
            }
        }
    }

    private fun startRecording() {
        if (cameraDevice == null || !binding.textureView.isAvailable) return

        try {
            closePreviewSession()
            setupMediaRecorder()

            val texture = binding.textureView.surfaceTexture?.apply {
                setDefaultBufferSize(previewSize?.width ?: 1280, previewSize?.height ?: 720)
            }
            val previewSurface = Surface(texture)
            val recorderSurface = mediaRecorder!!.surface

            val surfaces = listOf(previewSurface, recorderSurface)

            val captureRequestBuilder = cameraDevice?.createCaptureRequest(
                CameraDevice.TEMPLATE_RECORD
            )?.apply {
                addTarget(previewSurface)
                addTarget(recorderSurface)
            } ?: return

            cameraDevice?.createCaptureSession(
                surfaces,
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        captureSession = session
                        try {
                            session.setRepeatingRequest(
                                captureRequestBuilder.build(),
                                null,
                                backgroundHandler
                            )
                            runOnUiThread {
                                isRecording = true
                                binding.recordButton.isEnabled = false
                                binding.uploadButton.isEnabled = false
                                binding.statusText.text = getString(R.string.status_recording)
                                binding.timerText.visibility = android.view.View.VISIBLE
                                mediaRecorder?.start()
                                startCountdown()
                            }
                        } catch (e: CameraAccessException) {
                            e.printStackTrace()
                        }
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        Toast.makeText(
                            this@RecordActivity,
                            getString(R.string.error_recording_failed),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                backgroundHandler
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, getString(R.string.error_recording_failed), Toast.LENGTH_SHORT).show()
        }
    }

    private fun closePreviewSession() {
        captureSession?.close()
        captureSession = null
    }

    private fun startCountdown() {
        object : CountDownTimer(RECORDING_DURATION_MS, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = (millisUntilFinished / 1000).toInt() + 1
                binding.timerText.text = seconds.toString()
            }

            override fun onFinish() {
                stopRecording()
            }
        }.start()
    }

    private fun stopRecording() {
        try {
            captureSession?.stopRepeating()
            captureSession?.abortCaptures()
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

        mediaRecorder?.apply {
            stop()
            reset()
            release()
        }
        mediaRecorder = null

        isRecording = false
        binding.recordButton.isEnabled = true
        binding.uploadButton.isEnabled = true
        binding.timerText.visibility = android.view.View.GONE
        binding.statusText.text = getString(R.string.status_recorded)

        // Restart preview
        startPreview()
    }

    private fun uploadVideo(file: File) {
        binding.uploadButton.isEnabled = false
        binding.statusText.text = getString(R.string.status_uploading)

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                file.name,
                file.asRequestBody("video/mp4".toMediaType())
            )
            .build()

        val request = Request.Builder()
            .url(SERVER_URL)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    binding.uploadButton.isEnabled = true
                    binding.statusText.text = getString(R.string.status_upload_failed)
                    Toast.makeText(
                        this@RecordActivity,
                        getString(R.string.error_upload_failed, e.message),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        binding.statusText.text = getString(R.string.status_upload_success)
                        Toast.makeText(
                            this@RecordActivity,
                            getString(R.string.upload_success),
                            Toast.LENGTH_SHORT
                        ).show()
                        // Increment practice number for next recording
                        practiceNumber++
                        updatePracticeCountText()
                        binding.recordButton.isEnabled = true
                        binding.uploadButton.isEnabled = false
                    } else {
                        binding.uploadButton.isEnabled = true
                        binding.statusText.text = getString(R.string.status_upload_failed)
                        Toast.makeText(
                            this@RecordActivity,
                            getString(R.string.error_server_response, response.code),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        })
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
        private const val RECORDING_DURATION_MS = 5000L
        private const val SERVER_URL = "http://10.0.2.2:5000/upload"
    }
}
