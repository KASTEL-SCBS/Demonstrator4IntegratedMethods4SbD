package edu.kit.informatik.pcc.core.data;

import java.io.File;

/**
 * An IFileManager instance manages files.
 * @author Jan Wittler
 */
public interface IFileManager {
	/**
	 * Returns a file instance for the given name.
	 * If the file does not yet exist, it is created.
	 * @param name The name of the file.
	 * @return A file instance representing the file with provided name.
	 */
	public File file(String name);
	public File existingFile(String name);
	public void deleteFile(File file);
}
