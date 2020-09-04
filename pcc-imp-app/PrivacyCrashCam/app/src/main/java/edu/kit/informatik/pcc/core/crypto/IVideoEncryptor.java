package edu.kit.informatik.pcc.core.crypto;

import java.io.File;

/**
 * An IVideoEncryptor instance provides functionality to encrypt videos and metadata.
 * @author Jan Wittler
 */
public interface IVideoEncryptor {
	/**
	 * Encrypts the provided files with a self-generated key. The data of the generated key is returned.
	 * @param inputVideo The video to encrypt. Must not be null.
	 * @param inputMetadata The metadata to encrypt. Must not be null.
	 * @param outputVideo The output file for the encrypted video. Must not be null.
	 * @param outputMetadata The output file for the encrypted metadata. Must not be null.
	 * @return The data of the key used to encrypt the provided files.
	 */
	public byte[] encrypt(File inputVideo, File inputMetadata, File outputVideo, File outputMetadata);
}
