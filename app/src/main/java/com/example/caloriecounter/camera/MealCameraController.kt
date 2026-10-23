package com.example.caloriecounter.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureFailure
import android.hardware.camera2.CaptureRequest
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.caloriecounter.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

/**
 * Zarządza podglądem aparatu oraz przyciskiem wykonywania zdjęcia na ekranie skanowania.
 * Zapis zdjęcia i uruchomienie analizy pozostają po stronie aktywności.
 */
class MealCameraController(
    private val activity: AppCompatActivity,
    private val onPhotoCaptured: (ByteArray, String?) -> Unit,
    private val requestCameraPermission: () -> Unit
) {
    private var cameraDevice: CameraDevice? = null
    private var cameraSession: CameraCaptureSession? = null
    private var cameraThread: HandlerThread? = null
    private var cameraHandler: Handler? = null
    private var cameraTexture: TextureView? = null
    private var imageReader: ImageReader? = null
    private var cameraPlaceholder: MaterialButton? = null
    private var captureButton: MaterialButton? = null
    private var noteInput: TextInputEditText? = null
    private var cameraEnabled = true
    private var pendingPhotoNote: String? = null

    // Podłącza aparat do widoków tworzonych za każdym wejściem na ekran skanowania.
    fun attach(
        texture: TextureView,
        placeholder: MaterialButton,
        captureButton: MaterialButton,
        noteInput: TextInputEditText
    ) {
        detach()
        cameraTexture = texture
        cameraPlaceholder = placeholder
        this.captureButton = captureButton
        this.noteInput = noteInput
        texture.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                if (cameraEnabled) openCamera()
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) = Unit

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                if (cameraTexture?.surfaceTexture === surface) closeCamera()
                return true
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) = Unit
        }
    }

    // Zamyka zasoby i odłącza widoki, gdy użytkownik opuszcza ekran skanowania.
    fun detach() {
        closeCamera()
        cameraTexture?.surfaceTextureListener = null
        cameraTexture = null
        cameraPlaceholder = null
        captureButton = null
        noteInput = null
        cameraEnabled = true
    }

    fun onPermissionResult(granted: Boolean) {
        if (granted) {
            if (cameraEnabled && cameraTexture?.isAvailable == true) openCamera()
        } else {
            cameraEnabled = false
            cameraTexture?.visibility = View.GONE
            cameraPlaceholder?.visibility = View.VISIBLE
        }
    }

    // Wyłącza podgląd podczas edycji notatki, aby zwolnić zasoby aparatu.
    fun disableForNote() {
        if (!cameraEnabled) return
        cameraEnabled = false
        closeCamera()
        cameraTexture?.visibility = View.GONE
        cameraPlaceholder?.visibility = View.VISIBLE
        captureButton?.isEnabled = false
    }

    // Wznawia podgląd albo prosi Androida o uprawnienie do kamery.
    fun enablePreview() {
        cameraEnabled = true
        cameraPlaceholder?.visibility = View.GONE
        cameraTexture?.visibility = View.VISIBLE
        if (activity.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            if (cameraTexture?.isAvailable == true) openCamera()
        } else {
            requestCameraPermission()
        }
    }

    // Wykonuje zdjęcie i blokuje przycisk do czasu zakończenia zapisu w aktywności.
    fun capturePhoto() {
        val camera = cameraDevice ?: return
        val session = cameraSession ?: return
        val reader = imageReader ?: return
        pendingPhotoNote = noteInput?.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }
        captureButton?.isEnabled = false
        try {
            val request = camera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE).apply {
                addTarget(reader.surface)
                set(CaptureRequest.JPEG_ORIENTATION, jpegOrientation(camera.id))
            }
            session.capture(request.build(), object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureFailed(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    failure: CaptureFailure
                ) {
                    activity.runOnUiThread { showCaptureError() }
                }
            }, cameraHandler)
        } catch (_: Exception) {
            showCaptureError()
        }
    }

    // Ponownie aktywuje akcję zdjęcia po zakończeniu zapisu lub analizy.
    fun refreshCaptureButton() {
        captureButton?.isEnabled = cameraEnabled && cameraSession != null
    }

    @SuppressLint("MissingPermission")
    private fun openCamera() {
        if (!cameraEnabled || activity.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) return
        val texture = cameraTexture ?: return
        if (!texture.isAvailable || cameraDevice != null || cameraThread != null) return

        val manager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = try {
            manager.cameraIdList.firstOrNull { id ->
                manager.getCameraCharacteristics(id).get(CameraCharacteristics.LENS_FACING) ==
                    CameraCharacteristics.LENS_FACING_BACK
            } ?: manager.cameraIdList.firstOrNull()
        } catch (_: Exception) {
            null
        } ?: return

        val surfaceTexture = texture.surfaceTexture ?: return
        previewSize(manager, cameraId, texture.width)?.let {
            surfaceTexture.setDefaultBufferSize(it.width, it.height)
        }

        cameraThread = HandlerThread("camera").also { it.start() }
        cameraHandler = Handler(cameraThread!!.looper)
        val jpegSize = jpegSize(manager, cameraId)
        if (jpegSize == null) {
            closeCamera()
            return
        }
        imageReader = ImageReader.newInstance(jpegSize.width, jpegSize.height, ImageFormat.JPEG, 2).also { reader ->
            reader.setOnImageAvailableListener({ imageSource ->
                val image = try {
                    imageSource.acquireNextImage()
                } catch (_: Exception) {
                    null
                } ?: return@setOnImageAvailableListener
                val bytes = try {
                    val buffer = image.planes[0].buffer
                    ByteArray(buffer.remaining()).also(buffer::get)
                } finally {
                    image.close()
                }
                onPhotoCaptured(bytes, pendingPhotoNote)
            }, cameraHandler)
        }

        try {
            manager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    if (cameraTexture !== texture || !texture.isAvailable || !cameraEnabled ||
                        activity.isFinishing || activity.isDestroyed
                    ) {
                        camera.close()
                        return
                    }
                    try {
                        cameraDevice = camera
                        val activeSurfaceTexture = texture.surfaceTexture ?: run {
                            camera.close()
                            return
                        }
                        val previewSurface = Surface(activeSurfaceTexture)
                        val request = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                            addTarget(previewSurface)
                        }
                        val photoSurface = imageReader?.surface ?: run {
                            camera.close()
                            return
                        }
                        camera.createCaptureSession(
                            listOf(previewSurface, photoSurface),
                            object : CameraCaptureSession.StateCallback() {
                                override fun onConfigured(session: CameraCaptureSession) {
                                    if (cameraDevice !== camera || !cameraEnabled ||
                                        activity.isFinishing || activity.isDestroyed
                                    ) {
                                        session.close()
                                        camera.close()
                                        return
                                    }
                                    cameraSession = session
                                    try {
                                        session.setRepeatingRequest(request.build(), null, cameraHandler)
                                        activity.runOnUiThread { captureButton?.isEnabled = true }
                                    } catch (_: Exception) {
                                        closeCamera()
                                    }
                                }

                                override fun onConfigureFailed(session: CameraCaptureSession) {
                                    if (cameraDevice === camera) {
                                        closeCamera()
                                        showCameraUnavailable()
                                    }
                                }
                            },
                            cameraHandler
                        )
                    } catch (_: Exception) {
                        camera.close()
                        cameraDevice = null
                    }
                }

                override fun onDisconnected(camera: CameraDevice) {
                    camera.close()
                    if (cameraDevice === camera) {
                        closeCamera()
                        showCameraUnavailable()
                    }
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    camera.close()
                    if (cameraDevice === camera) {
                        closeCamera()
                        showCameraUnavailable()
                    }
                }
            }, cameraHandler)
        } catch (_: Exception) {
            closeCamera()
        }
    }

    private fun jpegSize(manager: CameraManager, cameraId: String): android.util.Size? {
        val sizes = try {
            manager.getCameraCharacteristics(cameraId)
                .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                ?.getOutputSizes(ImageFormat.JPEG)
                ?.toList()
                .orEmpty()
        } catch (_: Exception) {
            emptyList()
        }
        return sizes.filter { it.width <= MAX_JPEG_WIDTH }
            .maxByOrNull { it.width.toLong() * it.height }
            ?: sizes.maxByOrNull { it.width.toLong() * it.height }
    }

    private fun previewSize(manager: CameraManager, cameraId: String, viewWidth: Int): android.util.Size? {
        val sizes = try {
            val characteristics = manager.getCameraCharacteristics(cameraId)
            val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            map?.getOutputSizes(SurfaceTexture::class.java)?.toList().orEmpty()
        } catch (_: Exception) {
            emptyList()
        }
        if (sizes.isEmpty()) return null

        val fourByThreeSizes = sizes.filter { it.width * 3 == it.height * 4 }
        val candidates = fourByThreeSizes.ifEmpty { sizes }
        val targetWidth = viewWidth.takeIf { it > 0 } ?: DEFAULT_PREVIEW_WIDTH
        return candidates
            .filter { it.width >= targetWidth }
            .minByOrNull { it.width }
            ?: candidates.maxByOrNull { it.width.toLong() * it.height }
    }

    private fun jpegOrientation(cameraId: String): Int {
        val manager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val characteristics = try {
            manager.getCameraCharacteristics(cameraId)
        } catch (_: Exception) {
            return 0
        }
        val sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) ?: return 0
        val deviceOrientation = when (activity.windowManager.defaultDisplay.rotation) {
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> 0
        }
        val isFrontFacing = characteristics.get(CameraCharacteristics.LENS_FACING) ==
            CameraCharacteristics.LENS_FACING_FRONT
        return if (isFrontFacing) {
            (sensorOrientation + deviceOrientation) % 360
        } else {
            (sensorOrientation - deviceOrientation + 360) % 360
        }
    }

    private fun showCaptureError() {
        captureButton?.isEnabled = cameraEnabled
        Toast.makeText(activity, R.string.camera_capture_error, Toast.LENGTH_SHORT).show()
    }

    private fun showCameraUnavailable() {
        activity.runOnUiThread {
            cameraEnabled = false
            cameraTexture?.visibility = View.GONE
            cameraPlaceholder?.visibility = View.VISIBLE
            captureButton?.isEnabled = false
        }
    }

    private fun closeCamera() {
        cameraSession?.close()
        cameraDevice?.close()
        imageReader?.setOnImageAvailableListener(null, null)
        imageReader?.close()
        imageReader = null
        cameraSession = null
        cameraDevice = null
        cameraThread?.quitSafely()
        cameraThread = null
        cameraHandler = null
    }

    companion object {
        private const val MAX_JPEG_WIDTH = 1600
        private const val DEFAULT_PREVIEW_WIDTH = 1080
    }
}
