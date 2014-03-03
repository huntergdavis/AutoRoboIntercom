package com.hunterdavis.autorobointercom;

import com.hunterdavis.autorobointercom.network.NetworkConstants;
import com.hunterdavis.autorobointercom.network.NetworkReceiverThread;
import com.hunterdavis.autorobointercom.network.RemoteIntercomClient;
import com.hunterdavis.autorobointercom.util.AutoRoboApplication;
import com.hunterdavis.autorobointercom.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Locale;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class AutoRoboMainScreen extends Activity implements
        TextToSpeech.OnInitListener {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    // just a request status for voice input
    protected static final int REQUEST_OK = 1;

    // our TTS
    private TextToSpeech tts;

    // our multicast lock
    private WifiManager.MulticastLock multicastLock;

    // our network receiver thread
    private NetworkReceiverThread networkThread;

    private LinkedHashSet<RemoteIntercomClient> clients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.autorobo_fullscreen);

        clients = new LinkedHashSet<RemoteIntercomClient>();

        tts = new TextToSpeech(this, this);

        // setup our UI elements
        setupUI();


        // set up our record audio button to actually record audio
        findViewById(R.id.record_audio_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
                try {
                    startActivityForResult(i, REQUEST_OK);
                } catch (Exception e) {
                    Toast.makeText(v.getContext(), "Error initializing speech to text engine.", Toast.LENGTH_LONG).show();
                }
            }
        });

        // Acquire multicast lock
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        multicastLock = wifi.createMulticastLock("multicastLock");
        multicastLock.setReferenceCounted(true);
        multicastLock.acquire();

        // set up our send text button to actuall send text
        findViewById(R.id.send_text_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    sendTextToAllClients();
                } catch (Exception e) {
                    Toast.makeText(v.getContext(), "Error sending text to clients.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private BroadcastReceiver networkDataReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String networkMessage = intent.getStringExtra(NetworkConstants.BROADCAST_EXTRA_STRING_UDP_MESSAGE);
            handleNetworkData(networkMessage);
        }
    };

    @Override
    protected void onPause() {

        synchronized (networkThread) {
            try {
                ((Runnable)networkThread).wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        LocalBroadcastManager.getInstance(this).unregisterReceiver(networkDataReceiver);
        super.onPause();


    }

    @Override
    protected void onResume() {
        super.onResume();

        ((Runnable)networkThread).notifyAll();

        IntentFilter iff= new IntentFilter(NetworkConstants.BROADCAST_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(networkDataReceiver, iff);

        networkThread = new NetworkReceiverThread();
        networkThread.run();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_OK  && resultCode==RESULT_OK) {
            ArrayList<String> thingsYouSaid = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            ((TextView)findViewById(R.id.text_to_send)).setText(thingsYouSaid.get(0));
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.auto_robo_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.enter_name:
                getUserNameAndStoreIt();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getUserNameAndStoreIt() {
        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        new AlertDialog.Builder(AutoRoboMainScreen.this)
                .setTitle("Update Name")
                .setMessage("Enter A Name For This Room")
                .setView(input)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Editable value = input.getText();
                        if(!TextUtils.isEmpty(value)) {
                            AutoRoboApplication.storeName(value.toString());
                        }

                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Do nothing.
            }
        }).show();
    }

    private void sendTextToAllClients() throws IndexOutOfBoundsException, IOException {
        String nameAndText = AutoRoboApplication.getName() + NetworkConstants.BROADCAST_EXTRA_SPECIAL_CHARACTER_DELIMINATOR + ((EditText)findViewById(R.id.text_to_send)).getText();

        DatagramSocket socket = new DatagramSocket(NetworkConstants.DEFAULT_PORT);
        byte buff[] = nameAndText.getBytes();
        DatagramPacket packet = new DatagramPacket(buff, buff.length, InetAddress.getByName(NetworkConstants.DEFAULT_GROUP),9999);
        socket.send(packet);
        socket.close();
    }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    public void onDestroy() {
        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        // Once your finish using it, release multicast lock
        if (multicastLock != null) {
            multicastLock.release();
            multicastLock = null;
        }


        super.onDestroy();
    }

    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Missing text to speech language!", Toast.LENGTH_LONG).show();
            } else {
                // success condition!  we're good to go
            }

        } else {
            Toast.makeText(this, "Error initializing text to speech engine.", Toast.LENGTH_LONG).show();

        }

    }

    public void setupUI() {
        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.record_audio_button).setOnTouchListener(mDelayHideTouchListener);
        findViewById(R.id.send_text_button).setOnTouchListener(mDelayHideTouchListener);

    }

    // here we handle our network data
    private void handleNetworkData(String data) {

        // our string is deliminated by our special character
        String[] results = data.split(NetworkConstants.BROADCAST_EXTRA_SPECIAL_CHARACTER_DELIMINATOR);

        if(results.length < 3) {
            Toast.makeText(this,"Error with received message",Toast.LENGTH_LONG);
        }

        String name = results[0];
        String message = results[1];
        String ip = results[2];

        // add this message into client list
        addToClientList(name,ip);

        if(!TextUtils.isEmpty(message)) {
            speakOut(name + " says " + message);
        }
    };

    private void addToClientList(String name, String ip) {
        RemoteIntercomClient newClient = new RemoteIntercomClient(name,ip);
        clients.add(newClient);
    }

    // just a quick helper method to output speech from text
    private void speakOut(String textToSpeak) {

        tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null);
    }
}
