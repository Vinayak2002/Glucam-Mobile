package com.example.glucam

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


class GalleryPickActivity : AppCompatActivity() {

    private lateinit var imgView: ImageView
    private lateinit var btnChoose: Button
    private lateinit var btnNext: Button
    private lateinit var txtEyePrompt: TextView

    private val IMAGE_PICK_CODE = 1000
    private var ImagePath = ""
    private lateinit var ImageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery_pick)

        imgView = findViewById(R.id.imgView)
        btnChoose = findViewById(R.id.btnChoose)
        btnNext = findViewById(R.id.btnNext)
        txtEyePrompt = findViewById(R.id.txtEyePrompt)

        val file = ImagePath
        val sPref = getSharedPreferences("eyePref", Context.MODE_PRIVATE)

        if (sPref.getBoolean("first", true)) {
            txtEyePrompt.text = "Left Eye"
        } else {
            txtEyePrompt.text = "Right Eye"
        }


        btnChoose.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                    get_permissions()
                } else {
                    pickImage()
                }
            } else {
                pickImage()
            }
        }

        btnNext.setOnClickListener {
            var msgToast = ""

            val imgUriC = ImageUri
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imgUriC)
            val stream = ByteArrayOutputStream()
            var savefile: File? = null
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            val bytesData: ByteArray = stream.toByteArray()

            if (sPref.getBoolean("first", true)) {
                savefile = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "first.jpeg")
                msgToast = "First Image Selected"
                intent = Intent(this@GalleryPickActivity, NextImageActivity::class.java)
                setPref(sPref, b=false, imgBool = true, savefile.absolutePath.toString())
            } else {
                savefile = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "second.jpeg")
                msgToast = "Second Image Selected"
                intent = Intent(this@GalleryPickActivity, NextImageActivity::class.java)
                setPref(sPref, b=true, imgBool = false, savefile.absolutePath.toString())
            }
            Log.d("address - selected", savefile.absolutePath.toString())

            val opStream = FileOutputStream(savefile)
            opStream.write(bytesData)
            opStream.close()
            stream.close()

            Toast.makeText(this@GalleryPickActivity, msgToast, Toast.LENGTH_SHORT).show()
            finish()
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            val file = data?.data
            if (file != null) {
                ImageUri = file
            }
            ImagePath = file.toString()
            imgView.setImageURI(file)
        }
    }

    private fun setPref(sPref: SharedPreferences, b: Boolean, imgBool: Boolean, imgName:String) {
        val editor = sPref.edit()
        editor.apply {
            putBoolean("gallery", true)
            putBoolean("first", b)
            if (imgBool) {
                putString("left_image", imgName)
            } else {
                putString("right_image", imgName)
            }
        }.apply()
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
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
}