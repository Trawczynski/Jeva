package com.jeva.jeva.home

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import androidx.preference.PreferenceManager
import java.util.*


object LocaleHelper {


    private const val SELECTED_LANGUAGE = "Locale.Helper.Selected.Language"

    fun setLocale(context: Context?, language: String?): Context? {
        persist(context, language)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return updateResources(context, language);
        }
        return updateResourcesLegacy(context, language);
    }

    private fun getPersistedData(context: Context, defaultLanguage: String): String? {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getString(SELECTED_LANGUAGE, defaultLanguage)
    }


    private fun persist(context: Context?, language: String?) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        editor.putString(SELECTED_LANGUAGE, language)
        editor.apply()
    }


    @TargetApi(Build.VERSION_CODES.N)
    private fun updateResources(context: Context?, language: String?): Context? {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val configuration = context!!.resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        return context?.createConfigurationContext(configuration)
    }

    private fun updateResourcesLegacy(context: Context?, language: String?): Context? {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val resources = context?.resources
        val configuration = resources?.configuration
        configuration?.setLocale(locale)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration?.setLayoutDirection(locale)
        }
        context?.applicationContext?.createConfigurationContext(configuration!!)
        context?.resources?.displayMetrics?.setTo(resources?.displayMetrics)
        return context
    }
}