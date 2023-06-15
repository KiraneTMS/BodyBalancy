import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.bodybalancy.*
import com.example.bodybalancy.databinding.FragmentHomeBinding
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        val gridView: GridView = binding.gridView
        val adapter = ShapeAdapter()
        gridView.numColumns = 2
        gridView.adapter = adapter

        adapter.addShapeItem(ShapeItem(R.drawable.circle, "Sport Recommendation", R.drawable.recomended_sport))
        adapter.addShapeItem(ShapeItem(R.drawable.circle, "Food Recommendation", R.drawable.recomended_food))

        setProfileValue()
        return view
    }

    inner class ShapeAdapter : BaseAdapter() {
        private val shapeItems = ArrayList<ShapeItem>()

        fun addShapeItem(item: ShapeItem) {
            shapeItems.add(item)
            notifyDataSetChanged()
        }

        override fun getCount(): Int {
            return shapeItems.size
        }

        override fun getItem(position: Int): Any {
            return shapeItems[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: LayoutInflater.from(parent?.context)
                .inflate(R.layout.circle_button, parent, false)

            val shapeItem: ShapeItem = getItem(position) as ShapeItem

            val shapeImageView: ImageView = view.findViewById(R.id.shapeImageView)
            shapeImageView.setImageResource(shapeItem.shapeRes)

            val image: ImageView = view.findViewById(R.id.ivIcon)
            image.setImageResource(shapeItem.image)

            val shapeTextView: TextView = view.findViewById(R.id.shapeTextView)
            shapeTextView.text = shapeItem.text

            view.setOnClickListener {
                val clickedItem = shapeItems[position]
                when (clickedItem.text) {
                    "Sport Recommendation" -> {
                        val sharedPreferences = requireContext().getSharedPreferences("WeightType", Context.MODE_PRIVATE)
                        val userWeightType = sharedPreferences.getString("Weight Type", null)

                        if (userWeightType.isNullOrEmpty()) {
                            val alertDialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogStyle)
                                .setTitle("Obesity Status")
                                .setMessage("Please predict your Obesity Status in the profile section")
                                .setPositiveButton("OK") { _, _ ->
                                    // Handle the positive button click if needed
                                }
                                .create()

                            alertDialog.show()
                        } else {
                            val sportRecommendationFragment = SportRecommendationFragment()
                            requireActivity().supportFragmentManager.beginTransaction()
                                .replace(R.id.fragmentContainer, sportRecommendationFragment)
                                .addToBackStack(null)
                                .commit()
                        }
                    }
                    "Food Recommendation" -> {
                        val foodRecommendationFragment = FoodRecommendationFragment()
                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, foodRecommendationFragment)
                            .addToBackStack(null)
                            .commit()
                    }
                    // Add more cases for additional buttons

                    else -> {
                        // Handle the default case
                    }
                }
            }

            return view
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

                            // Perform the desired actions with the retrieved data
                            // For example, you can set the name and picture to appropriate views
                            binding.greeting.text = "Hi ${name}!"
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

    data class ShapeItem(val shapeRes: Int, val text: String, val image: Int)
}
