package com.example.bamfinalproject.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.bamfinalproject.MainActivity
import com.example.bamfinalproject.R
import com.example.bamfinalproject.database.entity.User
import com.example.bamfinalproject.util.MigrationUtils
import java.io.BufferedInputStream
import java.io.InputStream

class RegisterActivity : AppCompatActivity() {
    private lateinit var firstNameText: EditText
    private lateinit var loginText: EditText
    private lateinit var passwordText: EditText
    private lateinit var validationText: TextView
    private lateinit var registerButton: Button
    private lateinit var loginButton: Button
    private lateinit var importButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initUI()

        registerButton.setOnClickListener { validateData() }
        loginButton.setOnClickListener { startActivity(Intent(this, LoginActivity::class.java)) }
        importButton.setOnClickListener { openFileManager() }
    }

    private fun validateData() {
        if (firstNameText.text.toString().isEmpty()) {
            validationText.text = "Imię nie może być puste!"
        } else if (loginText.text.toString().isEmpty()) {
            validationText.text = "login nie może być pusty!"
        } else if (passwordText.text.toString().isEmpty()) {
            validationText.text = "Hasło nie może być puste!"
        } else if (userExist()) {
            validationText.text = "Użytkownik o podanym loginie istnieje"
        } else {
            val user = User(firstNameText.text.toString(), loginText.text.toString(), passwordText.text.toString())
            MainActivity.db.userDao().insertAll(user)
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun userExist(): Boolean {
        return MainActivity.db.userDao().findByLogin(loginText.text.toString()) != null
    }

    private fun openFileManager() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        getDataFromFile.launch(intent)
    }

    private var getDataFromFile =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val uri = result.data?.data
                val fileContents = readTextFromUri(uri!!)
                val alert = AlertDialog.Builder(this)
                val editText = EditText(applicationContext)

                alert.setMessage("Wpisz hasło backupu!")
                alert.setTitle("Importuj backup")
                alert.setView(editText)

                alert.setPositiveButton("Wykonaj") { _, _ -> if(MigrationUtils.importData(editText.text.toString(), fileContents) == 0) {
                    Toast.makeText(this, "Nowe konto zostało odtworzone!", Toast.LENGTH_SHORT).show()
                } else if(MigrationUtils.importData(editText.text.toString(), fileContents) == 1){
                    Toast.makeText(this, "Konto istnieje, aktualizacja danych!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Niepoprawne haslo!", Toast.LENGTH_SHORT).show()
                }}

                alert.setNegativeButton("Anuluj") { _, _ -> }
                alert.show()
            }
        }

    private fun readTextFromUri(uri: Uri): ByteArray {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val reader = BufferedInputStream(inputStream)
        val output = reader.readBytes()
        reader.close()
        return output
    }

    private fun initUI() {
        firstNameText = findViewById(R.id.firstNameText)
        loginText = findViewById(R.id.loginText)
        passwordText = findViewById(R.id.passwordText)
        validationText = findViewById(R.id.validationText)
        registerButton = findViewById(R.id.registerButton)
        loginButton = findViewById(R.id.loginButton)
        importButton = findViewById(R.id.importButton)
    }
}