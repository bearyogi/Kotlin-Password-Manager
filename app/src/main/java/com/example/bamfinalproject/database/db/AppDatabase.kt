package com.example.bamfinalproject.database.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.bamfinalproject.database.dao.UserDao
import com.example.bamfinalproject.database.dao.UserDataDao
import com.example.bamfinalproject.database.entity.User
import com.example.bamfinalproject.database.entity.UserData
import com.example.bamfinalproject.util.CryptoUtils
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

@Database(entities = [User::class, UserData::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun userDataDao(): UserDataDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "bam_final.db"
                ).openHelperFactory(SupportOpenHelperFactory(CryptoUtils.getPassphrase()))
                    .allowMainThreadQueries()
                    .build()
                INSTANCE = instance

                instance
            }
        }
    }
}
