package net.vyltx.inventory_transfer.networking.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.vyltx.inventory_transfer.event.ClientEvents;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * TargetConfirmPacket
 * Assigns the target player after it has been confirmed that they exist and are online
 */
public class TargetConfirmPacket {
    private final UUID targetUUID;

    public TargetConfirmPacket(UUID targetUUID) {
        this.targetUUID = targetUUID;
    }

    public TargetConfirmPacket(FriendlyByteBuf buf) {
        this.targetUUID = buf.readUUID();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(targetUUID);
    }

    /**
     * Handles assigning the target value to the targetUUID variable initialized in ClientEvents
     * @param  supplier     Environment for network packets
     */
    public void handle(Supplier<NetworkEvent.Context> supplier) {
        // Client-side only
        // Target has been properly selected through select packet, so now value is being assigned to targetUUID
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> ClientEvents.targetUUID = targetUUID);

        context.setPacketHandled(true);
    }
}
