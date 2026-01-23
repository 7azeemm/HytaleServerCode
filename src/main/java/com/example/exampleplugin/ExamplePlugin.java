package com.example.exampleplugin;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.auth.PlayerAuthentication;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.io.adapter.PacketAdapters;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import java.util.Arrays;

public class ExamplePlugin extends JavaPlugin {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public ExamplePlugin(JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("Hello from %s", this.getName());
        HytaleServer.get().getEventBus().register(PlayerConnectEvent.class, p -> {
            PlayerAuthentication auth = p.getPlayerRef().getPacketHandler().getAuth();
            assert auth != null;
            System.out.println(auth.getUsername());
            System.out.println(auth.getUuid());
            System.out.println(auth.getReferralSource());
            System.out.println(Arrays.toString(auth.getReferralData()));
        });
    }

    @Override
    protected void setup() {
        this.getCommandRegistry().registerCommand(new ExampleCommand(this.getName(), this.getManifest().getVersion().toString()));
    }
}
