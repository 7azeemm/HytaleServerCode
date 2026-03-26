/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.server.core.modules.voice;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.packets.stream.StreamType;
import com.hypixel.hytale.protocol.packets.voice.VoiceData;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.io.handlers.game.GamePacketHandler;
import com.hypixel.hytale.server.core.modules.voice.VoiceModule;
import com.hypixel.hytale.server.core.modules.voice.VoicePlayerState;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class VoiceStreamHandler
extends SimpleChannelInboundHandler<Packet> {
    private final PacketHandler packetHandler;
    private final VoiceModule voiceModule;
    private final HytaleLogger logger;
    private volatile PlayerRef cachedPlayerRef;
    private volatile boolean loggedFirstPacket = false;
    private volatile boolean loggedFirstVoiceData = false;

    public VoiceStreamHandler(@Nonnull PacketHandler packetHandler) {
        this.packetHandler = packetHandler;
        this.voiceModule = VoiceModule.get();
        this.logger = this.voiceModule.getLogger();
    }

    @Override
    public void handlerAdded(@Nonnull ChannelHandlerContext ctx) throws Exception {
        this.packetHandler.setChannel(StreamType.Voice, ctx.channel());
        PacketHandler packetHandler = this.packetHandler;
        if (packetHandler instanceof GamePacketHandler) {
            GamePacketHandler gameHandler = (GamePacketHandler)packetHandler;
            this.cachedPlayerRef = gameHandler.getPlayerRef();
        }
        this.logger.at(Level.FINE).log("[VoiceStream] Voice stream registered for %s (channel active=%s, playerRef=%s)", this.packetHandler.getIdentifier(), ctx.channel().isActive(), this.cachedPlayerRef != null ? this.cachedPlayerRef.getUsername() : "null");
        super.handlerAdded(ctx);
    }

    @Override
    protected void channelRead0(@Nonnull ChannelHandlerContext ctx, @Nonnull Packet packet) {
        PlayerRef playerRef;
        if (!this.loggedFirstPacket) {
            this.loggedFirstPacket = true;
            this.logger.at(Level.FINE).log("[VoiceStream] First packet received from %s: %s", (Object)this.packetHandler.getIdentifier(), (Object)packet.getClass().getSimpleName());
        }
        if ((playerRef = this.getPlayerRef()) == null) {
            this.logger.at(Level.WARNING).log("[VoiceStream] No player ref for voice packet from %s", this.packetHandler.getIdentifier());
            return;
        }
        if (packet instanceof VoiceData) {
            VoiceData voiceData = (VoiceData)packet;
            this.handleVoiceData(playerRef, voiceData);
        } else {
            this.logger.at(Level.WARNING).log("[VoiceStream] Unexpected packet type %s from %s", (Object)packet.getClass().getSimpleName(), (Object)this.packetHandler.getIdentifier());
        }
    }

    private void handleVoiceData(@Nonnull PlayerRef playerRef, @Nonnull VoiceData data) {
        if (!this.voiceModule.isVoiceEnabled()) {
            return;
        }
        if (!this.loggedFirstVoiceData) {
            this.loggedFirstVoiceData = true;
            this.logger.at(Level.FINE).log("[VoiceStream] Routing first VoiceData from %s: seq=%d, dataSize=%d", playerRef.getUsername(), data.sequenceNumber, data.opusData != null ? data.opusData.length : 0);
        }
        if (this.voiceModule.isShutdown()) {
            return;
        }
        VoicePlayerState state = this.voiceModule.getPlayerState(playerRef.getUuid());
        if (state == null) {
            return;
        }
        if (state.isRoutingDisabled()) {
            return;
        }
        if (state.isSilenced()) {
            return;
        }
        if (this.voiceModule.isPlayerMuted(playerRef.getUuid())) {
            return;
        }
        if (!state.checkRateLimit(this.voiceModule.getMaxPacketsPerSecond(), this.voiceModule.getBurstCapacity())) {
            if (state.shouldLogRateLimit()) {
                this.logger.at(Level.WARNING).log("[VoiceStream] RATE_LIMITED: player=%s, tokens=%.2f, maxPps=%d, burstCapacity=%d", playerRef.getUsername(), state.getTokenBucket(), this.voiceModule.getMaxPacketsPerSecond(), this.voiceModule.getBurstCapacity());
            }
            return;
        }
        if (data.opusData == null || data.opusData.length == 0) {
            this.logger.at(Level.FINE).log("[VoiceStream] REJECTED_EMPTY: player=%s, seq=%d", (Object)playerRef.getUsername(), data.sequenceNumber);
            return;
        }
        if (data.opusData.length > this.voiceModule.getMaxPacketSize()) {
            this.logger.at(Level.WARNING).log("[VoiceStream] REJECTED_OVERSIZE: player=%s, size=%d, maxSize=%d", playerRef.getUsername(), data.opusData.length, this.voiceModule.getMaxPacketSize());
            return;
        }
        this.voiceModule.getVoiceExecutor(playerRef.getUuid()).execute(() -> {
            try {
                this.voiceModule.getVoiceRouter().routeVoiceFromCache(playerRef, data);
                state.resetConsecutiveErrors();
            }
            catch (Exception e) {
                int failures = state.incrementConsecutiveErrors();
                if (failures >= 10) {
                    this.logger.at(Level.WARNING).log("[VoiceStream] Disabled voice routing for %s after %d consecutive errors", (Object)playerRef.getUuid(), failures);
                    state.setRoutingDisabled(true);
                }
                ((HytaleLogger.Api)this.logger.at(Level.SEVERE).withCause(e)).log("[VoiceStream] Exception in routeVoiceFromCache for %s (failure %d/%d)", playerRef.getUuid(), failures, 10);
            }
        });
    }

    private PlayerRef getPlayerRef() {
        if (this.cachedPlayerRef != null) {
            return this.cachedPlayerRef;
        }
        PacketHandler packetHandler = this.packetHandler;
        if (packetHandler instanceof GamePacketHandler) {
            GamePacketHandler gameHandler = (GamePacketHandler)packetHandler;
            this.cachedPlayerRef = gameHandler.getPlayerRef();
        }
        return this.cachedPlayerRef;
    }

    @Override
    public void channelInactive(@Nonnull ChannelHandlerContext ctx) throws Exception {
        this.packetHandler.compareAndSetChannel(StreamType.Voice, ctx.channel(), null);
        this.logger.at(Level.FINE).log("[VoiceStream] Voice stream closed for %s", this.packetHandler.getIdentifier());
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(@Nonnull ChannelHandlerContext ctx, @Nonnull Throwable cause) {
        ((HytaleLogger.Api)this.logger.at(Level.WARNING).withCause(cause)).log("[VoiceStream] Exception in voice stream for %s", this.packetHandler.getIdentifier());
        ctx.close();
    }
}

