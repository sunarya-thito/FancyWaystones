package thito.fancywaystones.proxy.message;

import thito.fancywaystones.location.TeleportState;
import thito.fancywaystones.proxy.SerializableLocation;

import java.util.UUID;

public class RefundTeleportationMessage extends Message {
    private SerializableLocation source, target;
    private UUID playerUUID;
    private TeleportState state;

    public RefundTeleportationMessage(SerializableLocation source, SerializableLocation target, UUID playerUUID, TeleportState state) {
        this.source = source;
        this.target = target;
        this.playerUUID = playerUUID;
        this.state = state;
    }

    public TeleportState getState() {
        return state;
    }

    public SerializableLocation getSource() {
        return source;
    }

    public SerializableLocation getTarget() {
        return target;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }
}
