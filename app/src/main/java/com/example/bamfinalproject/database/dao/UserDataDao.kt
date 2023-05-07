package com.example.bamfinalproject.database.dao

import androidx.room.*
import com.example.bamfinalproject.database.entity.UserData

@Dao
interface UserDataDao {
    @Query("SELECT * FROM userData")
    fun getAll(): List<UserData>

    @Query("SELECT * FROM userData WHERE id = :tid")
    fun get(tid: Long): UserData

    @Query("SELECT * FROM userData WHERE createdUser = :userLogin")
    fun getAllByLogin(userLogin: String): List<UserData>

    @Insert
    fun insertAll(vararg userDatas: UserData)

    @Delete
    fun delete(userData: UserData)

    @Query("UPDATE userData SET login = :tLogin, password = :tPassword WHERE id = :tid")
    fun updateUserData(tid: Long, tLogin: String?, tPassword: String?)
}