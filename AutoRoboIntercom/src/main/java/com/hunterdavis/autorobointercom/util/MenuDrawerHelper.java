package com.hunterdavis.autorobointercom.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
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

    public static void setupMenuDrawerUI(final Activity activity, DrawerLayout mDrawerLayout,ListView mDrawerList) {

        // setup our drawer layout
        mDrawerLayout = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) activity.findViewById(R.id.left_drawer);
        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<CharSequence>(activity,
                R.layout.drawer_list_item, AutoRoboApplication.menuDrawerChoices));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = null;
                switch (position) {
                    case 0:
                        intent = new Intent(activity, AutoRoboMainScreen.class);
                        activity.startActivity(intent);
                        break;
                    case 1:
                        intent = new Intent(activity, AlarmsActivity.class);
                        activity.startActivity(intent);
                        break;
                    default:
                        break;
                }
            }
        });
    }
}
