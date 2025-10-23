package net.vyltx.inventorytransfer.event;

import net.minecraft.client.KeyMapping;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.vyltx.inventorytransfer.client.screen.PlayerSelectScreen;
import net.vyltx.inventorytransfer.networking.ModNetworking;
import net.vyltx.inventorytransfer.networking.packets.C2SPacket;
import net.vyltx.inventorytransfer.InventoryTransfer;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

// Initializes and checks content during gameplay
public class ClientEvents {
    public static KeyMapping sendKey;
    public static KeyMapping menuKey;
    public static UUID targetUUID;

    // Configures/initializes any client setup needed before gameplay starts (ex. register keybinds)
    @Mod.EventBusSubscriber(modid = InventoryTransfer.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModBusEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            InventoryTransfer.LOGGER.info("Client setup complete for {}", InventoryTransfer.MOD_ID);
        }

        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            sendKey = new KeyMapping(
                    "key.inventory_transfer.send_item",
                    GLFW.GLFW_KEY_G,
                    "key.categories.inventory_transfer"
            );

            menuKey = new KeyMapping(
                    "key.inventory_transfer.open_menu",
                    GLFW.GLFW_KEY_H,
                    "key.categories.inventory_transfer"
            );

            event.register(sendKey);
            event.register(menuKey);
        }
    }

    // Registered on Forge’s main event bus for any content that needs to be checked continuously during game
    @Mod.EventBusSubscriber(modid = InventoryTransfer.MOD_ID, value = Dist.CLIENT)
    public static class ClientForgeEvents {
        @SubscribeEvent
        public static void onKeyPress(InputEvent.Key event) {
            //FIXME add conditions for sending packet
            // Need to check the following:
            // Correct key is pressed ✅
            // Hovering over an item slot to send ✅
            // Item slot has something to send (not empty) ✅
            // Target player is online
            // Target player is selected to receive items (no target, no sending) ✅
            // Target player has inventory space to receive item (or can drop it at their feet)
            Minecraft mc = Minecraft.getInstance();

            if (menuKey.consumeClick() && mc.player != null) {
                Minecraft.getInstance().setScreen(new PlayerSelectScreen());
            }

            if (sendKey.consumeClick() && mc.player != null && mc.screen instanceof AbstractContainerScreen<?> screen) {
                Slot hoveredSlot = screen.getSlotUnderMouse();

                if (hoveredSlot != null && hoveredSlot.hasItem()) {
                    /*
                    // Check that player isn’t already carrying something
                    ItemStack carried = mc.player.containerMenu.getCarried();
                    if (!carried.isEmpty()) {
                        mc.player.displayClientMessage(Component.literal("Cannot send while holding another item."), true);
                        return;
                    }
                    */
                    if (targetUUID == null) {
                        InventoryTransfer.LOGGER.warn("Target player not found or offline. Cancelling send.");
                        mc.player.displayClientMessage(Component.literal("No target player selected."), true);
                        return;
                    }

                    // If all good, send packet with hovered slot index and target player UUID
                    int slotId = hoveredSlot.index;
                    ModNetworking.sendToServer(new C2SPacket(slotId, targetUUID));
                }
            }
        }
    }
}
