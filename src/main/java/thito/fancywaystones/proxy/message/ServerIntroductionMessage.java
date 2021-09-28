package thito.fancywaystones.proxy.message;

public class ServerIntroductionMessage extends Message {
    private String serverName;

    public ServerIntroductionMessage(String serverName) {
        this.serverName = serverName;
    }

    public String getServerName() {
        return serverName;
    }

}
