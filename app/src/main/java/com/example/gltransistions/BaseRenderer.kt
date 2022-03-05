package com.example.gltransistions

import android.graphics.Bitmap
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

import android.opengl.GLES30.*
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder

abstract class BaseRenderer : GLSurfaceView.Renderer {

    abstract val vertexShaderString: String
    abstract val fragmentShaderString: String
    abstract val bitmaps: List<Bitmap>

    //图片总数
    private val num = imageResList.size

    //循环的索引，动画完成一次+1
    var index = 0

    //动画进度
    var progress = 0f

    //图片纹理id
    private var textureIds = IntArray(num)

    private val programID by lazy {
        glCreateProgram()
    }

    // 顶点 以及 纹理坐标  没有翻转
    private val vertices = floatArrayOf(
        -1f, -1f, 0f, 1f, // 左下
        1f, -1f, 1f, 1f, // 右下
        1f, 1f, 1f, 0f, //右上
        -1f, 1f, 0f, 0f// 左上
    )

    private val vao = IntArray(1)
    private val vbo = IntArray(1)

    //顶点ByteBuffer
    private val vertexBuffer = ByteBuffer.allocateDirect(vertices.size * Float.SIZE_BYTES).run {
        order(ByteOrder.nativeOrder())
        asFloatBuffer().apply {
            put(vertices)
            position(0)
        }
    }


    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glClearColor(0.2f, 0.3f, 0.3f, 1f)

        //生成vao vbo ebo
        glGenVertexArrays(1, vao, 0)
        glGenBuffers(1, vbo, 0)

        //绑定vao，填入数据
        glBindVertexArray(vao[0])
        glBindBuffer(GL_ARRAY_BUFFER, vbo[0])
        glBufferData(GL_ARRAY_BUFFER, vertices.size * Float.SIZE_BYTES, vertexBuffer, GL_STATIC_DRAW)
        //给vao解释数据，前两个是顶点坐标，后两个是纹理坐标
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.SIZE_BYTES, 0)
        glEnableVertexAttribArray(0)
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.SIZE_BYTES, 2 * Float.SIZE_BYTES)
        glEnableVertexAttribArray(1)

        //shader  program
        val state = IntArray(3)
        //编译顶点着色器
        val vertexShader = glCreateShader(GL_VERTEX_SHADER)
        glShaderSource(vertexShader, vertexShaderString)
        glCompileShader(vertexShader)
        //检查编译状态
        glGetShaderiv(vertexShader, GL_COMPILE_STATUS, state, 0)
        if (state[0] == 0) {
            val message = glGetShaderInfoLog(vertexShader)
            Log.d("YUEDEVTAG", "vertex shader error: $message")
            return
        }
        //片元着色器
        val fragmentShader = glCreateShader(GL_FRAGMENT_SHADER)
        glShaderSource(fragmentShader, fragmentShaderString)
        glCompileShader(fragmentShader)
        glGetShaderiv(fragmentShader, GL_COMPILE_STATUS, state, 1)
        if (state[1] == 0) {
            val message = glGetShaderInfoLog(fragmentShader)
            Log.d("YUEDEVTAG", "vertex shader error: $message")
            return
        }
        //把顶点和片元的着色器链接到到程序上program
        glAttachShader(programID, vertexShader)
        glAttachShader(programID, fragmentShader)
        glLinkProgram(programID)
        //检查program的状态
        glGetProgramiv(programID, GL_LINK_STATUS, state, 2)
        if (state[2] == 0) {
            val message = glGetProgramInfoLog(programID)
            Log.d("YUEDEVTAG", "program compile error: $message")

        }
        //link结束，删除vertexShader和fragmentShader
        glDeleteShader(vertexShader)
        glDeleteShader(fragmentShader)

        //开启GL_BLEND才能显示png图片的透明
        glEnable(GL_BLEND)
        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA)

        //生成图片纹理
        repeat(num) {
            textureIds[it] = OPENGL.genBitmapTexture(bitmaps[it])
        }

        onCreated()

    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {

        onPreDraw()

        glClear(GL_COLOR_BUFFER_BIT)

        glUseProgram(programID)


        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, textureIds[index % num])
        glUniform1i(glGetUniformLocation(programID, OPENGL.UNIFORM_TEXTURES[0]), 0);

        glActiveTexture(GL_TEXTURE1)
        glBindTexture(GL_TEXTURE_2D, textureIds[(index + 1) % num])
        glUniform1i(glGetUniformLocation(programID, OPENGL.UNIFORM_TEXTURES[1]), 1);

        glUniform1f(glGetUniformLocation(programID, OPENGL.UNIFORM_PROGRESS), progress)

        onDraw()

        glDrawArrays(GL_TRIANGLE_FAN, 0, 4)

    }


    fun onDestroy() {
        glDeleteVertexArrays(1, vao, 0)
        glDeleteBuffers(1, vbo, 0)
        glDeleteProgram(programID)
        glDeleteTextures(textureIds.size, textureIds, 0)

    }


    fun setFloatUniform(name: String, value: Float) {
        glUniform1f(glGetUniformLocation(programID, name), value)
    }

    fun setFloatUniform(name: String, value1: Float, value2: Float) {
        glUniform2f(glGetUniformLocation(programID, name), value1, value2)
    }

    protected open fun onCreated() {

    }

    protected open fun onDraw() {

    }

    protected open fun onPreDraw() {

    }


}