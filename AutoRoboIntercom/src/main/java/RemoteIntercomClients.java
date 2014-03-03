/**
 * Created by hunter on 3/3/14.
 */
public class RemoteIntercomClients {


    public String clientName;
    public String clientMacAddress;
    public long lastClientBroadcastTime;

    public RemoteIntercomClients(String clientName, String clientMacAddress) {
        this.clientName = clientName;
        this.clientMacAddress = clientMacAddress;
        setLastClientBroadcastTime();
    }

    private void setLastClientBroadcastTime() {
        lastClientBroadcastTime = System.currentTimeMillis();
    }
}
