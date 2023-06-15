package com.example.bodybalancy

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import com.example.bodybalancy.databinding.ActivityRegisterBinding
import com.example.bodybalancy.utils.FontManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore


// Initialize Firebase Authentication
val auth: FirebaseAuth = FirebaseAuth.getInstance()

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

//        FontManager.initialize(this)

        supportActionBar?.hide()


        progressBar = binding.progressBar

        // Set initial state of show password buttons
        binding.showPasswordButton.isChecked = false
        binding.showPasswordIcon.setImageResource(R.drawable.ic_visibility_off)

        binding.showConfirmPasswordButton.isChecked = false
        binding.showConfirmPasswordIcon.setImageResource(R.drawable.ic_visibility_off)

// Set click listeners for buttons
        binding.RegisterButton.setOnClickListener(View.OnClickListener { view ->
            registerUser(binding.usernameEditText.text.toString(), binding.passwordEditText.text.toString())
            val selectedGender = when (binding.genderRadioGroup.checkedRadioButtonId) {
                R.id.maleRadioButton -> "Male"
                R.id.femaleRadioButton -> "Female"
                else -> ""
            }
            saveUserGender(selectedGender)
        })

        binding.showPasswordButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Show password
                binding.passwordEditText.transformationMethod = null
                binding.showPasswordIcon.setImageResource(R.drawable.ic_visibility)
                ViewCompat.setElevation(binding.showPasswordIcon, 8F)
            } else {
                // Hide password
                binding.passwordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.showPasswordIcon.setImageResource(R.drawable.ic_visibility_off)
                ViewCompat.setElevation(binding.showPasswordIcon, 8F)
            }
        }

        binding.showConfirmPasswordButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Show password
                binding.confirmPasswordEditText.transformationMethod = null
                binding.showConfirmPasswordIcon.setImageResource(R.drawable.ic_visibility)
                ViewCompat.setElevation(binding.showConfirmPasswordIcon, 8F)
            } else {
                // Hide password
                binding.confirmPasswordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.showConfirmPasswordIcon.setImageResource(R.drawable.ic_visibility_off)
                ViewCompat.setElevation(binding.showConfirmPasswordIcon, 8F)
            }
        }
    }

    fun saveUserGender(gender: String) {

        val firestore = FirebaseFirestore.getInstance()
        val collectionRef = firestore.collection("user_detail")
        val documentId = binding.usernameEditText.text.toString()

        val data = hashMapOf(

            "gender" to gender
        )

        collectionRef.document(documentId)
            .set(data)
            .addOnSuccessListener {
                // Data successfully saved with custom document ID
            }
            .addOnFailureListener { exception ->
                // An error occurred while saving the data

            }
    }

    private fun registerUser(email: String, password: String) {
        if (!validatePassword(password, binding.confirmPasswordEditText.text.toString())) {
            return
        }

        progressBar.visibility = View.VISIBLE // Show the progress bar

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                progressBar.visibility = View.GONE // Hide the progress bar

                if (task.isSuccessful) {
                    // Registration successful, get the registered user
                    val user: FirebaseUser? = auth.currentUser
                    // can perform additional actions here, such as sending a verification email

                    // Move to MainActivity
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)

                    // Remove the current activity
                    finish()
                } else {
                    // Registration failed, display an error message
                    val exception = task.exception
                    Toast.makeText(this, "Registration failed: ${exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }



    private fun validatePassword(password: String, confirmPassword: String): Boolean {
        println("$password $confirmPassword")
        if (password.length < 8) {
            // Password is less than 8 characters
            showPopupScreen("Password must be at least 8 characters long")
            return false
        }

        if (password != confirmPassword) {
            // Password and Confirm Password fields do not match
            showPopupScreen("Password and Confirm Password do not match")
            return false
        }

        // Password is valid
        return true
    }

    private fun showPopupScreen(message: String) {
        // Create a AlertDialog.Builder to show the popup screen
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Validation Error")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                // Dismiss the dialog
                dialog.dismiss()
            }
        val dialog = builder.create()
        dialog.show()
    }


}
