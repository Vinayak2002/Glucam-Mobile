package com.example.glucam

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class CameraChoiceActivity : AppCompatActivity() {

    private lateinit var btnFront: Button
    private lateinit var btnRear: Button
    private lateinit var btnGallery: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_choice)

        btnFront = findViewById(R.id.btnFront)
        btnRear = findViewById(R.id.btnRear)
        btnGallery = findViewById(R.id.btnGallery)

        btnFront.setOnClickListener {
            setPref(false)
            startNext(false)
        }

        btnRear.setOnClickListener {
            setPref(true)
            startNext(false)
        }

        btnGallery.setOnClickListener {
            startNext(true)
        }
    }

    private fun setPref(value: Boolean) {
        val sPref = getSharedPreferences("eyePref", Context.MODE_PRIVATE)
        val editor = sPref.edit()

        editor.apply {
            putBoolean("camera_rear", value)
            putBoolean("first", true)
            putBoolean("gallery", false)
            putInt("minutes", 60)
        }.apply()
    }

    private fun startNext(gallery:Boolean) {

        if (gallery) {
            val i = Intent(this@CameraChoiceActivity, GalleryPickActivity::class.java)
            startActivity(i)
            finish()
        } else {
            val i = Intent(this@CameraChoiceActivity, ImageCaptureActivity::class.java)
            startActivity(i)
            finish()
        }
    }
}