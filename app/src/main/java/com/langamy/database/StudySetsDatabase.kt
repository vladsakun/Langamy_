package com.langamy.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.langamy.base.classes.StudySet
import com.langamy.base.classes.User

@Database(
        entities = [StudySet::class, User::class],
        version = 1
)
abstract class StudySetsDatabase : RoomDatabase() {

    abstract fun studySetDao(): DaoStudySet
    abstract fun userDao():UserDao

    companion object {
        @Volatile
        private var instance: StudySetsDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDB(context).also { instance = it }
        }

        private fun buildDB(context: Context) =
                Room.databaseBuilder(context.applicationContext,
                        StudySetsDatabase::class.java, "study_set_db.db")
                        .build()

    }
}