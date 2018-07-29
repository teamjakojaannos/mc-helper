package jakojaannos.mchelper.forge;

import lombok.NonNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MinecraftVersionEntry {
    public static final String DEFAULT_VERSION = "1.12.2";

    @NonNull
    private final String version;
    @NonNull
    private Set<ForgeVersionEntry> forgeVersionEntries = new HashSet<>();

    public MinecraftVersionEntry() {
        this.version = DEFAULT_VERSION;
    }

    public String getVersion() {
        return version;
    }

    public MinecraftVersionEntry(@NonNull String version) {
        this.version = version;
    }

    public void addForgeVersions(@NonNull ForgeVersionEntry... forgeVersionEntries) {
        this.forgeVersionEntries.addAll(Arrays.asList(forgeVersionEntries));
    }

    @NonNull
    public Set<ForgeVersionEntry> getForgeVersionEntries() {
        return new HashSet<>(forgeVersionEntries);
    }

    @Override
    public String toString() {
        return version;
    }
}
