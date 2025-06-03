package com.example.travelscolombia

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

object SecurePrefs {
    private const val PREFS_NAME = "secure_prefs"
    private const val KEY_EMAIL = "email"
    private const val KEY_PASSWORD = "password"

    private fun prefs(context: Context) = EncryptedSharedPreferences.create(
        PREFS_NAME,
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveCredentials(context: Context, email: String, password: String) {
        prefs(context).edit().apply {
            putString(KEY_EMAIL, email)
            putString(KEY_PASSWORD, password)
            apply()
        }
    }

    fun getEmail(context: Context): String? = prefs(context).getString(KEY_EMAIL, null)
    fun getPassword(context: Context): String? = prefs(context).getString(KEY_PASSWORD, null)
}
