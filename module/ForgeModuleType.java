package jakojaannos.mchelper.module;

import com.intellij.ide.util.projectWizard.JavaModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.module.ModuleTypeManager;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import jakojaannos.mchelper.util.McHelperIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ForgeModuleType extends JavaModuleType {
    private static final String ID = "MINECRAFTFORGE_MODULE";

    private static final String NAME = "Minecraft Forge Mod Module";
    private static final String DESCRIPTION = "Module for Minecraft Forge mod development.";


    static ForgeModuleType getInstance() {
        return (ForgeModuleType) ModuleTypeManager.getInstance().findByID(ID);
    }

    @NotNull
    @Override
    public String getName() {
        return NAME;
    }

    @NotNull
    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public Icon getIcon() {
        return McHelperIcons.FORGE;
    }

    @Override
    public Icon getNodeIcon(boolean isOpened) {
        return McHelperIcons.MINECRAFT;
    }

    @NotNull
    @Override
    public JavaModuleBuilder createModuleBuilder() {
        return new ForgeModuleBuilder();
    }

    @NotNull
    @Override
    public ModuleWizardStep[] createWizardSteps(@NotNull WizardContext wizardContext, @NotNull JavaModuleBuilder moduleBuilder, @NotNull ModulesProvider modulesProvider) {
        return new ModuleWizardStep[]{

        };
    }
}
