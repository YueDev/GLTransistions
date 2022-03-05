package com.example.gltransistions.video

/**
 * Created by Yue on 2021/7/15.
 * gl的sprite，由canvas层转换而来
 *
 */

interface GLSprite {

    fun glInit(width: Int, height: Int)

    //gl相关
    fun glPreDraw()
    fun glDraw(programId: Int)

    fun glPreFrame()
    fun glNextFrame(time: Int)
    fun glNextFrame(frames: Int, fps: Int)

    fun glRelease()


    //新增手势  android view调用来更改gl层的矩阵
    fun postTranslateFromView(dx: Float, dy: Float)
    fun postScaleFromView(scale: Float, px: Float, py: Float)
    fun postRotateFromView(degree: Float, px: Float, py: Float)

}