/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.server.core.cosmetics;

import com.hypixel.hytale.assetstore.AssetUpdateQuery;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.protocol.ProtocolEmote;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateEmotes;
import com.hypixel.hytale.server.core.asset.packet.AssetPacketGenerator;
import com.hypixel.hytale.server.core.cosmetics.EmoteAsset;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class EmoteAssetPacketGenerator
extends AssetPacketGenerator<String, EmoteAsset, IndexedLookupTableAssetMap<String, EmoteAsset>> {
    @Override
    public ToClientPacket generateInitPacket(IndexedLookupTableAssetMap<String, EmoteAsset> assetMap, Map<String, EmoteAsset> assets) {
        Int2ObjectOpenHashMap<ProtocolEmote> emoteAssets = new Int2ObjectOpenHashMap<ProtocolEmote>();
        for (Map.Entry<String, EmoteAsset> entry : assets.entrySet()) {
            emoteAssets.put(assetMap.getIndex(entry.getKey()), entry.getValue().toPacket());
        }
        return new UpdateEmotes(UpdateType.Init, assetMap.getNextIndex(), emoteAssets);
    }

    @Override
    public ToClientPacket generateUpdatePacket(IndexedLookupTableAssetMap<String, EmoteAsset> assetMap, Map<String, EmoteAsset> loadedAssets, @NonNullDecl AssetUpdateQuery query) {
        Int2ObjectOpenHashMap<ProtocolEmote> emoteAssets = new Int2ObjectOpenHashMap<ProtocolEmote>();
        for (Map.Entry<String, EmoteAsset> entry : loadedAssets.entrySet()) {
            emoteAssets.put(assetMap.getIndex(entry.getKey()), entry.getValue().toPacket());
        }
        return new UpdateEmotes(UpdateType.AddOrUpdate, assetMap.getNextIndex(), emoteAssets);
    }

    @Override
    @NullableDecl
    public ToClientPacket generateRemovePacket(IndexedLookupTableAssetMap<String, EmoteAsset> assetMap, Set<String> removedAssets, @NonNullDecl AssetUpdateQuery query) {
        Int2ObjectOpenHashMap<ProtocolEmote> emoteAssets = new Int2ObjectOpenHashMap<ProtocolEmote>();
        for (String entry : removedAssets) {
            emoteAssets.put(assetMap.getIndex(entry), (ProtocolEmote)null);
        }
        return new UpdateEmotes(UpdateType.Remove, assetMap.getNextIndex(), emoteAssets);
    }
}

