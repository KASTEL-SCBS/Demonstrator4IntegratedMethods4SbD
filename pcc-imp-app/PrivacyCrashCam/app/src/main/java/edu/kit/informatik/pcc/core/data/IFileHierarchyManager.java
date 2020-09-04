package edu.kit.informatik.pcc.core.data;

import java.io.File;

/**
 * An IFileHierarchyManager instance manages files and directories.
 * @author Jan Wittler
 */
public interface IFileHierarchyManager {
	/**
	 * Returns a file instance for the given parameters.
	 * If the file at the provided location does not yet exist, it is created.
	 * @param name The name of the file.
	 * @param directoryPath The path of the directory in which the file should be.
	 * @return A file instance representing the file with given parameters.
	 */
	public File file(String name, String directoryPath);
	public File existingFile(String name, String directoryPath);
	public void createDirectory(String directoryPath);
	public File[] filesInDirectory(String directoryPath);
	public void deleteFile(File file);
}
