package fr.supermax_8.endertranslate.paper;

import fr.supermax_8.endertranslate.core.EnderTranslateConfig;
import fr.supermax_8.endertranslate.core.communication.ServerWebSocketClient;
import fr.supermax_8.endertranslate.core.communication.packets.PlayerChangeLanguagePacket;
import fr.supermax_8.endertranslate.core.language.Language;
import fr.supermax_8.endertranslate.core.language.LanguageManager;
import fr.supermax_8.endertranslate.core.player.TranslatePlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class LangCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player p)) return false;
        LanguageManager manager = LanguageManager.getInstance();
        if (manager == null) return false;
        String languageId = args[0];
        Language language = manager.getLanguage(languageId);
        if (language == null) {
            p.sendMessage("§cInvalid language");
            for (String l : manager.getLanguages()) p.sendMessage(s);
            p.sendMessage(manager.getLanguageMap().toString());
            return false;
        }

        if (EnderTranslateConfig.getInstance().isMainServer()) {
            TranslatePlayerManager.getInstance().setPlayerLanguage(p.getUniqueId(), languageId);
        } else {
            CompletableFuture.runAsync(() ->
                    ServerWebSocketClient.getInstance().sendPacket(new PlayerChangeLanguagePacket(p.getUniqueId(), languageId)));
        }
        EndertranslatePaper.getInstance().getScheduler().runLater(() -> {
            p.updateInventory();
        }, 5);
        return false;
    }


}