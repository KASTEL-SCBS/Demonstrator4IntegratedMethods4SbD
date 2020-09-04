package edu.kit.informatik.pcc.core.crypto;

import java.io.File;
import java.security.Key;

public class DoubleSecuredFileDecryptor implements IFileDecryptor {
	private IAsymmetricDecryptor asymmetricDecryptor;
	private ISymmetricDecryptor symmetricDecryptor;
	
	public void setAsymmetricDecryptor(IAsymmetricDecryptor asymmetricDecryptor) {
		assert this.asymmetricDecryptor == null;
		this.asymmetricDecryptor = asymmetricDecryptor;
	}
	
	public void setSymmetricDecryptor(ISymmetricDecryptor symmetricDecryptor) {
		assert this.symmetricDecryptor == null;
		this.symmetricDecryptor = symmetricDecryptor;
	}

	@Override
	public Key decryptKey(byte[] keyData, Key privateKey) {
		assertCompletelySetup();
		return asymmetricDecryptor.decryptKey(keyData, symmetricDecryptor.keyAlgorithm(), privateKey);
	}

	@Override
	public void decryptFile(File inputFile, Key key, File outputFile) {
		assertCompletelySetup();
		symmetricDecryptor.decryptFile(inputFile, key, outputFile);
	}
	
	private void assertCompletelySetup() {
		assert asymmetricDecryptor != null;
		assert symmetricDecryptor != null;
	}
}
