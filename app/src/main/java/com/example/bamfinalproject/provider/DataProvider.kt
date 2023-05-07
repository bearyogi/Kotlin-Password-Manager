package com.example.bamfinalproject.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import androidx.sqlite.db.SupportSQLiteQueryBuilder
import com.example.bamfinalproject.MainActivity
import com.example.bamfinalproject.database.entity.User

class DataProvider : ContentProvider() {
    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
        addURI(AUTHORITY, TABLE_NAME_LOGIN_PASSWORD, CODE_LOGIN_PASSWORD_TABLE)
    }

    override fun onCreate(): Boolean {
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        return when (uriMatcher.match(uri)) {
            CODE_LOGIN_PASSWORD_TABLE -> {
                if (!MainActivity.isCtxInitialized) return null
                if (!areSelectionCriteriaValid(selection, selectionArgs)) return null

                val foundUser = MainActivity.db.userDao().findUser(
                    login = selectionArgs!![0],
                    password = selectionArgs[1]
                )

                if(foundUser == null) return null

                val supportDatabase = MainActivity.db.openHelper.readableDatabase
                supportDatabase.query(
                    SupportSQLiteQueryBuilder.builder(TABLE_NAME_LOGIN_PASSWORD)
                        .selection("createdUser = ?", arrayOf(foundUser.login))
                        .columns(projection)
                        .orderBy(sortOrder)
                        .create()
                )
            }

            else -> {
                throw IllegalArgumentException("Unknown URI: $uri")
            }
            }
        }

    private fun areSelectionCriteriaValid(selection: String?, selectionArgs: Array<out String>?): Boolean {
        if (selection == null || selectionArgs == null) return false
        if (selectionArgs.size != 2) return false // we need login and password
        if (!selection.contains("login", ignoreCase = false)) return false
        if (!selection.contains("password", ignoreCase = false)) return false

        return true
    }

    override fun getType(p0: Uri): String? {
        return null
    }

    override fun insert(p0: Uri, p1: ContentValues?): Uri? {
        return null
    }

    override fun delete(p0: Uri, p1: String?, p2: Array<out String>?): Int {
        return 0
    }

    override fun update(p0: Uri, p1: ContentValues?, p2: String?, p3: Array<out String>?): Int {
        return 0
    }

    private companion object {
        private const val AUTHORITY = "com.example.bamfinalproject.authorities.LOGIN_PASSWORD"
        private const val TABLE_NAME_LOGIN_PASSWORD = "userData"
        private const val CODE_LOGIN_PASSWORD_TABLE = 1
    }
}