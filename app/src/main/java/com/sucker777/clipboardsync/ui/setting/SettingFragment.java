package com.sucker777.clipboardsync.ui.setting;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.sucker777.clipboardsync.R;

public class SettingFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }
}