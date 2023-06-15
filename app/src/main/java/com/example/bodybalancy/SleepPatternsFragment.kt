package com.example.bodybalancy

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.bodybalancy.databinding.FragmentSleepPatternsBinding

class SleepPatternsFragment : Fragment() {

    private var _binding: FragmentSleepPatternsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment using View Binding
        _binding = FragmentSleepPatternsBinding.inflate(inflater, container, false)
        val view = binding.root

        // Set the toolbar as the action bar
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar)

        // Enable the back button
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayShowHomeEnabled(true)

        // Set the back button icon
        binding.toolbar.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_back)
        binding.toolbar.navigationContentDescription = "Sleep Patterns"

        // Set the back button click listener
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        return view
    }

    // Handle back button press
    override fun onResume() {
        super.onResume()

        // Add your custom back button functionality here
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up the binding
        _binding = null
    }
}
