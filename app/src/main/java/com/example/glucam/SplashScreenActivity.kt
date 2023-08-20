package com.example.glucam

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Handler;
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform

class SplashScreenActivity : AppCompatActivity() {

    private val SPLASH_SCREEN_TIME_OUT = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setInitialPref()

        startPython()

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.activity_main)

        Handler().postDelayed(Runnable {
            val i = Intent(this@SplashScreenActivity, CameraChoiceActivity::class.java)
            startActivity(i)
            finish()
        }, SPLASH_SCREEN_TIME_OUT.toLong())
    }

    private fun startPython() {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
    }

    private fun setInitialPref() {
        val sPref = getSharedPreferences("eyePref", Context.MODE_PRIVATE)
        val editor = sPref.edit()

        editor.apply {
            putBoolean("first", true)
            putBoolean("gallery", false)
            putBoolean("camera_rear", true)
            putInt("minutes", 60)
        }.apply()
    }
}