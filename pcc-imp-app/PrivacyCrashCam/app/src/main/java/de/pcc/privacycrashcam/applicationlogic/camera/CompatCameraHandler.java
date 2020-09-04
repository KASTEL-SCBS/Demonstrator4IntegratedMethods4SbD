package de.pcc.privacycrashcam.applicationlogic.camera;

import android.content.Context;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import de.pcc.privacycrashcam.R;
import de.pcc.privacycrashcam.data.Metadata;
import edu.kit.informatik.pcc.android.settings.Settings;
import de.pcc.privacycrashcam.data.Video;
import de.pcc.privacycrashcam.utils.dataprocessing.AsyncPersistor;
import de.pcc.privacycrashcam.utils.dataprocessing.PersistCallback;
import de.pcc.privacycrashcam.utils.datastructures.VideoRingBuffer;
import edu.kit.informatik.pcc.android.Client;

import static android.content.Context.WINDOW_SERVICE;

/**
 * Camera handler which uses the old Camera API. Handles access to the camera, displaying the
 * preview and writing the buffer. Also manages persisting the buffer's content.
 *
 * @author Giorgio Gross
 */
@SuppressWarnings("deprecation")
public class CompatCameraHandler extends CameraHandler implements MediaRecorder.OnInfoListener {
    private final static String TAG = "CMP_CAM_HANDLER";
    private final static int VIDEO_CHUNK_LENGTH = 5; // length of video chunks in seconds

    private Camera camera = null;
    private CamcorderProfile camcorderProfile = null;
    private MediaRecorder mediaRecorder = null;

    private boolean isHandlerRunning = false;
    private boolean isRecording = false;
    private boolean canOperate = true;

    private Context context;
    private SurfaceView previewView;

    private Settings settings;
    private Metadata metadata;

    private RecordCallback recordCallback;
    private File currentOutputFile;
    private VideoRingBuffer videoRingBuffer;

    private PersistCallback persistCallback;

    /**
     * Creates a new camera handler with the passed parameters and sets up callbacks, camera, media
     * recorder and buffer.
     *
     * @param context        application context
     * @param previewView    view to be used as preview for the camera
     * @param recordCallback callback to be notified about recording state
     */
    public CompatCameraHandler(Context context, SurfaceView previewView,
                               RecordCallback recordCallback) {
        this.context = context;
        this.previewView = previewView;
        this.recordCallback = recordCallback;

        // get notified about state changes during persisting a video
        this.persistCallback = new PersistCallback() {
            @Override
            public void onPersistingStarted() {
                // update UI
                CompatCameraHandler.this.recordCallback.onRecordingStopped();

                // save current file and set up new one
                forceStopMediaRecorder();
                setUpBuffer();
                // restart media recorder to force the use of a new file
                restartMediaRecorder();
            }

            @Override
            public void onPersistingStopped(boolean success) {
                if (!success) CompatCameraHandler.this.recordCallback.onError(
                        CompatCameraHandler.this.context.getString(R.string.error_recorder));

                // allow user to save new video (Multiple async tasks are not allowed to run)
                isRecording = false;
            }
        };
    }

    private void setUpBuffer() {
        // +1 capacity to record at least the desired video length
        int bufferCapacity = settings.getBufferSizeSec() / VIDEO_CHUNK_LENGTH + 1;


        File someTempFile = Client.getGlobal().getTemporaryFileManager().file(UUID.randomUUID().toString());
        someTempFile.delete();
        someTempFile.mkdir();
        this.videoRingBuffer = new VideoRingBuffer(bufferCapacity,
                someTempFile, Video.SUFFIX);
    }

    /**
     * Sets all presets and settings applying to the camcorder profile. Camcorder profile needs to
     * be set up only once and can be reused later.
     */
    private void setUpCamcorderProfile() {
        camcorderProfile = CamcorderProfile.get(settings.getQuality());

        // Set camcorder profile's video width, height, fps which will be applied to the
        // mediaRecorder by MediaRecorder.setProfile(..);
        camcorderProfile.fileFormat = MediaRecorder.OutputFormat.MPEG_4;
    }

    /**
     * Sets up the camera with respect to the user's settings
     */
    private boolean prepareCamera() {
        Log.d(TAG, "preparing camera");
        if (!CameraHelper.hasCameraHardware(context)) {
            recordCallback.onError(context.getResources().getString(R.string.error_no_camera));
            return false;
        }

        // set up camera
        camera = getCameraInstance();
        Log.d(TAG, "cam instance " + camera);
        if (camera == null) {
            // camera was not available
            recordCallback.onError(context.getResources().
                    getString(R.string.error_camera_unavailable));
            return false;
        }

        // set up camera parameters
        Camera.Parameters cameraParameters = camera.getParameters();

        // choose suitable fps rate
        int desiredFps = settings.getFps();
        List<int[]> fpsRanges = cameraParameters.getSupportedPreviewFpsRange();
        camcorderProfile.videoFrameRate = (int) Math.floor((double) fpsRanges.get(0)[Camera.Parameters.PREVIEW_FPS_MAX_INDEX] / (double) 1000);
        for (int[] range : fpsRanges) {
            int min = (int) Math.ceil((double) range[Camera.Parameters.PREVIEW_FPS_MIN_INDEX] / (double) 1000);
            int max = (int) Math.floor((double) range[Camera.Parameters.PREVIEW_FPS_MAX_INDEX] / (double) 1000);
            if (min <= desiredFps && max >= desiredFps) {
                camcorderProfile.videoFrameRate = desiredFps;
                break;
            }
        }

        // choose best video preview size
        List<Camera.Size> mSupportedPreviewSizes = cameraParameters.getSupportedPreviewSizes();
        List<Camera.Size> mSupportedVideoSizes = cameraParameters.getSupportedVideoSizes();

        // pay attention to screen orientation
        Camera.CameraInfo info = CameraHelper.getDefaultBackFacingCameraInfo();
        Display display = ((WindowManager) context.getSystemService(WINDOW_SERVICE))
                .getDefaultDisplay();
        Camera.Size optimalSize;
        int deviceAngle = 0;
        switch (display.getRotation()) {
            case Surface.ROTATION_0:
                optimalSize = CameraHelper.getOptimalVideoSize(mSupportedVideoSizes,
                        mSupportedPreviewSizes, previewView.getWidth(), previewView.getHeight());
                cameraParameters.setPreviewSize(optimalSize.width, optimalSize.height);
                deviceAngle = 0;
                break;
            case Surface.ROTATION_90:
                optimalSize = CameraHelper.getOptimalVideoSize(mSupportedVideoSizes,
                        mSupportedPreviewSizes, previewView.getHeight(), previewView.getWidth());
                cameraParameters.setPreviewSize(optimalSize.width, optimalSize.height);
                deviceAngle = 90;
                break;
            case Surface.ROTATION_180:
                optimalSize = CameraHelper.getOptimalVideoSize(mSupportedVideoSizes,
                        mSupportedPreviewSizes, previewView.getWidth(), previewView.getHeight());
                cameraParameters.setPreviewSize(optimalSize.width, optimalSize.height);
                deviceAngle = 180;
                break;
            case Surface.ROTATION_270:
                optimalSize = CameraHelper.getOptimalVideoSize(mSupportedVideoSizes,
                        mSupportedPreviewSizes, previewView.getHeight(), previewView.getWidth());
                cameraParameters.setPreviewSize(optimalSize.width, optimalSize.height);
                deviceAngle = 270;
                break;
        }
        camera.setDisplayOrientation((info.orientation - deviceAngle + 360) % 360);
        camera.setParameters(cameraParameters);

        try {
            camera.setPreviewDisplay(previewView.getHolder());
            camera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
            e.printStackTrace();
            // Do not send error message to UI.
            // We might be experiencing a bug if we reach this part.
            return false;
        }
        return true;
    }

    /**
     * Get an instance of the Camera object.
     *
     * @return the camera or null if the camera is unavailable
     */
    @Nullable
    private Camera getCameraInstance() {
        if (camera != null) return camera;
        // returns null if camera is unavailable
        return CameraHelper.getDefaultBackFacingCameraInstance();
    }

    /**
     * Stops the preview and releases the camera so that other applications can use it.
     */
    private void releaseCamera() {
        Log.d(TAG, "releasing camera");
        if (camera == null) return;

        camera.stopPreview();
        camera.release();
        camera = null;
    }

    /**
     * Sets up the media recorder with respect to the user's settings
     */
    private boolean prepareMediaRecorder() {
        // MutedMediaRecorder(); is not suitable for some devices
        mediaRecorder = new MediaRecorder();

        camera.unlock();
        mediaRecorder.setCamera(camera);

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setProfile(camcorderProfile);

        // get new file and add it to buffer and media recorder
        currentOutputFile = videoRingBuffer.newVideoFile();
        mediaRecorder.setOutputFile(currentOutputFile.getPath());

        mediaRecorder.setMaxDuration(VIDEO_CHUNK_LENGTH * 1000);
        mediaRecorder.setOrientationHint(90);
        mediaRecorder.setOnInfoListener(this);

        try {
            // this can be put in an async task if performance turns out to be poor
            mediaRecorder.prepare();
            Log.d(TAG, "Media Recorder is prepared");
        } catch (IOException e) {
            e.printStackTrace();
            recordCallback.onError(context.getResources().getString(R.string.error_recorder));
            return false;
        } catch (IllegalStateException e) {
            e.printStackTrace();
            recordCallback.onError(context.getResources().getString(R.string.error_undefined));
            pauseHandler();
            return false;
        }
        return true;
    }

    /**
     * Releases the media recorder and locks the camera
     */
    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            // Clear recorder configuration.
            mediaRecorder.reset();
            // Release the recorder object
            mediaRecorder.release();
            mediaRecorder = null;
        }
        if (camera != null) {
            Log.d(TAG, "LOCKING CAMERA");
            camera.lock();
        }
    }

    /**
     * Starts recording and writing into buffer
     *
     * @return true if starting succeeded
     */
    private boolean startRecordingChunk() {
        try {
            mediaRecorder.start();
        } catch (RuntimeException e) {
            // On some devices this exception is unexpectedly thrown when the user selected a
            // certain fps value. The catch block will make sure that our app doesn't bother
            // other apps if it crashes as we ensure by this routine to release the camera on crash
            e.printStackTrace();
            pauseHandler();
            recordCallback.onError(context.getResources().getString((R.string.error_undefined)));
            return false;
        }

        return true;
    }

    /**
     * Stops recording and writing into buffer
     */
    private void stopRecordingChunk() throws IllegalStateException {
        if (mediaRecorder != null) mediaRecorder.stop();
    }

    @Override
    public void createHandler() {
        super.createHandler();

        // Load and apply settings
        this.settings = Client.getGlobal().getSettingsManager().loadSettings();
        setUpBuffer();
        setUpCamcorderProfile();

        // avoid NPE's if a client forgets to set the metadata
        this.metadata = new Metadata();
    }

    @Override
    public void resumeHandler() {
        super.resumeHandler();
        if (!canOperate || isHandlerRunning) return;

        // take care of setting up camera, media recorder and recording
        if (!prepareCamera() || !prepareMediaRecorder() || !startRecordingChunk()) {
            pauseHandler();
            return;
        }
        isHandlerRunning = true;
    }

    @Override
    public void schedulePersisting() {
        // don't start recording if we already record
        if (isRecording) return;
        isRecording = true;

        recordCallback.onRecordingStarted();

        // create async task to persist the buffer
        AsyncPersistor mPersistor = new AsyncPersistor(videoRingBuffer,
                persistCallback);
        mPersistor.execute(metadata);
    }


    @Override
    public void updateMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public void pauseHandler() {
        super.pauseHandler();
        if (isHandlerRunning) {
            // take care of stopping recording
            forceStopMediaRecorder();
        }
        isHandlerRunning = false;
        // take care of stopping preview
        releaseMediaRecorder();
        releaseCamera();
    }

    @Override
    public void destroyHandler() {
        super.destroyHandler();
        tearDownBuffer();
    }

    private void tearDownBuffer() {
        videoRingBuffer.destroy();
        Client.getGlobal().getTemporaryFileManager().deleteFile(currentOutputFile);
        currentOutputFile = null;
    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        // Video is saved automatically, no need to call stopRecordingChunk() here.
        videoRingBuffer.put(currentOutputFile);
        // Just clean up last recording and restart recording
        restartMediaRecorder();
    }

    /**
     * Stops the media recorder and inserts the file into the ring buffer. If this is called in an
     * invalid state (e.g. immediately after starting the media recorder) the output file will be
     * deleted.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void forceStopMediaRecorder() {
        try {
            stopRecordingChunk(); // try to stop recording BEFORE inserting file into buffer
            videoRingBuffer.put(currentOutputFile);
        } catch (RuntimeException re) {
            // No valid data was recorded as MediaRecorder.stop() was called before or right after
            // MediaRecorder.start(). Remove the incomplete file from the buffer and delete it;
            // a new one will be allocated as soon as the Handler is resumed
            if (currentOutputFile != null && currentOutputFile.exists()) currentOutputFile.delete();
            re.printStackTrace();
        }
    }

    /**
     * Restarts the media recorder.
     */
    private void restartMediaRecorder() {
        // Maybe we will need to synchronize this method. (Calls to this method will be made in a
        // non predictable manner). Tough, all calls are stable and can be called safely in any
        // state but we might get some warnings.
        if (!isHandlerRunning) return;
        releaseMediaRecorder();
        // start recording new chunk
        if (!prepareMediaRecorder() || !startRecordingChunk()) { // will allocate also a new output file
            pauseHandler();
        }
    }
}
