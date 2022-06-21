package com.hughbone.fabrilousupdater.command;

import com.hughbone.fabrilousupdater.platform.ModPlatform;
import com.hughbone.fabrilousupdater.util.FabUtil;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;

public class UpdateCommand {
    public void register(String env) {
        if (env.equals("CLIENT")) {
            registerClient();
        } else {
            registerServer();
        }
    }

    private void registerClient() {


        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("fabdate")
                    .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("update").executes(ctx -> {
                                PlayerEntity player = ClientPlayerHack.getPlayer(ctx);
                                if (FabUtil.modPresentOnServer && player.hasPermissionLevel(4)) {
                                    player.sendMessage(Text.of("Note: Use '/fabdateserver update' for server mods."), false);
                                }
                                new StartThread(player).start();
                                return 1;
                            })
                    ));
        });
    }

    private void registerServer() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(CommandManager.literal("fabdateserver").requires(source -> source.hasPermissionLevel(4))
                .then(CommandManager.literal("update").executes(ctx -> {
                    new StartThread(ctx.getSource().getPlayer()).start();
                    return 1;
                }))
        ));
    }

    private static class StartThread extends Thread {
        PlayerEntity player;

        public StartThread(PlayerEntity player) {
            this.player = player;
        }

        public void run() {
            if (ModPlatform.isRunning) {
                player.sendMessage(Text.of("[Error] Already checking for updates!"), false);
            } else {
                player.sendMessage(Text.of("[FabrilousUpdater] Searching for updates. This may take a while..."), false);
                new ModPlatform().start(player, "update");
                player.sendMessage(Text.of("[FabrilousUpdater] Finished!"), false);
            }
        }
    }
}
