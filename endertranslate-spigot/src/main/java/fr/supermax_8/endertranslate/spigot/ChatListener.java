package fr.supermax_8.endertranslate.spigot;

import fr.supermax_8.endertranslate.core.EnderTranslateConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.regex.Pattern;

public class ChatListener implements Listener {


    @EventHandler
    public void playerChat(AsyncPlayerChatEvent e) {
        String message = e.getMessage();
        EnderTranslateConfig config = EnderTranslateConfig.getInstance();
        if (message.contains(config.getStartTag()) && message.contains(config.getEndTag()) && !e.getPlayer().hasPermission("endertranslate.chat"))
            e.setMessage(message.replaceAll(Pattern.quote(config.getStartTag()), ""));
    }


}