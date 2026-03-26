package com.example.exampleplugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.hypixel.hytale.server.core.io.adapter.PacketAdapters;
import com.hypixel.hytale.server.core.io.adapter.PacketWatcher;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class ExamplePlugin extends JavaPlugin {
    private static final Logger LOGGER = Logger.getLogger("PacketInterceptor");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final File captureFile = new File("packets.json");
    private final JsonObject capturedData = new JsonObject();
    private final Set<Integer> capturedIds = ConcurrentHashMap.newKeySet();

    public ExamplePlugin(JavaPluginInit init) {
        super(init);
    }

    @Override protected void setup() {
        try {
            if (captureFile.exists()) {
                String content = Files.readString(captureFile.toPath());
                JsonObject loaded = GSON.fromJson(content, JsonObject.class);
                if (loaded != null) {
                    loaded.entrySet().forEach(entry -> {
                        capturedData.addProperty(entry.getKey(), entry.getValue().getAsString());
                        try {
                            capturedIds.add(Integer.parseInt(entry.getKey()));
                        } catch (NumberFormatException ignored) {}
                    });
                }
            }

            PacketAdapters.registerOutbound((PacketWatcher) (handler, packet) -> {
                int packetId = packet.getId();
                if (!capturedIds.add(packetId)) return;

                ByteBuf payload = Unpooled.buffer(16384);
                try {
                    packet.serialize(payload);
                    int size = payload.readableBytes();

                    String base64 = "";
                    if (size > 0) {
                        byte[] bytes = new byte[size];
                        payload.readBytes(bytes);
                        base64 = Base64.getEncoder().encodeToString(bytes);
                    }

                    capturedData.addProperty(String.valueOf(packetId), base64);

                    LOGGER.info(String.format(
                            "[Captured] ID %d | %s | %d bytes",
                            packetId, packet.getClass().getSimpleName(), size
                    ));

                    saveCapture();
                } catch (Exception e) {
                    LOGGER.warning("Failed to serialize/capture packet ID " + packetId + ": " + e.getMessage());
                } finally {
                    payload.release();
                }
            });

            LOGGER.info("Packet capture initialized -> saving unique packets to: " + captureFile.getAbsolutePath());
        } catch (Exception e) {
            LOGGER.severe("Failed to initialize packet capture: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveCapture() {
        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(captureFile),
                StandardCharsets.UTF_8 )) {
            GSON.toJson(capturedData, writer);
            LOGGER.fine("Captured packets saved (" + capturedData.size() + " unique types)");
        } catch (Exception e) {
            LOGGER.severe("Failed to save captured packets JSON: " + e.getMessage());
        }
    }
}
