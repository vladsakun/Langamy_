package com.langamy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.langamy.repositories.StudySetsRepository

class StudySetsViewModelFactory(
        private val studySetsRepository: StudySetsRepository
) :ViewModelProvider.NewInstanceFactory(){

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return StudySetsViewModel(studySetsRepository) as T
    }
}