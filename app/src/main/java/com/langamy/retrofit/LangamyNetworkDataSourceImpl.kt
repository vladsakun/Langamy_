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

    override suspend fun fetchStudySets(userEmail: String) {
        try {
            val fetchedStudySetsList = langamyApiService
                    .getStudySets(userEmail)
                    .await()
            _downloadedStudySets.postValue(fetchedStudySetsList)
        }
        catch (e: NoConnectivityException){
            Log.e("Connectivity", "No internet connection")
        }
    }
}