package edu.kit.informatik.pcc.core.crypto;

import java.io.File;
import java.security.Key;

public interface ISymmetricEncryptor extends IKeyConsumer {
	public Key generateSymmetricKey();
	public void encryptFile(File inputFile, Key key, File outputFile);
}
