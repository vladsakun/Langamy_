package com.langamy.datasource

import androidx.lifecycle.LiveData
import com.langamy.base.classes.StudySet

interface LangamyNetworkDataSource {
    val downloadedStudySets:LiveData<List<StudySet>>
    val deleteStatus: LiveData<HashMap<String, Any>>
    val clonedStudySet: LiveData<StudySet>

    suspend fun fetchStudySets(
            userEmail:String
    )
    suspend fun deleteStudySet(id:Int)

    suspend fun patchStudySet(studySet: StudySet)

    suspend fun cloneStudySet(studySetId: Int, userEmail: String)
}