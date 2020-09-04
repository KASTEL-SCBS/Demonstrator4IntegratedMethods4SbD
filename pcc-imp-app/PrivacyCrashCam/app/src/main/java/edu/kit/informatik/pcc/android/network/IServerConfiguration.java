package edu.kit.informatik.pcc.android.network;

/**
 * An implementing instance provides getters for all relevant server parameters.
 * All provided values should not change over time.
 */
public interface IServerConfiguration {
    String scheme();
    String host();
    String path();
    int port();
}
