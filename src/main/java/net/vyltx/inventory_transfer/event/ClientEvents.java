package net.vyltx.inventory_transfer.event;

import net.minecraft.client.KeyMapping;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.vyltx.inventory_transfer.client.screen.PlayerSelectScreen;
import net.vyltx.inventory_transfer.client.screen.TargetOverlay;
import net.vyltx.inventory_transfer.networking.ModNetworking;
import net.vyltx.inventory_transfer.networking.packets.ItemTransferPacket;
import net.vyltx.inventory_transfer.InventoryTransfer;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;

import java.util.UUID;

/**
 * ClientEvents
 * Initializes and checks content during gameplay (keybinds, things happening during gameplay, etc.)
 */
public class ClientEvents {
    public static KeyMapping sendKey;
    public static KeyMapping menuKey;
    public static UUID targetUUID;
    public static TargetOverlay targetOverlay;

    // Configures/initializes any client setup needed before gameplay starts (ex. register keybinds)
    @Mod.EventBusSubscriber(modid = InventoryTransfer.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModBusEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            InventoryTransfer.LOGGER.info("Client setup complete for {}", InventoryTransfer.MOD_ID);
        }

        /**
         * Registers the necessary keys the mod will use
         * <p>
         * Registered keys include:
         * H - Opens up a GUI to select a target player
         * G - Transfers the selected/hovered item to the target player
         *
         * @param  event  Registers custom key mappings
         */
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
        /**
         * Determines what will occur when specific keys are pressed while playing
         * <p>
         * Actions associated with the following keys:
         * H - Opens up a GUI to select a target player
         * G - Transfers the item the player is actively holding to the target player
         * Note: The G key functionality has not yet been configured, it is currently left as a FIXME
         *
         * @param  event  An event that occurs when a specific key is pressed in the game
         */
        @SubscribeEvent
        public static void onKeyPress(InputEvent.Key event) {
            Minecraft mc = Minecraft.getInstance();

            if (menuKey.consumeClick() && mc.player != null) {
                Minecraft.getInstance().setScreen(new PlayerSelectScreen());
            }

            if (sendKey.consumeClick() && mc.player != null && mc.screen == null) {
                if (targetUUID == null) {
                    InventoryTransfer.LOGGER.warn("Target player not found or offline. Cancelling send.");
                    mc.player.displayClientMessage(Component.literal("No target player selected."), true);
                    return;
                }
                // If all good, send packet with hotbar slot index and target player UUID
                // (Note: Adding 36 because the hotbar ranges from 36-44)
                int slotId = mc.player.getInventory().selected + 36;
                ModNetworking.sendToServer(new ItemTransferPacket(slotId, targetUUID));
            }
        }

        /**
         * Determines what will occur when specific keys are pressed while the player is actively in a screen/container
         * <p>
         * Registered keys include:
         * G - Transfers the item that is hovered over by the player's mouse to the target player
         *
         * @param  event  An event that occurs when a specific key is pressed while a player is observing a screen
         */
        @SubscribeEvent
        public static void onGuiKeyPress(ScreenEvent.KeyPressed event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            // Make sure GUI is a container
            if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) return;

            // Check if pressed key matches send keybind
            if (sendKey.matches(event.getKeyCode(), event.getScanCode())) {
                Slot hoveredSlot = screen.getSlotUnderMouse();

                if (hoveredSlot != null && hoveredSlot.hasItem()) {
                    // if checking target status here, can consider removing it from ItemTransferPacket
                    if (targetUUID == null) {
                        InventoryTransfer.LOGGER.warn("Target player not found or offline. Cancelling send.");
                        mc.player.displayClientMessage(Component.literal("No target player selected."), true);
                        return;
                    }

                    // If all good, send packet with hovered slot index and target player UUID
                    int slotId = hoveredSlot.index;
                    ModNetworking.sendToServer(new ItemTransferPacket(slotId, targetUUID));
                }
                event.setCanceled(true);
            }
        }

        /**
         * Creates a small indicator when the inventory/container is open to remind users who their target is
         * <p>
         * The indicator includes the target player's name and an icon of their face
         * If the player has no target, the indicator does not appear
         *
         * @param  event     An event to be occurred AFTER the darkening and background effects of the screen have been rendered
         */
        @SubscribeEvent
        public static void onRenderOverlay(ScreenEvent.BackgroundRendered event) {

            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            // Only render overlay while viewing an inventory or container screen
            if (!(mc.screen instanceof net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?>)) return;

            // If there's no target, there's no overlay
            if (targetUUID == null) {
                targetOverlay = null;
                return;
            }

            var info = mc.getConnection().getPlayerInfo(targetUUID);
            if (info == null) return;

            // If there is no overlay, create a new one. Otherwise, modify the existing one
            if (targetOverlay == null) {
                targetOverlay = new TargetOverlay(info, 10, 10); // top left of screen
            } else {
                targetOverlay.setTarget(info);
            }

            // Render widget
            targetOverlay.renderWidget(event.getGuiGraphics(), 0, 0, mc.getFrameTime());
        }
    }
}
