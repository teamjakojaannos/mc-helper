package jakojaannos.mchelper.forge;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MinecraftVersionEntry {
    @NotNull
    private final String version;
    @NotNull
    private final String mappingsVersion;
    @NotNull
    private Set<ForgeVersionEntry> forgeVersionEntries = new HashSet<>();

    public String getVersion() {
        return version;
    }

    public MinecraftVersionEntry(@NotNull String version, @NotNull String mappingsVersion) {
        this.version = version;
        this.mappingsVersion = mappingsVersion;
    }

    public void addForgeVersions(@NotNull ForgeVersionEntry... forgeVersionEntries) {
        this.forgeVersionEntries.addAll(Arrays.asList(forgeVersionEntries));
    }

    @NotNull
    public Set<ForgeVersionEntry> getForgeVersionEntries() {
        return new HashSet<>(forgeVersionEntries);
    }

    @Override
    public String toString() {
        return version;
    }
}
