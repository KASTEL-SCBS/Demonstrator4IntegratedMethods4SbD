package de.pcc.privacycrashcam.applicationlogic;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import de.pcc.privacycrashcam.applicationlogic.camera.CameraHandler;

/**
 * Surface view optimized to be used as camera preview.
 *
 * @author Giorgio Gross
 */
public class CameraView extends SurfaceView implements SurfaceHolder.Callback {

    /* #############################################################################################
     *                                  attributes
     * ###########################################################################################*/

    private SurfaceHolder mHolder;
    private CameraHandler cameraHandler;

    private boolean hasBeenShownBefore = false; // set to true after view is shown first time

    /* #############################################################################################
     *                                  constructor
     * ###########################################################################################*/

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    /* #############################################################################################
     *                                  methods
     * ###########################################################################################*/

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created. Wait for surfaceChanged to be called.
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // pause camera handler before making changes
        if(hasBeenShownBefore) cameraHandler.pauseHandler();

        // ... do changes to surface here.
        // this will be necessary if we support landscape and portrait mode views
        // requestLayout();

        // resume camera handler. This will invalidate the camera.
        cameraHandler.resumeHandler();
        hasBeenShownBefore = true;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface has been destroyed. Pause the camera handler
        cameraHandler.pauseHandler();
        hasBeenShownBefore = false;
    }

    public void setCameraHandler(CameraHandler cameraHandler) {
        this.cameraHandler = cameraHandler;
    }
}
