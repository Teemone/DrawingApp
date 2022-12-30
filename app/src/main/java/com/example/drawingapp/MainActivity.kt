package com.example.drawingapp

import android.annotation.SuppressLint
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.drawingapp.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawingView: DrawingView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        drawingView = findViewById(R.id.drawingView)
        drawingView.setBrushSize(10f)

        binding.ibBrushSize.setOnClickListener { changeBrushSizeDialog() }


    }

    private fun changeBrushSizeDialog(){
        val adb = Dialog(this)
        var size = ""

        adb.setContentView(R.layout.my_custom_dialog)

        adb.findViewById<ImageButton>(R.id.ib_sm).setOnClickListener{
            drawingView.setBrushSize(10f)
            size = "Small brush: 10px"
            adb.dismiss()
        }

        adb.findViewById<ImageButton>(R.id.ib_md).setOnClickListener{
            drawingView.setBrushSize(15f)
            size = "Medium brush: 15px"
            adb.dismiss()
        }

        adb.findViewById<ImageButton>(R.id.ib_lg).setOnClickListener{
            drawingView.setBrushSize(20f)
            size = "Large brush: 20px"
            adb.dismiss()
        }

        adb.setOnDismissListener() {Snackbar.make(binding.parent, size, Snackbar.LENGTH_SHORT).show()}

        adb.show()
    }

}