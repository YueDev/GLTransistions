package com.example.gltransistions.frame_buffer

import android.animation.TimeAnimator
import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLSurfaceView
import android.util.AttributeSet


class FrameBufferSurfaceView : GLSurfaceView {


    private lateinit var renderer: FrameBufferRenderer

    private var duration = 2000
    private val animator = TimeAnimator()

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    fun setResources(bitmaps: List<Bitmap>) {
        setEGLContextClientVersion(3)

        val vertex = context.assets.open("shader/vertex_shader.glsl").reader().readText()
        val fbFragment = context.assets.open("shader/fragment_shader_frame_buffer_fb.glsl").reader().readText()
        val screenFragment = context.assets.open("shader/fragment_shader_frame_buffer_screen.glsl").reader().readText()

        renderer = FrameBufferRenderer(vertex, fbFragment, vertex, screenFragment, bitmaps)

        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY


        animator.setTimeListener { _, totalTime, _ ->
            val progress = totalTime % duration / duration.toFloat()
            val index = totalTime / duration
            //progress进行下插值  0-80  静止  80 - 100    0 - 100
            val interpolatedProgress = ((progress - 0.8f) * 5).coerceIn(0f, 1f)
            renderer.progress = interpolatedProgress
            renderer.index = index.toInt()
            requestRender()
        }
        animator.start()

    }

    fun setDuration(time: Int) {
        animator.cancel()
        renderer.index = 0
        renderer.progress = 0f
        duration = time
        animator.start()
    }



    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator.cancel()
        renderer.onDestroy()
    }



}