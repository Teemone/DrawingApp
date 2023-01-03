package com.example.drawingapp

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.core.view.iterator
import com.example.drawingapp.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawingView: DrawingView
    private lateinit var llColorChooser: LinearLayout
    private val resultLauncher: ActivityResultLauncher<Array<String>> = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()){
        permissions ->
        permissions.entries.forEach {
            val permName = it.key
            val isGranted = it.value

            if (isGranted){
                if (permName == Manifest.permission.READ_EXTERNAL_STORAGE){
                    Toast.makeText(this,
                        "Read ES Permission granted successfully!",
                        Toast.LENGTH_SHORT).show()

                    val toGallery = Intent(Intent.ACTION_PICK , MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    galleryResultLauncher.launch(toGallery)
                }
                else{
                }
            }
            else{
                if (permName == Manifest.permission.READ_EXTERNAL_STORAGE){
                    Toast.makeText(this, "Read ES Permission denied!", Toast.LENGTH_SHORT).show()
                }
                else{
                }            }

        }

    }

    private val galleryResultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){
        result ->
        if(result.resultCode == RESULT_OK && result.data != null){
            binding.ivBackground.setImageURI(result.data?.data)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        drawingView = findViewById(R.id.drawingView)
        drawingView.setBrushSize(10f)

        binding.ibBrushSize.setOnClickListener { changeBrushSizeDialog() }

        llColorChooser = binding.llColorChooser
        var selected = llColorChooser[0] as ImageButton
        selected.setImageResource(R.drawable.selected)


//        iterates through the views in the LinearLayout
//        and applies an onClick and onLongClick Listeners to them
        for (i in llColorChooser){
            (i as ImageButton).setOnClickListener {
                drawingView.setColor(i.tag.toString())
                if (selected !== i)
                    selected.setImageResource(R.drawable.unselected)
                selected = i
                selected.setImageResource(R.drawable.selected)
            }
            i.setOnLongClickListener {
                Toast.makeText(this, i.contentDescription.toString().capitalize(Locale.getDefault()), Toast.LENGTH_SHORT).show()
                true
            }
        }

        binding.ibGallery.setOnClickListener { handleResultLauncher() }

    }

//    Displays a dialog that allows the user to select the stroke / brush size
    private fun changeBrushSizeDialog() {
        val adb = Dialog(this)
        var size: String

    adb.setContentView(R.layout.my_custom_dialog)

        adb.findViewById<ImageButton>(R.id.ib_sm).setOnClickListener {
            drawingView.setBrushSize(10f)
            size = "Small brush: 10px"
            adb.dismiss()
            snackBar(size)
        }

        adb.findViewById<ImageButton>(R.id.ib_md).setOnClickListener {
            drawingView.setBrushSize(15f)
            size = "Medium brush: 15px"
            adb.dismiss()
            snackBar(size)
        }

        adb.findViewById<ImageButton>(R.id.ib_lg).setOnClickListener {
            drawingView.setBrushSize(20f)
            size = "Large brush: 20px"
            adb.dismiss()
            snackBar(size)
        }
        adb.show()
    }

    private fun handleResultLauncher(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
                ){
            mAlertDialog("Permission Denied",
                "Without the permission to read from external storage, the app wouldn't be able to import images from the gallery")
        }
        else{
            resultLauncher.launch(
                arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            )
        }
    }

    private fun mAlertDialog(title: String, message: String){
        val mAlert = AlertDialog.Builder(this)
        mAlert
            .setTitle(title)
            .setMessage(message)
            .setNegativeButton("Cancel"){dialog,_ -> dialog.dismiss()}
            .setPositiveButton("Retry"){d, _ ->
                ActivityResultContracts.RequestPermission()
                d.dismiss()
            }
            .setCancelable(true)
            .create()
            .show()
    }

    private fun snackBar(text: String) = Snackbar.make(binding.parent, text, Snackbar.LENGTH_SHORT).show()




}