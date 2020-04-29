package com.langamy.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.langamy.provider.UserProvider
import com.langamy.repositories.StudySetsRepository
import com.langamy.viewmodelSpecificStudySets.ProfileViewModel

class ProfileViewModelFactory(
        private val studySetsRepository: StudySetsRepository,
        private val userProvider: UserProvider
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ProfileViewModel(studySetsRepository, userProvider) as T
    }
}