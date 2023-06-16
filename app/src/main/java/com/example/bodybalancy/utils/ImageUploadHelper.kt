//package com.example.bodybalancy.utils
//
//import android.app.Activity
//import android.content.ContentResolver
//import android.content.Context
//import android.graphics.Bitmap
//import android.net.Uri
//import android.provider.MediaStore
//import android.util.Log
//import androidx.core.content.ContentProviderCompat.requireContext
//import androidx.lifecycle.MutableLiveData
//import com.google.firebase.firestore.FirebaseFirestore
//import id.zelory.compressor.Compressor
//import id.zelory.compressor.constraint.destination
//import id.zelory.compressor.constraint.quality
//import id.zelory.compressor.constraint.resolution
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import java.io.File
//import java.io.IOException
//
//class ImageUploadHelper(private val activity: Activity) {
//    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
//    val uploadStatus: MutableLiveData<Boolean> = MutableLiveData()
//
//    fun handleImageUpload(context: Context, imageUri: Uri) {
//        val originalImagePath = getRealPathFromUri(imageUri)
//        if (originalImagePath != null) {
//            compressImage(context, originalImagePath)
//        } else {
//            Log.e("ImageUploadHelper", "Failed to get image path from URI: $imageUri")
//            uploadStatus.value = false
//        }
//    }
//
//    private fun getRealPathFromUri(uri: Uri): String? {
//        val projection = arrayOf(MediaStore.Images.Media.DATA)
//        val resolver: ContentResolver = activity.contentResolver
//        val cursor = resolver.query(uri, projection, null, null, null)
//        cursor?.use {
//            if (it.moveToFirst()) {
//                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
//                return it.getString(columnIndex)
//            }
//        }
//        return null
//    }
//
//    private fun compressImage(context: Context, imagePath: String) {
//        GlobalScope.launch {
//            val file = File(imagePath)
//            val fileSizeInBytes = file.length()
//
//            if (fileSizeInBytes > (1024 * 1024)) { // Check if file size is larger than 1MB
//                val compressedImageFile = withContext(Dispatchers.IO) {
//                    try {
//                        Compressor.compress(context, file) {
//                            quality(80)
//                            destination(createTempFile("compressed_", ".jpg"))
//                            resolution(1280, 720)
//                        }
//                    } catch (e: IOException) {
//                        Log.e("ImageUploadHelper", "Failed to compress image: ${e.message}")
//                        null
//                    }
//                }
//
//                compressedImageFile?.let { uploadImageToFirestore(it) }
//                    ?: uploadStatus.postValue(false)
//            } else {
//                // No need to compress, directly upload the original image
//                uploadImageToFirestore(file)
//            }
//        }
//    }
//
//    private fun uploadImageToFirestore(imageFile: File) {
//        // Replace "images" with your Firestore collection name
//        val imageRef = firestore.collection("images").document()
//        val imageId = imageRef.id
//
//        imageRef.putFile(Uri.fromFile(imageFile))
//            .addOnSuccessListener {
//                // Image uploaded successfully
//                val downloadUrl = it.metadata?.reference?.downloadUrl?.toString()
//                if (downloadUrl != null) {
//                    // Save the download URL to Firestore or perform further operations
//                    // ...
//
//                    uploadStatus.postValue(true)
//                } else {
//                    uploadStatus.postValue(false)
//                }
//            }
//            .addOnFailureListener {
//                // Error uploading image
//                Log.e("ImageUploadHelper", "Failed to upload image: ${it.message}")
//                uploadStatus.postValue(false)
//            }
//    }
//}