package edu.kit.informatik.pcc.core.crypto;

import java.io.File;
import java.security.Key;
import java.security.KeyPair;

public class VideoDecryptor implements IVideoDecryptor, IPublicKeyProvider {
	private static final String publicKeyId = "VideoDecryptor.PublicKey";
	private static final String privateKeyId ="VideoDecryptor.PrivateKey";
	
	private IKeyStorage keyStorage;
	private IFileDecryptor fileDecryptor;
	private IAsymmetricDecryptor asymmetricDecryptor;
	
	public void setKeyStorage(IKeyStorage keyStorage) {
		assert this.keyStorage == null;
		this.keyStorage = keyStorage;
	}
	
	public void setFileDecryptor(IFileDecryptor fileDecryptor) {
		assert this.fileDecryptor == null;
		this.fileDecryptor = fileDecryptor;
	}
	
	public void setAsymmetricDecryptor(IAsymmetricDecryptor asymmetricDecryptor) {
		assert this.asymmetricDecryptor == null;
		this.asymmetricDecryptor = asymmetricDecryptor;
	}

	
	/**
	 * Returns the instance's public key.
	 * If there is none yet, a new public / private key pair is generated and its public key is returned.
	 */
	@Override
	public Key getPublicKey() {
		assertCompletelySetup();
		return getKey(publicKeyId);
	}
	
	private Key getPrivateKey() {
		return getKey(privateKeyId);
	}
	
	private Key getKey(String id) {
		Key key = keyStorage.loadKey(id);
		if (key != null) {
			return key;
		}
		setupAsymmetricKeyPair();
		return keyStorage.loadKey(id);
	}

	@Override
	public void decrypt(File inputVideo, File inputMetadata, byte[] keyData, File outputVideo, File outputMetadata) {
		assertCompletelySetup();
		Key privateKey = getPrivateKey();
		Key key = fileDecryptor.decryptKey(keyData, privateKey);
		fileDecryptor.decryptFile(inputVideo, key, outputVideo);
		fileDecryptor.decryptFile(inputMetadata, key, outputMetadata);
	}
	
	private void setupAsymmetricKeyPair() {
		KeyPair keyPair = asymmetricDecryptor.generateAsymmetricKeyPair();
		keyStorage.storeKey(publicKeyId, keyPair.getPublic());
		keyStorage.storeKey(privateKeyId, keyPair.getPrivate());
	}
	
	private void assertCompletelySetup() {
		assert keyStorage != null;
		assert fileDecryptor != null;
		assert asymmetricDecryptor != null;
	}
}
