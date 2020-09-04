package de.pcc.privacycrashcam.applicationlogic.camera;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;

import java.io.FileNotFoundException;

import de.pcc.privacycrashcam.R;
import de.pcc.privacycrashcam.data.Metadata;

/**
 * Decorates the CompatCameraHandler so that it triggers recording on its own after recognizing a
 * button click or after measured acceleration force exceeds the set maximum.
 */
public class TriggeringCompatCameraHandler extends CompatCameraHandler implements
        SensorEventListener, View.OnClickListener {
    private final static String TAG = "TRG_CMP_CAM_HANDLER";
    private final static float MAX_G_FORCE = 2.3f;
    private final static float GRAVITY_CONSTANT = 10.0f;

    private float[] accelValues = {0f, 0f, 0f};
    private long lastTap = 0;
    private static final long DOUBLE_TAP_TIME_SPAN = 700; // 700ms to double tap

    /**
     * Creates a new camera handler with the passed parameters and sets up callbacks, camera, media
     * recorder and buffer. Also sets up the sensor.
     *
     * @param context        application context
     * @param previewView    view to be used as preview for the camera
     * @param recordCallback callback to be notified about recording state
     */
    public TriggeringCompatCameraHandler(Context context, SurfaceView previewView,
                                         RecordCallback recordCallback) {
        super(context, previewView, recordCallback);

        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(!sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_FASTEST))
            recordCallback.onError(context.getString(R.string.error_sensor));
    }

    /**
     * Called when sensor values have changed.
     * <p>See {@link SensorManager SensorManager}
     * for details on possible sensor types.
     * <p>See also {@link SensorEvent SensorEvent}.
     * <p>
     * <p><b>NOTE:</b> The application doesn't own the
     * {@link SensorEvent event}
     * object passed as a parameter and therefore cannot hold on to it.
     * The object may be part of an internal pool and may be reused by
     * the framework.
     *
     * @param event the {@link SensorEvent SensorEvent}.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) return;
        if (event.values == null) return;

        /*
        Log.i(TAG, "VAL at 0: " + event.values[0]);
        Log.i(TAG, "VAL at 1: " + event.values[1]);
        Log.i(TAG, "VAL at 2: " + event.values[2]);
        */

        this.accelValues = event.values;

        float maxRawVal = GRAVITY_CONSTANT * MAX_G_FORCE;
        if(event.values[0] > maxRawVal
                || event.values[1] > maxRawVal
                || event.values[2] > maxRawVal) {
            Metadata metadata = new Metadata(System.currentTimeMillis(),
                    Metadata.TRIGGER_TYPE_SENSOR, event.values);
            updateMetadata(metadata);
            schedulePersisting();
        }
    }

    /**
     * Called when the accuracy of the registered sensor has changed.
     * <p>
     * <p>See the SENSOR_STATUS_* constants in
     * {@link SensorManager SensorManager} for details.
     *
     * @param sensor   acceleration sensor
     * @param accuracy The new accuracy of this sensor, one of
     *                 {@code SensorManager.SENSOR_STATUS_*}
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // ignored
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        long tapTime = System.currentTimeMillis();
        if (tapTime - lastTap <= DOUBLE_TAP_TIME_SPAN) {
            // double tap
            Metadata metadata = new Metadata(System.currentTimeMillis(),
                    Metadata.TRIGGER_TYPE_TOUCH, accelValues);
            updateMetadata(metadata);
            schedulePersisting();
        }
        lastTap = tapTime;
    }
}
