package fr.supermax_8.endertranslate.velocity;

import com.github.retrooper.packetevents.PacketEvents;
import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.supermax_8.endertranslate.core.ETLoader;
import fr.supermax_8.endertranslate.core.EnderTranslate;
import io.github.retrooper.packetevents.velocity.factory.VelocityPacketEventsBuilder;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
        id = "endertranslate",
        name = "EnderTranslate",
        version = "0.1.0"
)
public class EndertranslateVelocity {

    @Inject
    private Logger logger;

    private EnderTranslate enderTranslate;

    private ProxyServer proxyServer;
    private Path dataDirectory;

    @Inject
    public EndertranslateVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxyServer = server;
        this.dataDirectory = dataDirectory;

        long elapsedTime = ETLoader.loadLibs(dataDirectory.toFile());
        System.out.println("Libs loaded in " + elapsedTime + " ms");
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        PacketEvents.setAPI(VelocityPacketEventsBuilder.build(proxyServer, proxyServer.getPluginManager().fromInstance(this).get(), logger, dataDirectory));
        PacketEvents.getAPI().getSettings().reEncodeByDefault(false)
                .checkForUpdates(true)
                .bStats(true);

        enderTranslate = new EnderTranslate(
                dataDirectory.toFile(),
                obj -> ((Player) obj).getUniqueId(),
                s -> logger.atInfo().log(s)
        );

        PacketEvents.getAPI().init();
    }


}