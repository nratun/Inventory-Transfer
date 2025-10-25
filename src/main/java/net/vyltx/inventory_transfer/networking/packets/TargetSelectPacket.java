package net.vyltx.inventory_transfer.networking.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.vyltx.inventory_transfer.networking.ModNetworking;

import java.util.UUID;
import java.util.function.Supplier;

public class TargetSelectPacket {
    private final String targetName;
    public TargetSelectPacket(String targetName) {
        this.targetName = targetName;
    }

    public TargetSelectPacket(FriendlyByteBuf buf) {
        this.targetName = buf.readUtf(16); // max player name length
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(targetName);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender == null) return;

            // Check if target player exists and is online
            ServerPlayer target = sender.getServer().getPlayerList().getPlayerByName(targetName);
            if (target == null) {
                sender.displayClientMessage(Component.literal("Player '" + targetName + "' not found or offline."), true);
                return;
            }

            // At this point, target is valid, so send confirmation back to client
            ModNetworking.sendToPlayer(new TargetConfirmPacket(target.getUUID()), sender);
        });

        context.setPacketHandled(true);
        return true;
    }
}
