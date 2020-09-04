package de.pcc.privacycrashcam.gui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import de.pcc.privacycrashcam.R;

/**
 * Activity for simple views which have a toolbar and some content inside of a fragment.
 * <p>
 * Sets the fragment specified by the subclass as content of the {@link MainActivity}
 * </p>
 *
 * @author Giorgio
 */
public abstract class ContainerActivity extends MainActivity {

    /* #############################################################################################
     *                                  attributes
     * ###########################################################################################*/

    /* #############################################################################################
     *                                  methods
     * ###########################################################################################*/

    /**
     * Selects a fragment which will be displayed as content.
     *
     * @return Fragment to be shown
     */
    protected abstract Fragment selectFragment();

    /**
     * Get the base layout resource for this activity. The layout must contain a toolbar with an id
     * named <b>toolbar</b>.
     * <p>If there is a navigation drawer in the layout it will be displayed. Navigation is handled
     * by the {@link MainActivity MainActivity} class.</p>
     *
     * @return resource id for this activity
     */
    @Override
    public int getLayoutRes() {
        return R.layout.activity_main_drawer_toolbar;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        replaceFragment(selectFragment());
    }

    /**
     * Replaces the content_container with the passed fragment
     *
     * @param fragment new fragment
     */
    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.content_container, fragment);
        // transaction.addToBackStack(null);

        transaction.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
