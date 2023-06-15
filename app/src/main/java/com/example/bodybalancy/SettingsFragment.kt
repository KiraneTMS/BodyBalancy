package com.example.bodybalancy

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import com.example.bodybalancy.databinding.FragmentSettingsBinding
import com.example.bodybalancy.utils.SettingsManager
import com.google.firebase.auth.FirebaseAuth

class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var alarmSwitch: Switch
    private lateinit var settingsManager: SettingsManager


    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root


        sharedPreferences = requireContext().getSharedPreferences("WeightType", Context.MODE_PRIVATE)

        clearWeightType()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        settingsManager = SettingsManager(requireContext())

        alarmSwitch = binding.alarmSwitch
        alarmSwitch.isChecked = isAlarmEnabled

        alarmSwitch.setOnClickListener {
            val isChecked = alarmSwitch.isChecked
            isAlarmEnabled = isChecked
            settingsManager.setAlarmEnabled(isChecked)
        }

        binding.logoutTextView.setOnClickListener {
            signOutUser()
        }
    }

    private var isAlarmEnabled: Boolean
        get() = settingsManager.isAlarmEnabled()
        set(value) { settingsManager.setAlarmEnabled(value) }



    private fun signOutUser() {
        auth.signOut()
        // Handle the necessary navigation or UI updates after signing out the user

        // Delete the current activity
        activity?.finish()

        // Start the login activity
        val intent = Intent(activity, LoginActivity::class.java)
        startActivity(intent)
    }
    private fun clearWeightType() {
        val editor = sharedPreferences.edit()
        editor.remove("Weight Type")
        editor.apply()
    }

}
