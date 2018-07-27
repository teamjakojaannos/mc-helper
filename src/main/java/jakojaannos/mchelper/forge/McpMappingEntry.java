package jakojaannos.mchelper.forge;

public class McpMappingEntry {
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

    public McpMappingEntry(String version, String mcVersion) {
        this.version = version;
        this.mcVersion = mcVersion;
    }
}
