package jakojaannos.mchelper.facet;

import com.intellij.facet.ui.FacetEditorTab;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

class MinecraftForgeFacetEditorTab extends FacetEditorTab {
    private final MinecraftForgeFacetConfiguration configuration;

    private JPanel panel;

    MinecraftForgeFacetEditorTab(MinecraftForgeFacetConfiguration configuration) {
        this.configuration = configuration;
    }

    @NotNull
    @Override
    public JComponent createComponent() {
        // TODO: Allow changing forge version/settings
        panel = new JPanel();
        return panel;
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "MinecraftForge Module Settings";
    }
}
