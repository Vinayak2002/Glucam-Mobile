package com.example.glucam

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform

class NextImageActivity : AppCompatActivity() {

    private lateinit var btnNext: Button
    private lateinit var txtTCC: TextView
    private lateinit var imgOriginal: ImageView
    private lateinit var imgCropped: ImageView
    private lateinit var imgROI: ImageView
    private lateinit var imgCurves: ImageView

    private val INTERVAL = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_next_iimage)


        btnNext = findViewById(R.id.btnNext)
        txtTCC = findViewById(R.id.txtTCC)
        imgOriginal = findViewById(R.id.imgOriginal)
        imgCropped = findViewById(R.id.imgCropped)
        imgROI = findViewById(R.id.imgROI)
        imgCurves = findViewById(R.id.imgCurves)


        val sPref = getSharedPreferences("eyePref", Context.MODE_PRIVATE)
        val rearCam = sPref.getBoolean("camera_rear", true)
        val gallery = sPref.getBoolean("gallery", false)

        if (rearCam) {
            imgOriginal.rotation = 90F
        } else {
            imgOriginal.rotation = 270F
        }

        setView(sPref)
        startPython()
        val py = Python.getInstance()

        if (presentResults(py, sPref, rearCam, gallery) == 0) {
            Toast.makeText(this@NextImageActivity, "Eye Image not Clear", Toast.LENGTH_SHORT).show()
            val intent = Intent(this@NextImageActivity, CameraChoiceActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnNext.setOnClickListener {
            if (sPref.getBoolean("first", false)) {
                val intent = Intent(this@NextImageActivity, EatPromptActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                if (sPref.getBoolean("gallery", false)) {
                    val intent = Intent(this@NextImageActivity, GalleryPickActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val intent = Intent(this@NextImageActivity, ImageCaptureActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    private fun presentResults(
        py: Python,
        sPref: SharedPreferences,
        rearCam: Boolean,
        gallery: Boolean): Int {
        val module = py.getModule("crop_image")
        val module2 = py.getModule("roi_tccc")
        val main_dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString()
        Log.d("PATHX", main_dir)
        var path = ""
        if (sPref.getBoolean("first", false)) {
            sPref.getString("right_image", "null")?.let {
                val img = it
                Log.d("PyRes", img)
                showImage(img, imgOriginal)
                path = module.callAttr("main", img, false, rearCam, main_dir, gallery).toString()
            }
        } else {
            sPref.getString("left_image", "null")?.let {
                val img = it
                Log.d("PyRes", img)
                showImage(img, imgOriginal)
                path = module.callAttr("main", img, true, rearCam, main_dir, gallery).toString()
            }
        }
        if (path.substring(path.length - 3, path.length) == "jpg") {
            Log.d("Crop", path)
            showImage(path, imgCropped)
        }
        else {
            return 0
        }


        path = module2.callAttr("main", path, main_dir).toString()
        var prev = 0
        val resultArray: MutableList<String> = mutableListOf<String>()
        for (i in path.indices) {
            if (path[i] == '#') {
                resultArray.add(path.substring(prev, i))
                prev = i + 1
            }
        }
        resultArray.add(path.substring(prev, path.length))
        showImage(resultArray[0], imgROI)
        showImage(resultArray[1], imgCurves)

        val l = path.length
        val editor = sPref.edit()

        for (i in l-1 downTo 0) {
            if (path[i] == '#') {
                val str = "TCC Value = " + path.substring(i+1, l)
                txtTCC.text = str
                if (sPref.getBoolean("first", false)) {
                    editor.apply {
                        putFloat("tcc1", path.substring(i+1, l).toFloat())
                    }.apply()
                } else {
                    editor.apply {
                        putFloat("tcc2", path.substring(i+1, l).toFloat())
                    }.apply()
                }
                break
            }
        }

        Log.d("PyRes_After First -> ", resultArray[0])
        Log.d("PyRes_After Second -> ", resultArray[1])
        resultArray.clear()
        return 1
    }

    private fun setView(sPref: SharedPreferences) {

        if (sPref.getBoolean("first", false)) {
            btnNext.setText(R.string.next_step)
            sPref.getString("right_image", "null")?.let {
                showImage(it, imgOriginal)
                showImage(it, imgCropped)
                showImage(it, imgROI)
                showImage(it, imgCurves)
            }
        } else {
            btnNext.setText(R.string.capture_next_image)
            sPref.getString("left_image", "null")?.let {
                showImage(it, imgOriginal)
                showImage(it, imgCropped)
                showImage(it, imgROI)
                showImage(it, imgCurves)
            }
        }
    }

    private fun startPython() {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
    }

    private fun showImage(file: String, imgContainer: ImageView) {
        Glide.with(this)
            .load(file)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(imgContainer)
    }
}