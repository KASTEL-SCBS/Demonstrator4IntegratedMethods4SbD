package edu.kit.informatik.pcc.core.crypto;

import java.io.File;
import java.security.Key;

public class VideoEncryptor implements IVideoEncryptor {
	private IFileEncryptor fileEncryptor;
	private IPublicKeyProvider publicKeyProvider;
	private ISymmetricEncryptor symmetricEncryptor;
	
	public void setFileEncryptor(IFileEncryptor fileEncryptor) {
		assert this.fileEncryptor == null;
		this.fileEncryptor = fileEncryptor;
	}
	
	public void setPublicKeyProvider(IPublicKeyProvider publicKeyProvider) {
		assert this.publicKeyProvider == null;
		this.publicKeyProvider = publicKeyProvider;
	}
	
	public void setSymmetricEncryptor(ISymmetricEncryptor symmetricEncryptor) {
		assert this.symmetricEncryptor == null;
		this.symmetricEncryptor = symmetricEncryptor;
	}

	@Override
	public byte[] encrypt(File inputVideo, File inputMetadata, File outputVideo, File outputMetadata) {
		assertCompletelySetup();
		Key key = symmetricEncryptor.generateSymmetricKey();
		fileEncryptor.encryptFile(inputVideo, key, outputVideo);
		fileEncryptor.encryptFile(inputMetadata, key, outputMetadata);
		Key publicKey = publicKeyProvider.getPublicKey();
		return fileEncryptor.encryptKey(key, publicKey);
	}
	
	private void assertCompletelySetup() {
		assert this.fileEncryptor != null;
		assert this.publicKeyProvider != null;
		assert this.symmetricEncryptor != null;
	}
}
