package com.langamy.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.langamy.base.classes.StudySet
import com.langamy.base.classes.User
import com.langamy.database.DaoStudySet
import com.langamy.database.UserDao
import com.langamy.datasource.LangamyNetworkDataSource
import com.langamy.provider.UserProvider
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

            clonedStudySet.observeForever { newClonedStudySet ->
                persistFetchedStudySet(newClonedStudySet)
            }
        }
    }

    private val _clonedStudySet = MutableLiveData<StudySet>()

    override val clonedStudySet: LiveData<StudySet>
        get() = _clonedStudySet

    override suspend fun getStudySetsList(): LiveData<out List<StudySet>> {
        return withContext(Dispatchers.IO) {
            initStudySetsData()
            return@withContext daoStudySet.getAll()
        }
    }

    private fun persistFetchedStudySet(newClonedStudySet: StudySet) {
        GlobalScope.launch(Dispatchers.IO) {
            daoStudySet.upsertStudySet(newClonedStudySet)
            _clonedStudySet.postValue(newClonedStudySet)
        }
    }

    override suspend fun getStudySet(id: Int): LiveData<StudySet> {
        return withContext(Dispatchers.IO) {
            return@withContext daoStudySet.getSpecificStudySet(id)
        }
    }

    override suspend fun getNoLiveDataStudySet(id: Int): StudySet {
        return withContext(Dispatchers.IO){
            return@withContext daoStudySet.getNoLiveDataSpecificStudySet(id)!!
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

    override suspend fun updateStudySet(studySet: StudySet) {
        GlobalScope.launch(Dispatchers.IO) {
            daoStudySet.update(studySet)
        }
    }

    override suspend fun insertStudySet(studySet: StudySet) {
        GlobalScope.launch(Dispatchers.IO) {
            daoStudySet.upsertStudySet(studySet)
        }
    }

    override suspend fun cloneStudySet(studySetId: Int) {
        GlobalScope.launch(Dispatchers.IO) {
            langamyNetworkDataSource.cloneStudySet(studySetId, userProvider.getUserEmail())
        }
    }

    private suspend fun initStudySetsData() {

        if (isFetchStudySetsNeeded()) {
            fetchStudySetsList()
        }
    }

    private suspend fun fetchStudySetsList() {
        patchUnsyncedStudySets()
    }

    private suspend fun patchUnsyncedStudySets() {

        GlobalScope.launch(Dispatchers.IO) {

            val studySetsList = daoStudySet.getUnsyncedStudySet()

            for (studySet in studySetsList) {
                studySet.isSync_status = true
                langamyNetworkDataSource.patchStudySet(studySet)
            }

            langamyNetworkDataSource.fetchStudySets(userProvider.getUserEmail())
        }
    }

    private suspend fun deleteRemoteStudySet(id: Int) {
        langamyNetworkDataSource.deleteStudySet(id)
    }

    private fun isFetchStudySetsNeeded(): Boolean {
        return true
    }

}