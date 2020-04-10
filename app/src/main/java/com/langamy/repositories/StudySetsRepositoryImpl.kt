package com.langamy.repositories

import androidx.lifecycle.LiveData
import com.langamy.base.classes.StudySet
import com.langamy.database.DaoStudySet
import com.langamy.retrofit.LangamyNetworkDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StudySetsRepositoryImpl (
        private val daoStudySet: DaoStudySet,
        private val langamyNetworkDataSource: LangamyNetworkDataSource
): StudySetsRepository {

    init {
        langamyNetworkDataSource.downloadedStudySets.observeForever { newStudySetsList ->
            persistFetchedStudySetsList(newStudySetsList)
        }
    }

    override suspend fun getStudySetsList(): LiveData<out List<StudySet>> {
        return withContext(Dispatchers.IO) {
            initStudySetsData()
            return@withContext daoStudySet.getAll()
        }
    }

    private fun persistFetchedStudySetsList(fetchedStudySetsList: List<StudySet>){
        GlobalScope.launch(Dispatchers.IO) {
            daoStudySet.upsert(fetchedStudySetsList)
        }
    }

    private suspend fun initStudySetsData(){
        if(isFetchStudySetsNeeded()){
            fetchStudySetsList()
        }
    }

    private suspend fun fetchStudySetsList(){
        langamyNetworkDataSource.fetchStudySets("vlad120403@gmail.com")
    }

    private fun isFetchStudySetsNeeded() : Boolean{
        return true
    }
}