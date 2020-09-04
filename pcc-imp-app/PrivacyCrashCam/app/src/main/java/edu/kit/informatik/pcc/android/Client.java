package edu.kit.informatik.pcc.android;

import edu.kit.informatik.pcc.android.account.ISessionManager;
import edu.kit.informatik.pcc.android.network.IServerConfiguration;
import edu.kit.informatik.pcc.android.network.IVideoUploadWrapper;
import edu.kit.informatik.pcc.android.settings.ISettingsManager;
import edu.kit.informatik.pcc.android.storage.video.ILocalVideoManager;
import edu.kit.informatik.pcc.android.storage.video.IVideoDetailsProvider;
import edu.kit.informatik.pcc.core.data.IFileManager;

/**
 * A helper class to provide global access to provided interfaces.
 */
public class Client {
    private static Client global;

    public static Client getGlobal() {
        assert global != null;
        return global;
    }

    public static void setGlobal(Client client) {
        assert global == null;
        global = client;
    }

    private ISessionManager sessionManager;
    private ILocalVideoManager localVideoManager;
    private IVideoDetailsProvider videoDetailsProvider;
    private IFileManager temporaryFileManager;
    private ISettingsManager settingsManager;
    private IVideoUploadWrapper videoUploadWrapper;
    private IServerConfiguration serverConfiguration;

    public void setSessionManager(ISessionManager sessionManager) {
        assert this.sessionManager == null;
        this.sessionManager = sessionManager;
    }

    public ISessionManager getSessionManager() {
        assert sessionManager != null;
        return sessionManager;
    }

    public void setLocalVideoManager(ILocalVideoManager localVideoManager) {
        assert this.localVideoManager == null;
        this.localVideoManager = localVideoManager;
    }

    public ILocalVideoManager getLocalVideoManager() {
        assert localVideoManager != null;
        return localVideoManager;
    }

    public void setVideoDetailsProvider(IVideoDetailsProvider videoDetailsProvider) {
        assert this.videoDetailsProvider == null;
        this.videoDetailsProvider = videoDetailsProvider;
    }

    public IVideoDetailsProvider getVideoDetailsProvider() {
        assert videoDetailsProvider != null;
        return videoDetailsProvider;
    }

    public void setTemporaryFileManager(IFileManager temporaryFileManager) {
        assert this.temporaryFileManager == null;
        this.temporaryFileManager = temporaryFileManager;
    }

    public IFileManager getTemporaryFileManager() {
        assert temporaryFileManager != null;
        return temporaryFileManager;
    }

    public void setSettingsManager(ISettingsManager settingsManager) {
        assert this.settingsManager == null;
        this.settingsManager = settingsManager;
    }

    public ISettingsManager getSettingsManager() {
        assert settingsManager != null;
        return settingsManager;
    }

    public void setVideoUploadWrapper(IVideoUploadWrapper videoUploadWrapper) {
        assert this.videoUploadWrapper == null;
        this.videoUploadWrapper = videoUploadWrapper;
    }

    public IVideoUploadWrapper getVideoUploadWrapper() {
        assert videoUploadWrapper != null;
        return videoUploadWrapper;
    }

    public void setServerConfiguration(IServerConfiguration serverConfiguration) {
        assert this.serverConfiguration == null;
        this.serverConfiguration = serverConfiguration;
    }

    public IServerConfiguration getServerConfiguration() {
        assert this.serverConfiguration != null;
        return serverConfiguration;
    }
}
