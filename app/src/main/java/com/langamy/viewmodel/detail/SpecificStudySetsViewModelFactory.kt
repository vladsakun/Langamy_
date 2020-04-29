package com.langamy.viewmodel.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.langamy.repositories.StudySetsRepository

class SpecificStudySetsViewModelFactory(
        private val studySetsRepository: StudySetsRepository,
        private val studySetId:Int
) :ViewModelProvider.NewInstanceFactory(){

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SpecificStudySetViewModel(studySetsRepository, studySetId) as T
    }
}