package edu.kit.informatik.pcc.android.settings;

import org.json.JSONException;

import edu.kit.informatik.pcc.android.storage.ISimpleValueStorage;

public class SettingsManager implements ISettingsManager {
    private static final String settingsKey = SettingsManager.class.getName() + ".settings";
    private ISimpleValueStorage simpleValueStorage;

    public void setSimpleValueStorage(ISimpleValueStorage simpleValueStorage) {
        assert this.simpleValueStorage == null;
        this.simpleValueStorage = simpleValueStorage;
    }

    @Override
    public void storeSettings(Settings settings) {
        assertCompletelySetup();
        String settingsJSON = settings.getAsJSON();
        simpleValueStorage.putString(settingsKey, settingsJSON);
    }

    @Override
    public Settings loadSettings() {
        assertCompletelySetup();
        Settings settings = new Settings();
        String settingsJSON = simpleValueStorage.getString(settingsKey);
        if (settingsJSON == null) {
            return settings;
        }
        try {
            settings = new Settings(settingsJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return settings;
    }

    private void assertCompletelySetup() {
        assert simpleValueStorage != null;
    }
}
