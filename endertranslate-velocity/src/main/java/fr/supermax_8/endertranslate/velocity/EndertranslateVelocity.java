package fr.supermax_8.endertranslate.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.supermax_8.endertranslate.core.ETLoader;
import fr.supermax_8.endertranslate.core.EnderTranslate;
import fr.supermax_8.endertranslate.core.PacketEventsHandler;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
        id = "endertranslate",
        name = "EnderTranslate",
        version = "1.0.0",
        authors = "SuperMax_8"
)
public class EndertranslateVelocity {

    private Logger logger;

    private EnderTranslate enderTranslate;

    private ProxyServer proxyServer;
    private PluginContainer pluginContainer;
    private Path dataDirectory;
    private EnderTranslateVelocityLibLoaded libLoaded;

    @Inject
    public EndertranslateVelocity(Logger logger, ProxyServer proxyServer, PluginContainer pluginContainer, @DataDirectory Path dataDirectory) {
        this.proxyServer = proxyServer;
        this.pluginContainer = pluginContainer;
        this.dataDirectory = dataDirectory;
        this.logger = logger;


    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        long elapsedTime = ETLoader.loadLibs(dataDirectory.toFile());
        System.out.println("Libs loaded in " + elapsedTime + " ms");

        libLoaded = new EnderTranslateVelocityLibLoaded(this, proxyServer, pluginContainer, logger, dataDirectory);

        libLoaded.onProxyInitialization();

        enderTranslate = new EnderTranslate(
                dataDirectory.toFile(),
                obj -> ((Player) obj).getUniqueId(),
                id -> proxyServer.getPlayer(id),
                s -> logger.atInfo().log(s)
        );
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        libLoaded.shutdown();
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