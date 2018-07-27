package jakojaannos.mchelper.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetTypeId;
import com.intellij.facet.FacetTypeRegistry;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class MinecraftForgeFacet extends Facet<MinecraftForgeFacetConfiguration> {
    static final FacetTypeId<MinecraftForgeFacet> ID = new FacetTypeId<>(MinecraftForgeFacetType.TYPE_ID);

    MinecraftForgeFacet(@NotNull Module module,
                        String name,
                        @NotNull MinecraftForgeFacetConfiguration configuration,
                        @Nullable Facet underlyingFacet) {
        super(FacetTypeRegistry.getInstance().findFacetType(ID), module, name, configuration, underlyingFacet);
    }
}
