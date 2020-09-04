package de.pcc.privacycrashcam.gui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import de.pcc.privacycrashcam.R;
import de.pcc.privacycrashcam.applicationlogic.SettingsFragment;

/**
 * Activity showing the settings of the app.
 * <p>
 * Sets the {@link SettingsFragment SettingsFragment} as content of the
 * {@link ContainerActivity ContainerActivity}
 * </p>
 *
 * @author Giorgio Gross, David Laubenstein
 */
public class SettingsActivity extends ContainerActivity {

    /* #############################################################################################
     *                                  attributes
     * ###########################################################################################*/

    /* #############################################################################################
     *                                  methods
     * ###########################################################################################*/

    /**
     * Starts a new intent with the {@link SettingsActivity SettingsActivity}
     *
     * @param calling the activity which is doing this call
     */
    public static void Launch(Activity calling) {
        Intent intent = new Intent(calling, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        calling.startActivity(intent);
    }

    @Override
    protected Fragment selectFragment() {
        return new SettingsFragment();
    }

    @Override
    public int getMenuEntryId() {
        return R.id.nav_settings;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
}
