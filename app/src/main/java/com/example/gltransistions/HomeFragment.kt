package com.example.gltransistions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gltransistions.databinding.FragmentHomeBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val homeAdapter = HomeAdapter {
            findNavController().navigate(navResList[it].second)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = homeAdapter
        homeAdapter.submitList(navResList.map { it.first })

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    class HomeAdapter(private val itemClick: ((position: Int) -> Unit)? = null) : ListAdapter<Int, HomeHolder>(IntDiffCallback()) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
            return HomeHolder(itemView).also { holder ->
                holder.itemView.setOnClickListener {
                    if (holder.adapterPosition < 0) return@setOnClickListener
                    itemClick?.invoke(holder.adapterPosition)
                }
            }
        }

        override fun onBindViewHolder(holder: HomeHolder, position: Int) {
            holder.bind(getItem(position))
        }

    }


    class HomeHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView = itemView.findViewById<TextView>(android.R.id.text1)

        fun bind(@StringRes resId: Int) {
            textView.setText(resId)
        }
    }

    class IntDiffCallback : DiffUtil.ItemCallback<Int>() {
        override fun areItemsTheSame(oldItem: Int, newItem: Int): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Int, newItem: Int): Boolean {
            return oldItem == newItem
        }
    }

}