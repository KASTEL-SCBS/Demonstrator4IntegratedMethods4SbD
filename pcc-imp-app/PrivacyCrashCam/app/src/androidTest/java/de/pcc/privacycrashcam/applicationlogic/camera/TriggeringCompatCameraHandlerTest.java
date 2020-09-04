package de.pcc.privacycrashcam.applicationlogic.camera;

import android.view.View;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import de.pcc.privacycrashcam.BaseTest;

import static org.junit.Assert.*;

/**
 * Tests the triggering compat camera handler
 *
 * @author Giorgio Gross
 */
public class TriggeringCompatCameraHandlerTest extends BaseTest {
    private TriggeringCompatCameraHandler mHandler;
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

    @Before
    public void setUp() throws Exception {
        mHandler = new TriggeringCompatCameraHandler(context, surfaceViewMock, mCallback);
        mHandler.createHandler();
        mHandler.resumeHandler();
    }

    // todo Use reflection to mock SensorEvent class and to access private field "values"
    @Test
    @Ignore
    public void onSensorChangedNull() throws Exception {
        // event.values = null;
        mHandler.onSensorChanged(event);
        assertFalse(calledOnRecordingStarted);
    }

    @Test
    @Ignore
    public void onSensorChanged1G() throws Exception {
        float[] gData = {9f, 0f, 0f};
        // event.values = gData;
        mHandler.onSensorChanged(event);
        assertFalse(calledOnRecordingStarted);

        gData = new float[]{0f, 9f, 0f};
        // event.values = gData;
        mHandler.onSensorChanged(event);
        assertFalse(calledOnRecordingStarted);

        gData = new float[]{0f, 0f, 9f};
        // event.values = gData;
        mHandler.onSensorChanged(event);
        assertFalse(calledOnRecordingStarted);
    }

    @Test
    @Ignore
    public void onSensorChanged3G() throws Exception {
        float[] gData = {30f, 0f, 0f};
        // event.values = gData;
        mHandler.onSensorChanged(event);
        assertTrue(calledOnRecordingStarted);

        gData = new float[]{0f, 30f, 0f};
        // event.values = gData;
        mHandler.onSensorChanged(event);
        assertTrue(calledOnRecordingStarted);

        gData = new float[]{0f, 0f, 30f};
        // event.values = gData;
        mHandler.onSensorChanged(event);
        assertTrue(calledOnRecordingStarted);
    }

    @Test
    public void onClickSingle() throws Exception {
        mHandler.onClick(new View(context));
        assertFalse(calledOnRecordingStarted);
    }

    @Test
    public void onClickDelayed() throws Exception {
        mHandler.onClick(new View(context));
        Thread.sleep(701);
        mHandler.onClick(new View(context));
        assertFalse(calledOnRecordingStarted);
    }

    @Test
    public void onClickDouble() throws Exception {
        mHandler.onClick(new View(context));
        Thread.sleep(200);
        mHandler.onClick(new View(context));
        assertTrue(calledOnRecordingStarted);
    }

    @After
    public void tearDown() throws Exception {
        mHandler.pauseHandler();
        mHandler.destroyHandler();

        calledOnRecordingStarted = false;
        calledOnRecordingStopped = false;
        calledOnError = false;
    }

}