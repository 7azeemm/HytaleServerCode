/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.server.core.schema;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.JsonAsset;
import com.hypixel.hytale.assetstore.codec.AssetCodec;
import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec;
import com.hypixel.hytale.codec.EmptyExtraInfo;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.ObjectSchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.util.BsonUtil;
import com.hypixel.hytale.sneakythrow.SneakyThrow;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonString;

public class SchemaGenerator {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final List<ConfigRegistration> configRegistrations = new CopyOnWriteArrayList<ConfigRegistration>();
    private static final List<AssetSchemaRegistration> assetSchemaRegistrations = new CopyOnWriteArrayList<AssetSchemaRegistration>();

    public static void registerConfig(@Nonnull String name, @Nonnull BuilderCodec<?> codec, @Nullable String virtualPath, @Nullable List<String> fileMatchPatterns) {
        configRegistrations.add(new ConfigRegistration(name, codec, virtualPath, fileMatchPatterns));
    }

    public static void registerAssetSchema(@Nonnull String fileName, @Nonnull Function<SchemaContext, Schema> factory, @Nullable List<String> fileMatchPatterns, @Nullable String extension) {
        assetSchemaRegistrations.add(new AssetSchemaRegistration(fileName, factory, fileMatchPatterns, extension));
    }

    @Nonnull
    public static Map<String, Schema> generateAssetSchemas() {
        GenerationResult result = SchemaGenerator.collectSchemas(false);
        LinkedHashMap<String, Schema> schemas = new LinkedHashMap<String, Schema>();
        schemas.putAll(result.assetSchemas());
        schemas.putAll(result.customAssetSchemas());
        schemas.putAll(result.sharedSchemas());
        return schemas;
    }

    public static void generate(@Nullable Path assetOutputDir, @Nullable Path configOutputDir) {
        try {
            boolean sameDir;
            GenerationResult result = SchemaGenerator.collectSchemas(configOutputDir != null);
            boolean bl = sameDir = assetOutputDir != null && configOutputDir != null && assetOutputDir.toAbsolutePath().normalize().equals(configOutputDir.toAbsolutePath().normalize());
            if (sameDir) {
                Path schemaDir = assetOutputDir.resolve("Schema");
                SchemaGenerator.cleanAndCreateSchemaDir(schemaDir);
                LinkedHashMap<String, Schema> allSchemas = new LinkedHashMap<String, Schema>();
                allSchemas.putAll(result.assetSchemas());
                allSchemas.putAll(result.customAssetSchemas());
                allSchemas.putAll(result.configSchemas());
                allSchemas.putAll(result.sharedSchemas());
                SchemaGenerator.writeSchemas(allSchemas, schemaDir);
                ArrayList<VsCodeEntry> allVsCodeEntries = new ArrayList<VsCodeEntry>();
                allVsCodeEntries.addAll(result.assetVsCodeEntries());
                allVsCodeEntries.addAll(result.customAssetVsCodeEntries());
                allVsCodeEntries.addAll(result.configVsCodeEntries());
                SchemaGenerator.writeVsCodeSettings(assetOutputDir, allVsCodeEntries);
            } else {
                Path schemaDir;
                if (assetOutputDir != null) {
                    schemaDir = assetOutputDir.resolve("Schema");
                    SchemaGenerator.cleanAndCreateSchemaDir(schemaDir);
                    LinkedHashMap<String, Schema> allAssetSchemas = new LinkedHashMap<String, Schema>();
                    allAssetSchemas.putAll(result.assetSchemas());
                    allAssetSchemas.putAll(result.customAssetSchemas());
                    allAssetSchemas.putAll(result.sharedSchemas());
                    SchemaGenerator.writeSchemas(allAssetSchemas, schemaDir);
                    ArrayList<VsCodeEntry> allAssetVsCode = new ArrayList<VsCodeEntry>();
                    allAssetVsCode.addAll(result.assetVsCodeEntries());
                    allAssetVsCode.addAll(result.customAssetVsCodeEntries());
                    SchemaGenerator.writeVsCodeSettings(assetOutputDir, allAssetVsCode);
                }
                if (configOutputDir != null) {
                    schemaDir = configOutputDir.resolve("Schema");
                    SchemaGenerator.cleanAndCreateSchemaDir(schemaDir);
                    LinkedHashMap<String, Schema> allConfigSchemas = new LinkedHashMap<String, Schema>();
                    allConfigSchemas.putAll(result.configSchemas());
                    allConfigSchemas.putAll(result.sharedSchemas());
                    SchemaGenerator.writeSchemas(allConfigSchemas, schemaDir);
                    SchemaGenerator.writeVsCodeSettings(configOutputDir, result.configVsCodeEntries());
                }
            }
        }
        catch (Throwable t) {
            ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(t)).log("Schema generation failed");
            throw new RuntimeException("Schema generation failed", t);
        }
    }

    private static GenerationResult collectSchemas(boolean includeConfigs) {
        SchemaContext context = new SchemaContext();
        AssetStore[] assetStores = (AssetStore[])AssetRegistry.getStoreMap().values().toArray(AssetStore[]::new);
        Arrays.sort(assetStores, Comparator.comparing(store -> store.getAssetClass().getSimpleName()));
        for (AssetStore store2 : assetStores) {
            String name = store2.getAssetClass().getSimpleName();
            context.addFileReference(name + ".json", store2.getCodec());
        }
        LinkedHashMap<String, Schema> assetSchemas = new LinkedHashMap<String, Schema>();
        ArrayList<VsCodeEntry> assetVsCodeEntries = new ArrayList<VsCodeEntry>();
        for (AssetStore store3 : assetStores) {
            List preload;
            Class assetClass = store3.getAssetClass();
            String path = store3.getPath();
            String name = assetClass.getSimpleName();
            AssetCodec codec = store3.getCodec();
            Schema schema = codec.toSchema(context);
            if (codec instanceof AssetCodecMapCodec) {
                schema.setTitle(name);
            }
            schema.setId(name + ".json");
            Schema.HytaleMetadata hytale = schema.getHytale();
            hytale.setPath(path);
            hytale.setExtension(store3.getExtension());
            Class<JsonAsset<?>> idProvider = store3.getIdProvider();
            if (idProvider != null) {
                hytale.setIdProvider(idProvider.getSimpleName());
            }
            if ((preload = store3.getPreAddedAssets()) != null && !preload.isEmpty()) {
                String[] internal = new String[preload.size()];
                for (int i = 0; i < preload.size(); ++i) {
                    Object p2 = preload.get(i);
                    Object k = store3.getKeyFunction().apply(p2);
                    internal[i] = k.toString();
                }
                hytale.setInternalKeys(internal);
            }
            assetSchemas.put(name + ".json", schema);
            assetVsCodeEntries.add(new VsCodeEntry(name + ".json", List.of("/Server/" + path + "/*" + store3.getExtension(), "/Server/" + path + "/**/*" + store3.getExtension()), store3.getExtension()));
        }
        LinkedHashMap<String, Schema> customAssetSchemas = new LinkedHashMap<String, Schema>();
        ArrayList<VsCodeEntry> customAssetVsCodeEntries = new ArrayList<VsCodeEntry>();
        for (AssetSchemaRegistration reg : assetSchemaRegistrations) {
            Schema schema = reg.factory().apply(context);
            customAssetSchemas.put(reg.fileName(), schema);
            if (reg.fileMatchPatterns() == null || reg.fileMatchPatterns().isEmpty()) continue;
            String ext = reg.extension() != null ? reg.extension() : ".json";
            customAssetVsCodeEntries.add(new VsCodeEntry(reg.fileName(), reg.fileMatchPatterns().stream().map(p -> "/Server/" + p).toList(), ext));
        }
        LinkedHashMap<String, Schema> configSchemas = new LinkedHashMap<String, Schema>();
        ArrayList<VsCodeEntry> configVsCodeEntries = new ArrayList<VsCodeEntry>();
        if (includeConfigs) {
            for (ConfigRegistration reg : configRegistrations) {
                try {
                    ObjectSchema schema = reg.codec().toSchema(context);
                    schema.setTitle(reg.name());
                    String fileName = SchemaGenerator.toFileName(reg.name());
                    schema.setId(fileName);
                    Schema.HytaleMetadata hytale = schema.getHytale();
                    if (reg.virtualPath() != null) {
                        hytale.setVirtualPath(reg.virtualPath());
                    }
                    configSchemas.put(fileName, schema);
                    if (reg.fileMatchPatterns() == null || reg.fileMatchPatterns().isEmpty()) continue;
                    configVsCodeEntries.add(new VsCodeEntry(fileName, reg.fileMatchPatterns(), null));
                }
                catch (Throwable t) {
                    ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(t)).log("Failed to generate config schema for '%s', skipping", reg.name());
                }
            }
        }
        LinkedHashMap<String, Schema> sharedSchemas = new LinkedHashMap<String, Schema>();
        Schema definitions = new Schema();
        definitions.setDefinitions(context.getDefinitions());
        definitions.setId("common.json");
        sharedSchemas.put("common.json", definitions);
        Schema otherDefinitions = new Schema();
        otherDefinitions.setDefinitions(context.getOtherDefinitions());
        otherDefinitions.setId("other.json");
        sharedSchemas.put("other.json", otherDefinitions);
        return new GenerationResult(assetSchemas, customAssetSchemas, configSchemas, sharedSchemas, assetVsCodeEntries, customAssetVsCodeEntries, configVsCodeEntries);
    }

    private static void cleanAndCreateSchemaDir(@Nonnull Path schemaDir) {
        try {
            Files.createDirectories(schemaDir, new FileAttribute[0]);
            try (Stream<Path> stream = Files.walk(schemaDir, 1, new FileVisitOption[0]);){
                stream.filter(v -> v.toString().endsWith(".json")).forEach(SneakyThrow.sneakyConsumer(Files::delete));
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to prepare schema directory: " + String.valueOf(schemaDir), e);
        }
    }

    private static void writeVsCodeSettings(@Nonnull Path outputDir, @Nonnull List<VsCodeEntry> entries) {
        try {
            BsonDocument vsCodeConfig = new BsonDocument();
            BsonArray vsCodeSchemas = new BsonArray();
            BsonDocument vsCodeFiles = new BsonDocument();
            vsCodeConfig.put("json.schemas", vsCodeSchemas);
            vsCodeConfig.put("files.associations", vsCodeFiles);
            vsCodeConfig.put("editor.tabSize", new BsonInt32(2));
            for (VsCodeEntry entry : entries) {
                SchemaGenerator.addVsCodeSchemaLink(vsCodeConfig, entry.schemaFileName(), entry.fileMatchPatterns(), entry.extension());
            }
            Files.createDirectories(outputDir.resolve(".vscode"), new FileAttribute[0]);
            BsonUtil.writeDocument(outputDir.resolve(".vscode/settings.json"), vsCodeConfig, false).join();
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to write .vscode/settings.json", e);
        }
    }

    @Nonnull
    public static String toFileName(@Nonnull String name) {
        return name.replace(':', '.') + ".json";
    }

    public static void writeSchemas(@Nonnull Map<String, Schema> schemas, @Nonnull Path schemaDir) {
        for (Map.Entry<String, Schema> schema : schemas.entrySet()) {
            BsonUtil.writeDocument(schemaDir.resolve(schema.getKey()), Schema.CODEC.encode(schema.getValue(), EmptyExtraInfo.EMPTY).asDocument(), false).join();
        }
    }

    public static void addVsCodeSchemaLink(@Nonnull BsonDocument vsCodeConfig, @Nonnull String schemaFileName, @Nonnull List<String> fileMatchPatterns, @Nullable String extension) {
        BsonDocument config = new BsonDocument();
        config.put("fileMatch", new BsonArray(fileMatchPatterns.stream().map(BsonString::new).toList()));
        config.put("url", new BsonString("./Schema/" + schemaFileName));
        vsCodeConfig.getArray("json.schemas").add(config);
        if (extension != null && !extension.equals(".json")) {
            vsCodeConfig.getDocument("files.associations").put("*" + extension, new BsonString("json"));
        }
    }

    private record ConfigRegistration(@Nonnull String name, @Nonnull BuilderCodec<?> codec, @Nullable String virtualPath, @Nullable List<String> fileMatchPatterns) {
    }

    private record AssetSchemaRegistration(@Nonnull String fileName, @Nonnull Function<SchemaContext, Schema> factory, @Nullable List<String> fileMatchPatterns, @Nullable String extension) {
    }

    private record GenerationResult(LinkedHashMap<String, Schema> assetSchemas, LinkedHashMap<String, Schema> customAssetSchemas, LinkedHashMap<String, Schema> configSchemas, LinkedHashMap<String, Schema> sharedSchemas, List<VsCodeEntry> assetVsCodeEntries, List<VsCodeEntry> customAssetVsCodeEntries, List<VsCodeEntry> configVsCodeEntries) {
    }

    private record VsCodeEntry(String schemaFileName, List<String> fileMatchPatterns, @Nullable String extension) {
    }
}

