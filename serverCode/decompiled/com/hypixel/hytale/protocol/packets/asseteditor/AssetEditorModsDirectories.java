/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AssetEditorModsDirectories
implements Packet,
ToClientPacket {
    public static final int PACKET_ID = 356;
    public static final boolean IS_COMPRESSED = false;
    public static final int NULLABLE_BIT_FIELD_SIZE = 1;
    public static final int FIXED_BLOCK_SIZE = 1;
    public static final int VARIABLE_FIELD_COUNT = 1;
    public static final int VARIABLE_BLOCK_START = 1;
    public static final int MAX_SIZE = 0x64000000;
    @Nullable
    public String[] directories;

    @Override
    public int getId() {
        return 356;
    }

    @Override
    public NetworkChannel getChannel() {
        return NetworkChannel.Default;
    }

    public AssetEditorModsDirectories() {
    }

    public AssetEditorModsDirectories(@Nullable String[] directories) {
        this.directories = directories;
    }

    public AssetEditorModsDirectories(@Nonnull AssetEditorModsDirectories other) {
        this.directories = other.directories;
    }

    @Nonnull
    public static AssetEditorModsDirectories deserialize(@Nonnull ByteBuf buf, int offset) {
        AssetEditorModsDirectories obj = new AssetEditorModsDirectories();
        byte nullBits = buf.getByte(offset);
        int pos = offset + 1;
        if ((nullBits & 1) != 0) {
            int directoriesCount = VarInt.peek(buf, pos);
            if (directoriesCount < 0) {
                throw ProtocolException.negativeLength("Directories", directoriesCount);
            }
            if (directoriesCount > 4096000) {
                throw ProtocolException.arrayTooLong("Directories", directoriesCount, 4096000);
            }
            int directoriesVarLen = VarInt.size(directoriesCount);
            if ((long)(pos + directoriesVarLen) + (long)directoriesCount * 1L > (long)buf.readableBytes()) {
                throw ProtocolException.bufferTooSmall("Directories", pos + directoriesVarLen + directoriesCount * 1, buf.readableBytes());
            }
            pos += directoriesVarLen;
            obj.directories = new String[directoriesCount];
            for (int i = 0; i < directoriesCount; ++i) {
                int strLen = VarInt.peek(buf, pos);
                if (strLen < 0) {
                    throw ProtocolException.negativeLength("directories[" + i + "]", strLen);
                }
                if (strLen > 4096000) {
                    throw ProtocolException.stringTooLong("directories[" + i + "]", strLen, 4096000);
                }
                int strVarLen = VarInt.length(buf, pos);
                obj.directories[i] = PacketIO.readVarString(buf, pos);
                pos += strVarLen + strLen;
            }
        }
        return obj;
    }

    public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
        byte nullBits = buf.getByte(offset);
        int pos = offset + 1;
        if ((nullBits & 1) != 0) {
            int arrLen = VarInt.peek(buf, pos);
            pos += VarInt.length(buf, pos);
            for (int i = 0; i < arrLen; ++i) {
                int sl = VarInt.peek(buf, pos);
                pos += VarInt.length(buf, pos) + sl;
            }
        }
        return pos - offset;
    }

    @Override
    public void serialize(@Nonnull ByteBuf buf) {
        int nullBits = 0;
        if (this.directories != null) {
            nullBits = (byte)(nullBits | 1);
        }
        buf.writeByte(nullBits);
        if (this.directories != null) {
            if (this.directories.length > 4096000) {
                throw ProtocolException.arrayTooLong("Directories", this.directories.length, 4096000);
            }
            VarInt.write(buf, this.directories.length);
            for (String item : this.directories) {
                PacketIO.writeVarString(buf, item, 4096000);
            }
        }
    }

    @Override
    public int computeSize() {
        int size = 1;
        if (this.directories != null) {
            int directoriesSize = 0;
            for (String elem : this.directories) {
                directoriesSize += PacketIO.stringSize(elem);
            }
            size += VarInt.size(this.directories.length) + directoriesSize;
        }
        return size;
    }

    public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
        if (buffer.readableBytes() - offset < 1) {
            return ValidationResult.error("Buffer too small: expected at least 1 bytes");
        }
        byte nullBits = buffer.getByte(offset);
        int pos = offset + 1;
        if ((nullBits & 1) != 0) {
            int directoriesCount = VarInt.peek(buffer, pos);
            if (directoriesCount < 0) {
                return ValidationResult.error("Invalid array count for Directories");
            }
            if (directoriesCount > 4096000) {
                return ValidationResult.error("Directories exceeds max length 4096000");
            }
            pos += VarInt.length(buffer, pos);
            for (int i = 0; i < directoriesCount; ++i) {
                int strLen = VarInt.peek(buffer, pos);
                if (strLen < 0) {
                    return ValidationResult.error("Invalid string length in Directories");
                }
                pos += VarInt.length(buffer, pos);
                if ((pos += strLen) <= buffer.writerIndex()) continue;
                return ValidationResult.error("Buffer overflow reading string in Directories");
            }
        }
        return ValidationResult.OK;
    }

    public AssetEditorModsDirectories clone() {
        AssetEditorModsDirectories copy = new AssetEditorModsDirectories();
        copy.directories = this.directories != null ? Arrays.copyOf(this.directories, this.directories.length) : null;
        return copy;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AssetEditorModsDirectories)) {
            return false;
        }
        AssetEditorModsDirectories other = (AssetEditorModsDirectories)obj;
        return Arrays.equals(this.directories, other.directories);
    }

    public int hashCode() {
        int result = 1;
        result = 31 * result + Arrays.hashCode(this.directories);
        return result;
    }
}

