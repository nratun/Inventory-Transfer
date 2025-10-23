package net.vyltx.inventorytransfer.networking.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

// Handles content that will be in packets and sending between them
public class C2SPacket {
    // Empty constructor
    public C2SPacket(int slotNum, UUID target) {

    }

    public C2SPacket(FriendlyByteBuf buf) {

    }

    // writing (make sure write is written in same order as what's being read
    public void toBytes(FriendlyByteBuf buf) {

    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            ServerLevel level = player.serverLevel();
        });
        /* New way of checking server side, need to change to old
        if (level.isServerSide()) {
            ServerPlayer player = context.getSender();
        } else {
            context.setPacketHandled(true);
        }
        /*

         Here is where I would need to deal with checking player UUID is valid, and moving the inventory items

         Code to potentially use:

        // This is for if in the inventory, need to apply this to other containers as well
        ItemStack someItem = player.inventory.mainInventory[slotNum];
        // Maybe could directly change value by indexing, or by doing setStackInSlot?
        player.inventory.mainInventory[targetSlot] = someItem;
        player.mainInventory.setStackInSlot(targetSlot, someItem);
        player.mainInventory.setStackInSlot(slotNum, null);
         */

        // ServerLevel level = player.serverLevel();

        // Handle inventory transfer stuff
        context.setPacketHandled(true);
        return true;
    }
}
