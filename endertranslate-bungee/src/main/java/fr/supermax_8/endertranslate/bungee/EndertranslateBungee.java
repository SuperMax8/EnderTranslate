package fr.supermax_8.endertranslate.bungee;

import com.github.retrooper.packetevents.PacketEvents;
import fr.supermax_8.endertranslate.core.ETLoader;
import fr.supermax_8.endertranslate.core.EnderTranslate;
import fr.supermax_8.endertranslate.core.PacketEventsHandler;
import io.github.retrooper.packetevents.bungee.factory.BungeePacketEventsBuilder;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public final class EndertranslateBungee extends Plugin implements Listener {

    private EnderTranslate enderTranslate;

    @Override
    public void onLoad() {
        long elapsedTime = ETLoader.loadLibs(getDataFolder());


        PacketEvents.setAPI(BungeePacketEventsBuilder.build(this));
        PacketEvents.getAPI().getSettings().reEncodeByDefault(false)
                .checkForUpdates(false)
                .bStats(false);
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        enderTranslate = new EnderTranslate(
                getDataFolder(),
                obj -> ((ProxiedPlayer) obj).getUniqueId(),
                id -> getProxy().getPlayer(id),
                s -> ProxyServer.getInstance().getConsole().sendMessage(s)
        );

        ProxyServer.getInstance().getPluginManager().registerListener(this, this);

        PacketEvents.getAPI().init();
    }

    @Override
    public void onDisable() {
        enderTranslate.shutdown();
        PacketEvents.getAPI().terminate();
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent e) {
        PacketEventsHandler handler = PacketEventsHandler.getInstance();
        if (handler == null) return;
        handler.getPlayerEntitiesData().remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onSwitchServ(ServerConnectedEvent e) {
        PacketEventsHandler handler = PacketEventsHandler.getInstance();
        if (handler == null) return;
        handler.getPlayerEntitiesData().remove(e.getPlayer().getUniqueId());
    }

}