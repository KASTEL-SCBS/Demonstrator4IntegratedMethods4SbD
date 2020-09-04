package de.pcc.privacycrashcam.utils.dataprocessing;

/**
 * Observes persisting of files. Gets notified about state changes of persisting.
 *
 * @author Giorgio Gross
 */
public interface PersistCallback {

    /**
     * Called when persisting starts
     */
    void onPersistingStarted();

    /**
     * Called when persisting stops.
     *
     * @param success true if video was persisted, false if there was an error
     */
    void onPersistingStopped(boolean success);

}
