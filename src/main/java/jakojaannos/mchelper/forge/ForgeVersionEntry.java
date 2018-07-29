package jakojaannos.mchelper.forge;

import java.util.Objects;

public class ForgeVersionEntry {
    public static final String DEFAULT_VERSION = "14.23.4.2744";

    private final String version;
    private final McpMappingEntry defaultMappings;
    private final boolean latest;
    private final boolean recommended;

    public String getVersion() {
        return version;
    }

    public McpMappingEntry getDefaultMappings() {
        return defaultMappings;
    }

    public boolean isLatest() {
        return latest;
    }

    public boolean isRecommended() {
        return recommended;
    }

    public ForgeVersionEntry() {
        this.version = DEFAULT_VERSION;
        this.defaultMappings = new McpMappingEntry();
        this.latest = false;
        this.recommended = false;
    }

    public ForgeVersionEntry(String version, McpMappingEntry defaultMappings, boolean latest, boolean recommended) {
        this.version = version;
        this.defaultMappings = defaultMappings;
        this.latest = latest;
        this.recommended = recommended;
    }

    @Override
    public String toString() {
        if (!latest && !recommended) {
            return version;
        } else if (latest && recommended) {
            return version + " (Latest/Recommended)";
        } else {
            return version + " (" + (latest ? "Latest" : "Recommended") + ")";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ForgeVersionEntry that = (ForgeVersionEntry) o;
        return Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {

        return Objects.hash(version);
    }
}
