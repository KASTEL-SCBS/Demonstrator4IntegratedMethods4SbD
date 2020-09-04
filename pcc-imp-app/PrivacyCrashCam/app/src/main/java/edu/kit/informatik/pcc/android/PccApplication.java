package edu.kit.informatik.pcc.android;

import android.app.Application;
import android.content.Context;

import java.io.File;

import edu.kit.informatik.pcc.android.account.SessionManager;
import edu.kit.informatik.pcc.android.crypto.PublicKeyProvider;
import edu.kit.informatik.pcc.android.network.ServerConfiguration;
import edu.kit.informatik.pcc.android.network.UserNetworkAdapter;
import edu.kit.informatik.pcc.android.network.VideoNetworkAdapter;
import edu.kit.informatik.pcc.android.network.VideoUploadWrapper;
import edu.kit.informatik.pcc.android.settings.SettingsManager;
import edu.kit.informatik.pcc.android.storage.PreferencesStorage;
import edu.kit.informatik.pcc.android.storage.video.LocalVideoManager;
import edu.kit.informatik.pcc.core.crypto.JavaRSA_AESFileEncryptor;
import edu.kit.informatik.pcc.core.data.FileSystemManager;

public class PccApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        setupComponents();
    }

    private void setupComponents() {
        Context context = getApplicationContext();
        String temporaryFilesDir = context.getFilesDir().getAbsolutePath() + File.separator + "tmp";
        deleteDirectoryAndItsContent(new File(temporaryFilesDir));

        PreferencesStorage prefStorage = new PreferencesStorage(context);
        FileSystemManager temporaryFileManager = new FileSystemManager(temporaryFilesDir);
        FileSystemManager videoFilesManager = new FileSystemManager(context.getFilesDir().getAbsolutePath() + File.separator + "video_data");

        Client client = new Client();

        SessionManager sessionManager = new SessionManager();
        sessionManager.setSimpleValueStorage(prefStorage);
        client.setSessionManager(sessionManager);

        LocalVideoManager localVideoManager = new LocalVideoManager();
        localVideoManager.setFileHierarchyManager(videoFilesManager);
        client.setLocalVideoManager(localVideoManager);
        client.setVideoDetailsProvider(localVideoManager);
        sessionManager.setLocalVideoManager(localVideoManager);

        JavaRSA_AESFileEncryptor videoEncryptor = new JavaRSA_AESFileEncryptor();
        localVideoManager.setVideoEncryptor(videoEncryptor);

        PublicKeyProvider publicKeyProvider = new PublicKeyProvider(context);
        videoEncryptor.setPublicKeyProvider(publicKeyProvider);

        client.setTemporaryFileManager(temporaryFileManager);

        SettingsManager settingsManager = new SettingsManager();
        settingsManager.setSimpleValueStorage(prefStorage);
        client.setSettingsManager(settingsManager);

        ServerConfiguration serverConfiguration = new ServerConfiguration();
        client.setServerConfiguration(serverConfiguration);

        UserNetworkAdapter userNetworkAdapter = new UserNetworkAdapter();
        userNetworkAdapter.setServerConfiguration(serverConfiguration);
        sessionManager.setUserManagement(userNetworkAdapter);

        VideoUploadWrapper videoUploadWrapper = new VideoUploadWrapper();
        videoUploadWrapper.setSessionManager(sessionManager);
        videoUploadWrapper.setVideoDetailsProvider(localVideoManager);
        client.setVideoUploadWrapper(videoUploadWrapper);

        VideoNetworkAdapter videoNetworkAdapter = new VideoNetworkAdapter();
        videoNetworkAdapter.setServerConfiguration(serverConfiguration);
        videoUploadWrapper.setClientVideoUpload(videoNetworkAdapter);

        Client.setGlobal(client);
    }

    private static void deleteDirectoryAndItsContent(File dir) {
        if (dir == null || !dir.exists()) {
            return;
        }
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                deleteDirectoryAndItsContent(file);
            }
            else {
                file.delete();
            }
        }
        dir.delete();
    }
}
