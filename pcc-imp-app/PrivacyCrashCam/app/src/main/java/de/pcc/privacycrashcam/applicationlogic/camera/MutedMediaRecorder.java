package de.pcc.privacycrashcam.applicationlogic.camera;

import android.media.CamcorderProfile;
import android.media.MediaRecorder;

/**
 * Media Recorder for recording video only.
 *
 * @author Giorgio
 */
public class MutedMediaRecorder extends MediaRecorder {

    /**
     * Behaves like {@link MediaRecorder MediaRecorder} setProfile Method but does not set audio
     * channels.
     * @param profile Camcorder profile to be set
     */
    @Override
    public void setProfile(CamcorderProfile profile) {
        setOutputFormat(profile.fileFormat);
        setVideoFrameRate(profile.videoFrameRate);
        setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);
        setVideoEncodingBitRate(profile.videoBitRate);
        setVideoEncoder(profile.videoCodec);
    }
}
