package com.example.drawingapp

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.core.view.iterator
import androidx.lifecycle.lifecycleScope
import com.example.drawingapp.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import yuku.ambilwarna.AmbilWarnaDialog
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawingView: DrawingView
    private lateinit var llColorChooser: LinearLayout
    private lateinit var dial: Dialog
    private lateinit var selected: ImageButton

    private val resultLauncher: ActivityResultLauncher<Array<String>> = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach {
            val permName = it.key
            val isGranted = it.value

            if (isGranted) {
                if (permName == Manifest.permission.READ_EXTERNAL_STORAGE) {
                    val toGallery =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    galleryResultLauncher.launch(toGallery)
                } else {
                }
            } else {
                if (permName == Manifest.permission.READ_EXTERNAL_STORAGE) {

                } else {
                }
            }

        }

    }

    private val galleryResultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            binding.ivBackground.setImageURI(result.data?.data)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        drawingView = findViewById(R.id.drawingView)
        drawingView.setBrushSize(10f)

        llColorChooser = binding.llColorChooser
        selected = llColorChooser[0] as ImageButton
        selected.setImageResource(R.drawable.selected)

        /**
         * iterates through the views in the LinearLayout
         * and applies an onClick and onLongClick Listeners to them
         */
        for (i in llColorChooser) {
            (i as ImageButton).setOnClickListener {
                drawingView.setColor(i.tag.toString())
                if (selected !== i)
                    selected.setImageResource(R.drawable.unselected)
                selected = i
                selected.setImageResource(R.drawable.selected)
            }
            i.setOnLongClickListener {
                Toast.makeText(
                    this,
                    i.contentDescription.toString().capitalize(Locale.getDefault()),
                    Toast.LENGTH_SHORT
                ).show()
                true
            }
        }
        binding.ibCustom.setOnClickListener { onCustomColorClicked() }
        binding.ibBrushSize.setOnClickListener { changeBrushSizeDialog() }
        binding.ibGallery.setOnClickListener { handleResultLauncher() }
        binding.ibGallery.setOnLongClickListener {
            loadingDialog()
            lifecycleScope.launch {
                doSomething()
            }
            true
        }
        binding.ibRedo.setOnClickListener { drawingView.redo() }
        binding.ibUndo.setOnClickListener { drawingView.undo() }
        binding.ibDownload.setOnClickListener {
            if (isReadPermGranted()) {
                lifecycleScope.launch { saveBitmap(getBitmapFromView(binding.flBackground)) }
            }
        }

    }

    private fun onCustomColorClicked() {
        if(selected != binding.ibCustom){
            selected.setImageResource(R.drawable.unselected)
            selected = binding.ibCustom
        }

        selected.setImageResource(R.drawable.selected)

        AmbilWarnaDialog(this, Color.BLACK, object : OnAmbilWarnaListener {

            override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                drawingView.setColor(color)
                binding.ibCustom.setBackgroundColor(color)
            }

            override fun onCancel(dialog: AmbilWarnaDialog?) {
            }
        }).show()

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

    private fun handleResultLauncher() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
        ) {
            mAlertDialog(
                "Permission Denied",
                "Without the permission to read from external storage, the app wouldn't be able to import images from the gallery"
            )
        } else {
            resultLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            )
        }
    }

    private fun mAlertDialog(title: String, message: String) {
        val mAlert = AlertDialog.Builder(this)
        mAlert
            .setTitle(title)
            .setMessage(message)
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Retry") { d, _ ->
                ActivityResultContracts.RequestPermission()
                d.dismiss()
            }
            .setCancelable(true)
            .create()
            .show()
    }

    private suspend fun doSomething() {
        withContext(Dispatchers.IO) {
            for (i in 1..1000000) {
                Log.i("COUNT", i.toString())
            }
            runOnUiThread {
                dismissDialog()
                Toast.makeText(applicationContext, "Completed count!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadingDialog() {
        dial = Dialog(this)
        dial.setContentView(R.layout.loading)
        dial.setCancelable(false)
        dial.show()
    }

    private fun dismissDialog() =
        dial.dismiss()

    private fun snackBar(text: String) =
        Snackbar.make(binding.parent, text, Snackbar.LENGTH_SHORT).show()

    private fun
            getBitmapFromView(v: View): Bitmap {
        val returnBitmap = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnBitmap)
        val background = v.background

        if (background != null) {
            background.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }
        v.draw(canvas)
        return returnBitmap
    }

    private suspend fun saveBitmap(bitmap: Bitmap?): String {
        var result = ""

        withContext(Dispatchers.IO) {
            bitmap?.let {
                try {
                    val baos = ByteArrayOutputStream()
                    it.compress(Bitmap.CompressFormat.PNG, 90, baos)

                    val fileLocation = File(
                        "${externalCacheDir?.absoluteFile.toString()}${
                            File.separator
                        }KidsDrawingApp_${System.currentTimeMillis() / 1000}.png"
                    )
                    val fileOutStream = FileOutputStream(fileLocation)
                    fileOutStream.write(baos.toByteArray())
                    fileOutStream.close()

                    result = fileLocation.absolutePath
                    runOnUiThread {
                        if (result.isNotEmpty()) {
                            Toast.makeText(
                                applicationContext, result, Toast.LENGTH_LONG
                            ).show()
                        } else
                            Toast.makeText(
                                applicationContext,
                                "An error occurred while saving file", Toast.LENGTH_LONG
                            ).show()
                    }
                } catch (e: Exception) {
                    result = ""
                    e.printStackTrace()
                }
            }
        }
        return result
    }

    private fun isReadPermGranted(): Boolean {
        val confirm = ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        return confirm == PackageManager.PERMISSION_GRANTED
    }


}