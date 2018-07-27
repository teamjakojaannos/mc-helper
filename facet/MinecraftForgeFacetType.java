package jakojaannos.mchelper.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetType;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class MinecraftForgeFacetType extends FacetType<MinecraftForgeFacet, MinecraftForgeFacetConfiguration> {
    static final String TYPE_ID = "MINECRAFT_FORGE";
    private static final String PRESENTABLE_NAME = "Minecraft Forge";

    MinecraftForgeFacetType() {
        super(MinecraftForgeFacet.ID, TYPE_ID, PRESENTABLE_NAME);
    }

    @Override
    public MinecraftForgeFacet createFacet(@NotNull Module module,
                                           String name,
                                           @NotNull MinecraftForgeFacetConfiguration configuration,
                                           @Nullable Facet underlyingFacet) {
        return new MinecraftForgeFacet(module, name, configuration, underlyingFacet);
    }

    @Override
    public MinecraftForgeFacetConfiguration createDefaultConfiguration() {
        return new MinecraftForgeFacetConfiguration();
    }

    @Override
    public boolean isSuitableModuleType(ModuleType moduleType) {
        return true;
    }
}
