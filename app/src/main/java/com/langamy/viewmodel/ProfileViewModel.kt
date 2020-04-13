package com.langamy.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.langamy.provider.UserProvider
import com.langamy.repositories.StudySetsRepository

class ProfileViewModel(
        private val studySetsRepository: StudySetsRepository,
        private val userProvider: UserProvider
) : ViewModel() {

    suspend fun deleteAllStudySets(){
        studySetsRepository.deleteAllLocalStudySets()
    }

    fun getUserPhotoUrl(): Uri?{
        return userProvider.getUserPhotoUrl()
    }

    fun getUserEmail():String{
        return userProvider.getUserEmail()
    }
}