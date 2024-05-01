package fr.supermax_8.endertranslate.paper;

import com.github.retrooper.packetevents.PacketEvents;
import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.ServerImplementation;
import fr.supermax_8.endertranslate.core.EnderTranslate;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
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
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().getSettings().reEncodeByDefault(false)
                .checkForUpdates(true)
                .bStats(true);
    }


    @Override
    public void onEnable() {
        instance = this;
        enderTranslate = new EnderTranslate(
                getDataFolder(),
                obj -> ((Player) obj).getUniqueId(),
                s -> Bukkit.getConsoleSender().sendMessage(s)
        );
        getCommand("language").setExecutor(new LangCommand());
        getCommand("endertranslate").setExecutor(new EnderTranslateCommand());

        PacketEvents.getAPI().init();
    }

    @Override
    public void onDisable() {
    }

}