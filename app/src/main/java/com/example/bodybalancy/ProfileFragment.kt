package com.example.bodybalancy

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.bodybalancy.APIServices.PredictApiService
import com.example.bodybalancy.R
import com.example.bodybalancy.dataClasses.PredictionRequest
import com.example.bodybalancy.dataClasses.PredictionResponse
import com.example.bodybalancy.databinding.FragmentProfileBinding
import com.example.bodybalancy.databinding.PopupFormBinding
import com.example.bodybalancy.databinding.ProfilePopupFormBinding
import com.example.bodybalancy.models.ProfileViewModel
//import com.example.bodybalancy.utils.ImageUploadHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding

    private lateinit var viewModel: ProfileViewModel
    private var alertDialog: AlertDialog? = null

    private lateinit var sharedPreferences: SharedPreferences


    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Inside the ProfileFragment class

    // Add the following constant at the top
    private val PICK_IMAGE_REQUEST_CODE = 100


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setDetailValue()
        setProfileValue()

        binding.personImageview.visibility = View.GONE
        binding.shapeImageView.visibility = View.GONE

        sharedPreferences = requireContext().getSharedPreferences("WeightType", Context.MODE_PRIVATE)

        val userWeightType = sharedPreferences.getString("Weight Type", null)

        if (!userWeightType.isNullOrEmpty()) {
            binding.tvPredictionResult.text = userWeightType
            println("Weight Type: $userWeightType")
        } else {
            binding.tvPredictionResult.text = ""
            println("Weight Type: empty")
        }

        viewModel = ViewModelProvider(requireActivity()).get(ProfileViewModel::class.java)

        binding.btnEdit.setOnClickListener {
            showProfilePopupForm()
        }

        binding.btnDetailEdit.setOnClickListener {
            showPopupForm()
        }

        viewModel.age.observe(viewLifecycleOwner, Observer { age ->
            binding.tvAgeAnswer.text = age
        })

        viewModel.height.observe(viewLifecycleOwner, Observer { height ->
            binding.tvHeight.text = height
        })

        viewModel.weight.observe(viewLifecycleOwner, Observer { weight ->
            binding.tvWeight.text = weight
        })

        viewModel.obesity.observe(viewLifecycleOwner, Observer { obesity ->
            binding.tvObesityAnswer.text = obesity
        })

        viewModel.meat.observe(viewLifecycleOwner, Observer { meat ->
            binding.tvMeatAnswer.text = meat
        })

        binding.btnPredict.setOnClickListener {
            val weightString = binding.tvWeight.text.toString()
            val heightString = binding.tvHeight.text.toString()
            val ageString = binding.tvAgeAnswer.text.toString()
            val obesity = if (binding.tvObesityAnswer.text.toString() == "Yes") 1 else 0
            val meat = if (binding.tvMeatAnswer.text.toString() == "Yes") 1 else 0

            val weight = extractIntegerFromString(weightString).toFloat()
            val height = extractIntegerFromString(heightString).toFloat()
            val age = extractIntegerFromString(ageString)

            predict(weight, height, age, obesity, meat)

            // Display a toast message
            Toast.makeText(requireContext(), "This button clicked", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        alertDialog?.dismiss()
    }

    fun extractIntegerFromString(input: String): Int {
        val regex = Regex("\\d+")
        val matchResult = regex.find(input)
        return matchResult?.value?.toInt() ?: 0
    }

    private fun showProfilePopupForm() {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        val inflater = LayoutInflater.from(requireContext())
        val popupBinding = ProfilePopupFormBinding.inflate(inflater, null, false)
        dialogBuilder.setView(popupBinding.root)
        alertDialog = dialogBuilder.create()

        // Set the background drawable for the dialog window
        alertDialog?.window?.setBackgroundDrawableResource(R.drawable.background)

        // Observe the MutableLiveData fields and set the values to the EditTexts
        viewModel.name.observe(viewLifecycleOwner) { name ->
            popupBinding.editName.setText(name)
        }
//        viewModel.email.observe(viewLifecycleOwner) { email ->
//            popupBinding.editEmail.setText(email)
//        }

        // Set click listener for the profile picture ImageView
//        popupBinding.editProfilePicture.setOnClickListener {
//            // Open the image picker
//            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//            startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
//        }

        // Add a submit button to save the changes
        popupBinding.btnSubmit.setOnClickListener {
            val name = popupBinding.editName.text.toString().trim()
            val email = auth.currentUser?.email.toString()

            // Save the inputs to the ViewModel
            viewModel.saveNameAndEmail(name, email)

            saveUserName(name)
            binding.username.text = name

            // Show a toast message with the updated values
            val message = "Name: $name, Email: $email"
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

            alertDialog?.dismiss()

            viewModel.isPopupOpen = false
        }

        popupBinding.btnCancel.setOnClickListener {
            alertDialog?.dismiss() // Close the popup
        }

        alertDialog?.show()
        viewModel.isPopupOpen = true
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
//            data?.data?.let { imageUri ->
//                val imageUploadHelper = ImageUploadHelper(requireActivity())
//                imageUploadHelper.handleImageUpload(imageUri)
//            }
//        }
//    }

    fun saveUserName(name: String) {

        val firestore = FirebaseFirestore.getInstance()
        val collectionRef = firestore.collection("user_profile")
        val documentId = auth.currentUser?.email.toString()

        val data = hashMapOf(

            "name" to name
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


    private fun showPopupForm() {
        Log.d(TAG, "showPopupForm() called")

        val dialogBuilder = AlertDialog.Builder(requireContext())
        val inflater = LayoutInflater.from(requireContext())
        val popupBinding = PopupFormBinding.inflate(inflater, null, false)
        dialogBuilder.setView(popupBinding.root)
        alertDialog = dialogBuilder.create()

        // Set the background drawable for the dialog window
        alertDialog?.window?.setBackgroundDrawableResource(R.drawable.background)

        // Fetch the existing document data from Firestore and set it to the EditTexts and radio buttons
        val firestore = FirebaseFirestore.getInstance()
        val collectionRef = firestore.collection("user_detail")
        val documentId = auth.currentUser?.email.toString()

        collectionRef.document(documentId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    println("Document Exist")
                    val age = document.getLong("age")?.toString() ?: ""
                    val height = document.getLong("height")?.toString() ?: ""
                    val weight = document.getLong("weight")?.toString() ?: ""
                    val obesity = document.getLong("obesity_history")?.toInt() == 1
                    val meat = document.getLong("favc")?.toInt() == 1

                    popupBinding.etAge.setText(age)
                    popupBinding.etHeight.setText(height)
                    popupBinding.etWeight.setText(weight)

                    if (obesity) {
                        popupBinding.rbYesObesity.isChecked = true
                    } else {
                        popupBinding.rbNoObesity.isChecked = true
                    }

                    if (meat) {
                        popupBinding.rbYesMeat.isChecked = true
                    } else {
                        popupBinding.rbNoMeat.isChecked = true
                    }
                }else {
                    println("Document Does Not Exist")
                }
            }
            .addOnFailureListener { exception ->
                // Handle the failure to fetch document data from Firestore
                Toast.makeText(requireContext(), "Failed to fetch document data", Toast.LENGTH_SHORT).show()
                println("Error fetching document: ${exception.message}")
            }

        popupBinding.btnSubmit.setOnClickListener {
            val age = popupBinding.etAge.text.toString().trim().toInt()
            val height = popupBinding.etHeight.text.toString().trim().toFloatOrNull() ?: 0f
            val weight = popupBinding.etWeight.text.toString().trim().toFloatOrNull() ?: 0f
            val obesity = if (popupBinding.rbYesObesity.isChecked) 1 else 0
            val meat = if (popupBinding.rbYesMeat.isChecked) 1 else 0

            // Perform additional validation
            if (age != 0 && height != 0f && weight != 0f) {
                // Validate age, height, and weight values
                if (age >= 0 && height > 0 && weight > 0) {
                    // Valid input data

                    clearWeightType()
                    saveDetailUser(height, weight, age, obesity, meat)
                    viewModel.isPopupOpen = false
                    // Save the inputs to the ViewModel
//                    viewModel.saveInputs(age.toString(), height.toString(), weight.toString(), obesity.toString(), meat.toString())


                    setDetailValue()
                    // Show a toast message with the input values
                    val message =
                        "Age: $age, Height: $height, Weight: $weight, Obesity: $obesity, Meat: $meat"
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

                    alertDialog?.dismiss()
                } else {
                    // Invalid input values
                    Toast.makeText(
                        requireContext(),
                        "Please enter valid age, height, and weight",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                // Empty input fields
                Toast.makeText(
                    requireContext(),
                    "Please enter valid age, height, and weight",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


        popupBinding.btnCancel.setOnClickListener {
            viewModel.isPopupOpen = false
            alertDialog?.dismiss() // Close the popup
        }

        alertDialog?.show()
        viewModel.isPopupOpen = true
    }


    private fun predict(weight: Float, height: Float, age: Int, history: Int, favc: Int) {

        val call = PredictApiService.apiService.predict(
            weight = weight,
            riwayat = history,
            height = height,
            age = age,
            favc = favc
        )

        call.enqueue(object : Callback<PredictionResponse> {
            override fun onResponse(call: Call<PredictionResponse>, response: Response<PredictionResponse>) {
                Log.d("Predict", "onResponse called")
                if (response.isSuccessful) {
                    val prediction = response.body()?.prediction
                    if (prediction != null) {
                        // Handle the prediction value
//                        showPredictionPopup(prediction)
                        saveWeightType(prediction)
                        Toast.makeText(requireContext(), "Prediction: $prediction", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Invalid prediction response", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.d("Predict", "API call failed with code: ${response.code()}")
                    Toast.makeText(requireContext(), "Prediction API call failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PredictionResponse>, t: Throwable) {
                Log.d("Predict", "onFailure called")
                t.printStackTrace()
                // API call failed due to network or other issues
                Toast.makeText(requireContext(), "Prediction API call failed", Toast.LENGTH_SHORT).show()
            }
        })

        // Debug logs
        Log.d("Predict", "Predict function called")
        Log.d("Predict", "Weight: $weight, History: $history, Height: $height, Age: $age, FAVC: $favc")
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        if (viewModel.isPopupOpen) {
            showPopupForm()
        }
    }

    fun saveDetailUser(height: Float, weight: Float, age: Int, obesityHistory: Int, favc: Int) {
        if (auth.currentUser == null) {
            Toast.makeText(context, "Please login to use this feature", Toast.LENGTH_SHORT).show()
            return
        }
        val firestore = FirebaseFirestore.getInstance()
        val collectionRef = firestore.collection("user_detail")
        val documentId = auth.currentUser?.email.toString()

        collectionRef.document(documentId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    println("Document Exist")
                    val gender = document.getString("gender") ?: ""

                    val data = hashMapOf(
                        "age" to age,
                        "favc" to favc,
                        "height" to height,
                        "obesity_history" to obesityHistory,
                        "weight" to weight,
                        "gender" to gender
                    )

                    collectionRef.document(documentId)
                        .set(data)
                        .addOnSuccessListener {
                            // Data successfully saved with custom document ID
                            Toast.makeText(context, "Successfully add data", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { exception ->
                            // An error occurred while saving the data
                            Toast.makeText(context, "Failed to add data", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    println("Document Does Not Exist")
                }
            }
            .addOnFailureListener { exception ->
                // Handle the failure to fetch document data from Firestore
                Toast.makeText(requireContext(), "Failed to fetch document data", Toast.LENGTH_SHORT).show()
                println("Error fetching document: ${exception.message}")
            }
    }

    fun setDetailValue() {
        if (isAdded && !isDetached) {
            val firestore = FirebaseFirestore.getInstance()
            val collectionRef = firestore.collection("user_detail")
            val documentId = auth.currentUser?.email.toString()

            collectionRef.document(documentId)
                .get()
                .addOnSuccessListener { document ->
                    if (isAdded && !isDetached) {
                        if (document != null && document.exists()) {
                            println("Document Exist")
                            val age = document.getLong("age")?.toString() ?: ""
                            val height = document.getLong("height")?.toString() ?: ""
                            val weight = document.getLong("weight")?.toString() ?: ""
                            val obesity = document.getLong("obesity_history")?.toInt() == 1
                            val meat = document.getLong("favc")?.toInt() == 1

                            binding.tvAgeAnswer.setText(age)
                            binding.tvHeight.setText(height + " CM")
                            binding.tvWeight.setText(weight + " KG")

                            if (obesity) {
                                binding.tvObesityAnswer.text = "Yes"
                            } else {
                                binding.tvObesityAnswer.text = "No"
                            }

                            if (meat) {
                                binding.tvMeatAnswer.text = "Yes"
                            } else {
                                binding.tvMeatAnswer.text = "No"
                            }

                        } else {
                            println("Document Does Not Exist")
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    if (isAdded && !isDetached) {
                        // Handle the failure to fetch document data from Firestore
                        Toast.makeText(requireContext(), "Failed to fetch document data", Toast.LENGTH_SHORT).show()
                        println("Error fetching document: ${exception.message}")
                    }
                }
        }
    }

    fun setProfileValue() {
        if (isAdded && !isDetached) {
            val firestore = FirebaseFirestore.getInstance()
            val collectionRef = firestore.collection("user_profile")
            val documentId = auth.currentUser?.email.toString()

            collectionRef.document(documentId)
                .get()
                .addOnSuccessListener { document ->
                    if (isAdded && !isDetached) {
                        if (document != null && document.exists()) {
                            println("Document Exist")
                            val name = document.getString("name") ?: ""
                            val picture = document.getString("picture") ?: ""

                            // Perform the desired actions with the retrieved data
                            // For example, you can set the name and picture to appropriate views
                            binding.username.text = name
                            binding.email.text = auth.currentUser?.email.toString()
                            if (picture != null) {
                                Glide.with(requireContext())
                                    .load(picture)
                                    .transition(DrawableTransitionOptions.withCrossFade())
                                    .into(binding.personImageview)
                            } else {
                                Glide.with(requireContext())
                                    .load(R.drawable.ic_person)
                                    .transition(DrawableTransitionOptions.withCrossFade())
                                    .into(binding.personImageview)
                            }
                        } else {
                            println("Document Does Not Exist")
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    if (isAdded && !isDetached) {
                        // Handle the failure to fetch document data from Firestore
                        Toast.makeText(requireContext(), "Failed to fetch document data", Toast.LENGTH_SHORT).show()
                        println("Error fetching document: ${exception.message}")
                    }
                }
        }
    }

    private fun saveWeightType(weightType: String) {
        val editor = sharedPreferences.edit()
        editor.putString("Weight Type", weightType)
        editor.apply()
        val userWeightType = sharedPreferences.getString("Weight Type", null)
        println("Weight Type: $userWeightType")

        binding.tvPredictionResult.text = weightType
    }

    private fun clearWeightType() {
        val editor = sharedPreferences.edit()
        editor.remove("Weight Type")
        editor.apply()

        binding.tvPredictionResult.text = ""
    }


    companion object {
        @JvmStatic
        fun newInstance() = ProfileFragment()
    }
}