package edu.kit.informatik.pcc.android.crypto;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.security.Key;
import java.security.PublicKey;

import de.pcc.privacycrashcam.R;

import edu.kit.informatik.pcc.core.crypto.IPublicKeyProvider;

/**
 * A wrapper class for accessing the public key stored in the project resources.
 */
public class PublicKeyProvider implements IPublicKeyProvider {
    private Context context;

    public PublicKeyProvider(Context context) {
        this.context = context;
    }

    @Override
    public Key getPublicKey() {
        InputStream publicKey = context.getResources().openRawResource(R.raw.publickey);
        try (ObjectInputStream ois = new ObjectInputStream(publicKey)) {
            return (PublicKey)ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            Log.w(PublicKeyProvider.class.getName(), "Failed to load public key from resources");
            e.printStackTrace();
        }
        return null;
    }
}
