/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.server.core.ui.browser;

import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.common.plugin.AuthorInfo;
import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.common.semver.Semver;
import com.hypixel.hytale.common.util.StringCompareUtil;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Constants;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.HytaleServerConfig;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.Options;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import com.hypixel.hytale.server.core.ui.LocalizableString;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.browser.AssetPackSaveBrowserConfig;
import com.hypixel.hytale.server.core.ui.browser.AssetPackSaveBrowserEventData;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.util.BsonUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AssetPackSaveBrowser {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final Value<String> BUTTON_SELECTED = Value.ref("Pages/BasicTextButton.ui", "SelectedLabelStyle");
    private static final Value<String> TOOLTIP_STYLE = Value.ref("Common.ui", "DefaultTextTooltipStyle");
    private static final Pattern INVALID_FILENAME_CHARS = Pattern.compile("[<>:\"/\\\\|?*\\x00-\\x1F]");
    private static final String DIRECTORY_FILTER_ALL = "";
    @Nonnull
    private final AssetPackSaveBrowserConfig config;
    @Nonnull
    private String searchQuery = "";
    @Nullable
    private String selectedPackKey;
    @Nullable
    private PendingPack pendingPack;
    @Nonnull
    private String selectedDirectoryFilter = "";
    public static final String ACTION_OPEN_PACK_BROWSER = "OpenPackBrowser";
    public static final String ACTION_CONFIRM_PACK_BROWSER = "ConfirmPackBrowser";
    public static final String ACTION_CANCEL_PACK_BROWSER = "CancelPackBrowser";
    public static final String ACTION_OPEN_CREATE_PACK = "OpenCreatePack";
    public static final String ACTION_CREATE_PACK = "CreatePack";
    public static final String ACTION_CANCEL_CREATE_PACK = "CancelCreatePack";
    public static final String ACTION_PACK_SEARCH = "PackSearch";
    public static final String ACTION_PACK_SELECT = "PackSelect";

    public AssetPackSaveBrowser(@Nonnull AssetPackSaveBrowserConfig config) {
        this.config = config;
    }

    @Nonnull
    private List<ModsDirectory> collectModsDirectories() {
        ObjectArrayList<ModsDirectory> dirs = new ObjectArrayList<ModsDirectory>();
        if (Constants.SINGLEPLAYER) {
            dirs.add(new ModsDirectory("server.customUI.assetPackBrowser.create.targetDir.world", "server.customUI.assetPackBrowser.filter.world", PluginManager.MODS_PATH));
            for (Path modsPath : Options.getOptionSet().valuesOf(Options.MODS_DIRECTORIES)) {
                dirs.add(new ModsDirectory("server.customUI.assetPackBrowser.create.targetDir.global", "server.customUI.assetPackBrowser.filter.global", modsPath));
            }
        } else {
            dirs.add(new ModsDirectory("server.customUI.assetPackBrowser.create.targetDir.server", "server.customUI.assetPackBrowser.filter.server", PluginManager.MODS_PATH));
            for (Path modsPath : Options.getOptionSet().valuesOf(Options.MODS_DIRECTORIES)) {
                dirs.add(new ModsDirectory("server.customUI.assetPackBrowser.create.targetDir.server", "server.customUI.assetPackBrowser.filter.server", modsPath));
            }
        }
        return dirs;
    }

    @Nonnull
    private Path getDefaultTargetDirectory() {
        List<Path> cliDirs;
        if (Constants.SINGLEPLAYER && !(cliDirs = Options.getOptionSet().valuesOf(Options.MODS_DIRECTORIES)).isEmpty()) {
            return cliDirs.getFirst();
        }
        return PluginManager.MODS_PATH;
    }

    @Nullable
    private Path resolveTargetDirectory(@Nullable String pathStr) {
        if (pathStr == null || pathStr.isBlank()) {
            return this.getDefaultTargetDirectory();
        }
        Path requested = Path.of(pathStr, new String[0]).normalize();
        for (ModsDirectory dir : this.collectModsDirectories()) {
            if (!dir.path().normalize().equals(requested)) continue;
            return dir.path();
        }
        return null;
    }

    private void populateTargetDirectoryDropdown(@Nonnull UICommandBuilder commandBuilder) {
        List<ModsDirectory> dirs = this.collectModsDirectories();
        if (dirs.size() <= 1) {
            commandBuilder.set("#CreatePackPage #TargetDirectory.Visible", false);
            return;
        }
        commandBuilder.set("#CreatePackPage #TargetDirectory.Visible", true);
        ObjectArrayList<DropdownEntryInfo> entries = new ObjectArrayList<DropdownEntryInfo>();
        for (ModsDirectory dir : dirs) {
            entries.add(new DropdownEntryInfo(LocalizableString.fromMessageId(dir.langKey()), dir.path().toString()));
        }
        commandBuilder.set("#CreatePackPage #TargetDirDropdown.Entries", entries);
        commandBuilder.set("#CreatePackPage #TargetDirDropdown.Value", this.getDefaultTargetDirectory().toString());
    }

    private void populateDirectoryFilterDropdown(@Nonnull UICommandBuilder commandBuilder) {
        List<ModsDirectory> dirs = this.collectModsDirectories();
        if (dirs.size() <= 1) {
            commandBuilder.set("#PackBrowserPage #DirectoryFilter.Visible", false);
            return;
        }
        commandBuilder.set("#PackBrowserPage #DirectoryFilter.Visible", true);
        ObjectArrayList<DropdownEntryInfo> entries = new ObjectArrayList<DropdownEntryInfo>();
        entries.add(new DropdownEntryInfo(LocalizableString.fromMessageId("server.customUI.assetPackBrowser.filter.all"), DIRECTORY_FILTER_ALL));
        for (ModsDirectory dir : dirs) {
            entries.add(new DropdownEntryInfo(LocalizableString.fromMessageId(dir.filterLangKey()), dir.path().toString()));
        }
        commandBuilder.set("#PackBrowserPage #DirectoryFilter.Entries", entries);
        commandBuilder.set("#PackBrowserPage #DirectoryFilter.Value", this.selectedDirectoryFilter);
    }

    private boolean packBelongsToDirectory(@Nonnull AssetPack pack, @Nonnull Path directory) {
        Path packLocation = pack.getPackLocation().toAbsolutePath().normalize();
        Path dirNormalized = directory.toAbsolutePath().normalize();
        return packLocation.startsWith(dirNormalized);
    }

    public void setSelectedPackKey(@Nullable String key) {
        if (key == null) {
            this.selectedPackKey = null;
            return;
        }
        AssetPack pack = AssetModule.get().getAssetPack(key);
        if (pack != null && !pack.isImmutable() && AssetModule.get().validatePackExistsOnDisk(pack)) {
            this.selectedPackKey = key;
        }
    }

    public void buildUI(@Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder) {
        this.buildSearchInput(commandBuilder, eventBuilder);
        this.buildPackList(commandBuilder, eventBuilder);
    }

    public void buildEventBindings(@Nonnull UIEventBuilder eventBuilder, @Nonnull String browseButtonSelector) {
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, browseButtonSelector, new EventData().append("Action", ACTION_OPEN_PACK_BROWSER));
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#PackBrowserPage #SelectButton", new EventData().append("Action", ACTION_CONFIRM_PACK_BROWSER));
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#PackBrowserPage #CancelButton", new EventData().append("Action", ACTION_CANCEL_PACK_BROWSER));
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#PackBrowserPage #CreateNewPackButton", new EventData().append("Action", ACTION_OPEN_CREATE_PACK));
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CreatePackPage #CreateButton", new EventData().append("Action", ACTION_CREATE_PACK).append("@CreateName", "#CreatePackPage #PackName #Input.Value").append("@CreateGroup", "#CreatePackPage #PackGroup #Input.Value").append("@CreateDescription", "#CreatePackPage #PackDescription #Input.Value").append("@CreateVersion", "#CreatePackPage #PackVersion #Input.Value").append("@CreateWebsite", "#CreatePackPage #PackWebsite #Input.Value").append("@CreateAuthorName", "#CreatePackPage #PackAuthorName #Input.Value").append("@CreateTargetDir", "#CreatePackPage #TargetDirDropdown.Value"));
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CreatePackPage #CancelCreateButton", new EventData().append("Action", ACTION_CANCEL_CREATE_PACK));
        eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#CreatePackPage #PackName #Input", new EventData().append("ValidateCreate", "1").append("@CreateName", "#CreatePackPage #PackName #Input.Value").append("@CreateGroup", "#CreatePackPage #PackGroup #Input.Value"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#CreatePackPage #PackGroup #Input", new EventData().append("ValidateCreate", "1").append("@CreateName", "#CreatePackPage #PackName #Input.Value").append("@CreateGroup", "#CreatePackPage #PackGroup #Input.Value"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#PackBrowserPage #DirectoryFilter", new EventData().append("@DirectoryFilter", "#PackBrowserPage #DirectoryFilter.Value"), false);
    }

    private void buildSearchInput(@Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder) {
        if (this.config.searchInputId() == null) {
            return;
        }
        if (!this.searchQuery.isEmpty()) {
            commandBuilder.set(this.config.searchInputId() + ".Value", this.searchQuery);
        }
        eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, this.config.searchInputId(), EventData.of("@PackSearch", this.config.searchInputId() + ".Value"), false);
    }

    public void buildPackList(@Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder) {
        commandBuilder.clear(this.config.listElementId());
        List<PackEntry> entries = this.collectPackEntries();
        int buttonIndex = 0;
        for (PackEntry entry : entries) {
            commandBuilder.append(this.config.listElementId(), "Pages/BasicTextButton.ui");
            String selector = this.config.listElementId() + "[" + buttonIndex + "]";
            if (entry.immutable) {
                commandBuilder.set(selector + ".TextSpans", Message.raw(entry.displayName + " ").insert(Message.translation("server.customUI.assetPackBrowser.readOnly")));
                commandBuilder.set(selector + ".Disabled", true);
                commandBuilder.set(selector + ".Style.Default.LabelStyle.TextColor", "#6e7da1");
                commandBuilder.set(selector + ".TextTooltipStyle", TOOLTIP_STYLE);
                commandBuilder.set(selector + ".TooltipTextSpans", Message.translation("server.customUI.assetPackBrowser.readOnlyTooltip"));
            } else {
                commandBuilder.set(selector + ".Text", entry.displayName);
            }
            if (entry.key.equals(this.selectedPackKey)) {
                commandBuilder.set(selector + ".Style", BUTTON_SELECTED);
            }
            if (!entry.immutable) {
                eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, selector, EventData.of("Pack", entry.key));
            }
            ++buttonIndex;
        }
    }

    public boolean handleEvent(@Nonnull AssetPackSaveBrowserEventData data) {
        if (data.getSearch() != null) {
            this.searchQuery = data.getSearch().trim().toLowerCase();
            return true;
        }
        if (data.getPack() != null) {
            this.selectedPackKey = data.getPack();
            return true;
        }
        if (data.getDirectoryFilter() != null) {
            this.selectedDirectoryFilter = data.getDirectoryFilter();
            return true;
        }
        return data.getValidateCreate() != null;
    }

    @Nullable
    public ActionResult handleAction(@Nullable String actionName, @Nonnull AssetPackSaveBrowserEventData data, @Nonnull String selectedPackLabelSelector) {
        if (actionName == null) {
            if (data.getValidateCreate() != null) {
                UICommandBuilder commandBuilder = new UICommandBuilder();
                this.buildCreateFormValidation(commandBuilder, data.getCreateName(), data.getCreateGroup());
                return new ActionResult(commandBuilder, null, null, false);
            }
            if (this.handleEvent(data)) {
                UICommandBuilder commandBuilder = new UICommandBuilder();
                UIEventBuilder eventBuilder = new UIEventBuilder();
                this.buildPackList(commandBuilder, eventBuilder);
                return new ActionResult(commandBuilder, eventBuilder, null, false);
            }
            return null;
        }
        return switch (actionName) {
            case ACTION_OPEN_PACK_BROWSER -> {
                UICommandBuilder commandBuilder = new UICommandBuilder();
                UIEventBuilder eventBuilder = new UIEventBuilder();
                commandBuilder.set("#PackBrowserPage.Visible", true);
                this.populateDirectoryFilterDropdown(commandBuilder);
                this.buildPackList(commandBuilder, eventBuilder);
                yield new ActionResult(commandBuilder, eventBuilder, null, false);
            }
            case ACTION_CONFIRM_PACK_BROWSER -> {
                UICommandBuilder commandBuilder = new UICommandBuilder();
                commandBuilder.set("#PackBrowserPage.Visible", false);
                if (this.hasSelectedPack()) {
                    commandBuilder.set(selectedPackLabelSelector + ".Text", this.getSelectedPackDisplayName());
                }
                yield new ActionResult(commandBuilder, null, null, this.hasSelectedPack());
            }
            case ACTION_CANCEL_PACK_BROWSER -> {
                UICommandBuilder commandBuilder = new UICommandBuilder();
                commandBuilder.set("#PackBrowserPage.Visible", false);
                yield new ActionResult(commandBuilder, null, null, false);
            }
            case ACTION_OPEN_CREATE_PACK -> {
                UICommandBuilder commandBuilder = new UICommandBuilder();
                commandBuilder.set("#PackBrowserPage.Visible", false);
                commandBuilder.set("#CreatePackPage.Visible", true);
                commandBuilder.set("#CreatePackPage #PackName #Input.Value", DIRECTORY_FILTER_ALL);
                commandBuilder.set("#CreatePackPage #PackGroup #Input.Value", DIRECTORY_FILTER_ALL);
                commandBuilder.set("#CreatePackPage #PackDescription #Input.Value", DIRECTORY_FILTER_ALL);
                commandBuilder.set("#CreatePackPage #PackVersion #Input.Value", DIRECTORY_FILTER_ALL);
                commandBuilder.set("#CreatePackPage #PackWebsite #Input.Value", DIRECTORY_FILTER_ALL);
                commandBuilder.set("#CreatePackPage #PackAuthorName #Input.Value", DIRECTORY_FILTER_ALL);
                commandBuilder.set("#CreatePackPage #CreateErrorText.Visible", false);
                commandBuilder.set("#CreatePackPage #CreateButton.Disabled", false);
                this.populateTargetDirectoryDropdown(commandBuilder);
                yield new ActionResult(commandBuilder, null, null, false);
            }
            case ACTION_CREATE_PACK -> {
                CreatePackResult result = this.createPack(data);
                UICommandBuilder commandBuilder = new UICommandBuilder();
                UIEventBuilder eventBuilder = null;
                String errorKey = null;
                if (result.success()) {
                    eventBuilder = new UIEventBuilder();
                    commandBuilder.set("#CreatePackPage.Visible", false);
                    commandBuilder.set("#PackBrowserPage.Visible", true);
                    this.buildPackList(commandBuilder, eventBuilder);
                } else {
                    errorKey = result.errorKey();
                }
                yield new ActionResult(commandBuilder, eventBuilder, errorKey, false);
            }
            case ACTION_CANCEL_CREATE_PACK -> {
                UICommandBuilder commandBuilder = new UICommandBuilder();
                commandBuilder.set("#CreatePackPage.Visible", false);
                commandBuilder.set("#PackBrowserPage.Visible", true);
                yield new ActionResult(commandBuilder, null, null, false);
            }
            case ACTION_PACK_SEARCH, ACTION_PACK_SELECT -> {
                this.handleEvent(data);
                UICommandBuilder commandBuilder = new UICommandBuilder();
                UIEventBuilder eventBuilder = new UIEventBuilder();
                this.buildPackList(commandBuilder, eventBuilder);
                yield new ActionResult(commandBuilder, eventBuilder, null, false);
            }
            default -> null;
        };
    }

    @Nullable
    public AssetPack getSelectedPack() {
        if (this.selectedPackKey == null) {
            return null;
        }
        return AssetModule.get().getAssetPack(this.selectedPackKey);
    }

    public boolean hasSelectedPack() {
        return this.selectedPackKey != null && AssetModule.get().getAssetPack(this.selectedPackKey) != null;
    }

    @Nonnull
    public String getSelectedPackDisplayName() {
        if (this.selectedPackKey == null) {
            return DIRECTORY_FILTER_ALL;
        }
        AssetPack pack = AssetModule.get().getAssetPack(this.selectedPackKey);
        if (pack == null) {
            return this.selectedPackKey;
        }
        String displayName = AssetPackSaveBrowser.getPackDisplayName(pack);
        for (AssetPack other : AssetModule.get().getAssetPacks()) {
            if (other == pack || !displayName.equalsIgnoreCase(AssetPackSaveBrowser.getPackDisplayName(other))) continue;
            return pack.getName();
        }
        return displayName;
    }

    @Nonnull
    public CreatePackResult createPack(@Nonnull AssetPackSaveBrowserEventData data) {
        String name = data.getCreateName();
        String group = data.getCreateGroup();
        if (name == null || name.isBlank()) {
            return new CreatePackResult(false, "server.customUI.assetPackBrowser.create.nameRequired");
        }
        if (group == null || group.isBlank()) {
            return new CreatePackResult(false, "server.customUI.assetPackBrowser.create.groupRequired");
        }
        PluginManifest manifest = new PluginManifest();
        manifest.setName(name.trim());
        manifest.setGroup(group.trim());
        if (data.getCreateDescription() != null && !data.getCreateDescription().isBlank()) {
            manifest.setDescription(data.getCreateDescription().trim());
        }
        if (data.getCreateVersion() != null && !data.getCreateVersion().isBlank()) {
            try {
                manifest.setVersion(Semver.fromString(data.getCreateVersion().trim()));
            }
            catch (IllegalArgumentException illegalArgumentException) {
                // empty catch block
            }
        }
        if (data.getCreateWebsite() != null && !data.getCreateWebsite().isBlank()) {
            manifest.setWebsite(data.getCreateWebsite().trim());
        }
        if (data.getCreateAuthorName() != null && !data.getCreateAuthorName().isBlank()) {
            AuthorInfo author = new AuthorInfo();
            author.setName(data.getCreateAuthorName().trim());
            manifest.setAuthors(List.of(author));
        }
        String packId = new PluginIdentifier(manifest).toString();
        if (AssetModule.get().getAssetPack(packId) != null) {
            return new CreatePackResult(false, "server.customUI.assetPackBrowser.create.alreadyExists");
        }
        String dirName = INVALID_FILENAME_CHARS.matcher(group.trim() + "." + name.trim()).replaceAll(DIRECTORY_FILTER_ALL);
        Path normalized = Path.of(dirName, new String[0]).normalize();
        if (dirName.isEmpty() || normalized.toString().isEmpty()) {
            return new CreatePackResult(false, "server.customUI.assetPackBrowser.create.nameRequired");
        }
        Path modsPath = this.resolveTargetDirectory(data.getCreateTargetDir());
        if (modsPath == null) {
            return new CreatePackResult(false, "server.customUI.assetPackBrowser.create.failed");
        }
        Path packPath = modsPath.resolve(normalized).normalize();
        if (!packPath.startsWith(modsPath)) {
            return new CreatePackResult(false, "server.customUI.assetPackBrowser.create.failed");
        }
        if (Files.exists(packPath, new LinkOption[0])) {
            return new CreatePackResult(false, "server.customUI.assetPackBrowser.create.alreadyExists");
        }
        try {
            Files.createDirectories(packPath, new FileAttribute[0]);
            Path manifestPath = packPath.resolve("manifest.json");
            BsonUtil.writeSync(manifestPath, PluginManifest.CODEC, manifest, LOGGER);
            String fPackId = packId;
            Path fPackPath = packPath;
            HytaleServer.SCHEDULED_EXECUTOR.execute(() -> {
                HytaleServerConfig serverConfig = HytaleServer.get().getConfig();
                HytaleServerConfig.setBoot(serverConfig, new PluginIdentifier(manifest), true);
                serverConfig.markChanged();
                if (serverConfig.consumeHasChanged()) {
                    HytaleServerConfig.save(serverConfig).join();
                }
                AssetModule.get().registerPack(fPackId, fPackPath, manifest, false);
                ((HytaleLogger.Api)LOGGER.atInfo()).log("Created new asset pack: %s at %s", (Object)fPackId, (Object)fPackPath);
            });
            this.selectedPackKey = packId;
            this.pendingPack = new PendingPack(packId, name.trim());
            return new CreatePackResult(true, null);
        }
        catch (IOException e) {
            ((HytaleLogger.Api)((HytaleLogger.Api)LOGGER.atSevere()).withCause(e)).log("Failed to create asset pack %s", packId);
            return new CreatePackResult(false, "server.customUI.assetPackBrowser.create.failed");
        }
    }

    public boolean checkDuplicatePack(@Nullable String name, @Nullable String group) {
        if (name == null || name.isBlank() || group == null || group.isBlank()) {
            return false;
        }
        String packId = group.trim() + ":" + name.trim();
        if (AssetModule.get().getAssetPack(packId) != null) {
            return true;
        }
        String dirName = INVALID_FILENAME_CHARS.matcher(group.trim() + "." + name.trim()).replaceAll(DIRECTORY_FILTER_ALL);
        if (dirName.isEmpty()) {
            return false;
        }
        Path normalized = Path.of(dirName, new String[0]).normalize();
        for (ModsDirectory dir : this.collectModsDirectories()) {
            if (!Files.exists(dir.path().resolve(normalized).normalize(), new LinkOption[0])) continue;
            return true;
        }
        return false;
    }

    public void buildCreateFormValidation(@Nonnull UICommandBuilder commandBuilder, @Nullable String name, @Nullable String group) {
        boolean duplicate = this.checkDuplicatePack(name, group);
        commandBuilder.set("#CreatePackPage #CreateErrorText.Visible", duplicate);
        if (duplicate) {
            commandBuilder.set("#CreatePackPage #CreateErrorText.Text", Message.translation("server.customUI.assetPackBrowser.create.alreadyExists"));
        }
        commandBuilder.set("#CreatePackPage #CreateButton.Disabled", duplicate);
    }

    private List<PackEntry> collectPackEntries() {
        ObjectArrayList<PackEntry> mutable = new ObjectArrayList<PackEntry>();
        ObjectArrayList<PackEntry> immutable = new ObjectArrayList<PackEntry>();
        boolean pendingFound = false;
        for (AssetPack pack : AssetModule.get().getAssetPacks()) {
            Path filterPath;
            int score;
            if (!AssetModule.get().validatePackExistsOnDisk(pack)) continue;
            String displayName = AssetPackSaveBrowser.getPackDisplayName(pack);
            boolean isImmutable = pack.isImmutable();
            if (this.pendingPack != null && pack.getName().equals(this.pendingPack.key)) {
                pendingFound = true;
            }
            if (!this.searchQuery.isEmpty() && (score = StringCompareUtil.getFuzzyDistance(displayName.toLowerCase(), this.searchQuery, Locale.ENGLISH)) <= 0 || !this.selectedDirectoryFilter.isEmpty() && !this.packBelongsToDirectory(pack, filterPath = Path.of(this.selectedDirectoryFilter, new String[0]))) continue;
            PackEntry entry = new PackEntry(pack.getName(), displayName, isImmutable);
            if (isImmutable) {
                immutable.add(entry);
                continue;
            }
            mutable.add(entry);
        }
        if (pendingFound) {
            this.pendingPack = null;
        } else if (this.pendingPack != null) {
            String displayName = this.pendingPack.displayName;
            if (this.searchQuery.isEmpty() || StringCompareUtil.getFuzzyDistance(displayName.toLowerCase(), this.searchQuery, Locale.ENGLISH) > 0) {
                mutable.add(new PackEntry(this.pendingPack.key, displayName, false));
            }
        }
        AssetPackSaveBrowser.disambiguateDisplayNames(mutable);
        AssetPackSaveBrowser.disambiguateDisplayNames(immutable);
        String basePackName = AssetModule.get().getBaseAssetPack().getName();
        Comparator<PackEntry> packOrder = Comparator.comparing(e -> !e.key.equals(basePackName)).thenComparing(e -> e.displayName.toLowerCase());
        mutable.sort(packOrder);
        immutable.sort(packOrder);
        ObjectArrayList<PackEntry> result = new ObjectArrayList<PackEntry>(mutable.size() + immutable.size());
        result.addAll(mutable);
        result.addAll(immutable);
        return result;
    }

    private static void disambiguateDisplayNames(@Nonnull List<PackEntry> entries) {
        HashMap<String, Integer> nameCounts = new HashMap<String, Integer>();
        for (PackEntry entry : entries) {
            nameCounts.merge(entry.displayName.toLowerCase(), 1, Integer::sum);
        }
        for (int i = 0; i < entries.size(); ++i) {
            PackEntry entry;
            entry = entries.get(i);
            if (nameCounts.getOrDefault(entry.displayName.toLowerCase(), 0) <= 1) continue;
            entries.set(i, new PackEntry(entry.key, entry.key, entry.immutable));
        }
    }

    @Nonnull
    private static String getPackDisplayName(@Nonnull AssetPack pack) {
        if (pack.equals(AssetModule.get().getBaseAssetPack())) {
            return "HytaleAssets";
        }
        PluginManifest manifest = pack.getManifest();
        return manifest != null ? manifest.getName() : pack.getName();
    }

    public record ModsDirectory(@Nonnull String langKey, @Nonnull String filterLangKey, @Nonnull Path path) {
    }

    private record PackEntry(@Nonnull String key, @Nonnull String displayName, boolean immutable) {
    }

    public record ActionResult(@Nonnull UICommandBuilder commandBuilder, @Nullable UIEventBuilder eventBuilder, @Nullable String errorKey, boolean packConfirmed) {
    }

    public record CreatePackResult(boolean success, @Nullable String errorKey) {
    }

    private record PendingPack(@Nonnull String key, @Nonnull String displayName) {
    }
}

