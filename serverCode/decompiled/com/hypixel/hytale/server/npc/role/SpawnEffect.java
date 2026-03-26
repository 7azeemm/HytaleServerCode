/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.server.npc.role;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.ModelParticle;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.Vector3f;
import com.hypixel.hytale.protocol.packets.entities.SpawnModelParticles;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import java.util.List;
import javax.annotation.Nonnull;

public interface SpawnEffect {
    public String getSpawnParticles(@Nonnull BuilderSupport var1);

    public Vector3d getSpawnParticleOffset(@Nonnull BuilderSupport var1);

    public String getSpawnParticleTargetNode(@Nonnull BuilderSupport var1);

    public boolean isSpawnParticleDetached(@Nonnull BuilderSupport var1);

    public double getSpawnViewDistance();

    default public void spawnEffect(@Nonnull Holder<EntityStore> holder, @Nonnull BuilderSupport support, @Nonnull Vector3d position, @Nonnull com.hypixel.hytale.math.vector.Vector3f rotation, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
        String particles = this.getSpawnParticles(support);
        if (particles == null || particles.isEmpty()) {
            return;
        }
        Vector3d spawnPosition = new Vector3d(0.0, 0.0, 0.0);
        Vector3d offset = this.getSpawnParticleOffset(support);
        if (offset != null) {
            spawnPosition.assign(offset);
        }
        spawnPosition.rotateY(rotation.getYaw()).add(position);
        SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource = componentAccessor.getResource(EntityModule.get().getPlayerSpatialResourceType());
        List<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
        playerSpatialResource.getSpatialStructure().collect(spawnPosition, this.getSpawnViewDistance(), results);
        ParticleUtil.spawnParticleEffect(particles, spawnPosition, results, componentAccessor);
        com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle particle = new com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle();
        particle.setSystemId(particles);
        particle.setPositionOffset(new Vector3f((float)spawnPosition.x, (float)spawnPosition.y, (float)spawnPosition.z));
        particle.setTargetNodeName(this.getSpawnParticleTargetNode(support));
        particle.setDetachedFromModel(this.isSpawnParticleDetached(support));
        NetworkId networkIdComponent = holder.getComponent(NetworkId.getComponentType());
        if (networkIdComponent == null) {
            return;
        }
        SpawnModelParticles packet = new SpawnModelParticles(networkIdComponent.getId(), new ModelParticle[]{particle.toPacket()});
        for (Ref<EntityStore> playerRef : results) {
            PlayerRef playerRefComponent = componentAccessor.getComponent(playerRef, PlayerRef.getComponentType());
            if (playerRefComponent == null) continue;
            playerRefComponent.getPacketHandler().write((ToClientPacket)packet);
        }
    }
}

