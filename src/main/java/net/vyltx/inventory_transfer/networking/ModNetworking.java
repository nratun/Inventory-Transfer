package net.vyltx.inventory_transfer.networking;

import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry.ChannelBuilder;
import net.minecraftforge.network.simple.SimpleChannel;
import net.vyltx.inventory_transfer.InventoryTransfer;
import net.vyltx.inventory_transfer.networking.packets.ItemTransferPacket;
import net.vyltx.inventory_transfer.networking.packets.TargetConfirmPacket;
import net.vyltx.inventory_transfer.networking.packets.TargetSelectPacket;

/**
 * ModNetworking
 * Handles registering channel with which packets get sent
 */
public class ModNetworking {
    private static SimpleChannel INSTANCE;
    private static int packetID = 0;

    /**
     * Increments packet IDs by one
     * <p>
     * Registered keys include:

     * @return int
     */
    private static int id() {
        return packetID++;
    }

    /**
     * Sends and handles packets going in designated directions
     * <p>
     * Messages are encoded/decoded depending on if they're sent/received
     */
    public static void register() {
        SimpleChannel net = ChannelBuilder
                .named(ResourceLocation.fromNamespaceAndPath(InventoryTransfer.MOD_ID, "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        net.messageBuilder(ItemTransferPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(ItemTransferPacket::new)
                .encoder(ItemTransferPacket::toBytes)
                .consumerMainThread(ItemTransferPacket::handle)
                .add();

        net.messageBuilder(TargetSelectPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(TargetSelectPacket::new)
                .encoder(TargetSelectPacket::toBytes)
                .consumerMainThread(TargetSelectPacket::handle)
                .add();

        net.messageBuilder(TargetConfirmPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(TargetConfirmPacket::new)
                .encoder(TargetConfirmPacket::toBytes)
                .consumerMainThread(TargetConfirmPacket::handle)
                .add();
    }

    /**
     * Sends a message to the server
     *
     * @param  msg  the message to be sent
     */
    public static <MSG> void sendToServer(MSG msg) {
        INSTANCE.sendToServer(msg);
    }

    /**
     * Sends a message directly to the player
     *
     * @param  msg      the message to be sent
     * @param  player   the player who will receive the message
     */
    public static <MSG> void sendToPlayer(MSG msg, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }

    /**
     * Sends a message to all clients
     *
     * @param  msg  the message to be sent
     */
    public static <MSG> void sendToAllClients(MSG msg) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), msg);
    }
}
