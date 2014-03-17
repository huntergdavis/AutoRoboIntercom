package com.hunterdavis.autorobointercom.network;

import android.os.AsyncTask;
import android.util.Log;

import com.hunterdavis.autorobointercom.util.AutoRoboApplication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;

/**
 * Created by hunter on 3/3/14.
 */
public class NetworkTransmissionUtilities {

    private static final String TAG = "hunterhunter";

    // send socket communication on a quick background thread
    public static void sendTextToAllClients(String textToSend) throws IndexOutOfBoundsException, IOException {

        new AsyncTask<String,Object,Object>() {

            @Override
            protected Object doInBackground(String... params) {
                try {

                    sendTextToClients(params[0]);
                } catch (Exception e) {
                    Log.e(TAG, "exception sending to clients");
                }
                return null;
            }
        }.execute(textToSend);

    }

    public static void sendAlarmTextToAllClients(String[] recipients, String text, String metadata) {

        // todo - do something with recipients - for now all
        // todo - do something with metadata

        new AsyncTask<String,Object,Object>() {

            @Override
            protected Object doInBackground(String... params) {
                try {

                    sendTextToClients(params[0]);
                } catch (Exception e) {
                    Log.e(TAG, "exception sending to clients");
                }
                return null;
            }
        }.execute(text);

    }

    private static void sendTextToClients(String textToSend) throws IndexOutOfBoundsException, IOException {
        String nameAndText = AutoRoboApplication.getName() + NetworkConstants.BROADCAST_EXTRA_SPECIAL_CHARACTER_DELIMINATOR + textToSend;

        Log.e(TAG,"Sending this to clients:"+nameAndText);

        DatagramChannel channel = DatagramChannel.open();
        DatagramSocket socket = channel.socket();
        socket.setReuseAddress(true);
        socket.setBroadcast(true);
        socket.bind(new InetSocketAddress(NetworkConstants.DEFAULT_PORT));

        byte buff[] = nameAndText.getBytes();
        DatagramPacket packet = new DatagramPacket(buff, buff.length, InetAddress.getByName(NetworkConstants.DEFAULT_GROUP),NetworkConstants.DEFAULT_PORT);
        socket.send(packet);
        socket.close();
    }
}
