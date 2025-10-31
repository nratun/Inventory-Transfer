package net.vyltx.inventory_transfer.networking.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class ItemTransferPacket {

    private final int slotNum;
    private final UUID targetUUID;

    public ItemTransferPacket(int slotNum, UUID targetUUID) {
        this.slotNum = slotNum;
        this.targetUUID = targetUUID;
    }

    // writing (make sure write is written in same order as what's being read
    public ItemTransferPacket(FriendlyByteBuf buf) {
        this.slotNum = buf.readInt();
        this.targetUUID = buf.readUUID();
    }

    /**
     * Must describe
     * <p>
     * Additioal content goes here
     *
     * @param  buf  Need to add contents
     * @return        Void
     */
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(slotNum);
        buf.writeUUID(targetUUID);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender == null) return;

            // Validate target player
            ServerPlayer target = sender.getServer().getPlayerList().getPlayer(targetUUID);
            // Making sure again that the person didn't go offline (even if they were a chosen target)
            if (target == null) {
                sender.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("Target player is offline."),
                        true
                );
                return;
            }

            // Get the container and slot the sender is hovering over
            AbstractContainerMenu menu = sender.containerMenu;
            // Making sure the slot number isn't out of bounds for whatever reason
            if (slotNum < 0 || slotNum >= menu.slots.size()) return;

            Slot slot = menu.getSlot(slotNum);
            ItemStack itemToSend = slot.getItem();
            if (itemToSend.isEmpty()) return; // nothing to send

            // Attempt to transfer the stack to target's inventory
            ItemStack remaining = itemToSend.copy();
            for (int i = 9; i <= 35 && !remaining.isEmpty(); i++) {
                ItemStack targetStack = target.getInventory().getItem(i);
                if (ItemStack.isSameItemSameTags(remaining, targetStack)) {
                    int maxStack = Math.min(targetStack.getMaxStackSize(), remaining.getMaxStackSize());
                    int spaceLeft = maxStack - targetStack.getCount();
                    if (spaceLeft > 0) {
                        int transferAmount = Math.min(remaining.getCount(), spaceLeft);
                        targetStack.grow(transferAmount);
                        remaining.shrink(transferAmount);
                    }
                }
            }

            // Hotbar merge
            for (int i = 0; i <= 8 && !remaining.isEmpty(); i++) {
                ItemStack targetStack = target.getInventory().getItem(i);
                if (ItemStack.isSameItemSameTags(remaining, targetStack)) {
                    int maxStack = Math.min(targetStack.getMaxStackSize(), remaining.getMaxStackSize());
                    int spaceLeft = maxStack - targetStack.getCount();
                    if (spaceLeft > 0) {
                        int transferAmount = Math.min(remaining.getCount(), spaceLeft);
                        targetStack.grow(transferAmount);
                        remaining.shrink(transferAmount);
                    }
                }
            }

            // Place into empty slots (inventory first, then hotbar) ===
            for (int i = 9; i <= 35 && !remaining.isEmpty(); i++) {
                if (target.getInventory().getItem(i).isEmpty()) {
                    target.getInventory().setItem(i, remaining.copy());
                    remaining = ItemStack.EMPTY;
                    break;
                }
            }

            for (int i = 0; i <= 8 && !remaining.isEmpty(); i++) {
                if (target.getInventory().getItem(i).isEmpty()) {
                    target.getInventory().setItem(i, remaining.copy());
                    remaining = ItemStack.EMPTY;
                    break;
                }
            }

            // Drop remainder at target's feet if inventory full
            if (!remaining.isEmpty()) {
                target.drop(remaining, false);
            }

            // Remove original item from sender
            slot.set(ItemStack.EMPTY);
            menu.broadcastChanges();
        });

        context.setPacketHandled(true);
        return true;
    }
}
