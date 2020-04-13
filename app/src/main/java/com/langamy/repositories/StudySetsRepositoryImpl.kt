package com.langamy.repositories

import android.util.Log
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
        langamyNetworkDataSource.apply {

            downloadedStudySets.observeForever { newStudySetsList ->
                persistFetchedStudySetsList(newStudySetsList)
            }

//            deleteStatus.observeForever {
//                Log.d("DELETE", it["status"].toString())
//                if (it["status"] as Boolean) {
//                    deleteLocalStudySet(it["id"] as Int)
//                }
//            }

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

    override suspend fun deleteAllLocalStudySets() {
        GlobalScope.launch(Dispatchers.IO) {
            daoStudySet.deleteAll()
        }
    }

    override suspend fun deleteStudySet(id: Int) {
        GlobalScope.launch(Dispatchers.IO) {
            deleteRemoteStudySet(id)
        }
    }

    override suspend fun deleteLocalStudySet(id: Int) {
        GlobalScope.launch(Dispatchers.IO) {
            daoStudySet.deleteById(id)
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

    private suspend fun deleteRemoteStudySet(id: Int) {
        langamyNetworkDataSource.deleteStudySet(id)
    }

    private fun isFetchStudySetsNeeded(): Boolean {
        return true
    }

}