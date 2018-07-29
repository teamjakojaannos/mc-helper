package jakojaannos.mchelper.forge;

public class McpMappingEntry {
    // TODO: Get these from build script or sth.
    public static final String DEFAULT_MAPPINGS = "snapshot_20171003";
    public static final String DEFAULT_MC_VERSION = "1.12";

    private final String version;
    private final String mcVersion;

    public String getVersion() {
        return version;
    }

    public String getMcVersion() {
        return mcVersion;
    }

    public boolean isSupported(MinecraftVersionEntry mcVersion) {
        return this.mcVersion.equals(mcVersion.getVersion());
    }

    public boolean isMaybeSupported(MinecraftVersionEntry mcVersion) {
        return mcVersion.getVersion().startsWith(this.mcVersion);
    }

    public McpMappingEntry() {
        this.version = DEFAULT_MAPPINGS;
        this.mcVersion = DEFAULT_MC_VERSION;
    }

    public McpMappingEntry(String version, String mcVersion) {
        this.version = version;
        this.mcVersion = mcVersion;
    }

    @Override
    public String toString() {
        return version + " (" + mcVersion + ")";
    }
}
