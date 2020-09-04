package edu.kit.informatik.pcc.android.storage.video;

import java.io.File;

public interface ILocalVideoManager {
    void storeVideo(File video, File metadata);
    int[] getLocallyStoredVideoIds();
    void deleteContentForVideo(int videoId);
    File getMetadata(int videoId);
}
