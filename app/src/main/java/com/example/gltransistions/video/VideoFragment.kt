package com.example.gltransistions.video

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.activityViewModels
import com.example.gltransistions.MainViewModel
import com.example.gltransistions.R
import com.example.gltransistions.databinding.FragmentFrameBufferBinding
import com.example.gltransistions.databinding.FragmentVideoBinding


class VideoFragment : Fragment() {

    private lateinit var binding: FragmentVideoBinding
    private val viewModel by activityViewModels<MainViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentVideoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.videoLayout.post {

            val width = binding.videoLayout.measuredWidth
            val height = binding.videoLayout.measuredHeight

            val surfaceView = VideoSurfaceView(requireContext())
            val layoutParams = FrameLayout.LayoutParams(width, height)
            layoutParams.gravity = Gravity.CENTER

            surfaceView.layoutParams = layoutParams

            binding.videoLayout.addView(surfaceView)
            surfaceView.start(viewModel.reverseImage, width, height)
        }
    }


    companion object {
        @JvmStatic
        fun newInstance() = VideoFragment()
    }
}