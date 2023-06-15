package com.example.bodybalancy

import HomeFragment
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.bodybalancy.*
import com.example.bodybalancy.databinding.ActivityMainBinding
import com.example.bodybalancy.utils.FontManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var currentFragmentTag: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

//        FontManager.initialize(this)


        supportActionBar?.hide()
        supportActionBar?.apply {
            title = ""
        }

        supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.main)))

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.navbar)
        bottomNavigationView.selectedItemId = R.id.item3

        // Check if user is not logged in
        if (auth.currentUser == null) {
            // User is not logged in, navigate to the login page
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Optional: Close the current activity so that the user cannot go back to it without logging in
        } else {
            // User is logged in, continue with the main activity
            // ...
        }

        val bottomNav: BottomNavigationView = binding.navbar
        bottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.item1 -> {
                    supportActionBar?.show()
                    replaceFragment(ToDoFragment(), "ToDoFragment")
                    true
                }
                R.id.item2 -> {
                    supportActionBar?.hide()
                    replaceFragment(SearchFragment(), "SearchFragment")
                    true
                }
                R.id.item3 -> {
                    supportActionBar?.hide()
                    replaceFragment(HomeFragment(), "HomeFragment")
                    true
                }
                R.id.item4 -> {
                    supportActionBar?.hide()
                    replaceFragment(ProfileFragment(), "ProfileFragment")
                    true
                }
                R.id.item5 -> {
                    supportActionBar?.hide()
                    replaceFragment(SettingsFragment(), "SettingsFragment")
                    true
                }
                else -> false
            }
        }

        // Restore the current fragment after a configuration change (e.g., device rotation)
        if (savedInstanceState != null) {
            currentFragmentTag = savedInstanceState.getString(KEY_CURRENT_FRAGMENT_TAG)
            val currentFragment = supportFragmentManager.findFragmentByTag(currentFragmentTag)
            currentFragment?.let {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, it, currentFragmentTag)
                    .commit()
            }
        } else {
            // Set the initial fragment
            replaceFragment(HomeFragment(), "HomeFragment")
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save the tag of the current fragment
        outState.putString(KEY_CURRENT_FRAGMENT_TAG, currentFragmentTag)
    }

    private fun replaceFragment(fragment: Fragment, tag: String) {
        currentFragmentTag = tag
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment, tag)
            .commit()
    }

    companion object {
        private const val KEY_CURRENT_FRAGMENT_TAG = "current_fragment_tag"
    }
}
