/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.builtin.hytalegenerator.assignments;

import com.hypixel.hytale.builtin.hytalegenerator.assignments.Assignments;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.builtin.hytalegenerator.workerindexer.WorkerIndexer;
import com.hypixel.hytale.math.vector.Vector3d;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

public class ConstantAssignments
extends Assignments {
    @Nonnull
    private final Prop prop;

    public ConstantAssignments(@Nonnull Prop prop) {
        this.prop = prop;
    }

    @Override
    @Nonnull
    public Prop propAt(@Nonnull Vector3d position, @Nonnull WorkerIndexer.Id id, double distanceTOBiomeEdge) {
        return this.prop;
    }

    @Override
    @Nonnull
    public List<Prop> getAllPossibleProps() {
        return Collections.singletonList(this.prop);
    }
}

