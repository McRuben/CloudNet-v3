package de.dytanic.cloudnet.driver.event.events.network;

import de.dytanic.cloudnet.driver.event.ICancelable;
import de.dytanic.cloudnet.driver.network.INetworkChannel;
import de.dytanic.cloudnet.driver.network.protocol.IPacket;
import lombok.Getter;
import lombok.Setter;

@Getter
public class NetworkChannelPacketReceiveEvent extends NetworkEvent implements ICancelable {

    @Setter
    private boolean cancelled;

    private final IPacket packet;

    public NetworkChannelPacketReceiveEvent(INetworkChannel channel, IPacket packet)
    {
        super(channel);
        this.packet = packet;
    }
}