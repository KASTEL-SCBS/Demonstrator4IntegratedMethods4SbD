package de.pcc.privacycrashcam.applicationlogic;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import de.pcc.privacycrashcam.R;
import edu.kit.informatik.pcc.android.Client;
import edu.kit.informatik.pcc.android.settings.Settings;
import de.pcc.privacycrashcam.gui.LogInActivity;

import static android.content.ContentValues.TAG;

/**
 * Shows the settings view along with its view components and handles user input.
 *
 * @author Giorgio Gross, David Laubenstein
 */
public class SettingsFragment extends Fragment {

    /* #############################################################################################
     *                                  attributes
     * ###########################################################################################*/

    private Settings settings;

    private TextView fps;
    private TextView bufferSize;
    private SeekBar fpsBar;
    private Button res_High;
    private Button res_Med;
    private Button res_Low;
    private Button b_incBuffer;
    private Button b_decBuffer;
    private Button logOut;

    /**
     * this is the size, the + and - which increase/decrease the buffer size change the value
     */
    private static final int BUFFER_CHUNK_SIZE = 5;
    private static final int BUFFER_SIZE_MIN = 5;
    private static final int BUFFER_SIZE_MAX = 120;

    /* #############################################################################################
     *                                  methods
     * ###########################################################################################*/

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // get the main layout describing the content
        RelativeLayout base = (RelativeLayout) inflater.inflate(R.layout.content_settings, container, false);

        settings = Client.getGlobal().getSettingsManager().loadSettings();
        // init view components

        /**
         * resolution handling
         * onClickListener resHandler, which changes quality
         */
        View.OnClickListener resHandler = new View.OnClickListener() {
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.tv_resHigh:
                        settings.setQuality(Settings.QUALITY_HIGH);
                        resetButtonColors();
                        res_High.setTextColor(
                                ContextCompat.getColor(getContext(), R.color.colorAccent));
                        break;
                    case R.id.tv_resMed:
                        settings.setQuality(Settings.QUALITY_MEDIUM);
                        resetButtonColors();
                        res_Med.setTextColor(
                                ContextCompat.getColor(getContext(), R.color.colorAccent));
                        break;
                    case R.id.tv_resLow:
                        settings.setQuality(Settings.QUALITY_LOW);
                        resetButtonColors();
                        res_Low.setTextColor(
                                ContextCompat.getColor(getContext(), R.color.colorAccent));
                        break;
                    default:
                        System.out.println("default");
                        break;
                }
            }
        };


        res_High = (Button) base.findViewById(R.id.tv_resHigh);
        res_High.setOnClickListener(resHandler);
        res_Med = (Button) base.findViewById(R.id.tv_resMed);
        res_Med.setOnClickListener(resHandler);
        res_Low = (Button) base.findViewById(R.id.tv_resLow);
        res_Low.setOnClickListener(resHandler);

        // resets the colors of the resolution buttons
        resetButtonColors();
        // get the actual quality saved in the settings and set this as the active
        // button in the view
        switch (settings.getQuality()) {
            case Settings.QUALITY_DEFAULT:
                res_Med.setTextColor(
                        ContextCompat.getColor(getContext(), R.color.colorAccent));
                break;
            case Settings.QUALITY_HIGH:
                res_High.setTextColor(
                        ContextCompat.getColor(getContext(), R.color.colorAccent));
                break;
            case Settings.QUALITY_LOW:
                res_Low.setTextColor(
                        ContextCompat.getColor(getContext(), R.color.colorAccent));
                break;
            default:
                Log.d(TAG, "No default Quality set");
                break;
        }

        /**
         * frames handling
         */
        fpsBar = (SeekBar) base.findViewById(R.id.seekBar);
        fpsBar.setProgress(settings.getFps() - 10);
        fps = (TextView) base.findViewById(R.id.tv_frames);
        fps.setText(Integer.toString(settings.getFps()));
        fpsBar.requestLayout();
        fpsBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // fps should be between 20 and 60
                // there is no min value for progress, so we have to set the value + the min value
                // and add this to the max value
                int fps_Int = progress + 10;
                settings.setFps(fps_Int);
                fps.setText(Integer.toString(fps_Int));
            }
        });

        /**
         * buffer handling
         */

        View.OnClickListener bufferHandler = new View.OnClickListener() {

            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.b_incBuffer:
                        if ((settings.getBufferSizeSec() + BUFFER_CHUNK_SIZE) <= BUFFER_SIZE_MAX) {
                            settings.setBufferSizeSec(settings.getBufferSizeSec() + BUFFER_CHUNK_SIZE);
                        }
                        break;
                    case R.id.b_decBuffer:
                        if ((settings.getBufferSizeSec() - BUFFER_CHUNK_SIZE) > BUFFER_SIZE_MIN) {
                            settings.setBufferSizeSec(settings.getBufferSizeSec() -
                                    BUFFER_CHUNK_SIZE);
                        }
                        break;
                    default:
                        System.out.println("default");
                        break;
                }
                bufferSize.setText(String.format(
                        getString(R.string.bufferSizeValue), settings.getBufferSizeSec()));
            }
        };

        b_incBuffer = (Button) base.findViewById(R.id.b_incBuffer);
        b_incBuffer.setOnClickListener(bufferHandler);
        b_decBuffer = (Button) base.findViewById(R.id.b_decBuffer);
        b_decBuffer.setOnClickListener(bufferHandler);

        // get the actual buffer size saved in the settings and set this value in the view
        bufferSize = (TextView) base.findViewById(R.id.tv_bufferSize);
        bufferSize.setText(
                String.format(getString(R.string.bufferSizeValue), settings.getBufferSizeSec()));

        // logout
        logOut = (Button) base.findViewById(R.id.logOut);
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Client.getGlobal().getSessionManager().logout();
                LogInActivity.Launch(getActivity());
            }
        });
        return base;
    }

    /**
     * resets the resolution buttons to default color
     */
    private void resetButtonColors() {
        res_High.setTextColor(ContextCompat.getColor(getContext(), R.color.buttonColorDefault));
        res_Med.setTextColor(ContextCompat.getColor(getContext(), R.color.buttonColorDefault));
        res_Low.setTextColor(ContextCompat.getColor(getContext(), R.color.buttonColorDefault));
    }

    /**
     * if settingsFragment is paused, the settings will be saved, so that the settings
     * will be the same after reopening the settings fragment
     */
    @Override
    public void onPause() {
        super.onPause();
        Client.getGlobal().getSettingsManager().storeSettings(settings);
    }
}
