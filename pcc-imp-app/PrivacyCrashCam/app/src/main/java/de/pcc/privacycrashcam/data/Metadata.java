package de.pcc.privacycrashcam.data;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Data container storing the metadata to the video recording.
 *
 * @author David Laubenstein, Giorgio Groß, Josh Romanowski
 */
public class Metadata {

    // File pre- and suffixes
    public static final String PREFIX = "META_";
    public static final String PREFIX_READABLE = "META_R_";
    public static final String SUFFIX = "json";
    // trigger type constants
    public final static String TRIGGER_TYPE_DEFAULT = "NONE";
    public final static String TRIGGER_TYPE_SENSOR = "SENSOR_INPUT";
    public final static String TRIGGER_TYPE_TOUCH = "TOUCH_INPUT";
    private final static String TAG = Metadata.class.getName();
    // JSON keys
    private final static String JSON_KEY_DATE = "date";
    private final static String JSON_KEY_TRIGGER_TYPE = "triggerType";
    private final static String JSON_KEY_TRIGGER_FORCE_X = "triggerForceX";
    private final static String JSON_KEY_TRIGGER_FORCE_Y = "triggerForceY";
    private final static String JSON_KEY_TRIGGER_FORCE_Z = "triggerForceZ";

    /* #############################################################################################
     *                                  attributes
     * ###########################################################################################*/

    /**
     * Date of the video recording trigger.
     */
    private long date;
    /**
     * How the video recording was triggered.
     */
    private String triggerType;
    /**
     * G-Force values in the moment the recording is triggered.
     */
    private float[] gForce = new float[3];

    /* #############################################################################################
     *                                  constructor
     * ###########################################################################################*/

    /**
     * Default constructor. Will initialize default values.
     */
    public Metadata() {
        this(System.currentTimeMillis(), TRIGGER_TYPE_DEFAULT, new float[]{0, 0, 0});
    }

    /**
     * Creates new metadata from given values.
     *
     * @param date        Date of the recording.
     * @param triggerType Trigger type of the recording.
     * @param gForce      GForce when triggering.
     */
    public Metadata(long date, String triggerType, float[] gForce) {
        this.date = date;
        this.triggerType = triggerType;
        this.gForce = gForce;
    }

    /**
     * Creates new metadata with values read from a json string.
     *
     * @param json JSON string that contains
     * @throws JSONException
     */
    public Metadata(String json) throws JSONException {
        JSONObject metadata = new JSONObject(json);

        // retrieve json data
        this.date = metadata.getLong(JSON_KEY_DATE);
        this.triggerType = metadata.getString(JSON_KEY_TRIGGER_TYPE);
        this.gForce[0] = (float) metadata.getDouble(JSON_KEY_TRIGGER_FORCE_X);
        this.gForce[1] = (float) metadata.getDouble(JSON_KEY_TRIGGER_FORCE_Y);
        this.gForce[2] = (float) metadata.getDouble(JSON_KEY_TRIGGER_FORCE_Z);
    }

    public Metadata(File metaFile) throws JSONException, IOException {
        String json;
        // read file
        if (metaFile != null) {
            InputStream inputStream = new FileInputStream(metaFile);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String ret;
            StringBuilder stringBuilder = new StringBuilder();

            while ((ret = bufferedReader.readLine()) != null) {
                stringBuilder.append(ret);
            }

            inputStream.close();
            inputStreamReader.close();
            json = stringBuilder.toString();
            JSONObject metadata = new JSONObject(json);

            // retrieve json data
            this.date = metadata.getLong(JSON_KEY_DATE);
            this.triggerType = metadata.getString(JSON_KEY_TRIGGER_TYPE);
            this.gForce[0] = (float) metadata.getDouble(JSON_KEY_TRIGGER_FORCE_X);
            this.gForce[1] = (float) metadata.getDouble(JSON_KEY_TRIGGER_FORCE_Y);
            this.gForce[2] = (float) metadata.getDouble(JSON_KEY_TRIGGER_FORCE_Z);
        } else {

            // retrieve json data
            this.date = System.currentTimeMillis();
            this.triggerType = TRIGGER_TYPE_DEFAULT;
            this.gForce[0] = 0F;
            this.gForce[1] = 0F;
            this.gForce[2] = 0F;
        }
    }

    /* #############################################################################################
     *                                  getter/ setter
     * ###########################################################################################*/

    public long getDate() {
        return date;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public float[] getgForce() {
        return gForce;
    }

    /**
     * returns JSON String of metadata info
     *
     * @return json String
     */
    public String getAsJSON() {
        JSONObject json = new JSONObject();
        try {
            json.put(JSON_KEY_DATE, this.date);
            json.put(JSON_KEY_TRIGGER_TYPE, this.triggerType);
            json.put(JSON_KEY_TRIGGER_FORCE_X, this.gForce[0]);
            json.put(JSON_KEY_TRIGGER_FORCE_Y, this.gForce[1]);
            json.put(JSON_KEY_TRIGGER_FORCE_Z, this.gForce[2]);
        } catch (JSONException e) {
            Log.w(TAG, "Error creating metadata json");
        }
        return json.toString();
    }
}
