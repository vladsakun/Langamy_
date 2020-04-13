package com.langamy.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.langamy.base.classes.StudySet
import com.langamy.lazyDeferred
import com.langamy.livedata.ConnectionLiveData
import com.langamy.provider.UserProvider
import com.langamy.repositories.StudySetsRepository

class StudySetsViewModel(
        private val studySetsRepository: StudySetsRepository,
        userProvider: UserProvider, application: Application
) : AndroidViewModel(application) {

    val studySets by lazyDeferred {
        studySetsRepository.getStudySetsList()
    }

    suspend fun deleteStudySetById(id:Int){
        studySetsRepository.deleteStudySet(id)
        studySetsRepository.deleteLocalStudySet(id)
    }

    val connectivity = ConnectionLiveData(application)

}