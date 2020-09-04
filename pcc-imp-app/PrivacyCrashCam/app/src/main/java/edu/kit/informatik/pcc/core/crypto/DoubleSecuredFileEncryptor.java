package edu.kit.informatik.pcc.core.crypto;

import java.io.File;
import java.security.Key;

public class DoubleSecuredFileEncryptor implements IFileEncryptor {
	private IAsymmetricEncryptor asymmetricEncryptor;
	private ISymmetricEncryptor symmetricEncryptor;
	
	public void setAsymmetricEncryptor(IAsymmetricEncryptor asymmetricEncryptor) {
		assert this.asymmetricEncryptor == null;
		this.asymmetricEncryptor = asymmetricEncryptor;
	}
	
	public void setSymmetricEncryptor(ISymmetricEncryptor symmetricEncryptor) {
		assert this.symmetricEncryptor == null;
		this.symmetricEncryptor = symmetricEncryptor;
	}

	@Override
	public Key generateKey() {
		assertCompletelySetup();
		return symmetricEncryptor.generateSymmetricKey();
	}

	@Override
	public void encryptFile(File inputFile, Key key, File outputFile) {
		assertCompletelySetup();
		symmetricEncryptor.encryptFile(inputFile, key, outputFile);
	}

	@Override
	public byte[] encryptKey(Key key, Key publicKey) {
		assertCompletelySetup();
		return asymmetricEncryptor.encryptKey(key, publicKey);
	}
	
	private void assertCompletelySetup() {
		assert asymmetricEncryptor != null;
		assert symmetricEncryptor != null;
	}
}
