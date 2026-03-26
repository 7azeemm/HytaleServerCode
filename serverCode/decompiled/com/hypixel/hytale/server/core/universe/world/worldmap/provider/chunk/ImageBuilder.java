/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.server.core.universe.world.worldmap.provider.chunk;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.protocol.ShaderType;
import com.hypixel.hytale.protocol.packets.worldmap.MapImage;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkColumn;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.palette.BitFieldArr;
import com.hypixel.hytale.server.core.universe.world.chunk.section.FluidSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ImageBuilder {
    private static volatile boolean quantizationEnabled = true;
    private final long index;
    private final World world;
    private final int imageWidth;
    private final int imageHeight;
    @Nonnull
    private final int[] rawPixels;
    @Nullable
    private MapImage image;
    private final int sampleWidth;
    private final int sampleHeight;
    private final int blockStepX;
    private final int blockStepZ;
    @Nonnull
    private final short[] heightSamples;
    @Nonnull
    private final int[] tintSamples;
    @Nonnull
    private final int[] blockSamples;
    @Nonnull
    private final short[] neighborHeightSamples;
    @Nonnull
    private final short[] fluidDepthSamples;
    @Nonnull
    private final int[] environmentSamples;
    @Nonnull
    private final int[] fluidSamples;
    private final Color outColor = new Color();
    @Nullable
    private WorldChunk worldChunk;
    private FluidSection[] fluidSections;
    private static final int QUANTIZE_STEP = 8;
    private static final int QUANTIZE_HALF = 4;
    private static final int[][] BAYER_MATRIX = new int[][]{{0, 8, 2, 10}, {12, 4, 14, 6}, {3, 11, 1, 9}, {15, 7, 13, 5}};
    private static final int GRADIENT_THRESHOLD = 2;

    public static boolean isQuantizationEnabled() {
        return quantizationEnabled;
    }

    public static boolean toggleQuantization() {
        quantizationEnabled = !quantizationEnabled;
        return quantizationEnabled;
    }

    public ImageBuilder(long index, int imageWidth, int imageHeight, World world) {
        this.index = index;
        this.world = world;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.rawPixels = new int[imageWidth * imageHeight];
        this.sampleWidth = Math.min(32, imageWidth);
        this.sampleHeight = Math.min(32, imageHeight);
        this.blockStepX = Math.max(1, 32 / imageWidth);
        this.blockStepZ = Math.max(1, 32 / imageHeight);
        this.heightSamples = new short[this.sampleWidth * this.sampleHeight];
        this.tintSamples = new int[this.sampleWidth * this.sampleHeight];
        this.blockSamples = new int[this.sampleWidth * this.sampleHeight];
        this.neighborHeightSamples = new short[(this.sampleWidth + 2) * (this.sampleHeight + 2)];
        this.fluidDepthSamples = new short[this.sampleWidth * this.sampleHeight];
        this.environmentSamples = new int[this.sampleWidth * this.sampleHeight];
        this.fluidSamples = new int[this.sampleWidth * this.sampleHeight];
    }

    public long getIndex() {
        return this.index;
    }

    @Nonnull
    public MapImage getImage() {
        return this.image;
    }

    @Nonnull
    private CompletableFuture<ImageBuilder> fetchChunk() {
        return this.world.getChunkStore().getChunkReferenceAsync(this.index).thenApplyAsync(ref -> {
            if (ref == null || !ref.isValid()) {
                return null;
            }
            this.worldChunk = ref.getStore().getComponent((Ref<ChunkStore>)ref, WorldChunk.getComponentType());
            ChunkColumn chunkColumn = ref.getStore().getComponent((Ref<ChunkStore>)ref, ChunkColumn.getComponentType());
            this.fluidSections = new FluidSection[10];
            for (int y = 0; y < 10; ++y) {
                Ref<ChunkStore> sectionRef = chunkColumn.getSection(y);
                this.fluidSections[y] = this.world.getChunkStore().getStore().getComponent(sectionRef, FluidSection.getComponentType());
            }
            return this;
        }, (Executor)this.world);
    }

    @Nonnull
    private CompletableFuture<ImageBuilder> sampleNeighborsSync() {
        CompletionStage north = this.world.getChunkStore().getChunkReferenceAsync(ChunkUtil.indexChunk(this.worldChunk.getX(), this.worldChunk.getZ() - 1)).thenAcceptAsync(ref -> {
            if (ref == null || !ref.isValid()) {
                return;
            }
            WorldChunk worldChunk = ref.getStore().getComponent((Ref<ChunkStore>)ref, WorldChunk.getComponentType());
            int z = (this.sampleHeight - 1) * this.blockStepZ;
            for (int ix = 0; ix < this.sampleWidth; ++ix) {
                int x = ix * this.blockStepX;
                this.neighborHeightSamples[1 + ix] = worldChunk.getHeight(x, z);
            }
        }, (Executor)this.world);
        CompletionStage south = this.world.getChunkStore().getChunkReferenceAsync(ChunkUtil.indexChunk(this.worldChunk.getX(), this.worldChunk.getZ() + 1)).thenAcceptAsync(ref -> {
            if (ref == null || !ref.isValid()) {
                return;
            }
            WorldChunk worldChunk = ref.getStore().getComponent((Ref<ChunkStore>)ref, WorldChunk.getComponentType());
            int z = 0;
            int neighbourStartIndex = (this.sampleHeight + 1) * (this.sampleWidth + 2) + 1;
            for (int ix = 0; ix < this.sampleWidth; ++ix) {
                int x = ix * this.blockStepX;
                this.neighborHeightSamples[neighbourStartIndex + ix] = worldChunk.getHeight(x, z);
            }
        }, (Executor)this.world);
        CompletionStage west = this.world.getChunkStore().getChunkReferenceAsync(ChunkUtil.indexChunk(this.worldChunk.getX() - 1, this.worldChunk.getZ())).thenAcceptAsync(ref -> {
            if (ref == null || !ref.isValid()) {
                return;
            }
            WorldChunk worldChunk = ref.getStore().getComponent((Ref<ChunkStore>)ref, WorldChunk.getComponentType());
            int x = (this.sampleWidth - 1) * this.blockStepX;
            for (int iz = 0; iz < this.sampleHeight; ++iz) {
                int z = iz * this.blockStepZ;
                this.neighborHeightSamples[(iz + 1) * (this.sampleWidth + 2)] = worldChunk.getHeight(x, z);
            }
        }, (Executor)this.world);
        CompletionStage east = this.world.getChunkStore().getChunkReferenceAsync(ChunkUtil.indexChunk(this.worldChunk.getX() + 1, this.worldChunk.getZ())).thenAcceptAsync(ref -> {
            if (ref == null || !ref.isValid()) {
                return;
            }
            WorldChunk worldChunk = ref.getStore().getComponent((Ref<ChunkStore>)ref, WorldChunk.getComponentType());
            int x = 0;
            for (int iz = 0; iz < this.sampleHeight; ++iz) {
                int z = iz * this.blockStepZ;
                this.neighborHeightSamples[(iz + 1) * (this.sampleWidth + 2) + (this.sampleWidth + 1)] = worldChunk.getHeight(x, z);
            }
        }, (Executor)this.world);
        CompletionStage northeast = this.world.getChunkStore().getChunkReferenceAsync(ChunkUtil.indexChunk(this.worldChunk.getX() + 1, this.worldChunk.getZ() - 1)).thenAcceptAsync(ref -> {
            if (ref == null || !ref.isValid()) {
                return;
            }
            WorldChunk worldChunk = ref.getStore().getComponent((Ref<ChunkStore>)ref, WorldChunk.getComponentType());
            int x = 0;
            int z = (this.sampleHeight - 1) * this.blockStepZ;
            this.neighborHeightSamples[0] = worldChunk.getHeight(x, z);
        }, (Executor)this.world);
        CompletionStage northwest = this.world.getChunkStore().getChunkReferenceAsync(ChunkUtil.indexChunk(this.worldChunk.getX() - 1, this.worldChunk.getZ() - 1)).thenAcceptAsync(ref -> {
            if (ref == null || !ref.isValid()) {
                return;
            }
            WorldChunk worldChunk = ref.getStore().getComponent((Ref<ChunkStore>)ref, WorldChunk.getComponentType());
            int x = (this.sampleWidth - 1) * this.blockStepX;
            int z = (this.sampleHeight - 1) * this.blockStepZ;
            this.neighborHeightSamples[this.sampleWidth + 1] = worldChunk.getHeight(x, z);
        }, (Executor)this.world);
        CompletionStage southeast = this.world.getChunkStore().getChunkReferenceAsync(ChunkUtil.indexChunk(this.worldChunk.getX() + 1, this.worldChunk.getZ() + 1)).thenAcceptAsync(ref -> {
            if (ref == null || !ref.isValid()) {
                return;
            }
            WorldChunk worldChunk = ref.getStore().getComponent((Ref<ChunkStore>)ref, WorldChunk.getComponentType());
            int x = 0;
            int z = 0;
            this.neighborHeightSamples[(this.sampleHeight + 1) * (this.sampleWidth + 2) + (this.sampleWidth + 1)] = worldChunk.getHeight(x, z);
        }, (Executor)this.world);
        CompletionStage southwest = this.world.getChunkStore().getChunkReferenceAsync(ChunkUtil.indexChunk(this.worldChunk.getX() - 1, this.worldChunk.getZ() + 1)).thenAcceptAsync(ref -> {
            if (ref == null || !ref.isValid()) {
                return;
            }
            WorldChunk worldChunk = ref.getStore().getComponent((Ref<ChunkStore>)ref, WorldChunk.getComponentType());
            int x = (this.sampleWidth - 1) * this.blockStepX;
            int z = 0;
            this.neighborHeightSamples[(this.sampleHeight + 1) * (this.sampleWidth + 2)] = worldChunk.getHeight(x, z);
        }, (Executor)this.world);
        return CompletableFuture.allOf(new CompletableFuture[]{north, south, west, east, northeast, northwest, southeast, southwest}).thenApply(v -> this);
    }

    @Nonnull
    private ImageBuilder generateImageAsync() {
        for (int ix = 0; ix < this.sampleWidth; ++ix) {
            for (int iz = 0; iz < this.sampleHeight; ++iz) {
                int blockId;
                int sampleIndex = iz * this.sampleWidth + ix;
                int x = ix * this.blockStepX;
                int z = iz * this.blockStepZ;
                int height = this.worldChunk.getHeight(x, z);
                int tint = this.worldChunk.getTint(x, z);
                this.heightSamples[sampleIndex] = height;
                this.tintSamples[sampleIndex] = tint;
                this.blockSamples[sampleIndex] = blockId = this.worldChunk.getBlock(x, height, z);
                int fluidId = 0;
                int fluidTop = 320;
                Fluid fluid = null;
                int chunkYGround = ChunkUtil.chunkCoordinate(height);
                int chunkY = 9;
                block2: while (chunkY >= 0 && chunkY >= chunkYGround) {
                    int maxBlockY;
                    FluidSection fluidSection = this.fluidSections[chunkY];
                    if (fluidSection == null || fluidSection.isEmpty()) {
                        --chunkY;
                        continue;
                    }
                    int minBlockY = Math.max(ChunkUtil.minBlock(chunkY), height);
                    for (int blockY = maxBlockY = ChunkUtil.maxBlock(chunkY); blockY >= minBlockY; --blockY) {
                        fluidId = fluidSection.getFluidId(x, blockY, z);
                        if (fluidId == 0) continue;
                        fluid = Fluid.getAssetMap().getAsset(fluidId);
                        fluidTop = blockY;
                        break block2;
                    }
                    --chunkY;
                }
                int fluidBottom = height;
                block4: while (chunkY >= 0 && chunkY >= chunkYGround) {
                    int maxBlockY;
                    FluidSection fluidSection = this.fluidSections[chunkY];
                    if (fluidSection == null || fluidSection.isEmpty()) {
                        fluidBottom = Math.min(ChunkUtil.maxBlock(chunkY) + 1, fluidTop);
                        break;
                    }
                    int minBlockY = Math.max(ChunkUtil.minBlock(chunkY), height);
                    for (int blockY = maxBlockY = Math.min(ChunkUtil.maxBlock(chunkY), fluidTop - 1); blockY >= minBlockY; --blockY) {
                        int nextFluidId = fluidSection.getFluidId(x, blockY, z);
                        if (nextFluidId == fluidId) continue;
                        Fluid nextFluid = Fluid.getAssetMap().getAsset(nextFluidId);
                        if (Objects.equals(fluid.getParticleColor(), nextFluid.getParticleColor())) continue;
                        fluidBottom = blockY + 1;
                        break block4;
                    }
                    --chunkY;
                }
                short fluidDepth = fluidId != 0 ? (short)(fluidTop - fluidBottom + 1) : (short)0;
                int environmentId = this.worldChunk.getBlockChunk().getEnvironment(x, fluidTop, z);
                this.fluidDepthSamples[sampleIndex] = fluidDepth;
                this.environmentSamples[sampleIndex] = environmentId;
                this.fluidSamples[sampleIndex] = fluidId;
            }
        }
        float imageToSampleRatioWidth = (float)this.sampleWidth / (float)this.imageWidth;
        float imageToSampleRatioHeight = (float)this.sampleHeight / (float)this.imageHeight;
        int blockPixelWidth = Math.max(1, this.imageWidth / this.sampleWidth);
        int blockPixelHeight = Math.max(1, this.imageHeight / this.sampleHeight);
        for (int iz = 0; iz < this.sampleHeight; ++iz) {
            System.arraycopy(this.heightSamples, iz * this.sampleWidth, this.neighborHeightSamples, (iz + 1) * (this.sampleWidth + 2) + 1, this.sampleWidth);
        }
        for (int ix = 0; ix < this.imageWidth; ++ix) {
            for (int iz = 0; iz < this.imageHeight; ++iz) {
                int fluidId;
                int sampleX = Math.min((int)((float)ix * imageToSampleRatioWidth), this.sampleWidth - 1);
                int sampleZ = Math.min((int)((float)iz * imageToSampleRatioHeight), this.sampleHeight - 1);
                int sampleIndex = sampleZ * this.sampleWidth + sampleX;
                int blockPixelX = ix % blockPixelWidth;
                int blockPixelZ = iz % blockPixelHeight;
                short height = this.heightSamples[sampleIndex];
                int tint = this.tintSamples[sampleIndex];
                int blockId = this.blockSamples[sampleIndex];
                if (height < 0 || blockId == 0) {
                    this.outColor.a = 0;
                    this.outColor.b = 0;
                    this.outColor.g = 0;
                    this.outColor.r = 0;
                    this.packImageData(ix, iz);
                    continue;
                }
                ImageBuilder.getBlockColor(blockId, tint, this.outColor);
                short north = this.neighborHeightSamples[sampleZ * (this.sampleWidth + 2) + sampleX + 1];
                short south = this.neighborHeightSamples[(sampleZ + 2) * (this.sampleWidth + 2) + sampleX + 1];
                short west = this.neighborHeightSamples[(sampleZ + 1) * (this.sampleWidth + 2) + sampleX];
                short east = this.neighborHeightSamples[(sampleZ + 1) * (this.sampleWidth + 2) + sampleX + 2];
                short northWest = this.neighborHeightSamples[sampleZ * (this.sampleWidth + 2) + sampleX];
                short northEast = this.neighborHeightSamples[sampleZ * (this.sampleWidth + 2) + sampleX + 2];
                short southWest = this.neighborHeightSamples[(sampleZ + 2) * (this.sampleWidth + 2) + sampleX];
                short southEast = this.neighborHeightSamples[(sampleZ + 2) * (this.sampleWidth + 2) + sampleX + 2];
                float shade = ImageBuilder.shadeFromHeights(blockPixelX, blockPixelZ, blockPixelWidth, blockPixelHeight, height, north, south, west, east, northWest, northEast, southWest, southEast);
                this.outColor.multiply(shade);
                if (height < 320 && (fluidId = this.fluidSamples[sampleIndex]) != 0) {
                    short fluidDepth = this.fluidDepthSamples[sampleIndex];
                    int environmentId = this.environmentSamples[sampleIndex];
                    ImageBuilder.getFluidColor(fluidId, environmentId, fluidDepth, this.outColor);
                }
                this.packImageData(ix, iz);
            }
        }
        this.image = this.encodeToPalette();
        return this;
    }

    private static int quantizeChannel(int value) {
        return Math.min(255, (value + 4) / 8 * 8);
    }

    private static boolean isNearBoundary(int value) {
        int distanceFromBoundary = (value + 4) % 8;
        return distanceFromBoundary <= 2 || distanceFromBoundary >= 6;
    }

    private static int quantizeChannelWithDither(int value, int ditherOffset) {
        int adjusted = value + ditherOffset;
        adjusted = Math.max(0, Math.min(255, adjusted));
        return Math.min(255, (adjusted + 4) / 8 * 8);
    }

    private static int quantizeColor(int argb) {
        int r = ImageBuilder.quantizeChannel(argb >> 24 & 0xFF);
        int g = ImageBuilder.quantizeChannel(argb >> 16 & 0xFF);
        int b = ImageBuilder.quantizeChannel(argb >> 8 & 0xFF);
        int a = argb & 0xFF;
        return r << 24 | g << 16 | b << 8 | a;
    }

    private static boolean colorNearBoundary(int argb) {
        int r = argb >> 24 & 0xFF;
        int g = argb >> 16 & 0xFF;
        int b = argb >> 8 & 0xFF;
        return ImageBuilder.isNearBoundary(r) || ImageBuilder.isNearBoundary(g) || ImageBuilder.isNearBoundary(b);
    }

    private static int quantizeColorWithDither(int argb, int x, int y) {
        int bayerValue = BAYER_MATRIX[y & 3][x & 3];
        int ditherOffset = (bayerValue - 8) * 8 / 16;
        int r = ImageBuilder.quantizeChannelWithDither(argb >> 24 & 0xFF, ditherOffset);
        int g = ImageBuilder.quantizeChannelWithDither(argb >> 16 & 0xFF, ditherOffset);
        int b = ImageBuilder.quantizeChannelWithDither(argb >> 8 & 0xFF, ditherOffset);
        int a = argb & 0xFF;
        return r << 24 | g << 16 | b << 8 | a;
    }

    private boolean isInTransitionZone(int index) {
        int centerPixel = this.rawPixels[index];
        int centerQuantized = ImageBuilder.quantizeColor(centerPixel);
        int x = index % this.imageWidth;
        int y = index / this.imageWidth;
        for (int dy = -2; dy <= 2; ++dy) {
            for (int dx = -2; dx <= 2; ++dx) {
                int neighborPixel;
                int neighborQuantized;
                if (dx == 0 && dy == 0) continue;
                int nx = x + dx;
                int ny = y + dy;
                if (nx < 0 || nx >= this.imageWidth || ny < 0 || ny >= this.imageHeight || (neighborQuantized = ImageBuilder.quantizeColor(neighborPixel = this.rawPixels[ny * this.imageWidth + nx])) == centerQuantized) continue;
                return true;
            }
        }
        return false;
    }

    @Nonnull
    private MapImage encodeToPalette() {
        int pixelCount = this.rawPixels.length;
        int[] processedPixels = new int[pixelCount];
        IntOpenHashSet uniqueColors = new IntOpenHashSet();
        for (int i = 0; i < pixelCount; ++i) {
            int pixel;
            if (quantizationEnabled) {
                if (this.isInTransitionZone(i)) {
                    int x = i % this.imageWidth;
                    int y = i / this.imageWidth;
                    pixel = ImageBuilder.quantizeColorWithDither(this.rawPixels[i], x, y);
                } else {
                    pixel = ImageBuilder.quantizeColor(this.rawPixels[i]);
                }
            } else {
                pixel = this.rawPixels[i];
            }
            processedPixels[i] = pixel;
            uniqueColors.add(pixel);
        }
        int[] palette = uniqueColors.toIntArray();
        int bitsPerIndex = ImageBuilder.calculateBitsRequired(palette.length);
        Int2IntOpenHashMap colorToIndex = new Int2IntOpenHashMap(palette.length);
        for (int i = 0; i < palette.length; ++i) {
            colorToIndex.put(palette[i], i);
        }
        BitFieldArr indices = new BitFieldArr(bitsPerIndex, pixelCount);
        for (int i = 0; i < pixelCount; ++i) {
            indices.set(i, colorToIndex.get(processedPixels[i]));
        }
        byte[] packedIndices = indices.get();
        return new MapImage(this.imageWidth, this.imageHeight, palette, (byte)bitsPerIndex, packedIndices);
    }

    private static int calculateBitsRequired(int colorCount) {
        if (colorCount <= 16) {
            return 4;
        }
        if (colorCount <= 256) {
            return 8;
        }
        if (colorCount <= 4096) {
            return 12;
        }
        return 16;
    }

    private void packImageData(int ix, int iz) {
        this.rawPixels[iz * this.imageWidth + ix] = this.outColor.pack();
    }

    private static float shadeFromHeights(int blockPixelX, int blockPixelZ, int blockPixelWidth, int blockPixelHeight, short height, short north, short south, short west, short east, short northWest, short northEast, short southWest, short southEast) {
        float u = ((float)blockPixelX + 0.5f) / (float)blockPixelWidth;
        float v = ((float)blockPixelZ + 0.5f) / (float)blockPixelHeight;
        float ud = (u + v) / 2.0f;
        float vd = (1.0f - u + v) / 2.0f;
        float dhdx1 = (float)(height - west) * (1.0f - u) + (float)(east - height) * u;
        float dhdz1 = (float)(height - north) * (1.0f - v) + (float)(south - height) * v;
        float dhdx2 = (float)(height - northWest) * (1.0f - ud) + (float)(southEast - height) * ud;
        float dhdz2 = (float)(height - northEast) * (1.0f - vd) + (float)(southWest - height) * vd;
        float dhdx = dhdx1 * 2.0f + dhdx2;
        float dhdz = dhdz1 * 2.0f + dhdz2;
        float dy = 3.0f;
        float nx = dhdx;
        float ny = dy;
        float nz = dhdz;
        float invS = 1.0f / (float)Math.sqrt(nx * nx + ny * ny + nz * nz);
        nx *= invS;
        ny *= invS;
        float lx = -0.2f;
        float ly = 0.8f;
        float lz = 0.5f;
        float invL = 1.0f / (float)Math.sqrt(lx * lx + ly * ly + lz * lz);
        float lambert = Math.max(0.0f, nx * (lx *= invL) + ny * (ly *= invL) + (nz *= invS) * (lz *= invL));
        float ambient = 0.4f;
        float diffuse = 0.6f;
        return ambient + diffuse * lambert;
    }

    private static void getBlockColor(int blockId, int biomeTintColor, @Nonnull Color outColor) {
        BlockType block = BlockType.getAssetMap().getAsset(blockId);
        int biomeTintR = biomeTintColor >> 16 & 0xFF;
        int biomeTintG = biomeTintColor >> 8 & 0xFF;
        int biomeTintB = biomeTintColor >> 0 & 0xFF;
        com.hypixel.hytale.protocol.Color[] tintUp = block.getTintUp();
        boolean hasTint = tintUp != null && tintUp.length > 0;
        int selfTintR = hasTint ? tintUp[0].red & 0xFF : 255;
        int selfTintG = hasTint ? tintUp[0].green & 0xFF : 255;
        int selfTintB = hasTint ? tintUp[0].blue & 0xFF : 255;
        float biomeTintMultiplier = (float)block.getBiomeTintUp() / 100.0f;
        int tintColorR = (int)((float)selfTintR + (float)(biomeTintR - selfTintR) * biomeTintMultiplier);
        int tintColorG = (int)((float)selfTintG + (float)(biomeTintG - selfTintG) * biomeTintMultiplier);
        int tintColorB = (int)((float)selfTintB + (float)(biomeTintB - selfTintB) * biomeTintMultiplier);
        com.hypixel.hytale.protocol.Color particleColor = block.getParticleColor();
        if (particleColor != null && biomeTintMultiplier < 1.0f) {
            tintColorR = tintColorR * (particleColor.red & 0xFF) / 255;
            tintColorG = tintColorG * (particleColor.green & 0xFF) / 255;
            tintColorB = tintColorB * (particleColor.blue & 0xFF) / 255;
        }
        outColor.r = tintColorR & 0xFF;
        outColor.g = tintColorG & 0xFF;
        outColor.b = tintColorB & 0xFF;
        outColor.a = 255;
    }

    private static void getFluidColor(int fluidId, int environmentId, int fluidDepth, @Nonnull Color outColor) {
        com.hypixel.hytale.protocol.Color partcileColor;
        Environment environment;
        com.hypixel.hytale.protocol.Color waterTint;
        int tintColorR = 255;
        int tintColorG = 255;
        int tintColorB = 255;
        Fluid fluid = Fluid.getAssetMap().getAsset(fluidId);
        if (fluid == null) {
            return;
        }
        if (fluid.hasEffect(ShaderType.Water) && (waterTint = (environment = Environment.getAssetMap().getAsset(environmentId)).getWaterTint()) != null) {
            tintColorR = tintColorR * (waterTint.red & 0xFF) / 255;
            tintColorG = tintColorG * (waterTint.green & 0xFF) / 255;
            tintColorB = tintColorB * (waterTint.blue & 0xFF) / 255;
        }
        if ((partcileColor = fluid.getParticleColor()) != null) {
            tintColorR = tintColorR * (partcileColor.red & 0xFF) / 255;
            tintColorG = tintColorG * (partcileColor.green & 0xFF) / 255;
            tintColorB = tintColorB * (partcileColor.blue & 0xFF) / 255;
        }
        float depthMultiplier = Math.min(1.0f, 1.0f / (float)fluidDepth);
        outColor.r = (int)((float)tintColorR + (float)((outColor.r & 0xFF) - tintColorR) * depthMultiplier) & 0xFF;
        outColor.g = (int)((float)tintColorG + (float)((outColor.g & 0xFF) - tintColorG) * depthMultiplier) & 0xFF;
        outColor.b = (int)((float)tintColorB + (float)((outColor.b & 0xFF) - tintColorB) * depthMultiplier) & 0xFF;
    }

    @Nonnull
    public static CompletableFuture<ImageBuilder> build(long index, int imageWidth, int imageHeight, World world) {
        return ((CompletableFuture)((CompletableFuture)CompletableFuture.completedFuture(new ImageBuilder(index, imageWidth, imageHeight, world)).thenCompose(ImageBuilder::fetchChunk)).thenCompose(builder -> builder != null ? builder.sampleNeighborsSync() : CompletableFuture.completedFuture(null))).thenApplyAsync(builder -> builder != null ? builder.generateImageAsync() : null);
    }

    private static class Color {
        public int r;
        public int g;
        public int b;
        public int a;

        private Color() {
        }

        public int pack() {
            return (this.r & 0xFF) << 24 | (this.g & 0xFF) << 16 | (this.b & 0xFF) << 8 | this.a & 0xFF;
        }

        public void multiply(float value) {
            this.r = Math.min(255, Math.max(0, (int)((float)this.r * value)));
            this.g = Math.min(255, Math.max(0, (int)((float)this.g * value)));
            this.b = Math.min(255, Math.max(0, (int)((float)this.b * value)));
        }
    }
}

