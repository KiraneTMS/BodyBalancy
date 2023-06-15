package com.example.bodybalancy

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import okhttp3.*
import okhttp3.MediaType
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.SocketTimeoutException

class SearchFragment : Fragment() {

    private lateinit var imageView: ImageView
    private lateinit var selectButton: Button
    private lateinit var predictButton: Button
    private lateinit var resultTextView: TextView
    private var selectedImageUri: Uri? = null

    private val PICK_IMAGE_REQUEST = 1
    private val PERMISSION_REQUEST_CODE = 2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        imageView = view.findViewById(R.id.imageView)
        selectButton = view.findViewById(R.id.selectButton)
        predictButton = view.findViewById(R.id.predictButton)
        resultTextView = view.findViewById(R.id.result)

        selectButton.setOnClickListener {
            openImageChooser()
        }

        predictButton.setOnClickListener {
            selectedImageUri?.let { uri ->
                val inputStream = requireActivity().contentResolver.openInputStream(uri)
                val file = createTempFile("image", null, requireActivity().cacheDir)
                file.outputStream().use { outputStream ->
                    inputStream?.copyTo(outputStream)
                }
                sendPredictionRequest(file)
            }
        }

        return view
    }

    private fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            PERMISSION_REQUEST_CODE
        )
    }

    private fun openImageChooser() {
        if (checkPermission()) {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        } else {
            requestPermission()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImageChooser()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == AppCompatActivity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            imageView.setImageURI(selectedImageUri)
        }
    }

    private fun sendPredictionRequest(imageFile: File) {
        val client = OkHttpClient()
        val url = "https://food-model-fm5o2c5urq-et.a.run.app/predict"

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                "image.jpg",
                RequestBody.create(MediaType.parse("image/jpeg"), imageFile)
            )
            .build()

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                requireActivity().runOnUiThread {
                    resultTextView.text = "Error: Network request failed"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body()?.string()
                Log.d("ResponseData", responseData ?: "Response data is null")
                requireActivity().runOnUiThread {
                    if (responseData != null) {
                        try {
                            val jsonResponse = JSONObject(responseData)
                            val predictedLabel = jsonResponse.getString("predicted_label")
                            resultTextView.text = "Result: $predictedLabel"
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            resultTextView.text = responseData // Display the plain string response
                        }
                    } else {
                        resultTextView.text = "Error: Empty response"
                    }
                }
            }
        })
    }
}
