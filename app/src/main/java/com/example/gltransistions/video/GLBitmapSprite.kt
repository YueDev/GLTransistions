package com.example.gltransistions.video

import android.graphics.Bitmap
import android.opengl.GLES30.*
import android.opengl.Matrix
import android.util.Log

/**
 * Created by Yue on 2021/7/15.
 */


class GLBitmapSprite(
    private val bitmap: Bitmap,
    private val canvasWidth: Int,
    private val canvasHeight: Int,
    private val initCenterX: Float = canvasWidth / 2f,
    private val initCenterY: Float = canvasHeight / 2f,
    private val initScale: Float = 1f,
    private val initRotate: Float = 0f,
) : GLSprite {


    //视口边界
    private var glLeft = 1f
    private var glRight = 1f

    //gl的matrix
    private val glMatrix = FloatArray(16)

    //图片的g旋转角度（z轴，即xy平面内的旋转角度）
    private var glRotate = 0f

    //图片的整体缩放值
    private var glScale = 1f

    private var glTextureId = 0

    private val alpha = 1f

    //width height是gl的视口大小
    override fun glInit(width: Int, height: Int) {
        //1、先计算gl视口的边界
        //用的透视投影， height在视口里一直保持为1
        glRight = width.toFloat() / height
        glLeft = -glRight

        val bitmapWidth = bitmap.width
        val bitmapHeight = bitmap.height

//        2、 计算图片的正确比例，这样图片在opengl坐标系中比例正确
        //由于height是确定的，因此缩放x， 这样图片是按照height缩放到视口了

        var initScaleX = bitmapWidth.toFloat() / bitmapHeight
        var initScaleY = 1f


        //   3、按图片缩放原图

        val srcScale = (bitmapHeight.toFloat() / height) / initScaleY


        initScaleX *= srcScale
        initScaleY *= srcScale
        //至此，图片在视口中按照本身大小显示了，之后按照之前记录的scale和rotate等计算矩阵
//
//
        //4、按照initScale缩放
        initScaleX *= initScale
        initScaleY *= initScale
//
        glScale = initScale
//
        // 4.5、插一步， 现在得到的是原图canvas的大小，要换成opengl的大小

        val s = height.toFloat() / canvasHeight
        initScaleX *= s
        initScaleY *= s

        glScale *= s

        Matrix.setIdentityM(glMatrix, 0)
        Matrix.scaleM(glMatrix, 0, initScaleX, initScaleY, 1f)
//
//
        //5、按照initRotate旋转
        val lhs = FloatArray(16)
        Matrix.setRotateM(lhs, 0, -initRotate, 0f, 0f, 1f)
        Matrix.multiplyMM(glMatrix, 0, lhs, 0, glMatrix, 0)

        glRotate += -initRotate
//
//
        //6   求canvas层图片中心点的坐标，转换为gl层的坐标
        //   然后把纹理平移过去即可
        val initCenter = floatArrayOf(initCenterX, initCenterY)

        glCanvasPointToGL(initCenter)

        glPostTranslate(initCenter[0], initCenter[1])

    }

    //GL绘制前调用
    override fun glPreDraw() {
        glTextureId = GLSL.genBitmapTexture(bitmap)
    }

    //每次GL绘制调用
    override fun glDraw(programId: Int) {
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, glTextureId)
        val modelLocation = glGetUniformLocation(programId, GLSL.MODEL_UNIFORM_NAME)
        glUniformMatrix4fv(modelLocation, 1, false, glMatrix, 0)
        val alphaLocation = glGetUniformLocation(programId, GLSL.ALPHA_UNIFORM_NAME)
        glUniform1f(alphaLocation, alpha)
    }

    //动画之前记录初始化数据
    override fun glPreFrame() {

    }

    //动画时间改变时时候调用
    override fun glNextFrame(time: Int) {

    }

    //动画的每一帧调用，计算出动画时间调用glNextFrame(time: Int)
    override fun glNextFrame(frames: Int, fps: Int) {
        val time = frames * 1000
        glNextFrame(time)
    }

    //释放，清除纹理
    override fun glRelease() {
        glDeleteTextures(1, intArrayOf(glTextureId), 0)
    }

    //x y轴的位移
    private fun glPostTranslate(dx: Float, dy: Float) {
        val lhs = FloatArray(16)
        Matrix.setIdentityM(lhs, 0)
        Matrix.translateM(lhs, 0, dx, dy, 0f)
        Matrix.multiplyMM(glMatrix, 0, lhs, 0, glMatrix, 0)
    }

    //按照原点缩放x和y
    private fun glPostScale(scale: Float) {
        val lhs = FloatArray(16)
        Matrix.setIdentityM(lhs, 0)
        Matrix.scaleM(lhs, 0, scale, scale, 1f)
        Matrix.multiplyMM(glMatrix, 0, lhs, 0, glMatrix, 0)
        glScale *= scale
    }

    //按指定点缩放x y
    private fun glPostScale(scale: Float, sx: Float, sy: Float) {
        glPostTranslate(-sx, -sy)
        glPostScale(scale)
        glPostTranslate(sx, sy)
    }

    //按照图片中心点缩放
    private fun glPostScaleCenter(scale: Float) {
        val rhs = FloatArray(16)
        Matrix.setIdentityM(rhs, 0)
        Matrix.scaleM(rhs, 0, scale, scale, 1f)
        Matrix.multiplyMM(glMatrix, 0, glMatrix, 0, rhs, 0)
        glScale *= scale
    }


    //坐标轴原点的旋转
    private fun glPostRotateZ0(degree: Float) {
        val lhs = FloatArray(16)
        Matrix.setIdentityM(lhs, 0)
        Matrix.setRotateM(lhs, 0, degree, 0f, 0f, 1f)
        Matrix.multiplyMM(glMatrix, 0, lhs, 0, glMatrix, 0)
        glRotate += degree
    }


    //绕图片当前中心点旋转
    private fun glPostRotateZ(degree: Float) {

        val center = floatArrayOf(0f, 0f)
        glMapPoint(glMatrix, center)

        glPostTranslate(-center[0], -center[1])
        glPostRotateZ0(degree)
        glPostTranslate(center[0], center[1])
    }

    //绕图片上边中点旋转
    private fun glPostRotateZTop(degree: Float) {
        //由于投影的缘故，图片是fit height放置的，因此顶点中心坐标就是(0, 1)
        val top = floatArrayOf(0f, 1f)
        glMapPoint(glMatrix, top)

        glPostTranslate(-top[0], -top[1])
        glPostRotateZ0(degree)
        glPostTranslate(top[0], top[1])
    }

    //绕图片下边中点旋转
    private fun glPostRotateZBottom(degree: Float) {
        val top = floatArrayOf(0f, -1f)
        glMapPoint(glMatrix, top)

        glPostTranslate(-top[0], -top[1])
        glPostRotateZ0(degree)
        glPostTranslate(top[0], top[1])
    }


    // 绕图片的Y轴旋转，有一个3d效果要用
    // 具体实现就是 把图片的中心点移到原点，把Z轴转正，然后绕Y轴旋转，再把Z轴转回，再平移回去 比较麻烦
    private fun glPostRotateY(degree: Float) {

        //1、先将图片移到原点点，然后Z轴转正
        val rotateTemp = glRotate

        val center = floatArrayOf(0f, 0f)
        glMapPoint(glMatrix, center)
        glPostTranslate(-center[0], -center[1])

        glPostRotateZ0(-rotateTemp)

        //2、绕Y轴旋转
        val lhs = FloatArray(16)
        Matrix.setIdentityM(lhs, 0)
        Matrix.setRotateM(lhs, 0, degree, 0f, 1f, 0f)
        Matrix.multiplyMM(glMatrix, 0, lhs, 0, glMatrix, 0)

        //3、Z轴角度转回，然后移回原位置
        glPostRotateZ0(rotateTemp)
        glPostTranslate(center[0], center[1])
    }


    //把画布层的点转换为gl坐标
    private fun glCanvasPointToGL(array: FloatArray) {
        if (array.size != 2) return
        val s = canvasHeight / 2f
        array[0] = (array[0] - canvasWidth / 2f) / s
        array[1] = -(array[1] - canvasHeight / 2f) / s
    }


    private fun glMapPoint(matrix: FloatArray, array: FloatArray) {
        if (matrix.size != 16 || array.size != 2) return
        //矩阵X矢量（xyz1是点， xyz0就是向量了)
        val vector = floatArrayOf(array[0], array[1], 0f, 1f)
        Matrix.multiplyMV(vector, 0, matrix, 0, vector, 0)
        array[0] = vector[0]
        array[1] = vector[1]
    }


    //view层的位移转换成gl层的，注意y轴是相反的
    //注意按照高度来算
    override fun postTranslateFromView(dx: Float, dy: Float) {
        val glDx = dx * 2.0f / canvasHeight
        val glDy = -dy * 2.0f / canvasHeight
        glPostTranslate(glDx, glDy)
    }

    //view层的缩放，注意把view层的px py转换成gl层的坐标
    override fun postScaleFromView(scale: Float, px: Float, py: Float) {
        val glArray = floatArrayOf(px, py)
        glCanvasPointToGL(glArray)
        glPostScale(scale, glArray[0], glArray[1])
    }

    override fun postRotateFromView(degree: Float, px: Float, py: Float) {
        val glArray = floatArrayOf(px, py)
        glCanvasPointToGL(glArray)

        glPostTranslate(-glArray[0], -glArray[1])
        glPostRotateZ0(-degree)
        glPostTranslate(glArray[0], glArray[1])
    }
}