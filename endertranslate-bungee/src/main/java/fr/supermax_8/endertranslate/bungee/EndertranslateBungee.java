package fr.supermax_8.endertranslate.bungee;

import com.github.retrooper.packetevents.PacketEvents;
import fr.supermax_8.endertranslate.core.EnderTranslate;
import io.github.retrooper.packetevents.bungee.factory.BungeePacketEventsBuilder;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

public final class EndertranslateBungee extends Plugin {

    private EnderTranslate enderTranslate;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(BungeePacketEventsBuilder.build(this));
        PacketEvents.getAPI().getSettings().reEncodeByDefault(false)
                .checkForUpdates(true)
                .bStats(true);
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        enderTranslate = new EnderTranslate(
                getDataFolder(),
                obj -> ((ProxiedPlayer) obj).getUniqueId(),
                s -> ProxyServer.getInstance().getConsole().sendMessage(s)
        );

        PacketEvents.getAPI().init();
    }

    @Override
    public void onDisable() {
        enderTranslate.shutdown();
        PacketEvents.getAPI().terminate();
    }

}