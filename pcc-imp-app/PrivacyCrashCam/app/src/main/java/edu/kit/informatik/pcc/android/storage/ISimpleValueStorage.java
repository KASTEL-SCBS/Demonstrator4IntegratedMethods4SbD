package edu.kit.informatik.pcc.android.storage;

public interface ISimpleValueStorage {
    void putString(String key, String value);
    String getString(String key);
}
