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

public class AutoUpdateCommand {
    public void register(String env) {
        if (env.equals("CLIENT")) {
            registerClient();
        } else {
            registerServer();
        }
    }

    private Text getWarningMessage(String env) {
        return Text.Serializer.fromJson("[\"\",{\"text\":\"[Warning] \",\"color\":\"red\"},\"This command automatically downloads new versions and moves old mods into the 'Outdated_Mods' folder. \",{\"text\":\"Click here to continue.\",\"color\":\"dark_green\",\"clickEvent\":{" +
                "\"action\":\"run_command\",\"value\":\"/ඞmogus" + env + "\"}}]");
    }

    private void registerClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("fabdate")
                    .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("autoupdate").executes(ctx -> {
                        PlayerEntity player = ClientPlayerHack.getPlayer(ctx);

                        if (FabUtil.modPresentOnServer && player.hasPermissionLevel(4)) {
                            player.sendMessage(Text.of("Note: Use '/fabdateserver update' for server mods."), false);
                        }
                        player.sendMessage(getWarningMessage("Client"), false);
                        return 1;
                    }))
            );
        });


        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("ඞmogusClient")
                    .executes(ctx -> {
                        new StartThread(ClientPlayerHack.getPlayer(ctx)).start();
                        return 1;
                    })
            );
        });

    }

    private void registerServer() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(CommandManager.literal("fabdateserver").requires(source -> source.hasPermissionLevel(4))
                .then(CommandManager.literal("autoupdate").executes(ctx -> {
                    ctx.getSource().getPlayer().sendMessage(getWarningMessage("Server"), false);
                    return 1;
                }))
        ));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(CommandManager.literal("ඞmogusServer").requires(source -> source.hasPermissionLevel(4))
                .executes(ctx -> {
                    new StartThread(ctx.getSource().getPlayer()).start();
                    return 1;
                })
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
                player.sendMessage(Text.of("[Fabrilous Updater] Automatically updating all mods..."), false);
                new ModPlatform().start(player, "autoupdate");
                player.sendMessage(Text.of("[FabrilousUpdater] Finished! Restart Minecraft to apply updates."), false);
            }
        }
    }
}
