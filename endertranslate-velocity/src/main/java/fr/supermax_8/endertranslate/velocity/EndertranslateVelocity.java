package fr.supermax_8.endertranslate.velocity;

import com.github.retrooper.packetevents.PacketEvents;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.supermax_8.endertranslate.core.ETLoader;
import fr.supermax_8.endertranslate.core.EnderTranslate;
import fr.supermax_8.endertranslate.core.PacketEventsHandler;
import io.github.retrooper.packetevents.velocity.factory.VelocityPacketEventsBuilder;
import me.tofaa.entitylib.APIConfig;
import me.tofaa.entitylib.EntityLib;
import me.tofaa.entitylib.velocity.VelocityEntityLibPlatform;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
        id = "endertranslate",
        name = "EnderTranslate",
        version = "1.0.0"
)
public class EndertranslateVelocity {

    private Logger logger;

    private EnderTranslate enderTranslate;

    private ProxyServer proxyServer;
    private PluginContainer pluginContainer;
    private Path dataDirectory;

    @Inject
    public EndertranslateVelocity(Logger logger, ProxyServer proxyServer, PluginContainer pluginContainer, @DataDirectory Path dataDirectory) {
        this.proxyServer = proxyServer;
        this.pluginContainer = pluginContainer;
        this.dataDirectory = dataDirectory;
        this.logger = logger;

        long elapsedTime = ETLoader.loadLibs(dataDirectory.toFile());
        System.out.println("Libs loaded in " + elapsedTime + " ms");

        PacketEvents.setAPI(VelocityPacketEventsBuilder.build(proxyServer, pluginContainer, logger, dataDirectory));
        PacketEvents.getAPI().getSettings().reEncodeByDefault(false)
                .checkForUpdates(false)
                .bStats(false);
        PacketEvents.getAPI().load();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        enderTranslate = new EnderTranslate(
                dataDirectory.toFile(),
                obj -> ((Player) obj).getUniqueId(),
                id -> proxyServer.getPlayer(id),
                s -> logger.atInfo().log(s)
        );

        PacketEvents.getAPI().init();

        int protocolVersion = PacketEvents.getAPI().getServerManager().getVersion().getProtocolVersion();
        System.out.println("PROTO VERSION : " + protocolVersion);

        VelocityEntityLibPlatform platform = new VelocityEntityLibPlatform(this, proxyServer);
        APIConfig settings = new APIConfig(PacketEvents.getAPI())
                .tickTickables()
                .trackPlatformEntities()
                .usePlatformLogger();

        EntityLib.init(platform, settings);
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent e) {
        PacketEventsHandler handler = PacketEventsHandler.getInstance();
        if (handler == null) return;
        handler.getEntitiesMetaData().remove(e.getPlayer().getUniqueId());
    }

    @Subscribe
    public void onSwitchServ(ServerConnectedEvent e) {
        PacketEventsHandler handler = PacketEventsHandler.getInstance();
        if (handler == null) return;
        handler.getEntitiesMetaData().remove(e.getPlayer().getUniqueId());
    }

}