package fr.supermax_8.endertranslate.spigot;

import fr.supermax_8.endertranslate.core.ETLoader;
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
        ETLoader.libsLink.add("https://github.com/retrooper/packetevents/releases/download/v2.11.2/packetevents-spigot-2.11.2.jar");
        long elapsedTime = ETLoader.loadLibs(getDataFolder());
        System.out.println("Libs loaded in " + elapsedTime + " ms");

        initSpigot();
        spigot.onLoad();
    }


    @Override
    public void onEnable() {
        instance = this;
        initSpigot();
        spigot.onEnable();
    }

    @Override
    public void onDisable() {
        spigot.onDisable();
    }

    private synchronized void initSpigot() {
        if (spigot == null) spigot = new EndertranslateSpigot(this);
    }

}