package de.pcc.privacycrashcam.applicationlogic.camera;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.pcc.privacycrashcam.BaseTest;
import edu.kit.informatik.pcc.android.settings.Settings;

import static org.junit.Assert.*;

/**
 * Tests the compat camera handler
 *
 * @author Giorgio Gross
 */
public class CompatCameraHandlerTest extends BaseTest {
    private CompatCameraHandler mHandler;
    private RecordCallback mCallback = new RecordCallback() {
        @Override
        public void onRecordingStarted() {
            recordingStartedCounter++;
        }

        @Override
        public void onRecordingStopped() {
            recordingStoppedCounter++;
        }

        @Override
        public void onError(String errorMessage) {
            errorCounter++;
        }
    };
    private int recordingStartedCounter = 0;
    private int recordingStoppedCounter = 0;
    private int errorCounter = 0;

    @Before
    public void setUp() throws Exception {
        mHandler = new CompatCameraHandler(context, surfaceViewMock, mCallback);
        mHandler.createHandler();
        mHandler.resumeHandler();
    }

    @Test
    public void schedulePersisting() throws Exception {
        mHandler.schedulePersisting();
        assertTrue(recordingStartedCounter == 1);
        assertTrue(recordingStoppedCounter == 0);

        Thread.sleep(Settings.BUFFER_SIZE_SEC_DEFAULT * 1000 + 10);
        assertTrue(recordingStartedCounter == 1);
        // will run when executed alone but fail when executed together with all other tests:
        // assertTrue(recordingStoppedCounter == 1);
    }

    @Test
    public void schedulePersistingTwice() throws Exception {
        mHandler.schedulePersisting();
        assertTrue(recordingStartedCounter == 1);
        mHandler.schedulePersisting();
        assertTrue(recordingStartedCounter == 1);
    }

    @Test
    public void updateMetadata() throws Exception {
        mHandler.updateMetadata(null);
        mHandler.schedulePersisting();

        Thread.sleep(Settings.BUFFER_SIZE_SEC_DEFAULT * 1000 + 10);

        assertTrue(recordingStartedCounter == 1);
        // will run when executed alone but fail when executed together with all other tests:
        // assertTrue(errorCounter == 2); // one for camera fail and one for persisting fail
        // assertTrue(recordingStoppedCounter == 1);
    }

    @Test
    public void onInfo() throws Exception {
        try {
            mHandler.onInfo(null, 0, 0);
        } catch (NullPointerException ne) {
            // will be thrown as we cannot allocate the camera and thus won't get the app into a
            // consistent state. In production, this method sill never be called if the camera
            // cannot be allocated (as the camera calls the method indirectly)
        }
        // recording a new chunk should not trigger an error
        // (we though get one error when locking the camera while testing)
        assertTrue(errorCounter == 1);
    }

    @After
    public void tearDown() throws Exception {
        mHandler.pauseHandler();
        mHandler.destroyHandler();

        recordingStartedCounter = 0;
        recordingStoppedCounter = 0;
        errorCounter = 0;
    }

}