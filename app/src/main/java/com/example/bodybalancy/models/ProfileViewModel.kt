package com.example.bodybalancy.models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfileViewModel : ViewModel() {
    var isPopupOpen: Boolean = false
    val age: MutableLiveData<String> = MutableLiveData()
    val height: MutableLiveData<String> = MutableLiveData()
    val weight: MutableLiveData<String> = MutableLiveData()
    val obesity: MutableLiveData<String> = MutableLiveData()
    val meat: MutableLiveData<String> = MutableLiveData()

    //profile
    val email: MutableLiveData<String> = MutableLiveData()
    val name: MutableLiveData<String> = MutableLiveData()

    fun saveInputs(age: String, height: String, weight: String, obesity: String, meat: String) {
        this.age.value = age
        this.height.value = height
        this.weight.value = weight
        this.obesity.value = obesity
        this.meat.value = meat
    }

    fun saveNameAndEmail(name: String, email: String) {
        // Perform any necessary additional processing or validation

        // Save the name and email to the ViewModel
        this.name.value = name
        this.email.value = email
    }


    fun getSavedAge(): String? {
        return age.value
    }

    fun getSavedHeight(): String? {
        return height.value
    }

    fun getSavedWeight(): String? {
        return weight.value
    }

    fun getSavedObesity(): String? {
        return obesity.value
    }

    fun getSavedMeat(): String? {
        return meat.value
    }
}

