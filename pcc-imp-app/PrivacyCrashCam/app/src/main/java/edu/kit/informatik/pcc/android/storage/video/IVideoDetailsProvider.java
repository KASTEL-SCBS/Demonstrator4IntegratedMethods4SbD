package edu.kit.informatik.pcc.android.storage.video;

import java.io.File;

public interface IVideoDetailsProvider {
    File getEncryptedVideo(int videoId);
    File getEncryptedMetadata(int videoId);
    byte[] getEncryptedKey(int videoId);
}
