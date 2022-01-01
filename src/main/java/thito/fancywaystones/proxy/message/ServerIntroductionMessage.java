package thito.fancywaystones.proxy.message;

public class ServerIntroductionMessage extends Message {
    private static final long serialVersionUID = 1L;
    private String serverName;

    public ServerIntroductionMessage(String serverName) {
        this.serverName = serverName;
    }

    public String getServerName() {
        return serverName;
    }

}
