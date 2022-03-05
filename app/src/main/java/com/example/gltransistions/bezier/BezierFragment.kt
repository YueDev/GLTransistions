package com.example.gltransistions.bezier

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.PathInterpolator
import androidx.fragment.app.Fragment
import com.example.gltransistions.databinding.FragmentBezierBinding


//把一段均匀的0-1进度 调整成贝塞尔曲线
class BezierFragment : Fragment() {

    private lateinit var binding: FragmentBezierBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBezierBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        val v = binding.view
        val v2 = binding.view2

        val bezierView = binding.bezierView

        binding.button.setOnClickListener {

            v.translationX  = 0f
            v2.translationX = 0f

            val controls = bezierView.getControls()
            
            //自定义的插值器
            val interpolator1 = MyInterpolator(controls[0], controls[1], controls[2], controls[3])
            //系统的贝塞尔插值器
            val interpolator2 = PathInterpolator(controls[0], controls[1], controls[2], controls[3])

            val totalS = (requireView().measuredWidth - v.measuredWidth).toFloat()


            //同时两个动画进行比较
            val animator1 = ValueAnimator.ofFloat(0f, totalS)
            animator1.interpolator = interpolator1
            animator1.addUpdateListener {
                v.translationX = it.animatedValue as Float
            }

            val animator2 = ValueAnimator.ofFloat(0f, totalS)
            animator2.interpolator = interpolator2
            animator2.addUpdateListener {
                v2.translationX = it.animatedValue as Float
            }

            AnimatorSet().apply {
                playTogether(animator1, animator2)
                duration = 2000
            }.start()

        }

    }


}




