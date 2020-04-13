package com.langamy.viewmodel

import androidx.lifecycle.ViewModel
import com.langamy.lazyDeferred
import com.langamy.repositories.StudySetsRepository

class SpecificStudySetViewModel(
        private val studySetsRepository: StudySetsRepository
):ViewModel() {

    var id:Int = 0

    val studySet by lazyDeferred {
        studySetsRepository.getStudySet(id)
    }

}