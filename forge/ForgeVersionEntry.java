package jakojaannos.mchelper.forge;

import java.util.Objects;

public class ForgeVersionEntry {
    private final String version;
    private final String defaultMappings;
    private final boolean latest;
    private final boolean recommended;

    public String getVersion() {
        return version;
    }

    public boolean isLatest() {
        return latest;
    }

    public boolean isRecommended() {
        return recommended;
    }

    public ForgeVersionEntry(String version, String defaultMappings, boolean latest, boolean recommended) {
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
            return version + " (Latest/Recommended)" ;
        } else {
            return version + " (" + (latest ? "Latest" : "Recommended" ) + ")" ;
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
