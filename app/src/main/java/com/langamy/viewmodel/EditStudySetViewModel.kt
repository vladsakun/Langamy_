package com.langamy.viewmodel

import androidx.lifecycle.ViewModel
import com.langamy.base.classes.StudySet
import com.langamy.repositories.StudySetsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class EditStudySetViewModel(
        private val studySetsRepository: StudySetsRepository
): ViewModel() {

    fun updateStudySet(studySet:StudySet){
        GlobalScope.launch(Dispatchers.IO){
            studySetsRepository.updateStudySet(studySet)
        }
    }
}