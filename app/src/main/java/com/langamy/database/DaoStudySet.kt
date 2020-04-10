package com.langamy.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.langamy.base.classes.StudySet

@Dao
interface DaoStudySet {

    @Query("SELECT * FROM study_set_table")
    fun getAll(): LiveData<List<StudySet>>

    @Delete
    fun delete(studySet: StudySet)

    @Update
    fun update(studySet: StudySet?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(studySets: List<StudySet>)
}