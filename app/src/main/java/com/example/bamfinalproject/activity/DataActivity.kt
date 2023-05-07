package com.example.bamfinalproject.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.bamfinalproject.MainActivity
import com.example.bamfinalproject.R
import com.example.bamfinalproject.common.UserDataAdapter
import com.example.bamfinalproject.database.entity.UserData

class DataActivity : AppCompatActivity() {
    private lateinit var listView: ListView
    private lateinit var helloView: TextView
    private lateinit var newLogin: EditText
    private lateinit var newPassword: EditText
    private lateinit var optionsButton: Button
    private lateinit var addButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data)

        initUI()

        addButton.setOnClickListener { addNewCredentials() }
        optionsButton.setOnClickListener {
            val i = Intent(this, OptionsActivity::class.java)
            i.putExtra("login", getLogin())
            startActivity(i)
        }
    }

    private fun initUI() {
        newLogin = findViewById(R.id.newLogin)
        newPassword = findViewById(R.id.newPassword)
        addButton = findViewById(R.id.addButton)
        optionsButton = findViewById(R.id.optionsButton)

        listView = findViewById<View>(R.id.listView) as ListView
        listView.adapter = UserDataAdapter(this, getUserData() as ArrayList<UserData>)

        helloView = findViewById(R.id.helloView)
        helloView.text = String.format("Witaj, %s", MainActivity.db.userDao().findByLogin(getLogin()).firstName)
    }

    private fun getUserData(): List<UserData> {
        return MainActivity.db.userDataDao().getAllByLogin(getLogin())
    }

    private fun addNewCredentials() {
        if(validate()){
            MainActivity.db.userDataDao().insertAll(UserData(newLogin.text.toString(), newPassword.text.toString(), getLogin()))
            finish()
            startActivity(intent)
        }
    }

    private fun getLogin(): String{
        return (intent.extras?.getString("login") ?: "FAILED")
    }

    private fun validate(): Boolean {
        return newLogin.text.isNotEmpty() && newPassword.text.isNotEmpty()
    }
}
