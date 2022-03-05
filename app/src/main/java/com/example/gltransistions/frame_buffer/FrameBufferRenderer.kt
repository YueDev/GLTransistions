package com.example.gltransistions.frame_buffer

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLES30.*
import android.opengl.GLSurfaceView
import android.util.Log
import com.example.gltransistions.OPENGL
import com.example.gltransistions.imageResList
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class FrameBufferRenderer(
    private val fbVertexShader: String,
    private val fbFragmentShader: String,
    private val screenVertexShader: String,
    private val screenFragmentShader: String,
    private val bitmaps: List<Bitmap>
) : GLSurfaceView.Renderer {

    //图片总数
    private val num = imageResList.size

    //循环的索引，动画完成一次+1
    var index = 0
    //动画进度

    var progress = 0f

    //图片纹理id
    private var textureIds = IntArray(num)

    //颜色缓冲id
    private var colorTextureIds = IntArray(1)


    private val fbProgramID by lazy {
        glCreateProgram()
    }

    private val screenProgramID by lazy {
        glCreateProgram()
    }


    // 顶点 以及 纹理坐标  没有翻转
    private val vertices = floatArrayOf(
        -1f, -1f, 0f, 0f, // 左下
        1f, -1f, 1f, 0f, // 右下
        1f, 1f, 1f, 1f, //右上
        -1f, 1f, 0f, 1f// 左上
    )



    private val vao = IntArray(2)
    private val vbo = IntArray(2)


    //顶点ByteBuffer
    private val fbVertexBuffer = ByteBuffer.allocateDirect(vertices.size * Float.SIZE_BYTES).run {
        order(ByteOrder.nativeOrder())
        asFloatBuffer().apply {
            put(vertices.copyOf())
            position(0)
        }
    }



    private val screenVertexBuffer = ByteBuffer.allocateDirect(vertices.size * Float.SIZE_BYTES).run {
        order(ByteOrder.nativeOrder())
        asFloatBuffer().apply {
            put(vertices.copyOf())
            position(0)
        }
    }

    private var fbWidth = 0
    private var fbHeight = 0
    private var screenWidth = 0
    private var screenHeight = 0

    private val frameBuffer = IntArray(1)

    private var hasError = false

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {

        glEnable(GL_CULL_FACE)

        //开启GL_BLEND才能显示png图片的透明
        glEnable(GL_BLEND)
        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA)


        //生成vao vbo
        glGenVertexArrays(2, vao, 0)
        glGenBuffers(2, vbo, 0)

        initFrameBufferProgram()?.let {
            Log.d("YUEDEVTAG", it)
            hasError = true
            return
        }

        initScreenBufferProgram()?.let {
            Log.d("YUEDEVTAG", it)
            hasError = true
            return
        }

        //生成图片纹理
        repeat(num) {
            textureIds[it] = OPENGL.genBitmapTexture(bitmaps[it])
        }

        fbWidth = bitmaps[0].width
        fbHeight = bitmaps[0].height

        //生成颜色缓冲
        colorTextureIds[0] = OPENGL.genColorTexture(fbWidth, fbHeight)

        //生成frame buffer
        glGenFramebuffers(1, frameBuffer, 0)
        glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer[0])

        //把颜色缓冲附加给frame buffer
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, colorTextureIds[0], 0)
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            Log.d("YUEDEVTAG", "ERROR: Framebuffer is not complete!")
            hasError = true
            return
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0)

    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        screenWidth = width
        screenHeight = height
    }

    override fun onDrawFrame(gl: GL10?) {

        if (hasError) return

        //渲染到frame buffer
        glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer[0])
        glViewport(0, 0, fbWidth, fbHeight)
        glClearColor(0.2f, 0.3f, 0.3f, 1f)
        glClear(GL_COLOR_BUFFER_BIT)

        glUseProgram(fbProgramID)

        glBindVertexArray(vao[0])

        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, textureIds[index % num])
        glUniform1i(glGetUniformLocation(fbProgramID, OPENGL.UNIFORM_TEXTURES[0]), 0);
//
        glActiveTexture(GL_TEXTURE1)
        glBindTexture(GL_TEXTURE_2D, textureIds[(index + 1) % num])
        glUniform1i(glGetUniformLocation(fbProgramID, OPENGL.UNIFORM_TEXTURES[1]), 1);


        glUniform1f(glGetUniformLocation(fbProgramID, OPENGL.UNIFORM_PROGRESS), progress)
        glDrawArrays(GL_TRIANGLE_FAN, 0, 4)
        glBindVertexArray(0)



        //渲染screen
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
        glViewport(0, 0, screenWidth, screenHeight)
        glClearColor(0.2f, 0.3f, 0.3f, 1f)
        glClear(GL_COLOR_BUFFER_BIT)

        glUseProgram(screenProgramID)
        glBindVertexArray(vao[1])

        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, colorTextureIds[0])
        glUniform1i(glGetUniformLocation(screenProgramID, OPENGL.UNIFORM_TEXTURES[0]), 0);

        glDrawArrays(GL_TRIANGLE_FAN, 0, 4)
        glBindVertexArray(0)

    }


    fun onDestroy() {
        glDeleteVertexArrays(2, vao, 0)
        glDeleteBuffers(2, vbo, 0)
        glDeleteProgram(fbProgramID)
        glDeleteProgram(screenProgramID)
        glDeleteTextures(textureIds.size, textureIds, 0)
        glDeleteTextures(colorTextureIds.size, colorTextureIds, 0)
    }


    private fun initFrameBufferProgram(): String?{

        //绑定vao，填入数据
        glBindVertexArray(vao[0])
        glBindBuffer(GL_ARRAY_BUFFER, vbo[0])
        glBufferData(GL_ARRAY_BUFFER, vertices.size * Float.SIZE_BYTES, fbVertexBuffer, GL_STATIC_DRAW)
        //给vao解释数据，前两个是顶点坐标，后两个是纹理坐标
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.SIZE_BYTES, 0)
        glEnableVertexAttribArray(0)
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.SIZE_BYTES, 2 * Float.SIZE_BYTES)
        glEnableVertexAttribArray(1)

        glBindVertexArray(0)

        //shader  program
        val state = IntArray(3)
        //编译顶点着色器
        val vertexShader = glCreateShader(GL_VERTEX_SHADER)
        glShaderSource(vertexShader, fbVertexShader)
        glCompileShader(vertexShader)
        //检查编译状态
        glGetShaderiv(vertexShader, GL_COMPILE_STATUS, state, 0)
        if (state[0] == 0) {
            val message = glGetShaderInfoLog(vertexShader)
            return "fb vertex shader error:\n$message"
        }
        //片元着色器
        val fragmentShader = glCreateShader(GL_FRAGMENT_SHADER)
        glShaderSource(fragmentShader, fbFragmentShader)
        glCompileShader(fragmentShader)
        glGetShaderiv(fragmentShader, GL_COMPILE_STATUS, state, 1)
        if (state[1] == 0) {
            val message = glGetShaderInfoLog(fragmentShader)
            return "fb fragment shader error:\n$message"
        }
        //把顶点和片元的着色器链接到到程序上program
        glAttachShader(fbProgramID, vertexShader)
        glAttachShader(fbProgramID, fragmentShader)
        glLinkProgram(fbProgramID)
        //检查program的状态
        glGetProgramiv(fbProgramID, GL_LINK_STATUS, state, 2)
        if (state[2] == 0) {
            val message = glGetProgramInfoLog(fbProgramID)
            return "fb program compile error:\n$message"
        }
        //link结束，删除vertexShader和fragmentShader
        glDeleteShader(vertexShader)
        glDeleteShader(fragmentShader)
        return null
    }

    private fun initScreenBufferProgram(): String?{

        //绑定vao，填入数据
        glBindVertexArray(vao[1])
        glBindBuffer(GL_ARRAY_BUFFER, vbo[1])
        glBufferData(GL_ARRAY_BUFFER, vertices.size * Float.SIZE_BYTES, screenVertexBuffer, GL_STATIC_DRAW)
        //给vao解释数据，前两个是顶点坐标，后两个是纹理坐标
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.SIZE_BYTES, 0)
        glEnableVertexAttribArray(0)
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.SIZE_BYTES, 2 * Float.SIZE_BYTES)
        glEnableVertexAttribArray(1)

        glBindVertexArray(0)

        //shader  program
        val state = IntArray(3)
        //编译顶点着色器
        val vertexShader = glCreateShader(GL_VERTEX_SHADER)
        glShaderSource(vertexShader, screenVertexShader)
        glCompileShader(vertexShader)
        //检查编译状态
        glGetShaderiv(vertexShader, GL_COMPILE_STATUS, state, 0)
        if (state[0] == 0) {
            val message = glGetShaderInfoLog(vertexShader)
            return "screen vertex shader error:\n$message"
        }
        //片元着色器
        val fragmentShader = glCreateShader(GL_FRAGMENT_SHADER)
        glShaderSource(fragmentShader, screenFragmentShader)
        glCompileShader(fragmentShader)
        glGetShaderiv(fragmentShader, GL_COMPILE_STATUS, state, 1)
        if (state[1] == 0) {
            val message = glGetShaderInfoLog(fragmentShader)
            return "screen fragment shader error:\n$message"
        }
        //把顶点和片元的着色器链接到到程序上program
        glAttachShader(screenProgramID, vertexShader)
        glAttachShader(screenProgramID, fragmentShader)
        glLinkProgram(screenProgramID)
        //检查program的状态
        glGetProgramiv(screenProgramID, GL_LINK_STATUS, state, 2)
        if (state[2] == 0) {
            val message = glGetProgramInfoLog(screenProgramID)
            return "screen program compile error:\n$message"
        }
        //link结束，删除vertexShader和fragmentShader
        glDeleteShader(vertexShader)
        glDeleteShader(fragmentShader)
        return null
    }

}