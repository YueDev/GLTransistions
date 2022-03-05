package com.example.gltransistions.split

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.example.gltransistions.BaseRenderer
import com.example.gltransistions.OPENGL


class SplitGLSurfaceView : GLSurfaceView {


    private lateinit var renderer: SplitRenderer

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    fun setResources(bitmaps: List<Bitmap>) {
        setEGLContextClientVersion(3)

        val vertex = context.assets.open("shader/vertex_shader.glsl").reader().readText()
        val fragment = context.assets.open("shader/fragment_shader_split.glsl").reader().readText()

        renderer = SplitRenderer(vertex, fragment, bitmaps)

        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
    }


    fun setProgress(progress: Float, index: Int = 0, numX: Float = 1.0f, numY: Float = 1.0f) {
        renderer.progress = progress.coerceIn(0f, 100f)
        renderer.index = index
        renderer.numX = numX.coerceAtLeast(1.0f)
        renderer.numY = numY.coerceAtLeast(1.0f)
        requestRender()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        renderer.onDestroy()
    }


    class SplitRenderer(
        override val vertexShaderString: String,
        override val fragmentShaderString: String,
        override val bitmaps: List<Bitmap>
    ) : BaseRenderer() {

        var numX = 1.0f
        var numY = 1.0f

        override fun onDraw() {
            super.onDraw()
            setFloatUniform(OPENGL.UNIFORM_SPLIT_NUM, numX, numY)
        }
    }

}