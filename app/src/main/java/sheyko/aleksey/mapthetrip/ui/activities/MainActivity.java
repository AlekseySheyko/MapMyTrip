package sheyko.aleksey.mapthetrip.ui.activities;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import sheyko.aleksey.mapthetrip.R;
import sheyko.aleksey.mapthetrip.ui.adapters.NavigationAdapter;
import sheyko.aleksey.mapthetrip.ui.fragments.MapPane;
import sheyko.aleksey.mapthetrip.ui.fragments.MapPane.OnTabSelectedListener;


public class MainActivity extends Activity
    implements OnTabSelectedListener {

    // Navigation drawer
    private ActionBar mActionBar;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    // Action bar tabs
    private Tab tab1;
    private Tab tab2;
    private Tab tab3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addSideNavigation();
        addActionBarTabs();

        MapPane mapFragment = new MapPane();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, mapFragment);
        ft.commit();
    }

    private void addSideNavigation() {
        mActionBar = getActionBar();
        mTitle = mDrawerTitle = getTitle();
        String[] mActionTitles = getResources().getStringArray(R.array.action_titles);
        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ListView mDrawerList = (ListView) findViewById(R.id.navigationDrawerList);

        Integer[] mImageIds = new Integer[]{
                R.drawable.nav_record, R.drawable.nav_log_trip,
                R.drawable.nav_log_gas, R.drawable.nav_log_rest,
                R.drawable.nav_stats, R.drawable.nav_settings
        };
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        NavigationAdapter adapter = new NavigationAdapter(this, mActionTitles, mImageIds);
        mDrawerList.setAdapter(adapter);

        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                mActionBar.setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                mActionBar.setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void addActionBarTabs() {
        // Setup action bar for tabs
        if (mActionBar != null) {
            mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

            TabListener tabListener = new TabListener() {
                @Override
                public void onTabSelected(Tab tab, FragmentTransaction ft) {
                }

                @Override
                public void onTabUnselected(Tab tab, FragmentTransaction ft) {
                }

                @Override
                public void onTabReselected(Tab tab, FragmentTransaction ft) {
                }
            };

            tab1 = mActionBar.newTab()
                    .setText(R.string.travel_tab_label)
                    .setIcon(R.drawable.ic_travel);
            tab1.setTabListener(tabListener);
            mActionBar.addTab(tab1);

            tab2 = mActionBar.newTab()
                    .setText(R.string.gas_tab_label)
                    .setIcon(R.drawable.ic_gas);
            tab2.setTabListener(tabListener);
            mActionBar.addTab(tab2);

            tab3 = mActionBar.newTab()
                    .setText(R.string.rest_tab_label)
                    .setIcon(R.drawable.ic_rest);
            tab3.setTabListener(tabListener);
            mActionBar.addTab(tab3);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Navigation drawer toggle
        return mDrawerToggle.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(int position) {
        mActionBar.removeAllTabs();

        switch (position) {
            case 1:
                mActionBar.setTitle(getString(R.string.recording_label));
                mActionBar.addTab(tab1, 0, false);
                mActionBar.addTab(tab2, 1, true);
                mActionBar.addTab(tab3, 2, false);
                break;
            case 2:
                mActionBar.setTitle(getString(R.string.pause_label));
                mActionBar.addTab(tab1, 0, false);
                mActionBar.addTab(tab2, 1, false);
                mActionBar.addTab(tab3, 2, true);
                break;
        }
    }

    // Click listener for side navigation
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position == 0) {
                startActivity(new Intent(MainActivity.this, MainActivity.class));
            } else if (position == 4) {
                startActivity(new Intent(MainActivity.this, StatsActivity.class));
            } else {
                comingSoonToast();
            }
        }

        private void comingSoonToast() {
            Toast.makeText(MainActivity.this, getString(R.string.coming_soon), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggle
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
}
