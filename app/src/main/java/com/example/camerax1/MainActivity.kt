package com.example.camerax1

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.camerax1.databinding.ActivityMainBinding

// Based partly on example at: https://developer.android.com/codelabs/camerax-getting-started

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var imageCapture: ImageCapture? = null
    private var permissions = arrayOf(Manifest.permission.CAMERA)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.capture.setOnClickListener {
            if (checkPermissions()) {
                takePicture()
            }
        }
        if (!startCamera()) {
            ActivityCompat.requestPermissions(this, permissions, 0)
        }
    }

    private fun checkPermissions(): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0 && grantResults.all { it == PackageManager.PERMISSION_GRANTED } ) {
            startCamera()
        } else {
            AlertDialog.Builder(this).setPositiveButton("OK", null)
                .setMessage("Will not work as camera permission not granted").show()
        }
    }

    private fun startCamera(): Boolean {

        if (checkPermissions()) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
            cameraProviderFuture.addListener({
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(binding.preview1.surfaceProvider)
                }
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                imageCapture = ImageCapture.Builder().build()
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)

                } catch (e: Exception) {
                    Log.d("CAMERAX1", e.stackTraceToString())
                }
            }, ContextCompat.getMainExecutor(this))
            return true
        } else {
            return false
        }
    }

    private fun takePicture() {

        imageCapture?.apply {
            val name = "img${(System.currentTimeMillis() / 1000).toInt()}"
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX1")
            }
            val outputOptions = ImageCapture.OutputFileOptions.Builder(
                contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
            ).build()
            this.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this@MainActivity),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(e: ImageCaptureException) {
                        Log.e("CAMERAX1", e.stackTraceToString())
                        AlertDialog.Builder(this@MainActivity).setPositiveButton("OK", null)
                            .setMessage("Error: ${e.message}").show()
                    }

                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        AlertDialog.Builder(this@MainActivity).setPositiveButton("OK", null)
                            .setMessage("Saved Successfully").show()
                    }
                }
            )
        }
    }

}