package net.vyltx.inventory_transfer;

import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.vyltx.inventory_transfer.networking.ModNetworking;
import org.slf4j.Logger;

/**
 * InventoryTransfer Mod
 * Allows players to send items from their inventory or any open container (e.g. chest)
 * to another player currently in the world.
 */
@Mod(InventoryTransfer.MOD_ID)
public class InventoryTransfer
{
    public static final String MOD_ID = "inventory_transfer";
    public static final Logger LOGGER = LogUtils.getLogger();

    public InventoryTransfer(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        // May have an issue being in enqueue work, may need to register it outside of it
        event.enqueueWork(ModNetworking::register);
    }

    // Use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
    }

    // Use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {

        }
    }
}
