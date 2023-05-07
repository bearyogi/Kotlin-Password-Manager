package com.example.bamfinalproject.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.bamfinalproject.database.entity.User

@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    fun getAll(): List<User>

    @Query("SELECT * FROM user WHERE login LIKE :login")
    fun findByLogin(login: String): User

    @Query("SELECT * FROM user WHERE login = :login AND password = :password")
    fun findUser(login: String, password: String): User

    @Insert
    fun insertAll(vararg users: User)

    @Delete
    fun delete(user: User)
}
