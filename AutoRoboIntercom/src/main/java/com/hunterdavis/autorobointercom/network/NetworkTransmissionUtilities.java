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

    // send socket communication on a quick background thread
    public static void sendTextToAllClients(String textToSend) throws IndexOutOfBoundsException, IOException {

        new AsyncTask<String,Object,Object>() {

            @Override
            protected Object doInBackground(String... params) {
                try {
                    sendTextToClients(params[0]);
                } catch (Exception e) {
                    Log.e("hunter", "exception sending to clients");
                }
                return null;
            }
        }.execute(textToSend);

    }

    private static void sendTextToClients(String textToSend) throws IndexOutOfBoundsException, IOException {
        String nameAndText = AutoRoboApplication.getName() + NetworkConstants.BROADCAST_EXTRA_SPECIAL_CHARACTER_DELIMINATOR + textToSend;

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
