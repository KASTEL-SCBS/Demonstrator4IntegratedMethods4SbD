package edu.kit.informatik.pcc.android.storage;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * A class to store simple values in the applications shared preferences.
 */
public class PreferencesStorage implements ISimpleValueStorage {
    private SharedPreferences preferences;

    public PreferencesStorage(Context context) {
        preferences = context.getSharedPreferences("de.pcc.privacycrashcam.APP_PREFERENCES",
                Context.MODE_PRIVATE);
    }

    @Override
    public void putString(String key, String value) {
        preferences.edit().putString(key, value).commit();
    }

    @Override
    public String getString(String key) {
        try {
            return preferences.getString(key, null);
        }
        catch (ClassCastException e) {
            e.printStackTrace();
            return null;
        }
    }
}
