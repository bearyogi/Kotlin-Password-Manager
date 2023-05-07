package com.example.bamfinalproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.bamfinalproject.activity.DataActivity
import com.example.bamfinalproject.activity.LoginActivity
import com.example.bamfinalproject.activity.RegisterActivity
import com.example.bamfinalproject.database.db.AppDatabase


class MainActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        appContext = applicationContext
        System.loadLibrary("sqlcipher")

        initDb()
        routeToFirstActivity()
    }

    private fun initDb() {
        db = AppDatabase.getDatabase(appContext)
    }

    private fun routeToFirstActivity() {
        val sp = getSharedPreferences("login", MODE_PRIVATE)
        val userDao = db.userDao()
        db.close()

        if (userDao.getAll().isEmpty()) {
            startActivity(Intent(this, RegisterActivity::class.java))
        } else if (!sp.getBoolean("logged", false)) {
            startActivity(Intent(this, LoginActivity::class.java))
        } else {
            val i = Intent(this, DataActivity::class.java)
            i.putExtra("login", sp.getString("login", "FAILED"))
            startActivity(i)
        }
    }

    companion object {
        lateinit var appContext: Context
        val db by lazy { AppDatabase.getDatabase(appContext) }

        val isCtxInitialized: Boolean
            get() = this::appContext.isInitialized
    }
}