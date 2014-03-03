/**
 * Created by hunter on 3/3/14.
 */
public class RemoteIntercomClient {


    public String clientName;
    public String clientMacAddress;
    public String clientIpAddress;
    public long lastClientBroadcastTime;

    public RemoteIntercomClient(String clientName, String clientMacAddress, String clientIpAddress) {
        this.clientName = clientName;
        this.clientMacAddress = clientMacAddress;
        this.clientIpAddress = clientIpAddress;
        setLastClientBroadcastTime();
    }

    private void setLastClientBroadcastTime() {
        lastClientBroadcastTime = System.currentTimeMillis();
    }
}
