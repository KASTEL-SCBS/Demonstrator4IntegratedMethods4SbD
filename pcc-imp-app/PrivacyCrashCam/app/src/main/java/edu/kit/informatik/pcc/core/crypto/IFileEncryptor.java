package edu.kit.informatik.pcc.core.crypto;

import java.io.File;
import java.security.Key;

public interface IFileEncryptor {
	public Key generateKey();
	public void encryptFile(File inputFile, Key key, File outputFile);
	public byte[] encryptKey(Key key, Key publicKey);
}
