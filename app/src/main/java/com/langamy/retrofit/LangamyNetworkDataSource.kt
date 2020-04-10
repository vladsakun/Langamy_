package com.langamy.retrofit

import androidx.lifecycle.LiveData
import com.langamy.base.classes.StudySet

interface LangamyNetworkDataSource {
    val downloadedStudySets:LiveData<List<StudySet>>

    suspend fun fetchStudySets(
            userEmail:String
    )
}