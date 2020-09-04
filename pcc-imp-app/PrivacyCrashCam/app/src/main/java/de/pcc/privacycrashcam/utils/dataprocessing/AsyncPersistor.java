package de.pcc.privacycrashcam.utils.dataprocessing;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import de.pcc.privacycrashcam.data.Metadata;
import edu.kit.informatik.pcc.android.settings.Settings;
import de.pcc.privacycrashcam.utils.datastructures.VideoRingBuffer;
import edu.kit.informatik.pcc.android.Client;

/**
 * The AsyncPersistor saves all data after recording gets invoked in the app.
 * First it saves the metadata of the recording to a json file.
 * Then it takes all recorded video snippets and creates one coherent file.
 * After that all data gets encrypted and save to the app's data storage.
 * <p>The process of persisting is asynchronous to the app's main thread.
 * Therefore callbacks are used to inform the app about the persisting's progress.</p>
 *
 * @author Josh Romanowski, Giorgio Groß
 */
public class AsyncPersistor extends AsyncTask<Metadata, Void, Boolean> {
    private final static String TAG = AsyncPersistor.class.getName();

    /* #############################################################################################
     *                                  attributes
     * ###########################################################################################*/

    /**
     * Callback used to inform the app about the progress of persisting.
     */
    private PersistCallback persistCallback;
    /**
     * Ringbuffer that contains the recorded video snippets
     */
    private VideoRingBuffer ringbuffer;
    /**
     * Settings used to determine the ringbuffer size.
     */
    private Settings settings;

    /* #############################################################################################
     *                                  constructors
     * ###########################################################################################*/

    /**
     * Creates a new persistor with the given parameters. The AsyncPersistor will use create
     * temporary data and operate on temporary files accessible to the passed {@link MemoryManager}
     * instance.
     *
     * @param ringbuffer      Buffer containing the recorded video snippets.
     * @param persistCallback Callback used to give asynchronous response.
     */
    public AsyncPersistor(VideoRingBuffer ringbuffer, PersistCallback persistCallback) {
        this.ringbuffer = ringbuffer;
        this.persistCallback = persistCallback;
        this.settings = Client.getGlobal().getSettingsManager().loadSettings();
    }

    /* #############################################################################################
     *                                  methods
     * ###########################################################################################*/

    @Override
    protected Boolean doInBackground(Metadata... params) {
        Log.i(TAG, "Background task started");

        // wait half a buffer size
        int timeToWait = settings.getBufferSizeSec() * 1000 / 2;
        try {
            Thread.sleep(timeToWait);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }

        // post to UI thread and wait until the UI has migrated to a completely new RingBuffer
        final CyclicBarrier mBarrier = new CyclicBarrier(2);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "updating UI and CamHandler");
                persistCallback.onPersistingStarted();
                try {
                    mBarrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }
        });
        try {
            Log.i(TAG, "waiting for UI to finish update");
            mBarrier.await();
        } catch (InterruptedException ex) {
            return false;
        } catch (BrokenBarrierException ex) {
            return false;
        }
        // UI has no reference to the ring buffer and the memory manager instance anymore. We can
        // now freely operate on it.
        Log.i(TAG, "Start writing files");

        // save metadata
        Metadata metaData = params[0];
        if (metaData == null) {
            Log.w(TAG, "Did not receive metadata");
            return false;
        }

        String videoTag = String.valueOf(metaData.getDate());
        File metaLocation = Client.getGlobal().getTemporaryFileManager().file(videoTag + "_metadata");
        if (!saveMetadataToFile(metaLocation, metaData)) {
            return false;
        }

        // concat video snippets
        Queue<File> vidSnippets = ringbuffer.demandData();
        if (vidSnippets == null) {
            return false;
        }
        Log.i(TAG, "All files to be concatenated were written");

        File concatVideo = Client.getGlobal().getTemporaryFileManager().file(videoTag + "_video");
        if (!concatVideos(vidSnippets, concatVideo)) {
            return false;
        }

        Client.getGlobal().getLocalVideoManager().storeVideo(concatVideo, metaLocation);

        // delete temporary files
        Client.getGlobal().getTemporaryFileManager().deleteFile(metaLocation);
        Client.getGlobal().getTemporaryFileManager().deleteFile(concatVideo);
        for (File snippet: vidSnippets) {
            Client.getGlobal().getTemporaryFileManager().deleteFile(snippet);
        }
        ringbuffer.flushAll();

        Log.i(TAG, "Finished writing files");

        return true;
    }

    @Override
    protected void onPostExecute(Boolean status) {
        super.onPostExecute(status);
        persistCallback.onPersistingStopped(status);
    }

    /* #############################################################################################

                                        helper methods

     * ###########################################################################################*/

    /**
     * Takes a collection of videos and appends them in order.
     * Through that creates a continuous video and saves it to the desired location.
     *
     * @param videos      Collection of video snippets.
     * @param concatVideo Location of the merged video.
     * @return Returns whether concatting the videos was successful or not.
     */
    private boolean concatVideos(Queue<File> videos, File concatVideo) {
        // read all video snippets
        List<Movie> clips = new LinkedList<>();
        try {
            for (File video : videos) {
                Movie tm = MovieCreator.build(video.getAbsolutePath());
                clips.add(tm);
            }
        } catch (IOException e) {
            Log.w(TAG, "Error while reading video snippets");
            return false;
        }

        // filter out video tracks and ignore audio tracks
        List<Track> videoTracks = new LinkedList<>();
        for (Movie m : clips) {
            for (Track t : m.getTracks()) {
                if (t.getHandler().equals("vide")) {
                    videoTracks.add(t);
                }
            }
        }

        // create video
        Movie result = new Movie();
        try {
            if (videoTracks.size() > 0) {
                result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));
            }

            Container out = new DefaultMp4Builder().build(result);
            RandomAccessFile raf = new RandomAccessFile(concatVideo, "rw");
            FileChannel fc = raf.getChannel();
            out.writeContainer(fc);

            fc.close();
            raf.close();
        } catch (IOException|NoSuchElementException e) {
            Log.w(TAG, "Error while writing concat video");
            return false;
        }
        return true;
    }

    /**
     * Takes a metadata object, parses it into json format and saves it to a file.
     *
     * @param output   Output location of the metadata file.
     * @param metadata Metadata to be saved.
     * @return Returns whether saving was successful or not.
     */
    private boolean saveMetadataToFile(File output, Metadata metadata) {
        String metaJson = metadata.getAsJSON();
        try (PrintWriter out = new PrintWriter(output)) {
            out.println(metaJson);
        } catch (IOException e) {
            Log.w(TAG, "Error when saving metadata to files");
            return false;
        }
        return true;
    }
}
