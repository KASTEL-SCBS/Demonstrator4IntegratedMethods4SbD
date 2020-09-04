package edu.kit.informatik.pcc.core.crypto;

import java.io.File;
import java.security.Key;

import edu.kit.informatik.pcc.core.data.IFileManager;

public class JavaRSA_AESFileDecryptor implements IVideoDecryptor, IPublicKeyProvider {
	private VideoDecryptor videoDecryptor;
	private KeyStorage keyStorage;
	
	public JavaRSA_AESFileDecryptor() {
		JavaCryptoRSA javaCryptoRSA = new JavaCryptoRSA();
		JavaCryptoAES javaCryptoAES = new JavaCryptoAES();
		DoubleSecuredFileDecryptor doubleSecuredFileDecryptor = new DoubleSecuredFileDecryptor();
		VideoDecryptor videoDecryptor = new VideoDecryptor();
		KeyStorage keyStorage = new KeyStorage();
		
		doubleSecuredFileDecryptor.setAsymmetricDecryptor(javaCryptoRSA);
		doubleSecuredFileDecryptor.setSymmetricDecryptor(javaCryptoAES);
		videoDecryptor.setAsymmetricDecryptor(javaCryptoRSA);
		videoDecryptor.setFileDecryptor(doubleSecuredFileDecryptor);
		videoDecryptor.setKeyStorage(keyStorage);
		
		this.videoDecryptor = videoDecryptor;
		this.keyStorage = keyStorage;
	}
	
	public void setFileManager(IFileManager fileManager) {
		this.keyStorage.setFileManager(fileManager);
	}
	
	@Override
	public Key getPublicKey() {
		return videoDecryptor.getPublicKey();
	}

	@Override
	public void decrypt(File inputVideo, File inputMetadata, byte[] keyData, File outputVideo, File outputMetadata) {
		videoDecryptor.decrypt(inputVideo, inputMetadata, keyData, outputVideo, outputMetadata);
	}
}
