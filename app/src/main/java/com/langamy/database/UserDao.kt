package com.langamy.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.langamy.base.classes.User
import com.langamy.base.classes.User.USER_ID

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(user: User)

    @Query("SELECT * FROM user_table WHERE id = $USER_ID")
    fun getUserEmail(): LiveData<User>
}