package de.pcc.privacycrashcam.testUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utility for managing files in tests. Do not use this without modification in android app code!
 *
 * @author Giorgio Gross
 */

public class FileUtils {

    /**
     * Creates a file inside the passed directory.
     *
     * @param dir directory
     * @param name file name of the new file
     * @return the file or null if something went wrong
     * @throws IOException if the file cannot be created
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File CreateFile(File dir, String name) throws IOException {
        File file = new File(dir, name);
        file.createNewFile();
        return file;
    }

    /**
     * Gets a file inside the passed directory.
     *
     * @param dir directory
     * @param name file name of the new file
     * @return the file or null if it does not exist
     * @throws IOException if the file cannot be created
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File GetFile(File dir, String name) throws IOException {
        File file = new File(dir, name);
        if(file.exists()) return file;
        return null;
    }

    /**
     * Copies the passed input stream into the passed file.
     *
     * @param in the input stream
     * @param file the file
     * @throws Exception when the file cannot be written or read
     */
    public static void CopyInputStreamToFile(InputStream in, File file) throws Exception {
        OutputStream out = new FileOutputStream(file);
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        out.close();
        in.close();
    }

    /**
     * Recursively delete directory and files inside directory
     * @param dir directory or file to be deleted
     */
    public static void recDeleteDir(File dir) {
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                recDeleteDir(file);
            }
        }
        dir.delete();
    }
}
