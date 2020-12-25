package com.dabenxiang.mimi.widget.utility

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.io.File
import java.security.InvalidKeyException
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object CryptUtils {
    external fun cEncrypt(str: String?): String?
    external fun cDecrypt(str: String?): String?
    external fun cIsVerify(): Boolean

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    fun encodeBase64(text: String): String {
        return Base64.encodeToString(text.toByteArray(), Base64.DEFAULT)
    }

    fun decodeBase64(encode: String): String {
        return String(Base64.decode(encode, Base64.DEFAULT))
    }

    fun encryptFile(context: Context, filesDir: String, fileName: String, data: String): Boolean {
        return try {
            deleteFile(filesDir, fileName)
            val encryptedFile = getEncryptedFile(context, filesDir, fileName)
            encryptedFile.openFileOutput().use { output ->
                output.write(data.toByteArray())
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun decryptFile(context: Context, filesDir: String, fileName: String): ByteArray {
        return try {
            val encryptedFile = getEncryptedFile(context, filesDir, fileName)
            encryptedFile.openFileInput().use { input ->
                input.readBytes()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "".toByteArray()
        }
    }

    fun getEncryptedPref(context: Context, fileName: String): SharedPreferences {
        return EncryptedSharedPreferences.create(
            fileName,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun getEncryptedFile(
        context: Context,
        filesDir: String,
        fileName: String
    ): EncryptedFile {
        return EncryptedFile.Builder(
            File(filesDir, fileName),
            context,
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()
    }

    private fun deleteFile(filesDir: String, fileName: String) {
        val file = File(filesDir, fileName)
        if (file.exists()) file.delete()
    }

    fun decryptWithCEBNoPadding(byteArray: ByteArray, key: ByteArray): ByteArray {
        val cipher: Cipher = Cipher.getInstance("AES/ECB/NoPadding")
        return try {
            val secretKey = SecretKeySpec(key, "AES")
            cipher.init(Cipher.DECRYPT_MODE, secretKey)

            cipher.doFinal(byteArray)
        } catch (e: InvalidKeyException) {
            byteArray
        }
    }

    fun decryptWithCEBNoPadding(byteArray: ByteArray, key: String): ByteArray {
        return decryptWithCEBNoPadding(byteArray, key.toByteArray())
    }

}
