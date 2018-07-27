package jakojaannos.mchelper.module.wizard;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ui.HideableDecorator;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.util.ui.JBUI;
import jakojaannos.mchelper.forge.ForgeVersionEntry;
import jakojaannos.mchelper.forge.McpMappingEntry;
import jakojaannos.mchelper.forge.MinecraftVersionEntry;
import jakojaannos.mchelper.module.ForgeModuleBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ForgeWizardStep extends ModuleWizardStep {
    private final ForgeModuleBuilder.Config config;

    private JPanel root;
    private JComboBox<ForgeVersionEntry> forgeVersion;
    private JComboBox<MinecraftVersionEntry> minecraftVersion;
    private JPanel advancedPlaceholder;
    private JPanel advancedPanel;
    private JComboBox<McpMappingEntry> mcpMappings;
    private JCheckBox useDependencyATs;

    public ForgeWizardStep(final ForgeModuleBuilder.Config config) {
        this.config = config;

        HideableDecorator moreSettingsDecorator = new HideableDecorator(advancedPlaceholder, "Advanced Options", false);
        advancedPanel.setBorder(JBUI.Borders.empty(0, IdeBorderFactory.TITLED_BORDER_INDENT, 5, 0));
        moreSettingsDecorator.setContentComponent(advancedPanel);

        MinecraftVersionEntry mcVersion = new MinecraftVersionEntry("1.12.2", "1.12");
        mcVersion.addForgeVersions(
                new ForgeVersionEntry("14.23.4.2739", "snapshot_20171003", true, false)
        );

        minecraftVersion.addItem(mcVersion);
        mcVersion.getForgeVersionEntries().forEach(forgeVersion::addItem);
        mcpMappings.addItem(new McpMappingEntry("snapshot_20171003", "1.12"));
    }

    @Override
    public JComponent getComponent() {
        return root;
    }

    @Override
    public void updateDataModel() {
        config.getForgeConfig().setForgeVersion(getForgeVersion());
        config.getForgeConfig().setMinecraftVersion(getMinecraftVersion());
        config.getForgeConfig().setMcpMappings(getMcpMappings());
        config.getForgeConfig().setUseDependencyAts(getUseDependencyAts());
    }

    @NotNull
    private ForgeVersionEntry getForgeVersion() {
        ForgeVersionEntry entry = (ForgeVersionEntry) forgeVersion.getSelectedItem();
        if (entry == null) {
            throw new IllegalStateException("Selected Forge version entry cannot be null!");
        }

        return entry;
    }

    @NotNull
    private MinecraftVersionEntry getMinecraftVersion() {
        MinecraftVersionEntry entry = (MinecraftVersionEntry) minecraftVersion.getSelectedItem();
        if (entry == null) {
            throw new IllegalStateException("Selected Minecraft version entry cannot be null!");
        }

        return entry;
    }

    @NotNull
    private McpMappingEntry getMcpMappings() {
        McpMappingEntry entry = (McpMappingEntry) mcpMappings.getSelectedItem();
        if (entry == null) {
            throw new IllegalStateException("Selected MCP mapping version cannot be null!");
        }

        return entry;
    }

    private boolean getUseDependencyAts() {
        return useDependencyATs.isSelected();
    }
}
