package com.example.gltransistions.video

import android.graphics.Bitmap
import android.opengl.GLES30.*
import android.opengl.GLUtils

/**
 * Created by Yue on 2021/5/29.
 */


object GLSL {

    const val MODEL_UNIFORM_NAME = "model"
    const val PROJECTION_UNIFORM_NAME = "projection"
    const val VIEW_UNIFORM_NAME = "view"
    const val ALPHA_UNIFORM_NAME = "texAlpha"

    const val VERTEX_SHADER_STRING = "#version 300 es\n" +
            "\n" +
            "layout (location = 0) in vec3 aPos;\n" +
            "layout (location = 1) in vec2 aTexCoord;\n" +
            "\n" +
            "uniform mat4 model;\n" +
            "uniform mat4 view;\n" +
            "uniform mat4 projection;\n" +
            "\n" +
            "out vec2 texCoord;\n" +
            "\n" +
            "void main() {\n" +
            "    gl_Position = projection * view * model * vec4(aPos, 1.0f);\n" +
            "    texCoord = aTexCoord;\n" +
            "}"

    const val FRAGMENT_SHADER_STRING = "#version 300 es\n" +
            "\n" +
            "precision mediump float;\n" +
            "\n" +
            "in vec2 texCoord;\n" +
            "\n" +
            "uniform sampler2D hanaTexture;\n" +
            "uniform float texAlpha;\n" +
            "\n" +
            "out vec4 outColor;\n" +
            "\n" +
            "void main() {\n" +
            "    outColor = texture(hanaTexture, texCoord) * texAlpha;\n" +
            "}"


    fun genBitmapTexture(bitmap: Bitmap): Int {

        val textureId = IntArray(1)

        glActiveTexture(GL_TEXTURE0)
        glGenTextures(1, textureId, 0)
        glBindTexture(GL_TEXTURE_2D, textureId[0])
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR)

        GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0)

        glGenerateMipmap(GL_TEXTURE_2D)

        glBindTexture(GL_TEXTURE_2D, 0)

        return textureId[0]

    }


}






