package edu.kit.informatik.pcc.android.network;

import java.io.File;

import edu.kit.informatik.pcc.android.account.ISessionManager;
import edu.kit.informatik.pcc.android.storage.video.IVideoDetailsProvider;

public class VideoUploadWrapper implements IVideoUploadWrapper {
    private ISessionManager sessionManager;
    private IVideoDetailsProvider videoDetailsProvider;
    private IClientVideoUpload clientVideoUpload;

    public void setSessionManager(ISessionManager sessionManager) {
        assert this.sessionManager == null;
        this.sessionManager = sessionManager;
    }

    public void setVideoDetailsProvider(IVideoDetailsProvider videoDetailsProvider) {
        assert this.videoDetailsProvider == null;
        this.videoDetailsProvider = videoDetailsProvider;
    }

    public void setClientVideoUpload(IClientVideoUpload clientVideoUpload) {
        assert this.clientVideoUpload == null;
        this.clientVideoUpload = clientVideoUpload;
    }

    @Override
    public void uploadVideo(int videoId, IRequestCompletion<IClientVideoUpload.UploadResult> completion) {
        assertCompletelySetup();
        String authenticationToken = sessionManager.getAuthenticationToken();
        if (authenticationToken == null) {
            completion.onResponse(IClientVideoUpload.UploadResult.UNAUTHENTICATED);
            return;
        }
        File encryptedVideo = videoDetailsProvider.getEncryptedVideo(videoId);
        File encryptedMetadata = videoDetailsProvider.getEncryptedMetadata(videoId);
        byte[] encryptedKeyData = videoDetailsProvider.getEncryptedKey(videoId);
        if (encryptedVideo == null || encryptedMetadata == null || encryptedKeyData == null || encryptedKeyData.length == 0) {
            completion.onResponse(IClientVideoUpload.UploadResult.INPUT_FAILURE);
            return;
        }
        clientVideoUpload.uploadVideo(encryptedVideo, encryptedMetadata, encryptedKeyData, authenticationToken, completion);
    }

    private void assertCompletelySetup() {
        assert sessionManager != null;
        assert videoDetailsProvider != null;
        assert clientVideoUpload != null;
    }
}
