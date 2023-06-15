package com.example.bodybalancy

import android.content.Context
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.UnderlineSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bodybalancy.Interfaces.FoodRecommendationApiService
import com.example.bodybalancy.adapters.RecommendedFoodAdapter
import com.example.bodybalancy.dataClasses.FoodRecommendationResponse
import com.example.bodybalancy.dataClasses.RecommendedFood
import com.example.bodybalancy.databinding.FragmentFoodRecommendationBinding
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FoodRecommendationFragment : Fragment() {

    private var _binding: FragmentFoodRecommendationBinding? = null
    private val binding get() = _binding!!

    private lateinit var apiService: FoodRecommendationApiService

    // Create Retrofit instance
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://food-recomendation-fm5o2c5urq-et.a.run.app/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment using View Binding
        _binding = FragmentFoodRecommendationBinding.inflate(inflater, container, false)
        val view = binding.root

        // Create API service
        apiService = retrofit.create(FoodRecommendationApiService::class.java) as FoodRecommendationApiService

        // Set the back button icon
        binding.toolbar.navigationIcon =
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_back)
        binding.toolbar.navigationContentDescription = "Sport Recommendation"

        // Set the back button click listener
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        // Set a click listener for the submit button
        binding.submitButton.setOnClickListener {
            val activityLevel = binding.spinnerActivityLevel.selectedItem.toString().lowercase()
            // Show a progress indicator
            showProgressIndicator()

            // Create Firestore connection
            val collectionName = "user_detail"
            val documentId = auth.currentUser?.email.toString()

            val db = FirebaseFirestore.getInstance()

            db.collection(collectionName)
                .document(documentId)
                .get()
                .addOnSuccessListener { document ->
                    // Hide the progress indicator
                    hideProgressIndicator()

                    if (document.exists()) {
                        // Document exists, access its data
                        val age = document.getLong("age")?.toInt()
                        val favc = document.getLong("favc")?.toInt()
                        val gender = document.getString("gender")
                        val height = document.getLong("height")?.toInt()
                        val obesityHistory = document.getLong("obesity_history")?.toInt()
                        val weight = document.getLong("weight")?.toInt()

                        println(height.toString()+weight.toString()+age.toString()+gender+activityLevel)
                        if (age != null && favc != null && height != null && obesityHistory != null && weight != null && gender != null) {
                            getFoodRecommendation(height, weight, age, gender, activityLevel)
                        } else {
                            // Handle the case where any of the required fields is null
                            showErrorDialog(requireContext(), "Incomplete data. Please fill your data at Profile Section")
                        }
                    } else {
                        // Document doesn't exist
                        // Handle the case accordingly
                    }
                }
                .addOnFailureListener { exception ->
                    // Hide the progress indicator
                    hideProgressIndicator()

                    // Handle any errors that occurred
                }
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

    private fun showProgressIndicator() {
        // Show a ProgressBar or a loading dialog
        // For example:
         binding.progressBar.visibility = View.VISIBLE
        // or
        // progressDialog.show()
    }

    private fun hideProgressIndicator() {
        // Hide the ProgressBar or the loading dialog
        // For example:
         binding.progressBar.visibility = View.GONE
        // or
        // progressDialog.dismiss()
    }

    private fun getFoodRecommendation(height: Int, weight: Int, age: Int, gender: String, activityLevel: String) {
        val apiInterface = retrofit.create(FoodRecommendationApiService::class.java)
        val call = apiInterface.getFoodRecommendation(height, weight, age, gender, activityLevel)

        call.enqueue(object : Callback<FoodRecommendationResponse>{
            override fun onResponse(
                call: Call<FoodRecommendationResponse>,
                response: Response<FoodRecommendationResponse>
            ){
                if (response.isSuccessful) {
                    val recommendation = response.body()?.recommendation
                    if (recommendation != null) {
                        val bmr = recommendation.bmr
                        val dailyCalorieIntake = recommendation.dailyCalorieIntake
                        val recommendedFoods = recommendation.recommendedFoods

                        val convertedFoods = recommendedFoods.map { food ->
                            val foodName = food[0] as String
                            val foodCalories = food[1] as Double
                            RecommendedFood(foodName, foodCalories.toString())
                        }

                        val recommendationText = buildRecommendationText(
                            bmr,
                            dailyCalorieIntake,
                            convertedFoods
                        )

                        // Show the recommendation in a popup dialog
                        showRecommendationPopup(recommendationText.toString(), convertedFoods)
                    } else {
                        showErrorDialog(requireContext(), "No recommendation data available")
                    }
                } else {
                    showErrorDialog(requireContext(), "Request failed with error code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<FoodRecommendationResponse>, t: Throwable) {
                showErrorDialog(requireContext(), "Request failed with error message: ${t.message}")
            }
        })
    }

    private fun buildRecommendationText(
        bmr: Double,
        dailyCalorieIntake: Double,
        recommendedFoods: List<RecommendedFood>
    ): String {
        val sb = StringBuilder()
        sb.append("BMR: $bmr\n")
        sb.append("Daily Calorie Intake: $dailyCalorieIntake\n")

        return sb.toString()
    }


    private fun showRecommendationPopup(recommendationText: String, recommendedFoods: List<RecommendedFood>) {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        val view = layoutInflater.inflate(R.layout.popup_recommended_foods, null)
        dialogBuilder.setView(view)
        val textViewRecommendation = view.findViewById<TextView>(R.id.textViewRecommendation)
        val recyclerViewRecommendedFoods = view.findViewById<RecyclerView>(R.id.recyclerViewRecommendedFoods)

        // Combine the header and recommendation text
        val headerText = "$recommendationText"

        textViewRecommendation.text = headerText

        // Set up RecyclerView
        recyclerViewRecommendedFoods.layoutManager = LinearLayoutManager(requireContext())

        // Create a new list for the adapter with the header as the first item
        val adapterData = mutableListOf<RecommendedFood>()
        adapterData.add(RecommendedFood("Name", "Calories"))
        adapterData.addAll(recommendedFoods)

        // Update the RecommendedFoodAdapter initialization
        val recommendedFoodAdapter = RecommendedFoodAdapter(adapterData)
        recyclerViewRecommendedFoods.adapter = recommendedFoodAdapter

        dialogBuilder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }

        // Create and show the dialog
        val dialog = dialogBuilder.create()
        dialog.show()
    }



    private fun showRecommendationDialog(
        context: Context,
        recommendationText: String,
        recommendedFoods: List<RecommendedFood>
    ) {
        val dialogBuilder = AlertDialog.Builder(context)
        dialogBuilder.setMessage(recommendationText)
            .setCancelable(false)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
        val alert = dialogBuilder.create()
        alert.setTitle("Food Recommendation")
        alert.show()
    }

    private fun showErrorDialog(context: Context, errorMessage: String) {
        val dialogBuilder = AlertDialog.Builder(context)
        dialogBuilder.setMessage(errorMessage)
            .setCancelable(false)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
        val alert = dialogBuilder.create()
        alert.setTitle("Error")
        alert.show()
    }
}
