package edu.sg.nushigh.h1930006.anti_ncov;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class SettingsUtil {
    private static SettingsUtil INSTANCE;
    private final Context context;
    private final SharedPreferences settings;

    public SettingsUtil(Context context) {
        this.context = context;
        this.settings = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static SettingsUtil getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new SettingsUtil(context);
        }
        return INSTANCE;
    }

    public String getUserClass(String email) {
        return settings.getString(email, null);
    }

    public void setUserClass(String email, String userClass) {
        settings.edit().putString(email, userClass).apply();
    }

    public boolean isDarkModeEnabled() {
        return settings.getBoolean(context.getString(R.string.settings_key_dark_mode), false);
    }

    public boolean isNotificationsEnabled() {
        return settings.getBoolean(context.getString(R.string.settings_key_notifications), true);
    }
}
