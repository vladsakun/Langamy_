package com.langamy.repositories

import androidx.lifecycle.LiveData
import com.langamy.base.classes.StudySet
import com.langamy.base.classes.User
import com.langamy.database.DaoStudySet
import com.langamy.database.UserDao
import com.langamy.provider.UserProvider
import com.langamy.retrofit.LangamyNetworkDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StudySetsRepositoryImpl(
        private val daoStudySet: DaoStudySet,
        private val userDao: UserDao,
        private val langamyNetworkDataSource: LangamyNetworkDataSource,
        private val userProvider: UserProvider
) : StudySetsRepository {

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


    private fun persistFetchedStudySetsList(fetchedStudySetsList: List<StudySet>) {
        GlobalScope.launch(Dispatchers.IO) {
            daoStudySet.upsert(fetchedStudySetsList)
            userDao.upsert(User(userProvider.getUserEmail()))
        }
    }

    override suspend fun deleteAllStudySets() {
        GlobalScope.launch(Dispatchers.IO) {
            daoStudySet.deleteAll()
        }
    }

    private suspend fun initStudySetsData() {

        if (isFetchStudySetsNeeded()) {
            fetchStudySetsList()
        }
    }

    private suspend fun fetchStudySetsList() {
        langamyNetworkDataSource.fetchStudySets(userProvider.getUserEmail())
    }

    private fun isFetchStudySetsNeeded(): Boolean {
        return true
    }

}