package fr.supermax_8.endertranslate.velocity;

import com.github.retrooper.packetevents.PacketEvents;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.ProxyServer;
import io.github.retrooper.packetevents.velocity.factory.VelocityPacketEventsBuilder;
import me.tofaa.entitylib.APIConfig;
import me.tofaa.entitylib.EntityLib;
import me.tofaa.entitylib.velocity.VelocityEntityLibPlatform;
import org.slf4j.Logger;

import java.nio.file.Path;

public class EnderTranslateVelocityLibLoaded {

    private final EndertranslateVelocity plugin;
    private final ProxyServer server;
    private final PluginContainer pluginContainer;
    private final Logger logger;
    private final Path dataDirectory;

    public EnderTranslateVelocityLibLoaded(EndertranslateVelocity plugin, ProxyServer server, PluginContainer pluginContainer, Logger logger, Path dataDirectory) {
        this.plugin = plugin;
        this.server = server;
        this.pluginContainer = pluginContainer;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    public void onLoad() {
    }

    public void onProxyInitialization() {
        // PacketEvents
        PacketEvents.setAPI(VelocityPacketEventsBuilder.build(server, pluginContainer, logger, dataDirectory));
        PacketEvents.getAPI().getSettings().reEncodeByDefault(false)
                .checkForUpdates(false)
                .bStats(false);
        PacketEvents.getAPI().init();
        PacketEvents.getAPI().load();

        int protocolVersion = PacketEvents.getAPI().getServerManager().getVersion().getProtocolVersion();
        System.out.println("PROTO VERSION : " + protocolVersion);

        // EntityLib
        VelocityEntityLibPlatform platform = new VelocityEntityLibPlatform(plugin, server);
        APIConfig settings = new APIConfig(PacketEvents.getAPI())
                .tickTickables()
                .usePlatformLogger();

        EntityLib.init(platform, settings);
    }


    public void shutdown() {
        PacketEvents.getAPI().terminate();
    }

}