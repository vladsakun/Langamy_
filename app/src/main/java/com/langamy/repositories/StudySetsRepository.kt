package com.langamy.repositories

import androidx.lifecycle.LiveData
import com.langamy.base.classes.StudySet

interface StudySetsRepository {
    suspend fun getStudySetsList() : LiveData<out List<StudySet>>
    suspend fun getStudySet(id: Int) : LiveData<out StudySet>
    suspend fun deleteAllLocalStudySets()
    suspend fun deleteStudySet(id:Int)
    suspend fun deleteLocalStudySet(id:Int)
    suspend fun updateStudySet(studySet: StudySet)
    suspend fun insertStudySet(studySet: StudySet)
}