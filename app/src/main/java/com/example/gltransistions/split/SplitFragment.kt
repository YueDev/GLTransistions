package com.example.gltransistions.split

import android.animation.TimeAnimator
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.gltransistions.MainViewModel
import com.example.gltransistions.databinding.FragmentSplitBinding
import com.google.android.material.slider.Slider


class SplitFragment : Fragment() {

    private val mainViewModel by activityViewModels<MainViewModel>()

    private lateinit var binding: FragmentSplitBinding

    private val animatorTime = 5000

    private val animator = TimeAnimator()

    private var numX = 1.0f
    private var numY = 1.0f

    private lateinit var splitSurfaceView: SplitGLSurfaceView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSplitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        splitSurfaceView = binding.surfaceView

        splitSurfaceView.setResources(mainViewModel.bitmaps)

        animator.setTimeListener { _, totalTime, _ ->
            val progress = totalTime % animatorTime / animatorTime.toFloat()
            val index = totalTime / animatorTime
            splitSurfaceView.setProgress(0f, index.toInt(), numX, numY)
        }


        binding.slider1.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {

            }

            override fun onStopTrackingTouch(slider: Slider) {
                numX = slider.value
            }
        })

        binding.slider2.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {

            }

            override fun onStopTrackingTouch(slider: Slider) {
                numY = slider.value
            }
        })

    }


    override fun onResume() {
        super.onResume()
        if (!animator.isStarted) {
            splitSurfaceView.setProgress(0.0f, 0)
            animator.start()
        }
    }


    override fun onPause() {
        super.onPause()
        if (animator.isStarted) animator.cancel()
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) = SplitFragment()
    }
}