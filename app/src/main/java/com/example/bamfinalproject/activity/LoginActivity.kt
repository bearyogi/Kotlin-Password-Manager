package com.example.bamfinalproject.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.bamfinalproject.MainActivity
import com.example.bamfinalproject.R

class LoginActivity : AppCompatActivity() {
    private lateinit var loginText: EditText
    private lateinit var passwordText: EditText
    private lateinit var validationText: TextView
    private lateinit var registerButton: Button
    private lateinit var loginButton: Button
    private lateinit var checkBox: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initUI()

        loginButton.setOnClickListener { validateData() }
        registerButton.setOnClickListener { startActivity(Intent(this, RegisterActivity::class.java)) }
    }

    private fun validateData() {
        if (loginText.text.toString().isEmpty()) {
            validationText.text = "login nie może być pusty!"
        } else if (passwordText.text.toString().isEmpty()) {
            validationText.text = "Hasło nie może być puste!"
        } else if (!userExist()) {
            validationText.text = "Użytkownik o podanym loginie nie istnieje!"
        } else {
            userLogin()
        }
    }

    private fun userLogin() {
        val user = MainActivity.db.userDao().findByLogin(loginText.text.toString())
        if (user.password != passwordText.text.toString()) {
            validationText.text = "Wprowadzone hasło jest nieprawidłowe!"
        } else if (checkBox.isChecked){
            putIntoShared()
        }

        val i = Intent(this, DataActivity::class.java)
        i.putExtra("login", loginText.text.toString())
        startActivity(i)
    }

    private fun putIntoShared() {
        val sp = getSharedPreferences("login", MODE_PRIVATE)
        sp.edit().putBoolean("logged", true).apply()
        sp.edit().putString("login", loginText.text.toString()).apply()
    }

    private fun userExist(): Boolean {
        return MainActivity.db.userDao().findByLogin(loginText.text.toString()) != null
    }

    private fun initUI() {
        loginText = findViewById(R.id.loginText)
        passwordText = findViewById(R.id.passwordText)
        validationText = findViewById(R.id.validationText)
        registerButton = findViewById(R.id.registerButton)
        loginButton = findViewById(R.id.loginButton)
        checkBox = findViewById(R.id.checkBox)
    }
}