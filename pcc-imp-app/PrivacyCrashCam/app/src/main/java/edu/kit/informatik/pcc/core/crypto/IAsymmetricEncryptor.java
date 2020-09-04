package edu.kit.informatik.pcc.core.crypto;

import java.security.Key;

public interface IAsymmetricEncryptor extends IKeyConsumer {
	public byte[] encryptKey(Key key, Key publicKey);
}
