package fr.supermax_8.endertranslate.spigot;

import fr.supermax_8.endertranslate.core.player.TranslatePlayerManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlaceholderManager extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "endertranslate";
    }

    @Override
    public @NotNull String getAuthor() {
        return "SuperMax_8";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    public String onPlaceholderRequest(final Player player, @NotNull final String params) {
        String[] paramss = params.split("_", 2);
        switch (paramss[0]) {
            case "lang" -> {
                String playerLanguage = TranslatePlayerManager.getInstance().getPlayerLanguage(player.getUniqueId());
                return "";//AdventureSerializer.toLegacyFormat(PacketEventsHandler.getInstance().translatePlaceholder(paramss[1], playerLanguage));
            }
            case "langjson" -> {
                String playerLanguage = TranslatePlayerManager.getInstance().getPlayerLanguage(player.getUniqueId());
                return "";//AdventureSerializer.toJson(PacketEventsHandler.getInstance().translatePlaceholder(paramss[1], playerLanguage));
            }
        }
        return null;
    }

}