package de.pcc.privacycrashcam.gui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import de.pcc.privacycrashcam.R;
import de.pcc.privacycrashcam.applicationlogic.LegalFragment;

/**
 * Shows legal information and copyright notice.
 * <p>
 * Sets the {@link LegalFragment} as content of the
 * {@link ContainerActivity ContainerActivity}
 * </p>
 *
 * @author David Laubenstein
 *         Created by David Laubenstein on 1/27/17.
 */
public class LegalActivity extends ContainerActivity {

    /* #############################################################################################
     *                                  attributes
     * ###########################################################################################*/

    /* #############################################################################################
     *                                  methods
     * ###########################################################################################*/

    /**
     * Starts a new intent with the {@link LegalActivity LegalActivity}
     *
     * @param calling the activity which is doing this call
     */
    public static void Launch(Activity calling) {
        Intent intent = new Intent(calling, LegalActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        calling.startActivity(intent);
    }

    @Override
    protected Fragment selectFragment() {
        return new LegalFragment();
    }

    @Override
    public int getMenuEntryId() {
        return R.id.nav_legal;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
}
