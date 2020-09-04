package edu.kit.informatik.pcc.android.settings;

import android.media.CamcorderProfile;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class to store user settings.
 *
 * @author Giorgio Gross, David Laubenstein, Josh Romanowski
 * Created by Giorgio Gross at 01/20/2017
 */
public class Settings {

    // JSON keys
    public static final String SETTINGS_MAIN_KEY = "SETTINGS";
    public static final String JSON_KEY_FPS = "fps";
    public static final String JSON_KEY_BUFFER_SIZE_SEC = "bufferSizeSec";
    public static final String JSON_KEY_QUALITY = "quality";

    // default values
    public static final int FPS_DEFAULT = 10;
    public static final int BUFFER_SIZE_SEC_DEFAULT = 10;
    public static final int QUALITY_DEFAULT = CamcorderProfile.QUALITY_480P;
    public static final int QUALITY_HIGH = CamcorderProfile.QUALITY_720P;
    public static final int QUALITY_MEDIUM = CamcorderProfile.QUALITY_480P;
    public static final int QUALITY_LOW = CamcorderProfile.QUALITY_LOW;
    private final static String TAG = Settings.class.getName();

    /* #############################################################################################
     *                                  attributes
     * ###########################################################################################*/

    /**
     * Framerate of the videorecording.
     */
    private int fps;
    /**
     * Size of the ringbuffer in seconds.
     */
    private int bufferSizeSec;
    /**
     * Quality of the video recording. Uses the CamcorderProfile.QUALITY values.
     */
    private int quality;

    /* #############################################################################################
     *                                  constructors
     * ###########################################################################################*/

    /**
     * Creates new settings with default values.
     */
    public Settings() {
        this(FPS_DEFAULT, BUFFER_SIZE_SEC_DEFAULT, QUALITY_DEFAULT);
    }

    /**
     * Creates a settings instance with the passed parameters
     *
     * @param fps frames per seconds
     * @param bufferSizeSec size of the buffer in seconds
     * @param quality the quality. See {@link android.media.CamcorderProfile CamcorderProfile} for
     *                quality options
     */
    public Settings (int fps, int bufferSizeSec, int quality) {
        this.fps = fps;
        this.bufferSizeSec = bufferSizeSec;
        this.quality = quality;
    }

    /**
     * Created a settings instance from the passed json string
     *
     * @param jSettings settings in JSON string
     */
    public Settings (String jSettings) throws JSONException {
        JSONObject mJsonSettings = new JSONObject(jSettings);
        this.fps = mJsonSettings.getInt(JSON_KEY_FPS);
        this.bufferSizeSec = mJsonSettings.getInt(JSON_KEY_BUFFER_SIZE_SEC);
        this.quality = mJsonSettings.getInt(JSON_KEY_QUALITY);
    }

    /* #############################################################################################
     *                                  methods
     * ###########################################################################################*/

    /**
     * Returns the Settings object as JSON string
     *
     * @return Settings as JSON string or the default settings if there was an error
     */
    public String getAsJSON() {
        JSONObject json = new JSONObject();
        try {
            json.put(JSON_KEY_FPS, this.fps);
            json.put(JSON_KEY_BUFFER_SIZE_SEC, this.bufferSizeSec);
            json.put(JSON_KEY_QUALITY, this.quality);
        } catch (JSONException e) {
            Log.w(TAG, "Error creating settings json");
        }
        return json.toString();
    }

    /* #############################################################################################
     *                                  getter/ setter
     * ###########################################################################################*/

    public int getFps() {
        return fps;
    }

    public void setFps(int fps) {
        this.fps = fps;
    }

    public int getBufferSizeSec() {
        return bufferSizeSec;
    }

    public void setBufferSizeSec(int bufferSizeSec) {
        this.bufferSizeSec = bufferSizeSec;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }
}
