package net.vyltx.inventorytransfer.networking;

import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry.ChannelBuilder;
import net.minecraftforge.network.simple.SimpleChannel;
import net.vyltx.inventorytransfer.InventoryTransfer;
import net.vyltx.inventorytransfer.networking.packets.C2SPacket;

// handles registering channel with which packets get sent
public class ModNetworking {
    private static SimpleChannel INSTANCE;
    private static int packetID = 0;

    private static int id() {
        return packetID++;
    }
    public static void register() {
        SimpleChannel net = ChannelBuilder
                .named(ResourceLocation.fromNamespaceAndPath(InventoryTransfer.MOD_ID, "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        net.messageBuilder(C2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(C2SPacket::new)
                .encoder(C2SPacket::toBytes)
                .consumerMainThread(C2SPacket::handle)
                .add();
    }

    // Sends message to server
    public static <MSG> void sendToServer(MSG msg) {
        INSTANCE.sendToServer(msg);
    }

    // Send message directly to player
    public static <MSG> void sendToPlayer(MSG msg, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }

    public static <MSG> void sendToAllClients(MSG msg) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), msg);
    }
}
