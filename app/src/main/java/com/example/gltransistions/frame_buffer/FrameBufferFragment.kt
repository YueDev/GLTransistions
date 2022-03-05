package com.example.gltransistions.frame_buffer

import android.animation.TimeAnimator
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.gltransistions.MainViewModel
import com.example.gltransistions.R
import com.example.gltransistions.databinding.FragmentFrameBufferBinding
import com.google.android.material.slider.Slider

class FrameBufferFragment : Fragment() {

    private lateinit var binding: FragmentFrameBufferBinding
    private val viewModel by activityViewModels<MainViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentFrameBufferBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.surfaceView.setResources(viewModel.reverseBitmap)

        binding.slider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {

            }

            override fun onStopTrackingTouch(slider: Slider) {
                val duration = (slider.value * 1000).toInt()
                binding.surfaceView.setDuration(duration)
            }
        })

    }


    companion object {
        @JvmStatic
        fun newInstance() = FrameBufferFragment()
    }
}