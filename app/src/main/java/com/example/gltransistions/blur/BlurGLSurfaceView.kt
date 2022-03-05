package com.example.gltransistions.blur

import android.animation.TimeAnimator
import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import com.example.gltransistions.BaseRenderer
import com.example.gltransistions.OPENGL
import com.example.gltransistions.imageResList


class BlurGLSurfaceView : GLSurfaceView {

    var animatorTime = 5000

    private val animator = TimeAnimator()


    private lateinit var renderer: BlurRenderer

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    fun setResources(bitmaps: List<Bitmap>) {
        setEGLContextClientVersion(3)

        val vertex = context.assets.open("shader/vertex_shader_bezier.glsl").reader().readText()
        val fragment = context.assets.open("shader/fragment_shader_blur.glsl").reader().readText()

        renderer = BlurRenderer(vertex, fragment, bitmaps)

        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
    }


    fun startAnimator(c1x: Float = 0f, c1y: Float = 0f, c2x: Float = 0f, c2y: Float = 0f) {
        stopAnimator()
        prepare(c1x, c1y, c2x, c2y)
        animator.setTimeListener { _, totalTime, _ ->
            val progress = totalTime % animatorTime / animatorTime.toFloat()
            val index = totalTime / animatorTime
            setProgress(progress, index.toInt())
        }
        animator.start()

    }

    fun stopAnimator() {
        if (animator.isStarted) {
            animator.cancel()
            setProgress(0f)
        }
    }



    //传入贝塞尔的两个控制点
    private fun prepare(c1x: Float, c1y: Float, c2x: Float, c2y: Float) {
        renderer.c1x = c1x
        renderer.c1y = c1y
        renderer.c2x = c2x
        renderer.c2y = c2y
        setProgress(0f)
    }

    private fun setProgress(progress: Float, index: Int = 0) {
        renderer.progress = progress.coerceIn(0f, 100f)
        renderer.index = index
        requestRender()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (animator.isStarted) animator.cancel()
        renderer.onDestroy()
    }



    class BlurRenderer(
        override val vertexShaderString: String,
        override val fragmentShaderString: String,
        override val bitmaps: List<Bitmap>
    ) : BaseRenderer() {

        var c1x = 0.40f
        var c1y = 0.0f
        var c2x = 0.15f
        var c2y = 1.0f

        override fun onDraw() {
            super.onDraw()
            setFloatUniform(OPENGL.UNIFORM_BEZIER_C1X, c1x)
            setFloatUniform(OPENGL.UNIFORM_BEZIER_C1Y, c1y)
            setFloatUniform(OPENGL.UNIFORM_BEZIER_C2X, c2x)
            setFloatUniform(OPENGL.UNIFORM_BEZIER_C2Y, c2y)
        }


    }

}