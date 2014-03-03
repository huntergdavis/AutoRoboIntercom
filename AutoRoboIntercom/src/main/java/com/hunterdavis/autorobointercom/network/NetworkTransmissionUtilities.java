package com.hunterdavis.autorobointercom.network;

import com.hunterdavis.autorobointercom.util.AutoRoboApplication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by hunter on 3/3/14.
 */
public class NetworkTransmissionUtilities {

    public static void sendTextToAllClients(String textToSend) throws IndexOutOfBoundsException, IOException {
        String nameAndText = AutoRoboApplication.getName() + NetworkConstants.BROADCAST_EXTRA_SPECIAL_CHARACTER_DELIMINATOR + textToSend;

        DatagramSocket socket = new DatagramSocket(NetworkConstants.DEFAULT_PORT);
        byte buff[] = nameAndText.getBytes();
        DatagramPacket packet = new DatagramPacket(buff, buff.length, InetAddress.getByName(NetworkConstants.DEFAULT_GROUP),NetworkConstants.DEFAULT_PORT);
        socket.send(packet);
        socket.close();
    }
}
