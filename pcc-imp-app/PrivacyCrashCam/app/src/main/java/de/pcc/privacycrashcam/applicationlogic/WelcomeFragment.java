package de.pcc.privacycrashcam.applicationlogic;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.pcc.privacycrashcam.R;

/**
 * @author Giorgio
 */
public class WelcomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // get the main layout describing the content
        return inflater.inflate(R.layout.content_welcome, container, false);
    }
}
