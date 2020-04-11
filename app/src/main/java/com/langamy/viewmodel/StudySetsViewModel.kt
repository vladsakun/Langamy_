package com.langamy.viewmodel

import androidx.lifecycle.ViewModel
import com.langamy.lazyDeferred
import com.langamy.provider.UserProvider
import com.langamy.repositories.StudySetsRepository

class StudySetsViewModel(
        private val studySetsRepository: StudySetsRepository,
        userProvider: UserProvider
) : ViewModel() {

    val studySets by lazyDeferred {
        studySetsRepository.getStudySetsList()
    }

}