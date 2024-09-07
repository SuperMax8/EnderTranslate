package fr.supermax_8.endertranslate.spigot;

import com.github.retrooper.packetevents.PacketEvents;
import fr.supermax_8.endertranslate.core.ETLoader;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class EnderTranslateSpigotPlugin extends JavaPlugin {

    private static EnderTranslateSpigotPlugin instance;
    private EndertranslateSpigot spigot;

    public static @NotNull Plugin getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        long elapsedTime = ETLoader.loadLibs(getDataFolder());

        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().getSettings().reEncodeByDefault(false)
                .checkForUpdates(false)
                .bStats(false);
        PacketEvents.getAPI().load();
    }


    @Override
    public void onEnable() {
        instance = this;
        spigot = new EndertranslateSpigot(this);
        spigot.onEnable();
    }

}