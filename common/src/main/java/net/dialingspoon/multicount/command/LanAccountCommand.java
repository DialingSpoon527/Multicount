package net.dialingspoon.multicount.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.dialingspoon.multicount.server.interfaces.PlayerAdditions;
import net.dialingspoon.multicount.server.interfaces.PlayerManagerAdditions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;

public class LanAccountCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // /account [account number(optional)]
        dispatcher.register(Commands.literal("account")
                        .requires(source -> source.getServer() instanceof IntegratedServer server
                                && server.getMultiplayerScope() == MinecraftServer.MultiplayerScope.LAN)
                        .then(Commands.argument("account number", IntegerArgumentType.integer(1))
                                .executes(ctx -> run(ctx.getSource(), getInteger(ctx, "account number"))))
                        .executes(ctx -> run(ctx.getSource(), -1))); // Executes the command without an account number
    }

    private static int run(CommandSourceStack source, int i) throws CommandSyntaxException{
        ServerPlayer getplayer = source.getPlayer();
        if (getplayer == null) throw new SimpleCommandExceptionType(Component.literal("Account command must be run by player")).create();
        if (i != -1) {
            if (!getplayer.getStringUUID().equals(Minecraft.getInstance().player.getStringUUID())) {
                if (((PlayerAdditions) getplayer).getAccount() == i) {
                    throw new SimpleCommandExceptionType(Component.literal("That is the current account!")).create();
                } else {
                    ((PlayerManagerAdditions) source.getServer().getPlayerList())
                            .setAccount(getplayer.getUUID(), ((PlayerAdditions) getplayer).getAccount(), i);
                    getplayer.connection.disconnect(Component.literal("Switching accounts, please re-log."));
                }
            } else {
                throw new SimpleCommandExceptionType(Component.literal("Lan server source cannot use account command\nPlease log out and change on title screen")).create();
            }
        } else {
            Component text = Component.literal("Current account: " + ((PlayerAdditions) getplayer).getAccount());
            (source).sendSuccess(() -> text, false);
        }
        return 1;
    }
}
