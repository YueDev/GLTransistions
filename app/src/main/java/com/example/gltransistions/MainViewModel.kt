package com.example.gltransistions

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.lifecycle.AndroidViewModel

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val image = BitmapFactory.decodeResource(application.resources, R.mipmap.image1, BitmapFactory.Options().apply {
        inScaled = false
        inDensity = Bitmap.DENSITY_NONE
    })

    val reverseImage = image.let {
        val matrix = Matrix().apply {
            setScale(1f, -1f)
        }
        Bitmap.createBitmap(it, 0, 0, it.width, it.height, matrix, false)
    }

    val bitmaps = imageResList.map {
        BitmapFactory.decodeResource(application.resources, it, BitmapFactory.Options().apply {
            inScaled = false
            inDensity = Bitmap.DENSITY_NONE
        })
    }

    //y轴翻转的bitmap
    val reverseBitmap = bitmaps.map {
        val matrix = Matrix().apply {
            setScale(1f, -1f)
        }
        Bitmap.createBitmap(it, 0, 0, it.width, it.height, matrix, false)
    }


    fun doNothing() {

    }

}