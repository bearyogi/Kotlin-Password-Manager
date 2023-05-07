package com.example.bamfinalproject.util

import com.example.bamfinalproject.MainActivity
import com.example.bamfinalproject.database.entity.Data
import com.example.bamfinalproject.database.entity.User
import com.example.bamfinalproject.database.entity.UserData
import com.google.gson.GsonBuilder
import java.io.File
import java.util.*
import javax.crypto.AEADBadTagException

object MigrationUtils {
    private var gson = GsonBuilder().serializeNulls().setPrettyPrinting().create()

    fun importData(password: String, fileContents: ByteArray): Int{
        val key = CryptoUtils.generateStrongAESKey(password.toCharArray())
        try{
            val decryptedText = CryptoUtils.decryptTextFromFile(key, fileContents).decodeToString()
            val data = gson.fromJson(decryptedText, Data::class.java)
            return if(MainActivity.db.userDao().findByLogin(data.user.login!!) == null){
                MainActivity.db.userDao().insertAll(User(data.user.firstName, data.user.login, data.user.password))
                data.userDatas.stream().forEach{userData ->
                    MainActivity.db.userDataDao().insertAll(UserData(userData.login, userData.password, userData.createdUser))
                }
                0
            }else{
                checkUserData(data.userDatas, data.user.login!!)
                1
            }
        }catch(ex: AEADBadTagException){
            println(ex.toString())
        }
        return 2
    }

    fun writeTextToFile(myExternalFile: File, login: String): Boolean{
        val jsonResponse = convertClassToJson(Data(MainActivity.db.userDao().findByLogin(login), MainActivity.db.userDataDao().getAllByLogin(login)))

        return if (jsonResponse != "") {
            val key = CryptoUtils.generateStrongAESKey(MainActivity.db.userDao().findByLogin(login).password!!.toCharArray())
            CryptoUtils.encryptTextToFile(key, myExternalFile, jsonResponse?.toByteArray()!!)
            true
        }else{
            false
        }
    }

    private fun checkUserData(userDatas: List<UserData>, login: String){
        val datas = MainActivity.db.userDataDao().getAllByLogin(login)
        userDatas.stream().forEach { userData -> if(!userDataPresent(datas, userData)){
            MainActivity.db.userDataDao().insertAll(UserData(userData.login, userData.password, userData.createdUser))
        } }
    }

    private fun userDataPresent(datas: List<UserData>, userData: UserData): Boolean{
        return datas.stream().anyMatch { data -> data.compare(userData)}
    }

    fun getFileName(login: String): String {
        return "Bam-Final-" + login + "-" + Calendar.getInstance().timeInMillis.toString() + ".json"
    }

    private fun convertClassToJson(data: Data): String? {
        return gson.toJson(data)
    }

}