package fr.supermax_8.endertranslate.paper;

import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.ServerImplementation;
import fr.supermax_8.endertranslate.core.EnderTranslate;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class EndertranslatePaper extends JavaPlugin {

    @Getter
    private static EndertranslatePaper instance;

    private EnderTranslate enderTranslate;
    @Getter
    private final FoliaLib folia = new FoliaLib(this);
    @Getter
    private final ServerImplementation scheduler = folia.getImpl();

    @Override
    public void onEnable() {
        instance = this;
        // Plugin startup logic
        enderTranslate = new EnderTranslate(
                getDataFolder(),
                obj -> ((Player) obj).getUniqueId(),
                s -> Bukkit.getConsoleSender().sendMessage(s)
        );
        getCommand("language").setExecutor(new LangCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

}