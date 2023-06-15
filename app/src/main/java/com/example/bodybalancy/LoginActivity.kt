package com.example.bodybalancy

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.bodybalancy.R
import com.example.bodybalancy.databinding.ActivityLoginBinding
import com.example.bodybalancy.utils.FontManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*


class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    private lateinit var progressBar: ProgressBar


    private val RC_SIGN_IN = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

//        FontManager.initialize(this)

        supportActionBar?.hide()

        val textView = binding.registerTextView
        progressBar = binding.progressBar

        val registerText = "click here if you haven't registered"
        val spannableString = SpannableString(registerText)

        // Set the color of the 'register' word to blue
        val blueColor = resources.getColor(R.color.black)
        val registerClickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                // Handle the click action here

                val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                startActivity(intent)
            }
        }
        spannableString.setSpan(registerClickableSpan, 0, registerText.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(android.text.style.ForegroundColorSpan(blueColor), 0, registerText.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)

        textView.text = spannableString
        textView.movementMethod = android.text.method.LinkMovementMethod.getInstance()

        // Configure Google Sign-In options
//        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestIdToken("246687032439-1pd8f50ea8hd6o5slo0sdv6pkrjtaeq9.apps.googleusercontent.com")
//            .requestEmail()
//            .build()

        // Create a GoogleSignInClient with the options specified above
//        val googleSignInClient = GoogleSignIn.getClient(this, gso)


        binding.loginButton.setOnClickListener(View.OnClickListener { view ->
            signInUser(binding.usernameEditText.text.toString(), binding.passwordEditText.text.toString())
        })

//        binding.btnSignInWithGoogle.setOnClickListener(View.OnClickListener { view ->
//            val signInIntent = googleSignInClient.signInIntent
//            startActivityForResult(signInIntent, RC_SIGN_IN)
//        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign-In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account?.idToken)
            } catch (e: ApiException) {
                // Google Sign-In failed, display an error message
                Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Google Sign-In failed: ${e.message}")
            }
        }
    }

    private fun signInUser(email: String, password: String) {
        // Show the progress bar
        progressBar.visibility = View.VISIBLE

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                // Hide the progress bar
                progressBar.visibility = View.GONE

                if (task.isSuccessful) {
                    // Sign-in successful, get the signed-in user
                    val user: FirebaseUser? = auth.currentUser
                    // can perform additional actions here, such as updating UI or navigating to another screen

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)

                    // Remove the current activity
                    finish()
                } else {
                    // Sign-in failed, display an error message
                    val exception = task.exception
                    // Show a popup screen based on the specific authentication error
                    if (exception is FirebaseAuthInvalidUserException) {
                        // Handle invalid user error
                        showPopupScreen("Invalid user. Please check your email or register a new account.")
                    } else if (exception is FirebaseAuthInvalidCredentialsException) {
                        // Handle invalid credentials error
                        showPopupScreen("Invalid credentials. Please check your email and password.")
                    } else {
                        // Handle other errors
                        showPopupScreen("Sign-in failed. Please try again later.")
                    }
                    // Log the error message for debugging
                    Log.e(TAG, "Sign-in failed: ${exception?.message}")
                    // Handle the exception accordingly
                }
            }
    }

    private fun showPopupScreen(message: String) {
        // Create a AlertDialog.Builder to show the popup screen
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Sign-in Error")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                // Dismiss the dialog
                dialog.dismiss()
            }
        val dialog = builder.create()
        dialog.show()
    }

//Errors
    // Authenticate with Firebase using the Google ID token
    private fun firebaseAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign-in successful, update UI or perform further actions
                    Toast.makeText(this, "Google Sign-In successful", Toast.LENGTH_SHORT).show()
                } else {
                    // Sign-in failed, display an error message
                    Toast.makeText(this, "Firebase Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

}
