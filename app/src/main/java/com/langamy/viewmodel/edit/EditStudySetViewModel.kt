package com.langamy.viewmodel.edit

import androidx.lifecycle.ViewModel
import com.langamy.base.classes.StudySet
import com.langamy.repositories.StudySetsRepository
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class EditStudySetViewModel(
        private val studySetsRepository: StudySetsRepository
): ViewModel() {

    fun updateStudySet(studySet:StudySet){
        GlobalScope.launch(IO){
            studySetsRepository.updateStudySet(studySet)
        }
    }

    fun insertStudySet(studySet: StudySet){
        GlobalScope.launch(IO){
            studySetsRepository.insertStudySet(studySet)
        }
    }
}