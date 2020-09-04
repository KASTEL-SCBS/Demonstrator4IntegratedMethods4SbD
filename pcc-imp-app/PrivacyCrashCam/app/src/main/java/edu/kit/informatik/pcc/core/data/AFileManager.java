package edu.kit.informatik.pcc.core.data;

import java.io.File;
import java.util.logging.Logger;

public abstract class AFileManager implements IFileHierarchyManager, IFileManager {

	@Override
	public File file(String name) {
		return file(name, null);
	}

	@Override
	public File existingFile(String name) {
		return existingFile(name, null);
	}

	@Override
	public void deleteFile(File file) {
		if (file == null) {
			return;
		}
		try {
			file.delete();
		}
		catch (SecurityException e) {
			Logger.getGlobal().warning("failed to delete file: " + file.getAbsolutePath());
		}
	}
}
