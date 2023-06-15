package com.example.bodybalancy

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bodybalancy.adapters.AllSportsAdapter
import com.example.bodybalancy.adapters.SportCategoriesAdapter
import com.example.bodybalancy.databinding.FragmentSportRecommendationBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore

class SportRecommendationFragment : Fragment() {

    private var _binding: FragmentSportRecommendationBinding? = null
    private val binding get() = _binding!!

    private lateinit var sportCategoriesAdapter: SportCategoriesAdapter
    private lateinit var allSportsAdapter: AllSportsAdapter
    private lateinit var progressBar: ProgressBar

    private var categorySelected = "Material Arts"
    private lateinit var sharedPreferences: SharedPreferences

    private var isViewCreated = false


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment using View Binding
        _binding = FragmentSportRecommendationBinding.inflate(inflater, container, false)
        val view = binding.root

        isViewCreated = true

        sharedPreferences = requireContext().getSharedPreferences("WeightType", Context.MODE_PRIVATE)
        val weightSelected = sharedPreferences.getString("Weight Type", null)

        progressBar = binding.progressBar


        if (weightSelected != null) {
            retrieveSportsByCategories(weightSelected, categorySelected)
        }

        // Set the back button icon
        binding.toolbar.navigationIcon =
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_back)
        binding.toolbar.navigationContentDescription = "Sport Recommendation"

        // Set the back button click listener
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        // Set up RecyclerView for sport categories
        binding.categoriesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        sportCategoriesAdapter = SportCategoriesAdapter(emptyList()) { category ->
            categorySelected = category
//            binding.txtCategorySelected.text = categorySelected
            if (weightSelected != null) {
                retrieveSportsByCategories(weightSelected, category)
            }
        }
        binding.categoriesRecyclerView.adapter = sportCategoriesAdapter
        binding.categoriesRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        // Set up RecyclerView for recommended sports
        binding.recomendedRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        allSportsAdapter = AllSportsAdapter(emptyList())
        binding.recomendedRecyclerView.adapter = allSportsAdapter

        // Add sports to Firestore
        addSportsToFirestore()

        // Retrieve unique sport categories
        retrieveUniqueSportCategories()

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
        isViewCreated = false
    }

    private fun addSportsToFirestore() {
        val db = FirebaseFirestore.getInstance()
        val sports = listOf(
            mapOf(
                "name" to "Jogging/Running",
                "weightCategory" to "Normal Weight",
                "sportCategory" to "Cardiovascular"
            ),
            mapOf(
                "name" to "Cycling",
                "weightCategory" to "Normal Weight",
                "sportCategory" to "Cardiovascular"
            )
        )

        val sportsCollectionRef = db.collection("sports")
        for (sport in sports) {
            val sportName = sport["name"] as String
            sportsCollectionRef.whereEqualTo("name", sportName)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.isEmpty) {
                        sportsCollectionRef.add(sport)
                            .addOnSuccessListener {
                                println("Sport $sportName added to Firestore")
                            }
                            .addOnFailureListener { e ->
                                println("Error adding sport $sportName: ${e.message}")
                            }
                    } else {
                        println("Sport $sportName already exists in Firestore")
                    }
                }
                .addOnFailureListener { e ->
                    println("Error checking for sport $sportName: ${e.message}")
                }
        }
    }

    private fun retrieveSportsByCategories(weightCategory: String, sportCategory: String) {
        val db = FirebaseFirestore.getInstance()
        val sportsCollectionRef = db.collection("sports")

        val filteredSports = mutableListOf<Map<String, String>>()

        sportsCollectionRef.whereEqualTo("weightCategory", weightCategory)
            .whereEqualTo("sportCategory", sportCategory)
            .get()
            .addOnSuccessListener { querySnapshot ->
                // Check if the fragment's view is still available
                if (view != null) {
                    for (document in querySnapshot.documents) {
                        val sport = document.data as Map<String, String>
                        filteredSports.add(sport)
                    }

                    if (filteredSports.isNotEmpty()) {
                        allSportsAdapter.updateData(filteredSports)
                    } else {
                        val message = "No recommended sports available for this category. Please check other categories."
                        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
                        allSportsAdapter.clearData()
                    }
                }
            }
            .addOnFailureListener { e ->
                println("Error retrieving sports: ${e.message}")
            }
    }

    private fun retrieveUniqueSportCategories() {
        // Show progress bar while loading
        progressBar.visibility = View.VISIBLE

        val db = FirebaseFirestore.getInstance()
        val sportsCollectionRef = db.collection("sports")

        val uniqueSportCategories = HashSet<String>()

        sportsCollectionRef.get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    val currentSportCategory = document.getString("sportCategory")
                    if (currentSportCategory != null && !uniqueSportCategories.contains(currentSportCategory)) {
                        uniqueSportCategories.add(currentSportCategory)
                    }
                }

                sportCategoriesAdapter.updateData(uniqueSportCategories.toList())

                // Hide progress bar after loading
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                println("Error retrieving unique sport categories: ${e.message}")

                // Hide progress bar if an error occurs
                progressBar.visibility = View.GONE
            }
    }

}
