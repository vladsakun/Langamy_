package com.langamy.datasource

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.langamy.base.classes.StudySet
import com.langamy.exceptions.NoConnectivityException
import com.langamy.retrofit.LangamyApiService

class LangamyNetworkDataSourceImpl(
        private val langamyApiService: LangamyApiService
) : LangamyNetworkDataSource {

    private val _downloadedStudySets = MutableLiveData<List<StudySet>>()
    override val downloadedStudySets: LiveData<List<StudySet>>
        get() = _downloadedStudySets

    private val _deleteStatus = MutableLiveData<HashMap<String, Any>>()
    override val deleteStatus: LiveData<HashMap<String, Any>>
        get() = _deleteStatus

    private val _clonedStudySet = MutableLiveData<StudySet>()
    override val clonedStudySet: LiveData<StudySet>
        get() = _clonedStudySet


    override suspend fun fetchStudySets(userEmail: String) {
        try {
            val fetchedStudySetsList = langamyApiService
                    .getStudySets(userEmail)
                    .await()
            _downloadedStudySets.postValue(fetchedStudySetsList)
        } catch (e: NoConnectivityException) {
            Log.e("Connectivity", "No internet connection")
        }
    }

    override suspend fun deleteStudySet(id: Int) {
        try {
            langamyApiService.deleteStudySet(id).await()
        } catch (e: NoConnectivityException) {
            Log.e("Connectivity", "No internet connection")
        }
    }

    override suspend fun patchStudySet(studySet: StudySet) {
        try {
            langamyApiService.patchStudySet(studySet.id, studySet).await()
        } catch (e: NoConnectivityException) {
            Log.e("Connectivity", "No internet connection")
        }
    }

    override suspend fun cloneStudySet(studySet: StudySet, userEmail: String) {
        try {
            val clonedId = langamyApiService.cloneStudySet(studySet.id, userEmail).await()
            studySet.id = clonedId.toInt()
            _clonedStudySet.postValue(studySet)
        } catch (e: NoConnectivityException) {
            Log.e("Connectivity", "No internet connection")
        }
    }

}