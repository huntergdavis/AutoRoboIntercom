package com.hunterdavis.autorobointercom;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.speech.RecognizerIntent;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.hunterdavis.autorobointercom.network.NetworkTransmissionUtilities;
import com.hunterdavis.autorobointercom.util.MenuDrawerHelper;
import com.hunterdavis.autorobointercom.util.SpeechUtils;

import java.io.IOException;
import java.util.ArrayList;

public class AlarmsActivity extends ActionBarActivity {

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

    public void setupUI(ActionBarActivity activity) {

        // set up our record audio button to actually record audio
        findViewById(R.id.record_audio_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                try {
                    //Log.e(TAG,"requesting got here at least");
                    startActivityForResult(i, SpeechUtils.REQUEST_OK);
                } catch (Exception e) {
                    Toast.makeText(v.getContext(), "Error initializing speech to text engine.", Toast.LENGTH_LONG).show();
                }
            }
        });

        MenuDrawerHelper.setupMenuDrawerUI(activity, mDrawerLayout, mDrawerList);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (MenuDrawerHelper.mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle item selection
        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Log.e(TAG,"got here at least");
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==SpeechUtils.REQUEST_ALARM_OK  && resultCode==RESULT_OK) {
            ArrayList<String> thingsYouSaid = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            if(thingsYouSaid.size() > 0) {
                Toast.makeText(this,"Message: " + thingsYouSaid.get(0) + "Transcribed!",Toast.LENGTH_SHORT).show();
            }
        }
    }
    
}
