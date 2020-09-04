package edu.kit.informatik.pcc.android.settings;

public interface ISettingsManager {
    void storeSettings(Settings settings);
    Settings loadSettings();
}
