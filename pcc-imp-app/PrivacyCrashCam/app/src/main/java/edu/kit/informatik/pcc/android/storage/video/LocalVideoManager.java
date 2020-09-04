package edu.kit.informatik.pcc.android.storage.video;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import edu.kit.informatik.pcc.core.crypto.IVideoEncryptor;
import edu.kit.informatik.pcc.core.data.IFileHierarchyManager;

public class LocalVideoManager implements ILocalVideoManager, IVideoDetailsProvider {
    private static final String encryptedVideosDirectory = "videos";
    private static final String encryptedMetadataDirectory = "metadata";
    private static final String readableMetadataDirectory = "metadata_readable";
    private static final String encryptedKeysDirectory = "key";

    private IFileHierarchyManager fileHierarchyManager;
    private IVideoEncryptor videoEncryptor;

    public void setFileHierarchyManager(IFileHierarchyManager fileHierarchyManager) {
        assert this.fileHierarchyManager == null;
        this.fileHierarchyManager = fileHierarchyManager;
    }

    public void setVideoEncryptor(IVideoEncryptor videoEncryptor) {
        assert this.videoEncryptor == null;
        this.videoEncryptor = videoEncryptor;
    }

    /**
     * Stores an encrypted version of the video and an encrypted and a readable version of the metadata on disk.
     * @param video The video to store.
     * @param metadata The metadata to store.
     */
    @Override
    public void storeVideo(File video, File metadata) {
        assertCompletelySetup();
        int videoId = unusedVideoId();
        File encryptedVideo = fileHierarchyManager.file(videoId + ".mp4", encryptedVideosDirectory);
        File encryptedMetadata = fileHierarchyManager.file(videoId + ".json", encryptedMetadataDirectory);
        File readableMetadata = fileHierarchyManager.file(videoId + ".json", readableMetadataDirectory);
        File encryptedKey = fileHierarchyManager.file(videoId + ".key", encryptedKeysDirectory);

        byte[] keyData = videoEncryptor.encrypt(video, metadata, encryptedVideo, encryptedMetadata);
        try {
            FileUtils.copyFile(metadata, readableMetadata);
            FileUtils.writeByteArrayToFile(encryptedKey, keyData);
        } catch (IOException e) {
            e.printStackTrace();
            deleteContentForVideo(videoId);
            Logger.getGlobal().warning("failed to store video");
        }
    }

    @Override
    public int[] getLocallyStoredVideoIds() {
        assertCompletelySetup();
        File[] videos = fileHierarchyManager.filesInDirectory(encryptedVideosDirectory);
        List<Integer> videoIdList = new ArrayList<>();
        for (File video: videos) {
            try {
                int videoId = Integer.parseInt(FilenameUtils.removeExtension(video.getName()));
                videoIdList.add(videoId);
            }
            catch (NumberFormatException e) {
                Logger.getGlobal().warning("got video with invalid name " + video);
            }
        }
        int[] videoIds = new int[videoIdList.size()];
        for (int i = 0; i < videoIdList.size(); i++) {
            videoIds[i] = videoIdList.get(i);
        }
        return videoIds;
    }

    @Override
    public void deleteContentForVideo(int videoId) {
        assertCompletelySetup();
        fileHierarchyManager.deleteFile(getEncryptedVideo(videoId));
        fileHierarchyManager.deleteFile(getEncryptedMetadata(videoId));
        fileHierarchyManager.deleteFile(getMetadata(videoId));
        fileHierarchyManager.deleteFile(getEncryptedKeyFile(videoId));
    }

    @Override
    public File getMetadata(int videoId) {
        assertCompletelySetup();
        return fileHierarchyManager.existingFile(videoId + ".json", readableMetadataDirectory);
    }

    @Override
    public File getEncryptedVideo(int videoId) {
        assertCompletelySetup();
        return fileHierarchyManager.existingFile(videoId + ".mp4", encryptedVideosDirectory);
    }

    @Override
    public File getEncryptedMetadata(int videoId) {
        assertCompletelySetup();
        return fileHierarchyManager.existingFile(videoId + ".json", encryptedMetadataDirectory);
    }

    @Override
    public byte[] getEncryptedKey(int videoId) {
        assertCompletelySetup();
        File file = getEncryptedKeyFile(videoId);
        if (file == null) {
            return new byte[0];
        }
        try {
            return FileUtils.readFileToByteArray(file);
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    private File getEncryptedKeyFile(int videoId) {
        return fileHierarchyManager.existingFile(videoId + ".key", encryptedKeysDirectory);
    }

    private int unusedVideoId() {
        int[] videoIds = getLocallyStoredVideoIds();
        int max = 0;
        for (int videoId : videoIds) {
            max = max >= videoId ? max : videoId;
        }
        return max + 1;
    }

    private void assertCompletelySetup() {
        assert fileHierarchyManager != null;
        assert videoEncryptor != null;
    }
}
