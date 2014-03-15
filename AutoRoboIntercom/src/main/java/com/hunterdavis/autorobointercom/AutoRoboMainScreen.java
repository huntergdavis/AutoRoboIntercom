package com.hunterdavis.autorobointercom;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hunterdavis.autorobointercom.network.NetworkAnnounceThread;
import com.hunterdavis.autorobointercom.network.NetworkConstants;
import com.hunterdavis.autorobointercom.network.NetworkReceiverThread;
import com.hunterdavis.autorobointercom.network.NetworkTransmissionUtilities;
import com.hunterdavis.autorobointercom.network.RemoteIntercomClient;
import com.hunterdavis.autorobointercom.util.AutoRoboApplication;
import com.hunterdavis.autorobointercom.util.MessageProcessing;
import com.hunterdavis.autorobointercom.util.SystemUiHider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
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

    private static final String TAG = "hunterhunterAutoRobo";

    private static final long CLEAR_OUT_CLIENTS_TIMOUT = 1000 * 60 * 10; // 10 minutes

    // just a request status for voice input
    protected static final int REQUEST_OK = 1337;

    // drawer layout
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

    // our TTS
    private TextToSpeech tts;

    // our client list adapter
    ArrayAdapter clientListAdapter;
    private String[] clientList;


    // our network receiver, retransmission threads
    private NetworkReceiverThread networkThread;
    private NetworkAnnounceThread networkAnnounceThread;

    private LinkedHashSet<RemoteIntercomClient> clients;

    private Handler myUIHandler;
    PowerManager.WakeLock wl;

    private int currentBatteryLevel = 0;

    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            clearOutOldClients();
            if(myUIHandler == null) {
                myUIHandler = new Handler(Looper.getMainLooper());
            }
            myUIHandler.postDelayed(this, CLEAR_OUT_CLIENTS_TIMOUT);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.autorobo_fullscreen);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AutoRobo");
        wl.acquire();

        clients = new LinkedHashSet<RemoteIntercomClient>();

        tts = new TextToSpeech(this, this);

        // setup our UI elements
        setupUI();


        // setup our UI handler
        myUIHandler = new Handler(Looper.getMainLooper());

        // set up our record audio button to actually record audio
        findViewById(R.id.record_audio_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                try {
                    //Log.e(TAG,"requesting got here at least");
                    startActivityForResult(i, REQUEST_OK);
                } catch (Exception e) {
                    Toast.makeText(v.getContext(), "Error initializing speech to text engine.", Toast.LENGTH_LONG).show();
                }
            }
        });


        // set up our send text button to actuall send text
        findViewById(R.id.send_text_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    NetworkTransmissionUtilities.sendTextToAllClients(((EditText) findViewById(R.id.text_to_send)).getText().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(v.getContext(), "Error sending text to clients.", Toast.LENGTH_LONG).show();
                } finally {
                    ((EditText) findViewById(R.id.text_to_send)).setText("");
                }

            }
        });

        setupClientListAdapter();

        networkThread = new NetworkReceiverThread();
        networkThread.start();

        networkAnnounceThread = new NetworkAnnounceThread();
        networkAnnounceThread.start();

        myUIHandler.postDelayed(mUpdateTimeTask,CLEAR_OUT_CLIENTS_TIMOUT);

    }

    AdapterView.OnItemClickListener adapterListenerForClients = new AdapterView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String name = ((TextView)view.findViewById(android.R.id.text1)).getText().toString();
            showChoiceForAUser(name);
        }
    };

    private void showChoiceForAUser(final String name) {
        CharSequence choices[] = new CharSequence[] {"Battery Level"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choo-Choo-Choose Command");
        builder.setItems(choices, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        String textToSend = name
                                + NetworkConstants.NON_SPOKEN_EXTRA_CHARACTER_DELIMINATOR + NetworkConstants.BATTERY_REQUEST +
                                NetworkConstants.NON_SPOKEN_EXTRA_CHARACTER_DELIMINATOR + "shawty!";

                        Log.d(TAG,"Sending " + textToSend);
                        // this is battery level
                        try {
                            NetworkTransmissionUtilities.sendTextToAllClients(textToSend);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        });
        builder.show();
    }

    private void setupClientListAdapter() {
        // setup our client list adapter
        clientList = getClientNameList();
        clientListAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                clientList);
        ListView listview = (ListView)findViewById(R.id.info_list);
        listview.setAdapter(clientListAdapter);
        listview.setOnItemClickListener(adapterListenerForClients);
    }

    private BroadcastReceiver networkDataReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String networkMessage = intent.getStringExtra(NetworkConstants.BROADCAST_EXTRA_STRING_UDP_MESSAGE);
            //Log.e(TAG,"network messasge is: " + networkMessage);
            handleNetworkData(networkMessage);
        }
    };

    @Override
    protected void onPause() {

        LocalBroadcastManager.getInstance(this).unregisterReceiver(networkDataReceiver);
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter iff= new IntentFilter(NetworkConstants.BROADCAST_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(networkDataReceiver, iff);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Log.e(TAG,"got here at least");
        super.onActivityResult(requestCode, resultCode, data);


        //Log.e(TAG,"got here at least");

        if (requestCode==REQUEST_OK  && resultCode==RESULT_OK) {
            ArrayList<String> thingsYouSaid = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            if(thingsYouSaid.size() > 0) {
                //((TextView)findViewById(R.id.text_to_send)).setText(thingsYouSaid.get(0));
                Toast.makeText(this,"Message: " + thingsYouSaid.get(0) + "Sent!",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
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
                clientListAdapter.notifyDataSetChanged();
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



    @Override
    public void onDestroy() {
        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        networkAnnounceThread.setFinished();
        networkThread.setFinished();

        wl.release();

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

        // setup our drawer layout
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<CharSequence>(this,
                R.layout.drawer_list_item, AutoRoboApplication.menuDrawerChoices));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = null;
                switch (position) {
                    case 0:
                        intent = new Intent(AutoRoboApplication.getContext(), AutoRoboMainScreen.class);
                        startActivity(intent);
                        break;
                    case 1:
                        intent = new Intent(AutoRoboApplication.getContext(), AutoRoboMainScreen.class);
                        startActivity(intent);

                        break;
                    default:
                        break;
                }
            }
        });

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

        //Log.e(TAG,"name is " + name + ", and message is " + message + " and ip is" +ip);


        if(!TextUtils.isEmpty(message) && (!(message.equals(" ")))) {
            MessageProcessing.processMessage(name, message, ip,tts);

        }

        // add this message into client list
        addToClientList(name,ip);

    };


    private String[] getClientNameList() {
        ArrayList<String> clientNames = new ArrayList<String>();
        for(RemoteIntercomClient client : clients) {
            Log.d(TAG,"client name in list is: " + client.clientName);
            clientNames.add(client.clientName);
        }

        clientNames.add(AutoRoboApplication.getName() + "(this room)");

        Log.d(TAG,"client name size is" + clientNames.size());

        return clientNames.toArray(new String[clientNames.size()]);
    }

    private void addToClientList(String name, String ip) {
        RemoteIntercomClient newClient = new RemoteIntercomClient(name,ip);
        clients.add(newClient);

        // refresh our overall client list strings
        setupClientListAdapter();
        clientListAdapter.notifyDataSetChanged();
    }


    private void clearOutOldClients() {
        long currentTime = System.currentTimeMillis();

        Iterator<RemoteIntercomClient> clientIterator = clients.iterator();
        while (clientIterator.hasNext()) {
            if((currentTime - clientIterator.next().lastClientBroadcastTime) > CLEAR_OUT_CLIENTS_TIMOUT) {
                clientIterator.remove();
            }
        }

        // here we should refresh the UI adapter to the listview
        setupClientListAdapter();
        clientListAdapter.notifyDataSetChanged();

    } // end of clear out old clients function

} // end of class
