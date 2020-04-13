package com.langamy.retrofit

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.langamy.base.classes.StudySet
import com.langamy.exceptions.NoConnectivityException

class LangamyNetworkDataSourceImpl(
        private val langamyApiService: LangamyApiService
) : LangamyNetworkDataSource {

    private val _downloadedStudySets = MutableLiveData<List<StudySet>>()
    override val downloadedStudySets: LiveData<List<StudySet>>
        get() = _downloadedStudySets

    private val _deleteStatus = MutableLiveData<HashMap<String, Any>>()
    override val deleteStatus: LiveData<HashMap<String, Any>>
        get() = _deleteStatus

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

}