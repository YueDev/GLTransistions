package com.example.gltransistions

import android.graphics.Bitmap
import android.opengl.GLES11Ext
import android.opengl.GLES30.*
import android.opengl.GLUtils



val imageResList = listOf(
    R.mipmap.test1,
    R.mipmap.test2,
    R.mipmap.test3,
    R.mipmap.test4,
    R.mipmap.test5
)


val navResList = listOf(
    Pair(R.string.blur_fragment_label, R.id.action_HomeFragment_to_blurFragment),
    Pair(R.string.bezier_fragment_label, R.id.action_HomeFragment_to_bezierFragment),
    Pair(R.string.split_fragment_label, R.id.action_HomeFragment_to_splitFragment),
    Pair(R.string.frame_buffer_fragment_label, R.id.action_HomeFragment_to_frameBufferFragment),
    Pair(R.string.video_fragment_label, R.id.action_HomeFragment_to_videoFragment),

)

class OPENGL {

    companion object {

        val UNIFORM_TEXTURES = listOf("texture0", "texture1")

        const val UNIFORM_TEXTURE_VIDEO = "textureVideo"

        const val UNIFORM_PROGRESS = "progress"

        const val UNIFORM_VIDEO_RATIO = "videoRatio"


        const val UNIFORM_BEZIER_C1X = "c1x"
        const val UNIFORM_BEZIER_C1Y = "c1y"
        const val UNIFORM_BEZIER_C2X = "c2x"
        const val UNIFORM_BEZIER_C2Y = "c2y"

        const val UNIFORM_SPLIT_NUM = "num"


        fun genBitmapTexture(bitmap: Bitmap): Int {

            val textureId = IntArray(1)
            glActiveTexture(GL_TEXTURE0)
            glGenTextures(1, textureId, 0)
            glBindTexture(GL_TEXTURE_2D, textureId[0])
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR_MIPMAP_LINEAR)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR)

            GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0)

            glGenerateMipmap(GL_TEXTURE_2D)

            glBindTexture(GL_TEXTURE_2D, GL_NONE)

            return textureId[0]

        }


        fun getVideoTexture(): Int {
            val textureId = IntArray(1)
            glActiveTexture(GL_TEXTURE0)
            glGenTextures(1, textureId, 0)
            glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId[0])

            glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
            glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
            glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
            glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MIN_FILTER, GL_LINEAR)

            return textureId[0]
        }


        //生成一个空的纹理，一般是给frameBuffer当颜色缓冲
        fun genColorTexture(width:Int, height:Int): Int {
            val textureId = IntArray(1)
            glActiveTexture(GL_TEXTURE0)
            glGenTextures(1, textureId, 0)
            glBindTexture(GL_TEXTURE_2D, textureId[0])
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
            //空的纹理
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, null)

            glBindTexture(GL_TEXTURE_2D, GL_NONE)
            return textureId[0]
        }


    }

}
