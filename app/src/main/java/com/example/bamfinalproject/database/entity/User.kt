package com.example.bamfinalproject.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
class User(
    @ColumnInfo(name = "first_name")
    var firstName: String?,
    @ColumnInfo(name = "login")
    var login: String?,
    @ColumnInfo(name = "password")
    var password: String?
) {
    @PrimaryKey(autoGenerate = true)
    var id = 0
}