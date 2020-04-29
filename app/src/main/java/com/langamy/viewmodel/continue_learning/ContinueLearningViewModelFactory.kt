package com.langamy.viewmodel.continue_learning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.langamy.repositories.StudySetsRepository

class ContinueLearningViewModelFactory(
        private val studySetsRepository: StudySetsRepository
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ContinueLearningViewModel(studySetsRepository) as T
    }
}