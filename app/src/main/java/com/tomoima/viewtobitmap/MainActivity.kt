package com.tomoima.viewtobitmap

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment.DIRECTORY_PICTURES
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec.EXACTLY
import android.view.View.MeasureSpec.makeMeasureSpec
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {

    private lateinit var testView: RelativeLayout
    private  var width: Int = 0
    private  var height: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        testView = findViewById(R.id.content)
        testView.viewTreeObserver.addOnGlobalLayoutListener(ViewTreeObserver.OnGlobalLayoutListener {
            height = testView.height
            width = testView.width
        })

        val imageView = ImageView(this)
        imageView.setImageResource(R.mipmap.screen)
        val imageViewParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        imageView.layoutParams = imageViewParams
        testView.addView(imageView)

        val layoutInflater = applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val textBanner = layoutInflater.inflate(R.layout.banner_text, null)
        val textBannerParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, convertDpToPixels(42))
        textBanner.y = 300f
        textBanner.layoutParams = textBannerParams
        testView.addView(textBanner, textBannerParams)
        findViewById<Button>(R.id.button).let {
            it.setOnClickListener {
                val bitmap = createBitmapFromView(testView, width, height)

                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                val fname = "test_$timeStamp.jpg"
                storeFile(bitmap, fname)
            }
        }
    }

    private fun createBitmapFromView(view: View, width: Int, height: Int): Bitmap {
        Log.d("main", "====width ${width} height ${height}")
        if (width > 0 && height > 0) {
            view.measure(
                makeMeasureSpec(
                    width, EXACTLY
                ),
                makeMeasureSpec(
                    height, EXACTLY
                )
            )
        }
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)

        val bitmap = Bitmap.createBitmap(
            view.measuredWidth,
            view.measuredHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        val background = view.background

        background?.draw(canvas)
        view.draw(canvas)

        return bitmap
    }

    private fun convertDpToPixels(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(), Resources.getSystem().displayMetrics
        ).roundToInt()
    }

    private fun storeFile(bitmap: Bitmap, filename: String) {
        val pictureDir = getExternalFilesDir(DIRECTORY_PICTURES)
        if(!pictureDir.exists()) {
            pictureDir.mkdirs()
        }
        val f = File(pictureDir, filename)
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos)
        val bitmapData = bos.toByteArray()
        //write the bytes in file
        FileOutputStream(f).apply {
            this.write(bitmapData)
            this.flush()
            this.close()
            Log.d("main", "saved $f")
            updateGallery(f)
        }

    }

    private fun updateGallery(file: File) {
        sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)))

        MediaScannerConnection.scanFile(
            this,
            arrayOf(file.toString()),
            arrayOf(file.name),
            null
        )
    }
}
