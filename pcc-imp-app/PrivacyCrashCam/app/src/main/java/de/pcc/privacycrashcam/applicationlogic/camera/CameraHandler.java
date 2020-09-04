package de.pcc.privacycrashcam.applicationlogic.camera;

import de.pcc.privacycrashcam.data.Metadata;

/**
 * Interface each camera handler needs to implement. Clients will communicate through this interface
 * with the camera handler. The camera handler is then responsible for setting up, maintaining,
 * managing and cleaning up data structures and camera calls.
 * <p>
 * <p>The camera handler has a lifecycle which is related to the lifecycle
 * {@link android.app.Activity Activities} have. This way, activities and UI components can
 * synchronize their life cycle with the camera handler.<br>
 * The camera handlers methods should be called in the following order to make the handler work
 * properly and always make a call to super:
 * <ul>
 * <li>{@link #createHandler()}</li>
 * <li>{@link #resumeHandler()}</li>
 * <li>Now {@link #schedulePersisting()} or {@link #updateMetadata(Metadata)} can be called</li>
 * <li>{@link #pauseHandler()}</li>
 * <li>{@link #destroyHandler()}</li>
 * </ul>
 * After {@link #destroyHandler()} was called you will need to call createHandler again in order to
 * reuse the handler. Note that <b>consecutive calls to the same lifecycle method will have no
 * effect!</b>
 * <p>
 * </p>
 *
 * @author Giorgio Gross
 */
public abstract class CameraHandler {
    private enum State {
        INITIALIZED,
        CREATED,
        RESUMED,
        PAUSED,
        DESTROYED
    }

    private State state = State.INITIALIZED;

    /**
     * Should be called before {@link #resumeHandler()}. Performs memory related preparation tasks
     * or sets up variables which need to be set up once at start up.
     *
     * @throws IllegalStateException If you did not call lifecycle methods in the right order
     */
    public void createHandler() throws IllegalStateException {
        if (state == State.CREATED) return;
        if (state != State.INITIALIZED && state != State.DESTROYED)
            throw new IllegalStateException();
        state = State.CREATED;
    }

    /**
     * Resumes the camera handler. Called when UI becomes visible to the user. Makes camera and
     * recorder available and starts buffering video chunks.
     *
     * @throws IllegalStateException If you did not call lifecycle methods in the right order
     */
    public void resumeHandler() {
        if (state == State.RESUMED) return;
        if (state != State.CREATED && state != State.PAUSED) throw new IllegalStateException();
        state = State.RESUMED;
    }

    /**
     * Updates the camera handlers metadata reference.
     *
     * @param metadata new meta data object
     * @throws NullPointerException If you did not call lifecycle methods in the right order
     */
    abstract void updateMetadata(Metadata metadata) throws NullPointerException;

    /**
     * Tells the camera handler to start recording. The camera handler should decide on his own
     * when to actually persist video data.
     *
     * @throws NullPointerException If you did not call lifecycle methods in the right order
     */
    abstract void schedulePersisting() throws NullPointerException;

    /**
     * Pauses the handler. Called when the UI becomes invisible to the user. Releases camera and
     * recorder and stops buffering video chunks.
     *
     * @throws IllegalStateException If you did not call lifecycle methods in the right order
     */
    public void pauseHandler() throws IllegalStateException {
        if (state == State.PAUSED) return;
        if (state != State.RESUMED) throw new IllegalStateException();
        state = State.PAUSED;
    }

    /**
     * Releases all resources allocated by the handler. The handler gets into an invalid state after
     * making this call and thus cannot be reused.<br>
     * Call {@link #pauseHandler()} if you wish to reuse your handler instance instead.<br>
     * Call {@link #createHandler()} if you want to restore and reuse this camera handler.
     *
     * @throws IllegalStateException If you did not call lifecycle methods in the right order
     */
    public void destroyHandler() throws IllegalStateException {
        if (state == State.DESTROYED) return;
        if (state != State.PAUSED) throw new IllegalStateException();
        state = State.DESTROYED;
    }

}
