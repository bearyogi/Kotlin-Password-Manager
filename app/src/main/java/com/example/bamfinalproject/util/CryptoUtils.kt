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

/**
 * Class responsible for cryptographic operations.
 * Generates strong AES key, database passphrase,
 * encrypts text to file and decrypts text from file.
 */
object CryptoUtils {
    private const val keyAlias = "key_project_bam"
    private const val keyStoreName = "AndroidKeyStore"

    private const val transformation = KeyProperties.KEY_ALGORITHM_AES + "/" +
            KeyProperties.BLOCK_MODE_GCM + "/" +
            KeyProperties.ENCRYPTION_PADDING_NONE

    /**
     * Generates strong key using AES.
     * @param password Plain password to be used for key generation.
     * @return Generated strong key.
     */
    fun generateStrongAESKey(password: CharArray): SecretKey {
        val salt = byteArrayOf(
            -102, 97, 120, 51, 111, -11, -104, 120,
            -55, -100, -51, -126, 6, -97, -51, 116,
            -52, -69, 27, -13, 67, -34, -69, 19,
            -124, 0, 12, -110, 29, -102, 106, 40)
        val iterationCount = 10_000
        val defaultKeyLengthInBits = 256

        val keySpec = PBEKeySpec(password, salt, iterationCount, defaultKeyLengthInBits)
        val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = keyFactory.generateSecret(keySpec).encoded

        return SecretKeySpec(keyBytes, KeyProperties.KEY_ALGORITHM_AES)
    }

    /**
     * Gets passphrase - used by SQLCipher to generates its own strong AES master key.
     * If passphrase is already stored in file then is decrypted using Master Key from Android Key Store.
     * Otherwise passphrase is generated and encrypted to file using Master Key from Android Key Store.
     */
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

    /**
     * Decrypts text from file using provided key.
     * @param key Key used in encryption process.
     * @param fileContent Byte file content
     */
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

    fun decryptTextFromFile(key: Key, fileName: String): ByteArray {
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
        val authenticationTagLengthInBits = 128
        val params = GCMParameterSpec(authenticationTagLengthInBits, initializationVector)

        return Cipher.getInstance(transformation).apply {
            init(Cipher.DECRYPT_MODE, key, params)
        }
    }

    private fun generatePassphrase(): ByteArray {
        val passphraseLength = 32
        val result = ByteArray(passphraseLength)

        val random = SecureRandom()
        random.nextBytes(result)

        // filter out zero byte values, as SQLCipher does not like them
        while (result.contains(0)) {
            random.nextBytes(result)
        }

        return result
    }

    /**
     * Encrypts text to file using provided key.
     * @param key Key used for encryption process.
     * @param myExternalFile File to which data must be written.
     * @param text Text to be encrypted and saved to provided file.
     */
    fun encryptTextToFile(key: Key, myExternalFile: File, text: ByteArray) {
        val encryptionCipher = getEncryptionCipher(key)
        val encryptedText = encryptionCipher.doFinal(text)
        val initializationVector = encryptionCipher.iv

        val fos: FileOutputStream?
        try {
            fos = FileOutputStream(myExternalFile)
            fos.write(initializationVector)
            fos.write(encryptedText)
            fos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun encryptTextToFile(key: Key, fileName: String, text: ByteArray) {
        val encryptionCipher = getEncryptionCipher(key)
        val encryptedText = encryptionCipher.doFinal(text)
        val initializationVector = encryptionCipher.iv

        MainActivity.appContext
            .openFileOutput(fileName, Context.MODE_PRIVATE).use {
                it.write(initializationVector)
                it.write(encryptedText)
            }
    }

    private fun getEncryptionCipher(key: Key): Cipher =
        Cipher.getInstance(transformation).apply {
            init(Cipher.ENCRYPT_MODE, key)
        }

}