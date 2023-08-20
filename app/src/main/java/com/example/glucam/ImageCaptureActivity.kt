package com.example.glucam

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import java.io.File
import java.io.FileOutputStream

class ImageCaptureActivity : AppCompatActivity() {

    private lateinit var handler: Handler
    private lateinit var handlerThread: HandlerThread
    private lateinit var capReq: CaptureRequest.Builder
    private lateinit var cameraManager: CameraManager
    private lateinit var textureView: TextureView
    private lateinit var cameraCaptureSession: CameraCaptureSession
    private lateinit var cameraDevice: CameraDevice
    private lateinit var captureRequest: CaptureRequest
    private lateinit var imageReader: ImageReader
    private lateinit var txtEyePrompt: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_capture)

        val sPref = getSharedPreferences("eyePref", Context.MODE_PRIVATE)

        val camChoice = if (sPref.getBoolean("camera_rear", true)) {
            0
        } else {
            1
        }

        get_permissions()

        textureView = findViewById(R.id.contTextureView)
        txtEyePrompt = findViewById(R.id.txtEyePrompt)

        if (sPref.getBoolean("first", true)) {
            txtEyePrompt.text = "Left Eye"
        } else {
            txtEyePrompt.text = "Right Eye"
        }

        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        handlerThread = HandlerThread("cameraThread")
        handlerThread.start()

        handler = Handler((handlerThread).looper)

        textureView.surfaceTextureListener = object :TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(texture: SurfaceTexture, width: Int, height: Int) {
                open_camera(camChoice)
            }
            override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture, width: Int, height: Int) {

            }

            override fun onSurfaceTextureDestroyed(texture: SurfaceTexture): Boolean {
                return false
            }
            override fun onSurfaceTextureUpdated(texture: SurfaceTexture) {

            }
        }

        imageReader = ImageReader.newInstance(1080, 1920, ImageFormat.JPEG, 1)
        imageReader.setOnImageAvailableListener(object : ImageReader.OnImageAvailableListener {
            override fun onImageAvailable(reader: ImageReader?) {

                val image = reader?.acquireLatestImage()
                val buffer = image!!.planes[0].buffer
                val bytes = ByteArray(buffer.remaining())
                buffer.get(bytes)
                var file: File? = null
                var intent: Intent? = null

                var msgToast: String = ""

                if (sPref.getBoolean("first", true)) {
                    file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "first.jpeg")
                    intent = Intent(this@ImageCaptureActivity, NextImageActivity::class.java)
                    msgToast = "First Image Captured"
                    setPref(sPref, b=false, imgBool = true, file.absolutePath.toString())
                } else {
                    file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "second.jpeg")
                    intent = Intent(this@ImageCaptureActivity, NextImageActivity::class.java)
                    msgToast = "Second Image Captured"
                    setPref(sPref, b=true, imgBool = false, file.absolutePath.toString())
                }
                Log.d("address - captured", file.absolutePath.toString())

                val opStream = FileOutputStream(file)
                opStream.write(bytes)

                opStream.close()
                image.close()
                Toast.makeText(this@ImageCaptureActivity, msgToast, Toast.LENGTH_SHORT).show()
                finish()
                startActivity(intent)
            }
        }, handler)

    }

    private fun setPref(sPref: SharedPreferences, b: Boolean, imgBool: Boolean, imgName:String) {
        val editor = sPref.edit()
        editor.apply {
            putBoolean("first", b)
            if (imgBool) {
                putString("left_image", imgName)
            } else {
                putString("right_image", imgName)
            }
        }.apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraDevice.close()
        handler.removeCallbacksAndMessages(null)
        handlerThread.quitSafely()
    }

    fun open_camera(cam: Int) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            get_permissions()
        }
        cameraManager.openCamera(cameraManager.cameraIdList[cam], object : CameraDevice.StateCallback() {
            override fun onOpened(p0: CameraDevice) {
                cameraDevice = p0
                capReq = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)

                val surface = Surface(textureView.surfaceTexture)
                capReq.addTarget(surface)

                cameraDevice.createCaptureSession(listOf(surface, imageReader.surface), object:
                    CameraCaptureSession.StateCallback() {
                    override fun onConfigured(p0: CameraCaptureSession) {
                        cameraCaptureSession = p0
                        cameraCaptureSession.setRepeatingRequest(capReq.build(), null, null)
                    }

                    override fun onConfigureFailed(p0: CameraCaptureSession) {
                        TODO("Not yet implemented")
                    }
                }, handler )
            }

            override fun onDisconnected(p0: CameraDevice) {
                Toast.makeText(this@ImageCaptureActivity, "Device Disconnected", Toast.LENGTH_SHORT).show()
                TODO("Not yet implemented")
            }

            override fun onError(p0: CameraDevice, p1: Int) {
                Toast.makeText(this@ImageCaptureActivity, "Error Occurred", Toast.LENGTH_SHORT).show()
                TODO("Not yet implemented")
            }
        }, handler)

        findViewById<Button>(R.id.btnCapture).apply {
            setOnClickListener {
                capReq = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
                capReq.addTarget(imageReader.surface)
                cameraCaptureSession.capture(capReq.build(), null, null)
            }
        }
    }

    fun get_permissions() {
        val permissionList = mutableListOf<String>()

        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            permissionList.add(android.Manifest.permission.CAMERA)
        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                permissionList.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                permissionList.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (permissionList.size > 0) {
            requestPermissions(permissionList.toTypedArray(), 10)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        grantResults.forEach {
            if (it != PackageManager.PERMISSION_GRANTED) {
                get_permissions()
            }
        }
    }
}