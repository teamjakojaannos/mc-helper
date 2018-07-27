package jakojaannos.mchelper.module;

import com.intellij.ide.util.projectWizard.JavaModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import jakojaannos.mchelper.forge.ForgeVersionEntry;
import jakojaannos.mchelper.forge.McpMappingEntry;
import jakojaannos.mchelper.forge.MinecraftVersionEntry;
import jakojaannos.mchelper.module.wizard.ForgeWizardStep;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ForgeModuleBuilder extends JavaModuleBuilder {
    private final Config config = new Config();

    @Override
    public String getPresentableName() {
        return super.getPresentableName();
    }

    @Nullable
    @Override
    public String getBuilderId() {
        return "MINECRAFTFORGE_MODULE";
    }

    @Override
    public ModuleType getModuleType() {
        return ForgeModuleType.getInstance();
    }

    @Nullable
    @Override
    public ModuleWizardStep getCustomOptionsStep(WizardContext context, Disposable parentDisposable) {
        return new ForgeWizardStep(config);
    }

    @Override
    public ModuleWizardStep[] createWizardSteps(@NotNull WizardContext wizardContext, @NotNull ModulesProvider modulesProvider) {
        return new ModuleWizardStep[]{
        };
    }

    public static class Config {
        private final ForgeConfig forgeConfig = new ForgeConfig();

        public ForgeConfig getForgeConfig() {
            return forgeConfig;
        }

        public static class ForgeConfig {
            @NotNull
            private ForgeVersionEntry forgeVersion = new ForgeVersionEntry("ERROR", "ERROR", false, false);
            @NotNull
            private MinecraftVersionEntry minecraftVersion = new MinecraftVersionEntry("1.12.2", "snapshot_20171003");
            @NotNull
            private McpMappingEntry mcpMappings = new McpMappingEntry("snapshot_20171003", "1.12");
            private boolean useDependencyAts;

            public void setForgeVersion(@NotNull ForgeVersionEntry forgeVersion) {
                this.forgeVersion = forgeVersion;
            }

            @NotNull
            public ForgeVersionEntry getForgeVersion() {
                return forgeVersion;
            }

            public void setMinecraftVersion(@NotNull MinecraftVersionEntry minecraftVersion) {
                this.minecraftVersion = minecraftVersion;
            }

            @NotNull
            public MinecraftVersionEntry getMinecraftVersion() {
                return minecraftVersion;
            }

            public void setMcpMappings(@NotNull McpMappingEntry mcpMappings) {
                this.mcpMappings = mcpMappings;
            }

            @NotNull
            public McpMappingEntry getMcpMappings() {
                return mcpMappings;
            }

            public void setUseDependencyAts(boolean useDependencyAts) {
                this.useDependencyAts = useDependencyAts;
            }

            public boolean getUseDependencyAts() {
                return useDependencyAts;
            }
        }
    }
}
