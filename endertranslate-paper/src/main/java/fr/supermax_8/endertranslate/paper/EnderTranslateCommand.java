package fr.supermax_8.endertranslate.paper;

import fr.supermax_8.endertranslate.core.EnderTranslate;
import fr.supermax_8.endertranslate.core.EnderTranslateConfig;
import fr.supermax_8.endertranslate.core.communication.ServerWebSocketClient;
import fr.supermax_8.endertranslate.core.communication.packets.ReloadPluginPacket;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class EnderTranslateCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!commandSender.hasPermission("endertranslate.admin")) return false;
        try {
            switch (args[0].toLowerCase()) {
                case "reload" -> {
                    CompletableFuture.runAsync(() -> {
                        commandSender.sendMessage("§7Reload...");
                        if (!EnderTranslateConfig.getInstance().isMainServer())
                            ServerWebSocketClient.getInstance().sendPacket(new ReloadPluginPacket());

                        EndertranslatePaper.getInstance().getScheduler().runLaterAsync(() -> {
                            EnderTranslate.getInstance().reload();
                            commandSender.sendMessage("§aEnderTranslate Reloaded !");
                        }, 2);
                    });
                }
                default -> sendHelp(commandSender);
            }
        } catch (Exception e) {
            sendHelp(commandSender);
        }
        return false;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(new String[]{
                "§8[§d§lEnderTranslate§8]",
                "§8Made by SuperMax_8",
                "§7- /endertranslate reload §fReload the plugin & the main server plugin if proxy config"
        });
    }

}