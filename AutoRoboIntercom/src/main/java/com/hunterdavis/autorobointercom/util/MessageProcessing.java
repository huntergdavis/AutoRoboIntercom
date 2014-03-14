package com.hunterdavis.autorobointercom.util;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.hunterdavis.autorobointercom.network.NetworkConstants;
import com.hunterdavis.autorobointercom.network.NetworkTransmissionUtilities;

import java.io.IOException;

/**
 * Created by hunter on 3/13/14.
 */
public class MessageProcessing {
    private static final String TAG = "hunterhunterAutoRobo";

    public static void processMessage(String senderName, String message, String ip, TextToSpeech tts) {
        //Log.e(TAG,"attempting to say: " + name + " says " + message);


        // this protocol is per-user, so see if this is to "us"

        if(message.contains(NetworkConstants.NON_SPOKEN_EXTRA_CHARACTER_DELIMINATOR)) {
            // we've got a non-spoken protocol
            String[] results = message.split(NetworkConstants.NON_SPOKEN_EXTRA_CHARACTER_DELIMINATOR);
            String nameToMatch = results[0];
            String requestName = results[1];
            String requestValue = results[2];
            Log.d(TAG, "nameToMatch is:" + nameToMatch + "and requestName is:" + requestName + " and Value is" + requestValue);

            if(nameToMatch.equalsIgnoreCase(AutoRoboApplication.getName())) {
                Log.d(TAG,"we've got a matched name between our name "+AutoRoboApplication.getName()+", and the send name"+nameToMatch+", which was sent from "+senderName);
                processARequest(requestName, requestValue,senderName, tts);
            }

        }else {
            speakOut(senderName + " says " + message, tts);
        }
    }

    private static void processARequest(String requestName, String requestValue, String senderName, TextToSpeech tts) {
        // first, let's check for our one known protocol - battery
        if(requestName.equalsIgnoreCase(NetworkConstants.BATTERY_CONFIRMATION)) {
            speakOut("The Battery In " + senderName + " is " + requestValue +" percent full.", tts);
        }else if(requestName.equalsIgnoreCase(NetworkConstants.BATTERY_REQUEST)) {
            updateGlobalBattery();
            try {
                NetworkTransmissionUtilities.sendTextToAllClients(senderName
                        + NetworkConstants.NON_SPOKEN_EXTRA_CHARACTER_DELIMINATOR + NetworkConstants.BATTERY_CONFIRMATION
                        + NetworkConstants.NON_SPOKEN_EXTRA_CHARACTER_DELIMINATOR + AutoRoboApplication.currentBatteryLevel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    // just a quick helper method to output speech from text
    public static void speakOut(String textToSpeak, TextToSpeech tts) {

        //Log.e(TAG,"attempting to say: " + textToSpeak);
        tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null);
    }



    public static void updateGlobalBattery() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent intent = AutoRoboApplication.getContext().registerReceiver(null, ifilter);
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = level / (float)scale;

        Log.d(TAG,"isCharging is : " + isCharging +
                ", and chargePlug is:" + chargePlug +
                ", and usbCharge is:" + usbCharge +
                ", and acCharge is:" + acCharge +
                ", and level is:" + level +
                ", and scale is:" + scale
        );

        AutoRoboApplication.currentBatteryLevel = level;
    }
}
