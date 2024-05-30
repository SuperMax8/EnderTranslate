package fr.supermax_8.endertranslate.spigot;

import fr.supermax_8.endertranslate.core.EnderTranslateConfig;
import fr.supermax_8.endertranslate.core.communication.ServerWebSocketClient;
import fr.supermax_8.endertranslate.core.communication.packets.PlayerLanguageRequest;
import fr.supermax_8.endertranslate.core.player.TranslatePlayerManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class PlayerListener implements Listener {


    @EventHandler
    public void playerChat(AsyncPlayerChatEvent e) {
        String message = e.getMessage();
        EnderTranslateConfig config = EnderTranslateConfig.getInstance();
        if (message.contains(config.getStartTag()) && message.contains(config.getEndTag()) && !e.getPlayer().hasPermission("endertranslate.chat"))
            e.setMessage(message.replaceAll(Pattern.quote(config.getStartTag()), ""));
    }


    @EventHandler
    public void join(PlayerJoinEvent e) {
        if (EnderTranslateConfig.getInstance().isMainServer()) return;
        CompletableFuture.runAsync(() -> {
            UUID playerId = e.getPlayer().getUniqueId();
            ServerWebSocketClient.getInstance().sendPacket(new PlayerLanguageRequest(playerId, null, language ->
                    TranslatePlayerManager.getInstance().setPlayerLanguage(playerId, language)));
        });
    }

}