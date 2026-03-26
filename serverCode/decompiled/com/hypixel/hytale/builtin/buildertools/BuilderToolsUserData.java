/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.builtin.buildertools;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderToolsUserData
implements Component<EntityStore> {
    public static final String ID = "BuilderTools";
    private static final String SELECTION_HISTORY_KEY = "SelectionHistory";
    private static final String SELECTION_HISTORY_DOC = "Controls whether changes to the block selection box are recorded in the undo/redo history.";
    private static final String LAST_SAVE_PACK_KEY = "LastSavePack";
    public static final BuilderCodec<BuilderToolsUserData> CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(BuilderToolsUserData.class, BuilderToolsUserData::new).append(new KeyedCodec<Boolean>("SelectionHistory", Codec.BOOLEAN), BuilderToolsUserData::setRecordSelectionHistory, BuilderToolsUserData::isRecordingSelectionHistory).addValidator(Validators.nonNull()).documentation("Controls whether changes to the block selection box are recorded in the undo/redo history.").add()).addField(new KeyedCodec<String>("LastSavePack", Codec.STRING), BuilderToolsUserData::setLastSavePack, BuilderToolsUserData::getLastSavePack)).build();
    private boolean selectionHistory = true;
    @Nullable
    private String lastSavePack;

    @Nonnull
    public static BuilderToolsUserData get(@Nonnull Player player) {
        BuilderToolsUserData userData = player.toHolder().getComponent(BuilderToolsUserData.getComponentType());
        if (userData == null) {
            return new BuilderToolsUserData();
        }
        return userData;
    }

    public static ComponentType<EntityStore, BuilderToolsUserData> getComponentType() {
        return BuilderToolsPlugin.get().getUserDataComponentType();
    }

    public boolean isRecordingSelectionHistory() {
        return this.selectionHistory;
    }

    public void setRecordSelectionHistory(boolean selectionHistory) {
        this.selectionHistory = selectionHistory;
    }

    @Nullable
    public String getLastSavePack() {
        return this.lastSavePack;
    }

    public void setLastSavePack(@Nullable String lastSavePack) {
        this.lastSavePack = lastSavePack;
    }

    @Nonnull
    public String toString() {
        return "BuilderToolsUserData{selectionHistory=" + this.selectionHistory + ", lastSavePack=" + this.lastSavePack + "}";
    }

    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        BuilderToolsUserData that = (BuilderToolsUserData)o;
        return this.selectionHistory == that.selectionHistory && Objects.equals(this.lastSavePack, that.lastSavePack);
    }

    public int hashCode() {
        return Objects.hash(this.selectionHistory, this.lastSavePack);
    }

    @Override
    @Nonnull
    public Component<EntityStore> clone() {
        BuilderToolsUserData settings = new BuilderToolsUserData();
        settings.selectionHistory = this.selectionHistory;
        settings.lastSavePack = this.lastSavePack;
        return settings;
    }
}

