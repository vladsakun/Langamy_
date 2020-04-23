package com.langamy.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.langamy.base.classes.StudySet

@Dao
interface DaoStudySet {

    @Query("DELETE FROM study_set_table WHERE id=:id")
    fun deleteById(id:Int)

    @Query("SELECT * FROM study_set_table WHERE id=:id")
    fun getSpecificStudySet(id: Int): LiveData<StudySet>

    @Query("SELECT * FROM study_set_table WHERE id=:id")
    fun getNoLiveDataSpecificStudySet(id: Int): StudySet?

    @Query("SELECT * FROM study_set_table WHERE sync_status=0")
    fun getUnsyncedStudySet(): List<StudySet>

    @Update
    fun update(studySet: StudySet)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(studySets: List<StudySet>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertStudySet(studySet: StudySet)

    @Query("SELECT * FROM study_set_table ORDER BY id DESC ")
    fun getAll(): LiveData<List<StudySet>>

    @Query("DELETE FROM study_set_table")
    fun deleteAll()
}