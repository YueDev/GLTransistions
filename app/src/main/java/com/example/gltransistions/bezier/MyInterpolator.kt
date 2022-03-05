package com.example.gltransistions.bezier

import android.view.animation.Interpolator
import kotlin.math.absoluteValue
import kotlin.math.pow

//自定义的bezier插值器
class MyInterpolator(
    private val mX1: Float,
    private val mY1: Float,
    private val mX2: Float,
    private val mY2: Float
) : Interpolator {

    private val min = 0.00001f

    override fun getInterpolation(input: Float): Float {

        var t0 = 0.0f
        var t1 = 1.0f
        var t = 0.0f

        //2分法  根据x 求t
        do {
            t = t0 + (t1 - t0) / 2
            val x = bezier(t, mX1, mX2)
            if (x - input > 0) {
                t1 = t
            } else {
                t0 = t
            }
        } while ((x - input).absoluteValue > min)

        //根据t 计算出y
        return bezier(t, mY1, mY2)
    }


    //根据t 求 x或者 y
    private fun bezier(t: Float, c1: Float, c2: Float): Float {
        return (1 - t).pow(2) * 3 * t * c1 + t.pow(2) * (1 - t) * c2 * 3 + t.pow(3)
    }

}