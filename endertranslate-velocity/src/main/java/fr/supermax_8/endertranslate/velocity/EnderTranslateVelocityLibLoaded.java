package fr.supermax_8.endertranslate.velocity;

import com.github.retrooper.packetevents.PacketEvents;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import me.tofaa.entitylib.APIConfig;
import me.tofaa.entitylib.EntityLib;
import me.tofaa.entitylib.velocity.VelocityEntityLibPlatform;

public class EnderTranslateVelocityLibLoaded {

    private final EndertranslateVelocity plugin;

    public EnderTranslateVelocityLibLoaded(EndertranslateVelocity plugin) {
        this.plugin = plugin;
    }

    public void onProxyInitialization(ProxyInitializeEvent event, ProxyServer server) {
        VelocityEntityLibPlatform platform = new VelocityEntityLibPlatform(plugin, server);
        APIConfig settings = new APIConfig(PacketEvents.getAPI())
                .tickTickables()
                .trackPlatformEntities()
                .usePlatformLogger();

        EntityLib.init(platform, settings);
    }


}