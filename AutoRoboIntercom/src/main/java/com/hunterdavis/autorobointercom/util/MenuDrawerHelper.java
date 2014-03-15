package com.hunterdavis.autorobointercom.util;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.hunterdavis.autorobointercom.AlarmsActivity;
import com.hunterdavis.autorobointercom.AutoRoboMainScreen;
import com.hunterdavis.autorobointercom.R;

/**
 * Created by hunter on 3/15/14.
 */
public class MenuDrawerHelper {

    // our menudrawer items
    private static CharSequence menuDrawerChoices[] = new CharSequence[] {"Clients","Alarms"};
    public static ActionBarDrawerToggle mDrawerToggle;

    public static void setupMenuDrawerUI(final ActionBarActivity activity, DrawerLayout mDrawerLayout,ListView mDrawerList) {

        // setup our drawer layout
        mDrawerLayout = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) activity.findViewById(R.id.left_drawer);

        final DrawerLayout drawerLayoutReference = mDrawerLayout;
        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<CharSequence>(activity,
                R.layout.drawer_list_item, menuDrawerChoices));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = null;
                switch (position) {
                    case 0:

                        if(activity instanceof  AutoRoboMainScreen){
                            drawerLayoutReference.closeDrawers();
                            return;
                        }
                        intent = new Intent(activity, AutoRoboMainScreen.class);
                        activity.startActivity(intent);
                        break;
                    case 1:
                        if(activity instanceof  AlarmsActivity){
                            drawerLayoutReference.closeDrawers();
                            return;
                        }
                        intent = new Intent(activity, AlarmsActivity.class);
                        activity.startActivity(intent);
                        break;
                    default:
                        break;
                }
            }
        });

        setUpDrawerToggle(activity,mDrawerLayout);
    }

    private static void setUpDrawerToggle(final ActionBarActivity activity, DrawerLayout mDrawerLayout){
        android.support.v7.app.ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);



        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                activity,                             /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                R.drawable.ic_drawer,             /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                activity.invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                activity.invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

        };

        // Defer code dependent on restoration of previous instance state.
        // NB: required for the drawer indicator to show up!
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }
}
