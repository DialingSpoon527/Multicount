package net.dialingspoon.multicount.neoforge;

import net.dialingspoon.multicount.Multicount;
import net.dialingspoon.multicount.command.LanAccountCommand;
import net.dialingspoon.multicount.server.command.AccountCommand;
import net.dialingspoon.multicount.server.command.MaxAccountCommand;
import net.dialingspoon.multicount.server.command.MaxAccountQueryCommand;
import net.dialingspoon.multicount.server.config.ModConfigs;
import net.dialingspoon.multicount.server.util.Updater;
import net.dialingspoon.multicount.util.AccountStates;
import net.dialingspoon.multicount.util.SingleplayerAccountHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

import java.util.UUID;

@EventBusSubscriber(modid = Multicount.MOD_ID)
public class NeoForgeEvents {
    @EventBusSubscriber(modid = Multicount.MOD_ID)
    public static class CommonNeoForgeEvents {
        @SubscribeEvent
        public static void onServerStarted(ServerStartedEvent event) {
            Multicount.accountStates = event.getServer().getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(
                    new SavedDataType<>(Identifier.fromNamespaceAndPath(Multicount.MOD_ID, "playerdata"), AccountStates::new, AccountStates.CODEC, DataFixTypes.LEVEL)
            );
        }
    }

    @EventBusSubscriber(modid = Multicount.MOD_ID, value = Dist.CLIENT)
    public static class ClientNeoForgeEvents {
        @SubscribeEvent
        public static void onRegisterCommands(RegisterCommandsEvent event) {
            LanAccountCommand.register(event.getDispatcher());
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            Multicount.accountHandler.uuid = Minecraft.getInstance().getUser().getProfileId().toString();
        }

        @SubscribeEvent
        public static void onServerStopped(ServerStoppedEvent event) {
            SingleplayerAccountHandler accountHandler = Multicount.accountHandler;
            Multicount.accountStates.setValue(UUID.fromString(accountHandler.uuid), accountHandler.account);
            accountHandler.saveAccount(event.getServer());
        }
    }

    @EventBusSubscriber(modid = Multicount.MOD_ID, value = Dist.DEDICATED_SERVER)
    public static class ServerNeoForgeEvents {
        @SubscribeEvent
        public static void onDedicatedServerSetup(FMLDedicatedServerSetupEvent event) {
            Multicount.configs = new ModConfigs();
        }

        @SubscribeEvent
        public static void onServerStarted(ServerStartedEvent event) {
            Updater.checkAndUpdateDataFormat(event.getServer());
        }

        @SubscribeEvent
        public static void onRegisterCommands(RegisterCommandsEvent event) {
            AccountCommand.register(event.getDispatcher());
            MaxAccountQueryCommand.register(event.getDispatcher());
            if (Multicount.configs.command) {
                MaxAccountCommand.register(event.getDispatcher());
            }
        }
    }
}
