package de.pcc.privacycrashcam.applicationlogic.camera;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

import de.pcc.privacycrashcam.BaseTest;

/**
 * Tests if the lifecycle of the compat camera handler is working correctly
 *
 * @author Giorgio Gross
 */
public class CompatCameraHandlerLifecycleTest extends BaseTest {
    private CompatCameraHandler mHandler;
    private RecordCallback mCallback = new RecordCallback() {
        @Override
        public void onRecordingStarted() {
            calledOnRecordingStarted = true;
        }

        @Override
        public void onRecordingStopped() {
            calledOnRecordingStopped = true;
        }

        @Override
        public void onError(String errorMessage) {
            calledOnError = true;
        }
    };
    private boolean calledOnRecordingStarted = false;
    private boolean calledOnRecordingStopped = false;
    private boolean calledOnError = false;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        mHandler = new CompatCameraHandler(context, surfaceViewMock, mCallback);
    }

    @After
    public void tearDown() throws Exception {
        calledOnError = false;
        calledOnRecordingStarted = false;
        calledOnRecordingStopped = false;
    }

    @Test
    public void inOrder() throws Exception {
        mHandler.createHandler();
        mHandler.resumeHandler();
        mHandler.schedulePersisting();
        assertTrue(calledOnRecordingStarted);
        mHandler.pauseHandler();
        mHandler.destroyHandler();
    }

    @Test(expected = NullPointerException.class)
    public void noLifecycle() throws Exception {
        mHandler.schedulePersisting();
    }

    @Test(expected = IllegalStateException.class)
    public void resumeBeforeCreate() throws Exception {
        mHandler.resumeHandler();
    }

    @Test(expected = IllegalStateException.class)
    public void pauseBeforeCreate() throws Exception {
        mHandler.pauseHandler();
    }

    @Test(expected = IllegalStateException.class)
    public void pauseBeforeResume() throws Exception {
        mHandler.createHandler();
        mHandler.pauseHandler();
    }

    @Test(expected = IllegalStateException.class)
    public void destroyBeforeCreate() throws Exception {
        mHandler.destroyHandler();
    }

    @Test(expected = IllegalStateException.class)
    public void destroyBeforeResume() throws Exception {
        mHandler.createHandler();
        mHandler.destroyHandler();
    }

}
