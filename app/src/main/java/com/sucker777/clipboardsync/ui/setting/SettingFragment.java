package com.sucker777.clipboardsync.ui.setting;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.sucker777.clipboardsync.MainActivity;
import com.sucker777.clipboardsync.R;
import com.sucker777.clipboardsync.SQLQuery;

public class SettingFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        Preference deleteUUID = findPreference("delete_uuid");
        Preference deleteHistory = findPreference("delete_history");
        Preference deleteAll = findPreference("delete_all");

        Context mContext = this.getActivity();

        SQLQuery sql = new SQLQuery(mContext);

        deleteUUID.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                SharedPreferences pref = mContext.getSharedPreferences("data", mContext.MODE_PRIVATE);
                pref.edit().clear().commit();

                restart(mContext);
                return true;
            }
        });

        deleteHistory.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                sql.deleteDB();
                return true;
            }
        });

        deleteAll.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                SharedPreferences pref = mContext.getSharedPreferences("data", mContext.MODE_PRIVATE);
                pref.edit().clear().commit();

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                SharedPreferences.Editor editor = preferences.edit();
                editor.clear();
                editor.commit();

                sql.deleteDB();

                restart(mContext);
                return true;
            }
        });
    }

    public static void restart(Context context){
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        Runtime.getRuntime().exit(0);
    }
}