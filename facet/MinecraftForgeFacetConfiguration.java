package jakojaannos.mchelper.facet;

import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.openapi.components.PersistentStateComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class MinecraftForgeFacetConfiguration implements FacetConfiguration, PersistentStateComponent<MinecraftForgeFacetConfiguration.MinecraftForgeFacetConfigurationData> {

    private MinecraftForgeFacetConfigurationData state;

    MinecraftForgeFacetConfiguration() {
        this.state = new MinecraftForgeFacetConfigurationData();
    }

    @Override
    public FacetEditorTab[] createEditorTabs(FacetEditorContext editorContext, FacetValidatorsManager validatorsManager) {
        return new FacetEditorTab[]{new MinecraftForgeFacetEditorTab(this)};
    }

    @Nullable
    @Override
    public MinecraftForgeFacetConfigurationData getState() {
        return this.state;
    }

    @Override
    public void loadState(@NotNull MinecraftForgeFacetConfigurationData state) {
        this.state = state;
    }

    static class MinecraftForgeFacetConfigurationData {
        // Nothing here (yet) :c
    }
}
