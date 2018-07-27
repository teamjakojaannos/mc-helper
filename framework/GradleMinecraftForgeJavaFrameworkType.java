package jakojaannos.mchelper.framework;

import com.intellij.framework.FrameworkTypeEx;
import com.intellij.framework.addSupport.FrameworkSupportInModuleProvider;
import jakojaannos.mchelper.util.McHelperIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.gradle.frameworkSupport.GradleJavaFrameworkSupportProvider;

import javax.swing.*;

public class GradleMinecraftForgeJavaFrameworkType extends FrameworkTypeEx {
    public static final String ID = "minecraftforge";
    private final FrameworkSupportInModuleProvider provider;

    GradleMinecraftForgeJavaFrameworkType(FrameworkSupportInModuleProvider provider) {
        super(ID);
        this.provider = provider;
    }

    @NotNull
    @Override
    public FrameworkSupportInModuleProvider createProvider() {
        return provider;
    }

    @NotNull
    @Override
    public String getPresentableName() {
        return "Minecraft Forge";
    }

    @NotNull
    @Override
    public Icon getIcon() {
        return McHelperIcons.FORGE;
    }

    @Override
    public String getUnderlyingFrameworkTypeId() {
        return GradleJavaFrameworkSupportProvider.ID;
    }
}
