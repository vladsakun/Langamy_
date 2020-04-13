package com.langamy.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.langamy.base.classes.StudySet

@Dao
interface DaoStudySet {

    @Query("DELETE FROM study_set_table WHERE id=:id")
    fun deleteById(id:Int)

    @Update
    fun update(studySet: StudySet?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(studySets: List<StudySet>)

    @Query("SELECT * FROM study_set_table")
    fun getAll(): LiveData<List<StudySet>>

    @Query("DELETE FROM study_set_table")
    fun deleteAll()
}