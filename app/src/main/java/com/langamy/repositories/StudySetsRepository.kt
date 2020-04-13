package com.langamy.repositories

import androidx.lifecycle.LiveData
import com.langamy.base.classes.StudySet

interface StudySetsRepository {
    suspend fun getStudySetsList() : LiveData<out List<StudySet>>
    suspend fun deleteAllLocalStudySets()
    suspend fun deleteStudySet(id:Int)
    suspend fun deleteLocalStudySet(id:Int)
}