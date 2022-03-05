package com.example.gltransistions.video

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES30.*
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.tan


class VideoSurfaceView : GLSurfaceView {


    private lateinit var renderer: VideoRenderer

    private lateinit var touchItem: GLSprite

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)


    fun start(bitmap: Bitmap, width: Int, height: Int) {
        setEGLContextClientVersion(3)
        val glBitmapSprite = GLBitmapSprite(
            bitmap = bitmap,
            canvasWidth = width,
            canvasHeight = height,
            initScale = 0.5f
        )
        touchItem = glBitmapSprite
        val list = listOf(glBitmapSprite)
        renderer = VideoRenderer(list)
        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
    }


    private val myGestureDetector = object : MyGestureDetector(context) {
        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
            touchItem.postTranslateFromView(-distanceX, -distanceY)
            requestRender()
            return true
        }

        override fun onScale(detector: ScaleGestureDetector?): Boolean {
            detector?: return false
            val scale = detector.scaleFactor
            val px = detector.focusX
            val py = detector.focusY
            touchItem.postScaleFromView(scale, px, py)
            requestRender()
            return true
        }

        override fun onRotation(beginDegree: Float, prevDegree: Float, currentDegree: Float, focusX: Float, focusY: Float) {
            touchItem.postRotateFromView(currentDegree - prevDegree, focusX, focusY)
            requestRender()
        }

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return super.onTouchEvent(event)
        return myGestureDetector.onTouchEvent(event)
    }




    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        renderer.onDestroy()
    }


    class VideoRenderer(private val sprites: List<GLSprite>) : Renderer {

        private var error = false
        private var runOnce = false

        private val programID by lazy {
            glCreateProgram()
        }

        private val vao = IntArray(1)
        private val vbo = IntArray(1)

        // 没反转
        private val vertex = floatArrayOf(
            -1f, -1f, 0f, 0f, 0f,  // 左下
            1f, -1f, 0f, 1f, 0f,   // 右下
            1f, 1f, 0f, 1f, 1f,  //右上
            -1f, 1f, 0f, 0f, 1f,  // 左上
        )

        private val vertexBuffer = ByteBuffer.allocateDirect(vertex.size * Float.SIZE_BYTES).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(vertex)
                position(0)
            }
        }


        private val viewMatrix = FloatArray(16)
        private val projectionMatrix = FloatArray(16)


        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            glEnable(GL_CULL_FACE)
            glCullFace(GL_BACK)


            glClearColor(0.2f, 0.3f, 0.3f, 1.0f)

            //顶点的各种数据 vao， vbo
            glGenVertexArrays(1, vao, 0)
            glGenBuffers(1, vbo, 0)

            glBindVertexArray(vao[0])
            glBindBuffer(GL_ARRAY_BUFFER, vbo[0])
            glBufferData(
                GL_ARRAY_BUFFER,
                vertex.size * Float.SIZE_BYTES,
                vertexBuffer,
                GL_STATIC_DRAW
            )

            glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.SIZE_BYTES, 0)
            glEnableVertexAttribArray(0)

            glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.SIZE_BYTES, 3 * Float.SIZE_BYTES)
            glEnableVertexAttribArray(1)


            //shader program
            val vertexShader = glCreateShader(GL_VERTEX_SHADER)
            glShaderSource(vertexShader, GLSL.VERTEX_SHADER_STRING)
            glCompileShader(vertexShader)

            val state = IntArray(3)
            glGetShaderiv(vertexShader, GL_COMPILE_STATUS, state, 0)
            if (state[0] == 0) {
                val log = glGetShaderInfoLog(vertexShader)
                Log.d("YUEDEVTAG", "compile vertex shader error:\n$log")
                error = true
                return
            }

            val fragmentShader = glCreateShader(GL_FRAGMENT_SHADER)
            glShaderSource(fragmentShader, GLSL.FRAGMENT_SHADER_STRING)
            glCompileShader(fragmentShader)

            glGetShaderiv(fragmentShader, GL_COMPILE_STATUS, state, 1)
            if (state[1] == 0) {
                val log = glGetShaderInfoLog(fragmentShader)
                Log.d("YUEDEVTAG", "compile fragment shader error:\n$log")
                error = true
                return
            }

            glAttachShader(programID, vertexShader)
            glAttachShader(programID, fragmentShader)
            glLinkProgram(programID)

            glGetProgramiv(programID, GL_LINK_STATUS, state, 2)

            if (state[2] == 0) {
                val log = glGetProgramInfoLog(programID)
                Log.d("YUEDEVTAG", "link program error:\n$log")
                error = true
                return
            }

            glDeleteShader(vertexShader)
            glDeleteShader(fragmentShader)

            glUseProgram(programID)


            //开启GL_BLEND才能显示png图片的透明，
            glEnable(GL_BLEND)
            glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA)

            sprites.forEach {
                it.glPreDraw()
            }
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            if (width <= 0 || height <= 0) return

            if (runOnce) return
            runOnce = true

            glViewport(0, 0, width, height)


            val scale = width.toFloat() / height
            Matrix.perspectiveM(projectionMatrix, 0, 45f, scale, 0f, 100f)

            val f = 1.0f / tan(45 * (Math.PI / 360.0)).toFloat()

            Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, f, 0f, 0f, 0f, 0f, 1f, 0f)

            sprites.forEach {
                it.glInit(width, height)
            }
        }

        override fun onDrawFrame(gl: GL10?) {
            if (error) return
            glClear(GL_COLOR_BUFFER_BIT)

            glUseProgram(programID)


            val viewLocation = glGetUniformLocation(programID, GLSL.VIEW_UNIFORM_NAME)
            glUniformMatrix4fv(viewLocation, 1, false, viewMatrix, 0)

            val projectionLocation = glGetUniformLocation(programID, GLSL.PROJECTION_UNIFORM_NAME)
            glUniformMatrix4fv(projectionLocation, 1, false, projectionMatrix, 0)

            glBindVertexArray(vao[0])

            sprites.forEach {
                it.glDraw(programID)
                glDrawArrays(GL_TRIANGLE_FAN, 0, 4)
            }
        }

        fun onDestroy() {

            glDeleteVertexArrays(1, vao, 0)
            glDeleteBuffers(1, vbo, 0)
            glDeleteProgram(programID)

            sprites.forEach {
                it.glRelease()
            }

//            GLES30.glDeleteVertexArrays(1, vao, 0)
//            GLES30.glDeleteBuffers(1, vbo, 0)
//            GLES30.glDeleteProgram(programID)
//            GLES30.glDeleteTextures(textureIds.size, textureIds, 0)

        }
    }

}