package com.langamy.datasource

import androidx.lifecycle.LiveData
import com.langamy.base.classes.StudySet
import com.langamy.base.classes.TranslationResponse
import org.json.JSONObject

interface LangamyNetworkDataSource {
    val downloadedStudySets: LiveData<List<StudySet>>
    val deleteStatus: LiveData<HashMap<String, Any>>
    val clonedStudySet: LiveData<StudySet>
    val translation: LiveData<TranslationResponse>

    suspend fun fetchStudySets(
            userEmail: String
    )

    suspend fun deleteStudySet(id: Int)

    suspend fun patchStudySet(studySet: StudySet)

    suspend fun cloneStudySet(studySetId: Int, userEmail: String)

    suspend fun translate(stringToTranslate: JSONObject, fromLang:String, toLang:String, mode:String)
}