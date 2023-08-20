package com.example.glucam

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class ResultActivity : AppCompatActivity() {

    private lateinit var btnRecheck: Button
    private lateinit var txtBGL: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        btnRecheck = findViewById(R.id.btnRecheck)
        txtBGL = findViewById(R.id.txtBGL)

        val bgl = setInitialPref()
        txtBGL.text = bgl.toString()

        btnRecheck.setOnClickListener {
//            val bgl = setInitialPref()
            val intent =  Intent(this@ResultActivity, CameraChoiceActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setInitialPref(): Int {
        val sPref = getSharedPreferences("eyePref", Context.MODE_PRIVATE)
        val editor = sPref.edit()

        val bgl = bglLevel(
            sPref.getFloat("tcc1", 1.7F),
            sPref.getFloat("tcc2", 1.7F),
            sPref.getInt("minutes", 60))

        editor.apply {
            putBoolean("first", true)
            putInt("minutes", 60)
            putBoolean("gallery", false)
        }.apply()

        return bgl
    }

    private fun bglLevel(tcc1: Float, tcc2: Float, min: Int): Int {
        val offset = 105.857358
        val r = (0.65600544 * (tcc1 + tcc2) / 2) - (0.1057725 * min) + offset
        return r.toInt()
    }
}