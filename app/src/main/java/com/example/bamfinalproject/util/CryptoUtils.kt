package com.example.bamfinalproject.util

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.example.bamfinalproject.MainActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.Key
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object CryptoUtils {
    private const val keyAlias = "key_project_bam"
    private const val keyStoreName = "AndroidKeyStore"

    private const val transformation = KeyProperties.KEY_ALGORITHM_AES + "/" +
            KeyProperties.BLOCK_MODE_GCM + "/" +
            KeyProperties.ENCRYPTION_PADDING_NONE

    fun generateStrongAESKey(password: CharArray): SecretKey {
        val salt = byteArrayOf(
            60, 33, 113, -99, -25,
            115, -22, -77, 22, 29,
            -19, 44, -1, -60, -107,
            -62, 118, 22, 110, -95,
            39, 32, 74, -111, 114,
            -87, -27, -110, -109, 15,
            -20, -88)
        val iterationCount = 10_000
        val defaultKeyLengthInBits = 256

        val keySpec = PBEKeySpec(password, salt, iterationCount, defaultKeyLengthInBits)
        val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = keyFactory.generateSecret(keySpec).encoded

        return SecretKeySpec(keyBytes, KeyProperties.KEY_ALGORITHM_AES)
    }

    fun getPassphrase(): ByteArray {
        val passphraseFileName = "passphrase.bin"
        val passphraseFile = File(MainActivity.appContext.filesDir, passphraseFileName)
        val masterKey = getMasterKey()

        return if (passphraseFile.exists()) {
            decryptTextFromFile(masterKey, passphraseFileName)
        } else {
            val passphrase = generatePassphrase()
            encryptTextToFile(masterKey, passphraseFileName, passphrase)
            passphrase
        }
    }

    private fun getMasterKey(): SecretKey =
        getMasterKeyFromAndroidKeyStore() ?: generateMasterKey()

    private fun getMasterKeyFromAndroidKeyStore(): SecretKey? {
        val keyStore = KeyStore.getInstance(keyStoreName)
            .also { it.load(null) }
        return keyStore.getKey(keyAlias, null) as SecretKey?
    }

    private fun generateMasterKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, keyStoreName)
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).apply {
            setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        }.build()
        keyGenerator.init(keyGenParameterSpec)

        return keyGenerator.generateKey()
    }

    fun decryptTextFromFile(key: Key, fileContent: ByteArray): ByteArray {

        val initializationVectorLength = 12
        val initializationVector = fileContent.copyOfRange(
            fromIndex = 0,
            toIndex = initializationVectorLength
        )
        val encryptedText = fileContent.copyOfRange(
            fromIndex = initializationVectorLength,
            toIndex = fileContent.size
        )

        val decryptionCipher = getDecryptionCipher(key, initializationVector)
        return decryptionCipher.doFinal(encryptedText)
    }

    private fun decryptTextFromFile(key: Key, fileName: String): ByteArray {
        val ivAndEncryptedText = MainActivity.appContext
            .openFileInput(fileName).use {
                it.readBytes()
            }

        val initializationVectorLength = 12
        val initializationVector = ivAndEncryptedText.copyOfRange(
            fromIndex = 0,
            toIndex = initializationVectorLength
        )
        val encryptedText = ivAndEncryptedText.copyOfRange(
            fromIndex = initializationVectorLength,
            toIndex = ivAndEncryptedText.size
        )

        val decryptionCipher = getDecryptionCipher(key, initializationVector)
        return decryptionCipher.doFinal(encryptedText)
    }

    private fun getDecryptionCipher(key: Key, initializationVector: ByteArray): Cipher {
        return Cipher.getInstance(transformation).apply {
            init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, initializationVector))
        }
    }

    private fun generatePassphrase(): ByteArray {
        val result = ByteArray(32)

        val random = SecureRandom()
        random.nextBytes(result)

        // filter out zero byte values, as SQLCipher does not like them
        while (result.contains(0)) {
            random.nextBytes(result)
        }

        return result
    }

    fun encryptTextToFile(key: Key, myExternalFile: File, text: ByteArray) {
        val encryptionCipher = getEncryptionCipher(key)
        val fos: FileOutputStream?
        try {
            fos = FileOutputStream(myExternalFile)
            fos.write(encryptionCipher.iv)
            fos.write(encryptionCipher.doFinal(text))
            fos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun encryptTextToFile(key: Key, fileName: String, text: ByteArray) {
        val encryptionCipher = getEncryptionCipher(key)

        MainActivity.appContext
            .openFileOutput(fileName, Context.MODE_PRIVATE).use {
                it.write(encryptionCipher.iv)
                it.write(encryptionCipher.doFinal(text))
            }
    }

    private fun getEncryptionCipher(key: Key): Cipher =
        Cipher.getInstance(transformation).apply {
            init(Cipher.ENCRYPT_MODE, key)
        }

}