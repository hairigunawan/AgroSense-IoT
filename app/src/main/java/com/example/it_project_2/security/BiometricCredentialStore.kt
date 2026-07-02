package com.example.it_project_2.security

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

data class StoredPasswordCredentials(
    val email: String,
    val password: String
)

object BiometricCredentialStore {
    private const val PREF_NAME = "secret_shared_prefs"
    private const val KEY_EMAIL = "email"
    private const val KEY_PASSWORD = "password"
    private const val TAG = "BiometricCredStore"

    fun savePasswordCredentials(context: Context, email: String, password: String): Boolean {
        return runCatching {
            writeCredentials(context, email, password)
        }.recoverCatching { error ->
            Log.e(TAG, "Initial save failed, resetting encrypted prefs", error)
            resetEncryptedStorage(context)
            writeCredentials(context, email, password)
        }.onFailure {
            Log.e(TAG, "Failed to save password credentials after recovery", it)
        }.getOrDefault(false)
    }

    fun getPasswordCredentials(context: Context): StoredPasswordCredentials? {
        return runCatching {
            readCredentials(context)
        }.recoverCatching { error ->
            Log.e(TAG, "Initial read failed, resetting encrypted prefs", error)
            resetEncryptedStorage(context)
            readCredentials(context)
        }.onFailure {
            Log.e(TAG, "Failed to read password credentials after recovery", it)
        }.getOrNull()
    }

    fun hasPasswordCredentials(context: Context): Boolean {
        return getPasswordCredentials(context) != null
    }

    fun hasPasswordCredentialsForEmail(context: Context, email: String?): Boolean {
        if (email.isNullOrBlank()) return false
        val stored = getPasswordCredentials(context) ?: return false
        return stored.email.equals(email.trim(), ignoreCase = true)
    }

    private fun writeCredentials(context: Context, email: String, password: String): Boolean {
        return encryptedPrefs(context)
            .edit()
            .putString(KEY_EMAIL, email.trim())
            .putString(KEY_PASSWORD, password)
            .commit()
    }

    private fun readCredentials(context: Context): StoredPasswordCredentials? {
        val prefs = encryptedPrefs(context)
        val email = prefs.getString(KEY_EMAIL, null)?.trim().orEmpty()
        val password = prefs.getString(KEY_PASSWORD, null).orEmpty()

        return if (email.isBlank() || password.isBlank()) {
            null
        } else {
            StoredPasswordCredentials(email, password)
        }
    }

    private fun resetEncryptedStorage(context: Context) {
        context.applicationContext.deleteSharedPreferences(PREF_NAME)
    }

    private fun encryptedPrefs(context: Context) = EncryptedSharedPreferences.create(
        context.applicationContext,
        PREF_NAME,
        MasterKey.Builder(context.applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
}
