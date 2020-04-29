package com.langamy.viewmodel.learn

import androidx.lifecycle.ViewModel
import com.langamy.base.classes.StudySet
import com.langamy.repositories.StudySetsRepository

class LearnActivityViewModel(
        private val studySetsRepository: StudySetsRepository
): ViewModel() {

    suspend fun updateLocalStudySet(studySet:StudySet){
        studySetsRepository.updateStudySet(studySet)
    }
}