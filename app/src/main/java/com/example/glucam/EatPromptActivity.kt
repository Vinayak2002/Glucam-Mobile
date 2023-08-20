package com.example.glucam

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class EatPromptActivity : AppCompatActivity() {

    private lateinit var edtTxtLastEat: EditText
    private lateinit var btnNext: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_eat_prompt)

        val sPref = getSharedPreferences("eyePref", Context.MODE_PRIVATE)
        edtTxtLastEat = findViewById(R.id.edtTxtLastEat)
        btnNext = findViewById(R.id.btnNext)

        btnNext.setOnClickListener {

            val time = edtTxtLastEat.text.toString()
            val editor = sPref.edit()
            editor.apply {
                putInt("minutes", time.toInt())
            }.apply()

            val intent = Intent(this@EatPromptActivity, ResultActivity::class.java)
            Toast.makeText(this@EatPromptActivity, "Success", Toast.LENGTH_SHORT).show()
            startActivity(intent)
            finish()
        }
    }
}