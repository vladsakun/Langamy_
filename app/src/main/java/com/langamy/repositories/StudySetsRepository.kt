package com.langamy.repositories

import androidx.lifecycle.LiveData
import com.langamy.base.classes.StudySet

interface StudySetsRepository {
    suspend fun getStudySetsList() : LiveData<out List<StudySet>>
}