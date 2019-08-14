package com.example.ultimat

import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat

// Fragment to change settings
class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)


        val monitoredPreferences = listOf("server_address", "server_port")

        monitoredPreferences.forEach {
            val pref = findPreference(it) as EditTextPreference
            pref.summary = pref.text
            pref.setOnPreferenceChangeListener { preference, newValue ->
                preference.summary = newValue as String; true

            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SettingsFragment()
    }
}