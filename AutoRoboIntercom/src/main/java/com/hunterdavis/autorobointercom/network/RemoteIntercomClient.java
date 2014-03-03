package com.hunterdavis.autorobointercom.network;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by hunter on 3/3/14.
 */
public class RemoteIntercomClient {


    public String clientName;
    public String clientMacAddress;
    public String clientIpAddress;
    public long lastClientBroadcastTime;

    public RemoteIntercomClient(String clientName, String clientIpAddress) {
        this.clientName = clientName;
        this.clientMacAddress = getMacFromArpCache(clientIpAddress);
        this.clientIpAddress = clientIpAddress;
        setLastClientBroadcastTime();
    }

    public void setLastClientBroadcastTime() {
        lastClientBroadcastTime = System.currentTimeMillis();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RemoteIntercomClient that = (RemoteIntercomClient) o;

        if (lastClientBroadcastTime != that.lastClientBroadcastTime) return false;
        if (!clientIpAddress.equals(that.clientIpAddress)) return false;
        if (!clientMacAddress.equals(that.clientMacAddress)) return false;
        if (!clientName.equals(that.clientName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = clientName.hashCode();
        result = 31 * result + clientMacAddress.hashCode();
        result = 31 * result + clientIpAddress.hashCode();
        result = 31 * result + (int) (lastClientBroadcastTime ^ (lastClientBroadcastTime >>> 32));
        return result;
    }

    /**
     * Try to extract a hardware MAC address from a given IP address using the
     * ARP cache (/proc/net/arp).<br>
     * <br>
     * We assume that the file has this structure:<br>
     * <br>
     * IP address       HW type     Flags       HW address            Mask     Device
     * 192.168.18.11    0x1         0x2         00:04:20:06:55:1a     *        eth0
     * 192.168.18.36    0x1         0x2         00:22:43:ab:2a:5b     *        eth0
     *
     * @param ip
     * @return the MAC from the ARP cache
     */
    public static String getMacFromArpCache(String ip) {
        if (ip == null)
            return null;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4 && ip.equals(splitted[0])) {
                    // Basic sanity check
                    String mac = splitted[3];
                    if (mac.matches("..:..:..:..:..:..")) {
                        return mac;
                    } else {
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
