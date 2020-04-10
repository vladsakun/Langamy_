package com.langamy.viewmodel

import androidx.lifecycle.ViewModel
import com.langamy.lazyDeferred
import com.langamy.repositories.StudySetsRepository

class StudySetsViewModel(
        private val studySetsRepository: StudySetsRepository
) : ViewModel() {
    val studySets by lazyDeferred {
         studySetsRepository.getStudySetsList()
    }
}