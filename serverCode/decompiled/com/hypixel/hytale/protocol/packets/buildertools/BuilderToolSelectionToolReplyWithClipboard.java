/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.protocol.packets.buildertools;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import com.hypixel.hytale.protocol.packets.buildertools.ClipboardEntityChange;
import com.hypixel.hytale.protocol.packets.interface_.BlockChange;
import com.hypixel.hytale.protocol.packets.interface_.FluidChange;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderToolSelectionToolReplyWithClipboard
implements Packet,
ToClientPacket {
    public static final int PACKET_ID = 411;
    public static final boolean IS_COMPRESSED = true;
    public static final int NULLABLE_BIT_FIELD_SIZE = 1;
    public static final int FIXED_BLOCK_SIZE = 1;
    public static final int VARIABLE_FIELD_COUNT = 3;
    public static final int VARIABLE_BLOCK_START = 13;
    public static final int MAX_SIZE = 0x64000000;
    @Nullable
    public BlockChange[] blocksChange;
    @Nullable
    public FluidChange[] fluidsChange;
    @Nullable
    public ClipboardEntityChange[] entityChanges;

    @Override
    public int getId() {
        return 411;
    }

    @Override
    public NetworkChannel getChannel() {
        return NetworkChannel.Default;
    }

    public BuilderToolSelectionToolReplyWithClipboard() {
    }

    public BuilderToolSelectionToolReplyWithClipboard(@Nullable BlockChange[] blocksChange, @Nullable FluidChange[] fluidsChange, @Nullable ClipboardEntityChange[] entityChanges) {
        this.blocksChange = blocksChange;
        this.fluidsChange = fluidsChange;
        this.entityChanges = entityChanges;
    }

    public BuilderToolSelectionToolReplyWithClipboard(@Nonnull BuilderToolSelectionToolReplyWithClipboard other) {
        this.blocksChange = other.blocksChange;
        this.fluidsChange = other.fluidsChange;
        this.entityChanges = other.entityChanges;
    }

    @Nonnull
    public static BuilderToolSelectionToolReplyWithClipboard deserialize(@Nonnull ByteBuf buf, int offset) {
        int i;
        int elemPos;
        int varIntLen;
        BuilderToolSelectionToolReplyWithClipboard obj = new BuilderToolSelectionToolReplyWithClipboard();
        byte nullBits = buf.getByte(offset);
        if ((nullBits & 1) != 0) {
            int varPos0 = offset + 13 + buf.getIntLE(offset + 1);
            int blocksChangeCount = VarInt.peek(buf, varPos0);
            if (blocksChangeCount < 0) {
                throw ProtocolException.negativeLength("BlocksChange", blocksChangeCount);
            }
            if (blocksChangeCount > 4096000) {
                throw ProtocolException.arrayTooLong("BlocksChange", blocksChangeCount, 4096000);
            }
            varIntLen = VarInt.length(buf, varPos0);
            if ((long)(varPos0 + varIntLen) + (long)blocksChangeCount * 17L > (long)buf.readableBytes()) {
                throw ProtocolException.bufferTooSmall("BlocksChange", varPos0 + varIntLen + blocksChangeCount * 17, buf.readableBytes());
            }
            obj.blocksChange = new BlockChange[blocksChangeCount];
            elemPos = varPos0 + varIntLen;
            for (i = 0; i < blocksChangeCount; ++i) {
                obj.blocksChange[i] = BlockChange.deserialize(buf, elemPos);
                elemPos += BlockChange.computeBytesConsumed(buf, elemPos);
            }
        }
        if ((nullBits & 2) != 0) {
            int varPos1 = offset + 13 + buf.getIntLE(offset + 5);
            int fluidsChangeCount = VarInt.peek(buf, varPos1);
            if (fluidsChangeCount < 0) {
                throw ProtocolException.negativeLength("FluidsChange", fluidsChangeCount);
            }
            if (fluidsChangeCount > 4096000) {
                throw ProtocolException.arrayTooLong("FluidsChange", fluidsChangeCount, 4096000);
            }
            varIntLen = VarInt.length(buf, varPos1);
            if ((long)(varPos1 + varIntLen) + (long)fluidsChangeCount * 17L > (long)buf.readableBytes()) {
                throw ProtocolException.bufferTooSmall("FluidsChange", varPos1 + varIntLen + fluidsChangeCount * 17, buf.readableBytes());
            }
            obj.fluidsChange = new FluidChange[fluidsChangeCount];
            elemPos = varPos1 + varIntLen;
            for (i = 0; i < fluidsChangeCount; ++i) {
                obj.fluidsChange[i] = FluidChange.deserialize(buf, elemPos);
                elemPos += FluidChange.computeBytesConsumed(buf, elemPos);
            }
        }
        if ((nullBits & 4) != 0) {
            int varPos2 = offset + 13 + buf.getIntLE(offset + 9);
            int entityChangesCount = VarInt.peek(buf, varPos2);
            if (entityChangesCount < 0) {
                throw ProtocolException.negativeLength("EntityChanges", entityChangesCount);
            }
            if (entityChangesCount > 4096000) {
                throw ProtocolException.arrayTooLong("EntityChanges", entityChangesCount, 4096000);
            }
            varIntLen = VarInt.length(buf, varPos2);
            if ((long)(varPos2 + varIntLen) + (long)entityChangesCount * 45L > (long)buf.readableBytes()) {
                throw ProtocolException.bufferTooSmall("EntityChanges", varPos2 + varIntLen + entityChangesCount * 45, buf.readableBytes());
            }
            obj.entityChanges = new ClipboardEntityChange[entityChangesCount];
            elemPos = varPos2 + varIntLen;
            for (i = 0; i < entityChangesCount; ++i) {
                obj.entityChanges[i] = ClipboardEntityChange.deserialize(buf, elemPos);
                elemPos += ClipboardEntityChange.computeBytesConsumed(buf, elemPos);
            }
        }
        return obj;
    }

    public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
        int i;
        int arrLen;
        byte nullBits = buf.getByte(offset);
        int maxEnd = 13;
        if ((nullBits & 1) != 0) {
            int fieldOffset0 = buf.getIntLE(offset + 1);
            int pos0 = offset + 13 + fieldOffset0;
            arrLen = VarInt.peek(buf, pos0);
            pos0 += VarInt.length(buf, pos0);
            for (i = 0; i < arrLen; ++i) {
                pos0 += BlockChange.computeBytesConsumed(buf, pos0);
            }
            if (pos0 - offset > maxEnd) {
                maxEnd = pos0 - offset;
            }
        }
        if ((nullBits & 2) != 0) {
            int fieldOffset1 = buf.getIntLE(offset + 5);
            int pos1 = offset + 13 + fieldOffset1;
            arrLen = VarInt.peek(buf, pos1);
            pos1 += VarInt.length(buf, pos1);
            for (i = 0; i < arrLen; ++i) {
                pos1 += FluidChange.computeBytesConsumed(buf, pos1);
            }
            if (pos1 - offset > maxEnd) {
                maxEnd = pos1 - offset;
            }
        }
        if ((nullBits & 4) != 0) {
            int fieldOffset2 = buf.getIntLE(offset + 9);
            int pos2 = offset + 13 + fieldOffset2;
            arrLen = VarInt.peek(buf, pos2);
            pos2 += VarInt.length(buf, pos2);
            for (i = 0; i < arrLen; ++i) {
                pos2 += ClipboardEntityChange.computeBytesConsumed(buf, pos2);
            }
            if (pos2 - offset > maxEnd) {
                maxEnd = pos2 - offset;
            }
        }
        return maxEnd;
    }

    @Override
    public void serialize(@Nonnull ByteBuf buf) {
        int startPos = buf.writerIndex();
        byte nullBits = 0;
        if (this.blocksChange != null) {
            nullBits = (byte)(nullBits | 1);
        }
        if (this.fluidsChange != null) {
            nullBits = (byte)(nullBits | 2);
        }
        if (this.entityChanges != null) {
            nullBits = (byte)(nullBits | 4);
        }
        buf.writeByte(nullBits);
        int blocksChangeOffsetSlot = buf.writerIndex();
        buf.writeIntLE(0);
        int fluidsChangeOffsetSlot = buf.writerIndex();
        buf.writeIntLE(0);
        int entityChangesOffsetSlot = buf.writerIndex();
        buf.writeIntLE(0);
        int varBlockStart = buf.writerIndex();
        if (this.blocksChange != null) {
            buf.setIntLE(blocksChangeOffsetSlot, buf.writerIndex() - varBlockStart);
            if (this.blocksChange.length > 4096000) {
                throw ProtocolException.arrayTooLong("BlocksChange", this.blocksChange.length, 4096000);
            }
            VarInt.write(buf, this.blocksChange.length);
            for (BlockChange blockChange : this.blocksChange) {
                blockChange.serialize(buf);
            }
        } else {
            buf.setIntLE(blocksChangeOffsetSlot, -1);
        }
        if (this.fluidsChange != null) {
            buf.setIntLE(fluidsChangeOffsetSlot, buf.writerIndex() - varBlockStart);
            if (this.fluidsChange.length > 4096000) {
                throw ProtocolException.arrayTooLong("FluidsChange", this.fluidsChange.length, 4096000);
            }
            VarInt.write(buf, this.fluidsChange.length);
            for (FluidChange fluidChange : this.fluidsChange) {
                fluidChange.serialize(buf);
            }
        } else {
            buf.setIntLE(fluidsChangeOffsetSlot, -1);
        }
        if (this.entityChanges != null) {
            buf.setIntLE(entityChangesOffsetSlot, buf.writerIndex() - varBlockStart);
            if (this.entityChanges.length > 4096000) {
                throw ProtocolException.arrayTooLong("EntityChanges", this.entityChanges.length, 4096000);
            }
            VarInt.write(buf, this.entityChanges.length);
            for (ClipboardEntityChange clipboardEntityChange : this.entityChanges) {
                clipboardEntityChange.serialize(buf);
            }
        } else {
            buf.setIntLE(entityChangesOffsetSlot, -1);
        }
    }

    @Override
    public int computeSize() {
        int size = 13;
        if (this.blocksChange != null) {
            size += VarInt.size(this.blocksChange.length) + this.blocksChange.length * 17;
        }
        if (this.fluidsChange != null) {
            size += VarInt.size(this.fluidsChange.length) + this.fluidsChange.length * 17;
        }
        if (this.entityChanges != null) {
            int entityChangesSize = 0;
            for (ClipboardEntityChange elem : this.entityChanges) {
                entityChangesSize += elem.computeSize();
            }
            size += VarInt.size(this.entityChanges.length) + entityChangesSize;
        }
        return size;
    }

    public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
        int pos;
        if (buffer.readableBytes() - offset < 13) {
            return ValidationResult.error("Buffer too small: expected at least 13 bytes");
        }
        byte nullBits = buffer.getByte(offset);
        if ((nullBits & 1) != 0) {
            int blocksChangeOffset = buffer.getIntLE(offset + 1);
            if (blocksChangeOffset < 0) {
                return ValidationResult.error("Invalid offset for BlocksChange");
            }
            pos = offset + 13 + blocksChangeOffset;
            if (pos >= buffer.writerIndex()) {
                return ValidationResult.error("Offset out of bounds for BlocksChange");
            }
            int blocksChangeCount = VarInt.peek(buffer, pos);
            if (blocksChangeCount < 0) {
                return ValidationResult.error("Invalid array count for BlocksChange");
            }
            if (blocksChangeCount > 4096000) {
                return ValidationResult.error("BlocksChange exceeds max length 4096000");
            }
            pos += VarInt.length(buffer, pos);
            if ((pos += blocksChangeCount * 17) > buffer.writerIndex()) {
                return ValidationResult.error("Buffer overflow reading BlocksChange");
            }
        }
        if ((nullBits & 2) != 0) {
            int fluidsChangeOffset = buffer.getIntLE(offset + 5);
            if (fluidsChangeOffset < 0) {
                return ValidationResult.error("Invalid offset for FluidsChange");
            }
            pos = offset + 13 + fluidsChangeOffset;
            if (pos >= buffer.writerIndex()) {
                return ValidationResult.error("Offset out of bounds for FluidsChange");
            }
            int fluidsChangeCount = VarInt.peek(buffer, pos);
            if (fluidsChangeCount < 0) {
                return ValidationResult.error("Invalid array count for FluidsChange");
            }
            if (fluidsChangeCount > 4096000) {
                return ValidationResult.error("FluidsChange exceeds max length 4096000");
            }
            pos += VarInt.length(buffer, pos);
            if ((pos += fluidsChangeCount * 17) > buffer.writerIndex()) {
                return ValidationResult.error("Buffer overflow reading FluidsChange");
            }
        }
        if ((nullBits & 4) != 0) {
            int entityChangesOffset = buffer.getIntLE(offset + 9);
            if (entityChangesOffset < 0) {
                return ValidationResult.error("Invalid offset for EntityChanges");
            }
            pos = offset + 13 + entityChangesOffset;
            if (pos >= buffer.writerIndex()) {
                return ValidationResult.error("Offset out of bounds for EntityChanges");
            }
            int entityChangesCount = VarInt.peek(buffer, pos);
            if (entityChangesCount < 0) {
                return ValidationResult.error("Invalid array count for EntityChanges");
            }
            if (entityChangesCount > 4096000) {
                return ValidationResult.error("EntityChanges exceeds max length 4096000");
            }
            pos += VarInt.length(buffer, pos);
            for (int i = 0; i < entityChangesCount; ++i) {
                ValidationResult structResult = ClipboardEntityChange.validateStructure(buffer, pos);
                if (!structResult.isValid()) {
                    return ValidationResult.error("Invalid ClipboardEntityChange in EntityChanges[" + i + "]: " + structResult.error());
                }
                pos += ClipboardEntityChange.computeBytesConsumed(buffer, pos);
            }
        }
        return ValidationResult.OK;
    }

    public BuilderToolSelectionToolReplyWithClipboard clone() {
        BuilderToolSelectionToolReplyWithClipboard copy = new BuilderToolSelectionToolReplyWithClipboard();
        copy.blocksChange = this.blocksChange != null ? (BlockChange[])Arrays.stream(this.blocksChange).map(e -> e.clone()).toArray(BlockChange[]::new) : null;
        copy.fluidsChange = this.fluidsChange != null ? (FluidChange[])Arrays.stream(this.fluidsChange).map(e -> e.clone()).toArray(FluidChange[]::new) : null;
        copy.entityChanges = this.entityChanges != null ? (ClipboardEntityChange[])Arrays.stream(this.entityChanges).map(e -> e.clone()).toArray(ClipboardEntityChange[]::new) : null;
        return copy;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BuilderToolSelectionToolReplyWithClipboard)) {
            return false;
        }
        BuilderToolSelectionToolReplyWithClipboard other = (BuilderToolSelectionToolReplyWithClipboard)obj;
        return Arrays.equals(this.blocksChange, other.blocksChange) && Arrays.equals(this.fluidsChange, other.fluidsChange) && Arrays.equals(this.entityChanges, other.entityChanges);
    }

    public int hashCode() {
        int result = 1;
        result = 31 * result + Arrays.hashCode(this.blocksChange);
        result = 31 * result + Arrays.hashCode(this.fluidsChange);
        result = 31 * result + Arrays.hashCode(this.entityChanges);
        return result;
    }
}

