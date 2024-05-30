package fr.supermax_8.endertranslate.spigot;

import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import fr.supermax_8.endertranslate.core.EnderTranslateConfig;
import fr.supermax_8.endertranslate.core.communication.ServerWebSocketClient;
import fr.supermax_8.endertranslate.core.communication.packets.PlayerChangeLanguagePacket;
import fr.supermax_8.endertranslate.core.language.Language;
import fr.supermax_8.endertranslate.core.language.LanguageManager;
import fr.supermax_8.endertranslate.core.player.TranslatePlayerManager;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LangCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player p)) return false;
        try {
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

            setLanguage(p, languageId);
        } catch (Exception e) {
            openGui(p);
        }
        return false;
    }

    private void setLanguage(Player p, String language) {
        TranslatePlayerManager.getInstance().setPlayerLanguage(p.getUniqueId(), language);
        if (!EnderTranslateConfig.getInstance().isMainServer()) {
            CompletableFuture.runAsync(() ->
                    ServerWebSocketClient.getInstance().sendPacket(new PlayerChangeLanguagePacket(p.getUniqueId(), language)));
        }
        EndertranslateSpigot.getInstance().getScheduler().runLater(() -> {
            p.updateInventory();
            p.sendMessage(EnderTranslateConfig.getInstance().getStartTag() + "endertranslate_langchange{" + language + "}" + EnderTranslateConfig.getInstance().getEndTag());
        }, 5);
    }


    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return LanguageManager.getInstance().getLanguages();
    }


    private void openGui(Player p) {
        int rows = (int) Math.ceil(LanguageManager.getInstance().getLanguageMap().size() / 9f);

        Gui gui = Gui.gui()
                .title(Component.text(EnderTranslateConfig.getInstance().getStartTag() + "endertranslate_chooselang" + EnderTranslateConfig.getInstance().getEndTag()))
                .rows(rows)
                .create();

        LanguageManager.getInstance().getLanguageMap().values().forEach(l -> {
            gui.addItem(new GuiItem(BannerUtils.getBanner(l.getTitle(), l.getMaterial(), l.getPatterns()), e -> {
                e.setCancelled(true);
                setLanguage(p, l.getId());
                p.closeInventory();
            }));
        });

        gui.setDefaultTopClickAction(e -> {
            e.setCancelled(true);
        });

        gui.open(p);
    }


}