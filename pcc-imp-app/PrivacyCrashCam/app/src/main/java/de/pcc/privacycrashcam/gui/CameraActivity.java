package de.pcc.privacycrashcam.gui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import de.pcc.privacycrashcam.R;
import de.pcc.privacycrashcam.applicationlogic.CameraView;
import de.pcc.privacycrashcam.applicationlogic.camera.CameraHandler;
import de.pcc.privacycrashcam.applicationlogic.camera.RecordCallback;
import de.pcc.privacycrashcam.applicationlogic.camera.TriggeringCompatCameraHandler;

/**
 * Activity showing the camera preview and starting all components necessary to record videos.
 * <p>
 * Sets the {@link CameraView} as content of the
 * {@link MainActivity}
 * </p>
 *
 * @author Giorgio Gross
 */
public class CameraActivity extends MainActivity {

    /* #############################################################################################
     *                                  attributes
     * ###########################################################################################*/

    private final static String TAG = "CAM_ACTIVITY";

    private ImageView statusSymbol;
    private CameraView cameraPreview;
    private CameraHandler mCamHandler;
    private View decorView;

    /* #############################################################################################
     *                                  methods
     * ###########################################################################################*/

    /**
     * Starts a new intent with the {@link CameraActivity CameraActivity}
     *
     * @param calling the activity which is doing this call
     */
    public static void Launch(Activity calling) {
        Intent intent = new Intent(calling, CameraActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        calling.startActivity(intent);
    }

    /**
     * Get the base layout resource for this activity. The layout must contain a toolbar with an id
     * named <b>toolbar</b>.
     * <p>If there is a navigation drawer in the layout it will be displayed. Navigation is handled
     * by the {@link MainActivity MainActivity} class.</p>
     *
     * @return resource id for this activity
     */
    @Override
    public int getLayoutRes() {
        return R.layout.activity_main_drawer_transparenttoolbar;
    }

    /**
     * Get the menu entry which will be highlighted in the drawer. Pass -1 if you don't want to
     * highlight any navigation menu entry.
     *
     * @return R.id.[menu_entry_id]
     */
    @Override
    public int getMenuEntryId() {
        return R.id.nav_camera;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        decorView = getWindow().getDecorView();
        // make activity fullscreen and remove system bar shadows
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        // set camera view as content
        FrameLayout container = (FrameLayout) findViewById(R.id.content_container);
        getLayoutInflater().inflate(R.layout.content_camera, container, true);

        RecordCallback recordCallback = new RecordCallback() {
            @Override
            public void onRecordingStarted() {
                showRecordingSymbol();
            }

            @Override
            public void onRecordingStopped() {
                showViewingSymbol();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        };

        cameraPreview = (CameraView) findViewById(R.id.sv_camera);
        mCamHandler = new TriggeringCompatCameraHandler(getApplicationContext(),
                cameraPreview, recordCallback);
        mCamHandler.createHandler();
        cameraPreview.setOnClickListener((View.OnClickListener) mCamHandler);
        cameraPreview.setCameraHandler(mCamHandler);

        statusSymbol = (ImageView) findViewById(R.id.statusSymbol);
        showViewingSymbol();
        startStatusSymbolAnim();

        // don't show title
        setTitle("");
        // remove drawer shadow
        removeDrawerShadow();
    }

    private void showViewingSymbol() {
        statusSymbol.setImageResource(R.drawable.ic_visibility_black_24dp);
    }

    private void showRecordingSymbol() {
        statusSymbol.setImageResource(R.drawable.ic_videocam_black_24dp);
    }

    private void startStatusSymbolAnim() {
        Animation fadeInOut = new AlphaAnimation(0.0f, 1.0f);
        fadeInOut.setDuration(1500);
        fadeInOut.setStartOffset(0);
        fadeInOut.setRepeatMode(Animation.REVERSE);
        fadeInOut.setRepeatCount(Animation.INFINITE);
        statusSymbol.startAnimation(fadeInOut);
    }


    @Override
    protected void onStart() {
        super.onStart();
        cameraPreview.setVisibility(View.VISIBLE);
        // resuming mCamHandler is done by cameraPreview
    }

    @Override
    protected void onStop() {
        super.onStop();
        // cameraPreview.setVisibility(View.GONE);
        // pausing mCamHandler is done by cameraPreview
    }

    @Override
    protected void onDestroy() {
        mCamHandler.destroyHandler();
        super.onDestroy();
    }

    /**
     * Set Flags to hide system bars
     */
    private void hideSystemUI() {
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY); // enter immersive mode
    }

    /**
     * Removes all the flags except for the ones that make the content appear under the system bars
     */
    private void showSystemUI() {
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideSystemUI();

        // showing System Ui will happen after user the user swipes in from the edges of the screen
        // automatically..
    }
}
