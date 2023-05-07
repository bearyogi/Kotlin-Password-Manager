package com.example.bamfinalproject.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class UserData(
    @ColumnInfo(name = "login")
    var login: String?,
    @ColumnInfo(name = "password")
    var password: String?,
    @ColumnInfo(name = "createdUser")
    var createdUser: String?
) {
    @PrimaryKey(autoGenerate = true)
    var id = 0

    fun getCreatedBy(): String? {
        return createdUser
    }

    fun getUserLogin(): String? {
        return login
    }

    fun getUserPassword(): String? {
        return password
    }

    fun compare(userData: UserData): Boolean{
        return login == userData.getUserLogin()
                && password == userData.getUserPassword()
                && createdUser == userData.getCreatedBy()
    }
}