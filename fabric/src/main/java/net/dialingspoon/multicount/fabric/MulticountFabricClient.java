package net.dialingspoon.multicount.fabric;

import net.dialingspoon.multicount.Multicount;
import net.dialingspoon.multicount.command.LanAccountCommand;
import net.dialingspoon.multicount.util.SingleplayerAccountHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.client.Minecraft;

import java.util.UUID;

public class MulticountFabricClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> LanAccountCommand.register(dispatcher));

		SingleplayerAccountHandler accountHandler = Multicount.accountHandler;
		accountHandler.uuid = Minecraft.getInstance().getUser().getProfileId().toString();

		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			Multicount.accountStates.setValue(UUID.fromString(accountHandler.uuid), accountHandler.account);
			accountHandler.saveAccount(server);
		});
	}
}
