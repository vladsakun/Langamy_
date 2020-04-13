package com.langamy.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.langamy.provider.UserProvider
import com.langamy.repositories.StudySetsRepository

class StudySetsViewModelFactory(
        private val studySetsRepository: StudySetsRepository,
        private val userProvider: UserProvider,
        private val application: Application
) :ViewModelProvider.NewInstanceFactory(){

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return StudySetsViewModel(studySetsRepository, userProvider, application) as T
    }
}