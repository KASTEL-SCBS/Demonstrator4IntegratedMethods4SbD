package de.pcc.privacycrashcam.gui;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import de.pcc.privacycrashcam.R;

/**
 * Base class for all activities. Handles navigation through the application's views.
 *
 * @author Giorgio
 */
public abstract class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    /* #############################################################################################
     *                                  attributes
     * ###########################################################################################*/

    private static final String TAG = "MAIN_ACT";

    private int activeMenuItemId = R.id.nav_legal; // default is legal info

    private
    @Nullable
    DrawerLayout drawer;
    private
    @Nullable
    NavigationView navigationView;

    /* #############################################################################################
     *                                  methods
     * ###########################################################################################*/

    /**
     * Get the base layout resource for this activity. The layout must contain a toolbar with an id
     * named <b>toolbar</b>.
     * <p>If there is a navigation drawer in the layout it will be displayed. Navigation is handled
     * by the {@link MainActivity MainActivity} class.</p>
     *
     * @return resource id for this activity
     */
    public abstract
    @LayoutRes
    int getLayoutRes();

    /**
     * Get the menu entry which will be highlighted in the drawer. Pass -1 if you don't want to
     * highlight any navigation menu entry.
     *
     * @return R.id.[menu_entry_id]
     */
    public abstract int getMenuEntryId();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutRes());

        // set toolbar and nav nav_drawer
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar == null) throw new IllegalArgumentException("You passed a layout file " +
                "without a toolbar, but a toolbar with id \"toolbar\" was expected.");
        setSupportActionBar(toolbar);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();
            drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
                private boolean hasRequestedLayout = false;

                @Override
                public void onDrawerSlide(View drawerView, float slideOffset) {
                    redrawNavigationView();

                    // make sure that we will redraw the nav view next time we move the drawer
                    if (slideOffset == 1f) hasRequestedLayout = false;
                }

                @Override
                public void onDrawerOpened(View drawerView) {

                }

                @Override
                public void onDrawerClosed(View drawerView) {

                }

                @Override
                public void onDrawerStateChanged(int newState) {

                }

                /*
                redraw navigation view as any surface views in the layout will prevent it from
                rendering right.
                 */
                private void redrawNavigationView() {
                    if (!hasRequestedLayout) {
                        assert navigationView != null;
                        navigationView.requestLayout();
                        hasRequestedLayout = true;
                    }
                }

            });

            navigationView = (NavigationView) findViewById(R.id.nav_view);
            assert navigationView != null;
            navigationView.setNavigationItemSelectedListener(this);
            activeMenuItemId = getMenuEntryId();
            navigationView.setCheckedItem(activeMenuItemId);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        if (drawer == null || item.getItemId() == activeMenuItemId)
            return false;

        // Handle navigation view item clicks
        int id = item.getItemId();
        if (id == R.id.nav_camera) {
            // show camera
            CameraActivity.Launch(this);
        } else if (id == R.id.nav_recorded) {
            // show recorded videos
            VideosActivity.Launch(this);
        } else if (id == R.id.nav_settings) {
            // show settings
            SettingsActivity.Launch(this);
        } else if (id == R.id.nav_legal) {
            // show legal information
            LegalActivity.Launch(this);
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Removes the drawer shadow by setting it transparent. Can be called by subclasses.
     */
    protected void removeDrawerShadow() {
        if (drawer != null) drawer.setScrimColor(ContextCompat.getColor(getApplicationContext(),
                android.R.color.transparent));
    }

    @Override
    public void onBackPressed() {
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
