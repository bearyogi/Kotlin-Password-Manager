package com.example.bamfinalproject.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat.checkSelfPermission
import com.example.bamfinalproject.R
import com.example.bamfinalproject.util.MigrationUtils.getFileName
import com.example.bamfinalproject.util.MigrationUtils.importData
import com.example.bamfinalproject.util.MigrationUtils.writeTextToFile
import java.io.BufferedInputStream
import java.io.File
import java.io.InputStream

class OptionsActivity : AppCompatActivity() {
    private lateinit var backButton: Button
    private lateinit var logoutButton: Button
    private lateinit var importButton: Button
    private lateinit var exportButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_options)

        initUI()
        setButtonOnClick()
    }

    private fun setButtonOnClick() {
        backButton.setOnClickListener {
            val i = Intent(this, DataActivity::class.java)
            i.putExtra("login", getLogin())
            startActivity(i)}

        logoutButton.setOnClickListener {
            getSharedPreferences("login", MODE_PRIVATE).edit().clear().apply()
            startActivity(Intent(this, LoginActivity::class.java))
        }

        importButton.setOnClickListener { openFileManager() }
        exportButton.setOnClickListener { exportData() }
    }

    private fun initUI() {
        backButton = findViewById(R.id.backButton)
        logoutButton = findViewById(R.id.logoutButton)
        importButton = findViewById(R.id.importButton)
        exportButton = findViewById(R.id.exportButton)
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

                alert.setPositiveButton("Wykonaj") { _, _ -> if(importData(editText.text.toString(), fileContents) == 0) {
                    makeText(this, "Nowe konto zostało odtworzone!", Toast.LENGTH_SHORT).show()
                } else if(importData(editText.text.toString(), fileContents) == 1){
                    makeText(this, "Konto istnieje, aktualizacja danych!", Toast.LENGTH_SHORT).show()
                } else {
                    makeText(this, "Niepoprawne haslo!", Toast.LENGTH_SHORT).show()
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

    private fun exportData() {
        val fileName = getFileName(getLogin())
        val dir = File("//sdcard//Download//")
        val myExternalFile = File(dir, fileName)

        if (isStoragePermissionGranted()) {
            if(writeTextToFile(myExternalFile, getLogin())){
                makeText(this, "Information saved to SD card. $myExternalFile", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isStoragePermissionGranted(): Boolean {
        return if (checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            true
        } else {
            requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            false
        }
    }

    private fun getLogin(): String{
        return (intent.extras?.getString("login") ?: "FAILED")
    }
}