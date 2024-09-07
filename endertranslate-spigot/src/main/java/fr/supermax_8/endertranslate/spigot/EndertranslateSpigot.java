package fr.supermax_8.endertranslate.spigot;

import com.github.retrooper.packetevents.PacketEvents;
import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.ServerImplementation;
import fr.supermax_8.endertranslate.core.EnderTranslate;
import lombok.Getter;
import me.tofaa.entitylib.APIConfig;
import me.tofaa.entitylib.EntityLib;
import me.tofaa.entitylib.spigot.SpigotEntityLibPlatform;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class EndertranslateSpigot {

    @Getter
    private static EndertranslateSpigot instance;

    private EnderTranslate enderTranslate;
    @Getter
    private final FoliaLib folia;
    @Getter
    private final ServerImplementation scheduler;

    private final EnderTranslateSpigotPlugin plugin;

    public EndertranslateSpigot(EnderTranslateSpigotPlugin plugin) {
        this.plugin = plugin;
        folia = new FoliaLib(plugin);
        scheduler = folia.getImpl();
    }

    public void onEnable() {
        instance = this;
        enderTranslate = new EnderTranslate(
                plugin.getDataFolder(),
                obj -> ((Player) obj).getUniqueId(),
                Bukkit::getPlayer,
                s -> Bukkit.getConsoleSender().sendMessage(s)
        );

        if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderManager().register();
            EnderTranslate.log("PlaceholderAPI hook init !");
        }

        plugin.getCommand("language").setExecutor(new LangCommand());
        plugin.getCommand("endertranslate").setExecutor(new EnderTranslateCommand());
        plugin.getServer().getPluginManager().registerEvents(new PlayerListener(), plugin);

        SpigotEntityLibPlatform platform = new SpigotEntityLibPlatform(plugin);
        APIConfig settings = new APIConfig(PacketEvents.getAPI())
                .tickTickables()
                .trackPlatformEntities()
                .usePlatformLogger();

        EntityLib.init(platform, settings);
    }

}