package edu.kit.informatik.pcc.core.crypto;

import java.io.File;

public class JavaRSA_AESFileEncryptor implements IVideoEncryptor {
	private VideoEncryptor videoEncryptor;
	
	public JavaRSA_AESFileEncryptor() {
		JavaCryptoRSA javaCryptoRSA = new JavaCryptoRSA();
		JavaCryptoAES javaCryptoAES = new JavaCryptoAES();
		DoubleSecuredFileEncryptor doubleSecuredFileEncryptor = new DoubleSecuredFileEncryptor();
		VideoEncryptor videoEncryptor = new VideoEncryptor();
		
		doubleSecuredFileEncryptor.setAsymmetricEncryptor(javaCryptoRSA);
		doubleSecuredFileEncryptor.setSymmetricEncryptor(javaCryptoAES);
		videoEncryptor.setFileEncryptor(doubleSecuredFileEncryptor);
		videoEncryptor.setSymmetricEncryptor(javaCryptoAES);
		
		this.videoEncryptor = videoEncryptor;
	}
	
	public void setPublicKeyProvider(IPublicKeyProvider publicKeyProvider) {
		videoEncryptor.setPublicKeyProvider(publicKeyProvider);
	}

	@Override
	public byte[] encrypt(File inputVideo, File inputMetadata, File outputVideo, File outputMetadata) {
		return videoEncryptor.encrypt(inputVideo, inputMetadata, outputVideo, outputMetadata);
	}
}
