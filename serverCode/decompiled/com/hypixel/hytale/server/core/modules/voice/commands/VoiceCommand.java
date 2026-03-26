/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.server.core.modules.voice.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.auth.ProfileServiceClient;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.modules.voice.VoiceModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class VoiceCommand
extends AbstractCommandCollection {
    public VoiceCommand() {
        super("voice", "server.commands.voice.desc");
        this.addSubCommand(new VoiceEnabledCommand(this));
        this.addSubCommand(new VoiceMaxDistanceCommand(this));
        this.addSubCommand(new VoiceFullVolumeDistanceCommand(this));
        this.addSubCommand(new VoiceMuteCommand(this));
        this.addSubCommand(new VoiceUnmuteCommand(this));
        this.addSubCommand(new VoiceMutedListCommand(this));
        this.addSubCommand(new VoiceStatusCommand(this));
    }

    private class VoiceEnabledCommand
    extends AbstractCommand {
        @Nonnull
        private final RequiredArg<Boolean> enabledArg = this.withRequiredArg("enabled", "server.commands.voice.enabled.arg.desc", ArgTypes.BOOLEAN);

        public VoiceEnabledCommand(VoiceCommand voiceCommand) {
            super("enabled", "server.commands.voice.enabled.desc");
            this.setPermissionGroup(null);
        }

        @Override
        @Nullable
        protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
            boolean enabled = (Boolean)this.enabledArg.get(context);
            VoiceModule.get().setVoiceEnabled(enabled);
            String messageId = enabled ? "server.commands.voice.status.enabled" : "server.commands.voice.status.disabled";
            context.sendMessage(Message.translation(messageId));
            return null;
        }
    }

    private class VoiceMaxDistanceCommand
    extends AbstractCommand {
        @Nonnull
        private final RequiredArg<Float> distanceArg = this.withRequiredArg("blocks", "server.commands.voice.maxdistance.arg.desc", ArgTypes.FLOAT);

        public VoiceMaxDistanceCommand(VoiceCommand voiceCommand) {
            super("maxdistance", "server.commands.voice.maxdistance.desc");
            this.setPermissionGroup(null);
        }

        @Override
        @Nullable
        protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
            float distance = ((Float)this.distanceArg.get(context)).floatValue();
            if (distance <= 0.0f) {
                context.sendMessage(Message.translation("server.commands.voice.maxdistance.invalid"));
                return null;
            }
            VoiceModule.get().setMaxHearingDistance(distance);
            context.sendMessage(Message.translation("server.commands.voice.maxdistance.set").param("distance", distance));
            return null;
        }
    }

    private class VoiceFullVolumeDistanceCommand
    extends AbstractCommand {
        @Nonnull
        private final RequiredArg<Float> distanceArg = this.withRequiredArg("blocks", "server.commands.voice.fullvolumedistance.arg.desc", ArgTypes.FLOAT);

        public VoiceFullVolumeDistanceCommand(VoiceCommand voiceCommand) {
            super("fullvolumedistance", "server.commands.voice.fullvolumedistance.desc");
            this.setPermissionGroup(null);
        }

        @Override
        @Nullable
        protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
            float distance = ((Float)this.distanceArg.get(context)).floatValue();
            if (distance <= 0.0f) {
                context.sendMessage(Message.translation("server.commands.voice.fullvolumedistance.invalid"));
                return null;
            }
            VoiceModule.get().setReferenceDistance(distance);
            context.sendMessage(Message.translation("server.commands.voice.fullvolumedistance.set").param("distance", distance));
            return null;
        }
    }

    private class VoiceMuteCommand
    extends AbstractAsyncCommand {
        @Nonnull
        private final RequiredArg<ProfileServiceClient.PublicGameProfile> playerArg = this.withRequiredArg("player", "server.commands.voice.mute.arg.desc", ArgTypes.GAME_PROFILE_LOOKUP);

        public VoiceMuteCommand(VoiceCommand voiceCommand) {
            super("mute", "server.commands.voice.mute.desc");
            this.setPermissionGroup(null);
        }

        @Override
        @Nonnull
        protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
            ProfileServiceClient.PublicGameProfile profile = (ProfileServiceClient.PublicGameProfile)this.playerArg.get(context);
            if (profile == null) {
                return CompletableFuture.completedFuture(null);
            }
            UUID uuid = profile.getUuid();
            Message displayName = Message.raw(profile.getUsername()).bold(true);
            if (VoiceModule.get().isPlayerMuted(uuid)) {
                context.sendMessage(Message.translation("server.commands.voice.mute.already").param("player", displayName));
                return CompletableFuture.completedFuture(null);
            }
            VoiceModule.get().mutePlayer(uuid);
            context.sendMessage(Message.translation("server.commands.voice.mute.success").param("player", displayName));
            return CompletableFuture.completedFuture(null);
        }
    }

    private class VoiceUnmuteCommand
    extends AbstractAsyncCommand {
        @Nonnull
        private final RequiredArg<ProfileServiceClient.PublicGameProfile> playerArg = this.withRequiredArg("player", "server.commands.voice.unmute.arg.desc", ArgTypes.GAME_PROFILE_LOOKUP);

        public VoiceUnmuteCommand(VoiceCommand voiceCommand) {
            super("unmute", "server.commands.voice.unmute.desc");
            this.setPermissionGroup(null);
        }

        @Override
        @Nonnull
        protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
            ProfileServiceClient.PublicGameProfile profile = (ProfileServiceClient.PublicGameProfile)this.playerArg.get(context);
            if (profile == null) {
                return CompletableFuture.completedFuture(null);
            }
            UUID uuid = profile.getUuid();
            Message displayName = Message.raw(profile.getUsername()).bold(true);
            if (!VoiceModule.get().isPlayerMuted(uuid)) {
                context.sendMessage(Message.translation("server.commands.voice.unmute.notmuted").param("player", displayName));
                return CompletableFuture.completedFuture(null);
            }
            VoiceModule.get().unmutePlayer(uuid);
            context.sendMessage(Message.translation("server.commands.voice.unmute.success").param("player", displayName));
            return CompletableFuture.completedFuture(null);
        }
    }

    private class VoiceMutedListCommand
    extends AbstractCommand {
        public VoiceMutedListCommand(VoiceCommand voiceCommand) {
            super("muted", "server.commands.voice.muted.desc");
            this.setPermissionGroup(null);
        }

        @Override
        @Nullable
        protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
            Set<UUID> mutedPlayers = VoiceModule.get().getGloballyMutedPlayers();
            if (mutedPlayers.isEmpty()) {
                context.sendMessage(Message.translation("server.commands.voice.muted.empty"));
                return null;
            }
            String playerList = mutedPlayers.stream().map(uuid -> {
                PlayerRef playerRef = Universe.get().getPlayer((UUID)uuid);
                if (playerRef != null) {
                    return playerRef.getUsername() + " (" + String.valueOf(uuid) + ")";
                }
                return uuid.toString();
            }).collect(Collectors.joining(", "));
            context.sendMessage(Message.translation("server.commands.voice.muted.list").param("count", mutedPlayers.size()).param("players", playerList));
            return null;
        }
    }

    private class VoiceStatusCommand
    extends AbstractCommand {
        public VoiceStatusCommand(VoiceCommand voiceCommand) {
            super("status", "server.commands.voice.status.desc");
            this.setPermissionGroup(null);
        }

        @Override
        @Nullable
        protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
            VoiceModule voiceModule = VoiceModule.get();
            int mutedCount = voiceModule.getGloballyMutedPlayers().size();
            String messageId = voiceModule.isVoiceEnabled() ? "server.commands.voice.status.enabledInfo" : "server.commands.voice.status.disabledInfo";
            context.sendMessage(Message.translation(messageId).param("maxDistance", voiceModule.getMaxHearingDistance()).param("fullVolumeDistance", voiceModule.getReferenceDistance()).param("mutedCount", mutedCount));
            return null;
        }
    }
}

