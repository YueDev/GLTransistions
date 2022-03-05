package com.example.gltransistions.blur

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.gltransistions.MainViewModel
import com.example.gltransistions.databinding.FragmentBlurBinding
import com.google.android.material.slider.Slider


class BlurFragment : Fragment() {

    private lateinit var binding: FragmentBlurBinding

    private val mainViewModel by activityViewModels<MainViewModel>()

    private lateinit var surfaceView: BlurGLSurfaceView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBlurBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        surfaceView = binding.blurGLSurfaceView

        surfaceView.setResources(mainViewModel.bitmaps)


        binding.bezierView.onTouch = {
            surfaceView.stopAnimator()
        }

        binding.startButton.setOnClickListener {
            val array = binding.bezierView.getControls()
            surfaceView.startAnimator(array[0], array[1], array[2], array[3])
        }

        binding.slider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                surfaceView.stopAnimator()
            }

            override fun onStopTrackingTouch(slider: Slider) {
                binding.blurGLSurfaceView.animatorTime = slider.value.toInt() * 1000
            }
        })


    }


    override fun onPause() {
        super.onPause()
        surfaceView.stopAnimator()
    }

}