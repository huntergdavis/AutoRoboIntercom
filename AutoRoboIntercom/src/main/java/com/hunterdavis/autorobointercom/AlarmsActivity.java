package com.hunterdavis.autorobointercom;

import android.os.Bundle;
import android.app.Activity;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.widget.ListView;

import com.hunterdavis.autorobointercom.util.MenuDrawerHelper;

public class AlarmsActivity extends Activity {

    // drawer layout
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarms);

        setupUI(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.alarms, menu);
        return true;
    }

    public void setupUI(Activity activity) {
        MenuDrawerHelper.setupMenuDrawerUI(activity, mDrawerLayout, mDrawerList);
    }
    
}
