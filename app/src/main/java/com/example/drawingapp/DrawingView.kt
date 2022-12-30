package com.example.drawingapp

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class DrawingView(context: Context, attrs: AttributeSet):
    View(context, attrs) {

    private var drawPath: CustomPath? = null
    private var canvasBitmap: Bitmap? = null
    private var drawPaint: Paint? = null
    private var canvasPaint: Paint? = null
    private var brushSize: Float = 0.toFloat()
    private var color = Color.BLACK
    private var canvas: Canvas? = null
    private val paths = ArrayList<CustomPath>()

    init{
        setUpDrawing()
    }

    private fun setUpDrawing() {
        drawPaint = Paint()
        drawPath = CustomPath(color, brushSize)
        drawPaint!!.color = color
        drawPaint!!.strokeJoin = Paint.Join.ROUND
        drawPaint!!.strokeCap = Paint.Cap.ROUND
        drawPaint!!.isDither = true
        drawPaint!!.isAntiAlias = true
        drawPaint!!.style = Paint.Style.STROKE
        canvasPaint = Paint(Paint.DITHER_FLAG)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(canvasBitmap!!)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(canvasBitmap!!, 0f, 0f, canvasPaint)

        for (path in paths){
            drawPaint!!.strokeWidth = path.brushThickness
            drawPaint!!.color = path.color
            canvas.drawPath(path, drawPaint!!)
        }

        if(!drawPath!!.isEmpty){
            drawPaint!!.strokeWidth = drawPath!!.brushThickness
            drawPaint!!.color = drawPath!!.color
            canvas.drawPath(drawPath!!, drawPaint!!)
        }

        }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y

        when(event?.action){

            MotionEvent.ACTION_DOWN -> actionDown(touchX, touchY)



            MotionEvent.ACTION_MOVE -> actionMove(touchX, touchY)


            MotionEvent.ACTION_UP -> actionUp()

            else -> return false

        }
        invalidate()
        return true
    }

    private fun actionUp() {
        paths.add(drawPath!!)
        drawPath = CustomPath(color, brushSize)
    }

    private fun actionMove(touchX: Float?, touchY: Float?) {
        if (touchX != null && touchY != null)
            drawPath!!.lineTo(touchX, touchY)
    }

    private fun actionDown(touchX: Float?, touchY: Float?) {
        drawPath!!.color = color
        drawPath!!.brushThickness = brushSize
        drawPath!!.reset()

        if (touchX != null && touchY != null)
            drawPath!!.moveTo(touchX, touchY)
    }

    fun setBrushSize(newBrushSize: Float ){
        brushSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            newBrushSize,
            resources.displayMetrics)
        drawPaint!!.strokeWidth = brushSize
    }

    internal inner class CustomPath(var color: Int, var brushThickness: Float): Path() {

    }
}