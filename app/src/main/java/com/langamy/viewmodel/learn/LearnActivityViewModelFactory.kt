package com.langamy.viewmodel.learn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.langamy.repositories.StudySetsRepository

class LearnActivityViewModelFactory(
        private val studySetsRepository: StudySetsRepository
) :ViewModelProvider.NewInstanceFactory(){

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return LearnActivityViewModel(studySetsRepository) as T
    }
}