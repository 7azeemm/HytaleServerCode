/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.builtin.hytalegenerator.assignments;

import com.hypixel.hytale.builtin.hytalegenerator.assignments.Assignments;
import com.hypixel.hytale.builtin.hytalegenerator.props.EmptyProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.builtin.hytalegenerator.workerindexer.WorkerIndexer;
import com.hypixel.hytale.math.vector.Vector3d;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class SandwichAssignments
extends Assignments {
    @Nonnull
    private final List<VerticalDelimiter> delimiters;

    public SandwichAssignments(@Nonnull List<VerticalDelimiter> delimiters) {
        this.delimiters = new ArrayList<VerticalDelimiter>(delimiters);
    }

    @Override
    public Prop propAt(@Nonnull Vector3d position, @Nonnull WorkerIndexer.Id id, double distanceTOBiomeEdge) {
        if (this.delimiters.isEmpty()) {
            return EmptyProp.INSTANCE;
        }
        for (VerticalDelimiter delimiter : this.delimiters) {
            if (!delimiter.isInside(position.y)) continue;
            return delimiter.assignments.propAt(position, id, distanceTOBiomeEdge);
        }
        return EmptyProp.INSTANCE;
    }

    @Override
    @Nonnull
    public List<Prop> getAllPossibleProps() {
        ArrayList<Prop> list = new ArrayList<Prop>();
        for (VerticalDelimiter f : this.delimiters) {
            list.addAll(f.assignments.getAllPossibleProps());
        }
        return list;
    }

    public static class VerticalDelimiter {
        double maxY;
        double minY;
        Assignments assignments;

        public VerticalDelimiter(@Nonnull Assignments propDistributions, double minY, double maxY) {
            this.minY = minY;
            this.maxY = maxY;
            this.assignments = propDistributions;
        }

        boolean isInside(double fieldValue) {
            return fieldValue < this.maxY && fieldValue >= this.minY;
        }
    }
}

