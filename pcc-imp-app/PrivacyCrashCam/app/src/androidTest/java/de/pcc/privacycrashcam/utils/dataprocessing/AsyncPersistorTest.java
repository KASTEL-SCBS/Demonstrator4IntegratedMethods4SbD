package de.pcc.privacycrashcam.utils.dataprocessing;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;

import de.pcc.privacycrashcam.BaseTest;
import de.pcc.privacycrashcam.data.Metadata;
import de.pcc.privacycrashcam.testUtils.FileUtils;

import static org.junit.Assert.*;

/**
 * Tests the AsyncPersistor class
 *
 * @author Giorgio Gross
 */
public class AsyncPersistorTest extends BaseTest {
    private AsyncPersistor mPersistor;
    private PersistCallback mCallback = new PersistCallback() {
        @Override
        public void onPersistingStarted() {
            calledOnPersistingStarted = true;

            //noinspection StatementWithEmptyBody
            while (!doProceed) {
                // loop until doProceed is true
            }
        }

        @Override
        public void onPersistingStopped(boolean success) {
            resultStoppedPersisting = success;
        }
    };
    private Boolean resultStoppedPersisting = null;
    private boolean calledOnPersistingStarted = false;
    /**
     * Set this to false in order to simulate that the UI threaad needs some time. This will cause
     * the callback not to return immediately and thus the background task will have to wait for
     * this event.
     */
    private boolean doProceed = true;

    @Before
    public void setUp() throws Exception {
        mPersistor = new AsyncPersistor(bufferMock, mCallback);
    }

    @Test
    public void sleepEven() throws Exception {
        Mockito.when(settingsMock.getBufferSizeSec()).thenReturn(10); // even
        mPersistor.execute(metadataMock);
        Thread.sleep(settingsMock.getBufferSizeSec() * 1000 / 2 - 100);
        mPersistor.cancel(true);
        assertFalse(calledOnPersistingStarted);
    }

    @Test
    public void sleepUneven() throws Exception {
        Mockito.when(settingsMock.getBufferSizeSec()).thenReturn(9); // uneven
        mPersistor.execute(metadataMock);
        Thread.sleep(settingsMock.getBufferSizeSec() * 1000 / 2 - 100);
        mPersistor.cancel(true);
        assertFalse(calledOnPersistingStarted);
    }

    @Ignore // will run when executed alone but fail when executed together with all other tests
    @Test
    public void waitForUI() throws Exception {
        doProceed = false;
        mPersistor.execute(metadataMock);
        // sleep slightly longer than the persistor
        Thread.sleep(settingsMock.getBufferSizeSec() * 1000 / 2 + 200);
        // notified the ui
        assertTrue(calledOnPersistingStarted);
        // not ended yet
        assertNull(resultStoppedPersisting);

        Thread.sleep(1000);
        // still not ended
        assertNull(resultStoppedPersisting);

        // release the persistor
        doProceed = true;
    }

    @Test
    public void noMeta() throws Exception {
        Metadata[] array = {null};
        assertFalse(mPersistor.doInBackground(array));
    }

    @Test
    public void noVideoSnippets() throws Exception {
        Mockito.when(bufferMock.demandData()).thenReturn(null);
        assertFalse(mPersistor.doInBackground(metadataMock));
    }

    @Test
    public void onPostExecute() throws Exception {
        // check if callback passes the value without modifying it
        mPersistor.onPostExecute(true);
        assertNotNull(resultStoppedPersisting);
        assertTrue(resultStoppedPersisting);

        mPersistor.onPostExecute(false);
        assertFalse(resultStoppedPersisting);
    }

    @After
    public void tearDown() throws Exception {
        // reset
        calledOnPersistingStarted = false;
        resultStoppedPersisting = null;
    }
}