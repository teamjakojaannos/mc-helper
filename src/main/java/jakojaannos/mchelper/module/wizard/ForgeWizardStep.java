package jakojaannos.mchelper.module.wizard;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ui.HideableDecorator;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.util.ui.JBUI;
import jakojaannos.mchelper.forge.ForgeVersionEntry;
import jakojaannos.mchelper.forge.McpMappingEntry;
import jakojaannos.mchelper.forge.MinecraftVersionEntry;
import jakojaannos.mchelper.module.ForgeModuleBuilder;
import lombok.NonNull;
import lombok.val;

import javax.swing.*;

public class ForgeWizardStep extends ModuleWizardStep {
    private final ForgeModuleBuilder builder;

    private JPanel root;
    private JComboBox<ForgeVersionEntry> forgeVersion;
    private JComboBox<MinecraftVersionEntry> minecraftVersion;
    private JPanel advancedPlaceholder;
    private JPanel advancedPanel;
    private JComboBox<McpMappingEntry> mcpMappings;
    private JCheckBox useDependencyATs;
    private JComboBox mappingsMcVersion;

    public ForgeWizardStep(final ForgeModuleBuilder builder) {
        this.builder = builder;

        setupAdvancedPanel();
        setupComboBoxContents();
    }

    /**
     * Initializes version combo boxes. Gets lists of available versions and populates the combo boxes accordingly.
     */
    private void setupComboBoxContents() {
        // TODO: Poll mappings from internet
        MinecraftVersionEntry mcVersion = new MinecraftVersionEntry("1.12.2");
        McpMappingEntry mcpMappings = new McpMappingEntry("snapshot_20171003", "1.12");
        mcVersion.addForgeVersions(
                new ForgeVersionEntry("14.23.4.2739", mcpMappings, true, false)
        );

        minecraftVersion.addItem(mcVersion);
        mcVersion.getForgeVersionEntries().forEach(forgeVersion::addItem);
        this.mcpMappings.addItem(mcpMappings);
    }

    private void setupAdvancedPanel() {
        HideableDecorator moreSettingsDecorator = new HideableDecorator(advancedPlaceholder, "Advanced Options", false);
        advancedPanel.setBorder(JBUI.Borders.empty(0, IdeBorderFactory.TITLED_BORDER_INDENT, 5, 0));
        moreSettingsDecorator.setContentComponent(advancedPanel);
    }

    @Override
    public JComponent getComponent() {
        return root;
    }

    @Override
    public void updateDataModel() {
        builder.setForgeSettings(new ForgeModuleBuilder.ForgeSettings(
                getForgeVersion(),
                getMinecraftVersion(),
                getMcpMappings(),
                getUseDependencyAts()
        ));
    }

    @NonNull
    private ForgeVersionEntry getForgeVersion() {
        val entry = (ForgeVersionEntry) forgeVersion.getSelectedItem();
        if (entry == null) {
            return new ForgeVersionEntry();
        }

        return entry;
    }

    @NonNull
    private MinecraftVersionEntry getMinecraftVersion() {
        val entry = (MinecraftVersionEntry) minecraftVersion.getSelectedItem();
        if (entry == null) {
            return new MinecraftVersionEntry();
        }

        return entry;
    }

    @NonNull
    private McpMappingEntry getMcpMappings() {
        val entry = (McpMappingEntry) mcpMappings.getSelectedItem();
        if (entry == null) {
            return new McpMappingEntry();
        }

        return entry;
    }

    @NonNull
    private String getMappingsMcVersion() {
        val entry = (String) mappingsMcVersion.getSelectedItem();
        if (entry == null) {
            return McpMappingEntry.DEFAULT_MC_VERSION;
        }

        return entry;
    }

    private boolean getUseDependencyAts() {
        return useDependencyATs.isSelected();
    }
}
