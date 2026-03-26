/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.builtin.buildertools.prefabeditor.ui;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.PrefabEditSessionManager;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.ui.PrefabEditorLoadSettingsPage;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class PrefabEditorLoadOptionsPage
extends InteractiveCustomUIPage<PageData> {
    @Nonnull
    private final World world;

    public PrefabEditorLoadOptionsPage(@Nonnull PlayerRef playerRef, @Nonnull World world) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, PageData.CODEC);
        this.world = world;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store) {
        commandBuilder.append("Pages/PrefabEditorLoadOptions.ui");
        commandBuilder.set("#WarningTitle.TextSpans", Message.translation("server.commands.editprefab.prefabEditorLoadOptions.title"));
        commandBuilder.set("#WarningMessage.TextSpans", Message.translation("server.commands.editprefab.prefabEditorLoadOptions.message"));
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#LoadExistingSessionButton", new EventData().append("Action", Action.LoadExisting.name()));
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CancelButton", new EventData().append("Action", Action.Cancel.name()));
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CreateNewSessionButton", new EventData().append("Action", Action.CreateNew.name()));
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull PageData data) {
        Player playerComponent = store.getComponent(ref, Player.getComponentType());
        assert (playerComponent != null);
        PrefabEditSessionManager prefabEditSessionManager = BuilderToolsPlugin.get().getPrefabEditSessionManager();
        switch (data.action.ordinal()) {
            case 0: {
                playerComponent.getPageManager().setPage(ref, store, Page.None);
                prefabEditSessionManager.sendToEditWorld(ref, this.world, this.playerRef);
                break;
            }
            case 1: {
                playerComponent.getPageManager().setPage(ref, store, Page.None);
                break;
            }
            case 2: {
                prefabEditSessionManager.exitEditSession(ref, this.world, this.playerRef, store).thenRun(() -> playerComponent.getPageManager().openCustomPage(ref, store, new PrefabEditorLoadSettingsPage(this.playerRef)));
            }
        }
    }

    protected static class PageData {
        public static final BuilderCodec<PageData> CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(PageData.class, PageData::new).append(new KeyedCodec<Action>("Action", new EnumCodec<Action>(Action.class, EnumCodec.EnumStyle.LEGACY)), (o, action) -> {
            o.action = action;
        }, o -> o.action).add()).build();
        public Action action;
    }

    public static enum Action {
        LoadExisting,
        Cancel,
        CreateNew;

    }
}

