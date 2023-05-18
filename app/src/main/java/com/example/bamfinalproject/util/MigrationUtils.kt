package com.example.bamfinalproject.util

import com.example.bamfinalproject.MainActivity.Companion.db
import com.example.bamfinalproject.database.entity.Data
import com.example.bamfinalproject.database.entity.User
import com.example.bamfinalproject.database.entity.UserData
import com.example.bamfinalproject.util.CryptoUtils.encryptTextToFile
import com.example.bamfinalproject.util.CryptoUtils.generateStrongAESKey
import com.google.gson.GsonBuilder
import java.io.File
import java.util.*
import javax.crypto.AEADBadTagException

object MigrationUtils {
    private var gson = GsonBuilder().serializeNulls().setPrettyPrinting().create()

    fun importData(password: String, fileContents: ByteArray): Int{
        val key = generateStrongAESKey(password.toCharArray())
        try{
            val data = gson.fromJson(CryptoUtils.decryptTextFromFile(key, fileContents).decodeToString(), Data::class.java)
            return if(db.userDao().findByLogin(data.user.login!!) == null){
                db.userDao().insertAll(User(data.user.firstName, data.user.login, data.user.password))
                data.userDatas.stream().forEach{userData ->
                    db.userDataDao().insertAll(UserData(userData.login, userData.password, userData.createdUser))
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
        val jsonResponse = convertClassToJson(Data(db.userDao().findByLogin(login), db.userDataDao().getAllByLogin(login)))

        return if (jsonResponse != "") {
            val key = generateStrongAESKey(db.userDao().findByLogin(login).password!!.toCharArray())
            encryptTextToFile(key, myExternalFile, jsonResponse?.toByteArray()!!)
            true
        }else{
            false
        }
    }

    private fun checkUserData(userDatas: List<UserData>, login: String){
        val datas = db.userDataDao().getAllByLogin(login)
        userDatas.stream().forEach { userData -> if(!userDataPresent(datas, userData)){
            db.userDataDao().insertAll(UserData(userData.login, userData.password, userData.createdUser))
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