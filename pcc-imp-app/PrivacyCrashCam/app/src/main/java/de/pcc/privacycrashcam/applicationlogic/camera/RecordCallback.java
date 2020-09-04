package de.pcc.privacycrashcam.applicationlogic.camera;

/**
 * Observes camera recordings. Gets informed about state changes of recording.
 */
public interface RecordCallback {

    /**
     * Called when recording starts.
     */
    void onRecordingStarted();

    /**
     * Called when recording ends.
     */
    void onRecordingStopped();

    /**
     * Called when an error occurs.
     *
     * @param errorMessage User readable error message to be displayed (or ignored..)
     */
    void onError(String errorMessage);
}
