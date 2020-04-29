package com.langamy.viewmodel.continue_learning

import androidx.lifecycle.ViewModel
import com.langamy.repositories.StudySetsRepository
import org.json.JSONArray

class ContinueLearningViewModel(
        private val studySetsRepository: StudySetsRepository
) : ViewModel() {

    suspend fun updateStudySet(studySetId: Int, learnMarked: Boolean) {

        val studySet = studySetsRepository.getNoLiveDataStudySet(studySetId)

        val jsonArray: JSONArray = if (learnMarked) {
            JSONArray(studySet.marked_words)
        } else {
            JSONArray(studySet.words)
        }
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            jsonObject.put("firstStage", true)
            jsonObject.put("secondStage", false)
            jsonObject.put("thirdStage", false)
            jsonObject.put("forthStage", false)
        }

        if (learnMarked) {
            studySet.marked_words = jsonArray.toString()
        } else {
            studySet.words = jsonArray.toString()
        }

        studySetsRepository.updateStudySet(studySet)

    }
}