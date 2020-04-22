package com.langamy.viewmodel

import androidx.lifecycle.ViewModel
import com.langamy.lazyDeferred
import com.langamy.repositories.StudySetsRepository

class SpecificStudySetViewModel(
        private val studySetsRepository: StudySetsRepository,
        private var studySetId:Int
) : ViewModel() {

    val studySet by lazyDeferred {
        studySetsRepository.getStudySet(studySetId)
    }

    val clonedStudySet by lazyDeferred {
        studySetId = studySetsRepository.clonedId
        studySetsRepository.getStudySet(studySetId)
    }

}