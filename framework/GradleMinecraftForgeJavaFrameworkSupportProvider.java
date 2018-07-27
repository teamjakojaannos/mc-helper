package jakojaannos.mchelper.framework;

import com.intellij.framework.FrameworkTypeEx;
import com.intellij.openapi.externalSystem.model.project.ProjectId;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModifiableModelsProvider;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.vfs.VirtualFile;
import jakojaannos.mchelper.templates.MinecraftForgeFileTemplateGroupDescriptorFactory;
import jakojaannos.mchelper.util.ApplicationUtil;
import jakojaannos.mchelper.util.TemplateUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.gradle.frameworkSupport.BuildScriptDataBuilder;
import org.jetbrains.plugins.gradle.frameworkSupport.GradleJavaFrameworkSupportProvider;

import javax.swing.*;
import java.io.IOException;
import java.util.Properties;

// TODO: Move away from exposing forge as gradle framework, this just does not work
//  -> Problems with source roots etc.
//  -> Constant struggle to workaround getting build.gradle etc. look good enough
//  -> With separate module type we control everything, just auto-import as gradle project when we are done fucking around

public class GradleMinecraftForgeJavaFrameworkSupportProvider extends GradleJavaFrameworkSupportProvider {
    private final GradleMinecraftForgeJavaFrameworkConfiguration form = new GradleMinecraftForgeJavaFrameworkConfiguration();

    @NotNull
    @Override
    public FrameworkTypeEx getFrameworkType() {
        return new GradleMinecraftForgeJavaFrameworkType(this);
    }

    @Override
    public JComponent createComponent() {
        return form.getRootComponent();
    }


    @Override
    public void addSupport(@NotNull ProjectId projectId, @NotNull Module module, @NotNull ModifiableRootModel rootModel, @NotNull ModifiableModelsProvider modifiableModelsProvider, @NotNull BuildScriptDataBuilder buildScriptData) {
        applyBuildScriptModifications(buildScriptData);

        // Create mod main and gradle.properties
        final Configuration configuration = form.toConfiguration();
        final VirtualFile root = module.getProject().getBaseDir(); // FIXME: Support adding child modules
        final String group = projectId.getGroupId() == null ? "" : projectId.getGroupId();
        final String rawPath = "src.main.java." + projectId.getGroupId();

        ApplicationUtil.runWriteAction(() -> {
            try {
                VirtualFile propertiesFile = root.findOrCreateChildData(this, "gradle.properties" );

                Properties properties = new Properties();
                properties.setProperty("MOD_ID" , projectId.getArtifactId());
                properties.setProperty("MC_VERSION" , configuration.mcVersion);
                properties.setProperty("FORGE_VERSION" , configuration.forgeVersion);
                properties.setProperty("MAPPINGS" , configuration.mappingsVersion);
                properties.setProperty("USE_DEP_ATS" , String.valueOf(configuration.useDepATs));

                TemplateUtil.applyJ2eeTemplate(
                        module.getProject(),
                        MinecraftForgeFileTemplateGroupDescriptorFactory.GRADLE_PROPERTIES_TEMPLATE,
                        propertiesFile,
                        properties,
                        false);

            } catch (IOException ignored) {
                // TODO: Handle
            }

            final String[] path = rawPath.split("\\." );
            VirtualFile current = root;
            try {
                for (String dirName : path) {
                    current = current.createChildDirectory(this, dirName);
                }

                VirtualFile mainClassFile = current.findOrCreateChildData(this, configuration.mainClassName + ".java" );

                Properties properties = new Properties();
                properties.setProperty("PACKAGE" , group);
                properties.setProperty("CLASS_NAME" , configuration.mainClassName);
                properties.setProperty("MOD_ID" , projectId.getArtifactId());
                properties.setProperty("MOD_NAME" , configuration.modName);

                TemplateUtil.applyJ2eeTemplate(
                        module.getProject(),
                        MinecraftForgeFileTemplateGroupDescriptorFactory.MAIN_CLASS_TEMPLATE,
                        mainClassFile,
                        properties,
                        true);
            } catch (IOException ignored) {
                // TODO: Handle
            }

            try {
                VirtualFile commonProxyFile = current.findOrCreateChildData(this, "CommonProxy.java" );

                Properties properties = new Properties();
                properties.setProperty("PACKAGE" , group);

                TemplateUtil.applyJ2eeTemplate(
                        module.getProject(),
                        MinecraftForgeFileTemplateGroupDescriptorFactory.COMMON_PROXY_TEMPLATE,
                        commonProxyFile,
                        properties,
                        true);

                current = current.createChildDirectory(this, "client");

                VirtualFile clientProxyFile = current.findOrCreateChildData(this, "ClientProxy.java" );

                TemplateUtil.applyJ2eeTemplate(
                        module.getProject(),
                        MinecraftForgeFileTemplateGroupDescriptorFactory.CLIENT_PROXY_TEMPLATE,
                        clientProxyFile,
                        properties,
                        true);
            } catch (IOException ignored) {
                // TODO: Handle
            }
        });
    }

    private void applyBuildScriptModifications(@NotNull final BuildScriptDataBuilder buildScriptData) {
        buildScriptData
                .addBuildscriptRepositoriesDefinition("jcenter()" )
                .addBuildscriptRepositoriesDefinition("maven { url \"http://files.minecraftforge.net/maven\" }" )
                .addBuildscriptDependencyNotation("classpath 'net.minecraftforge.gradle:ForgeGradle:2.3-SNAPSHOT'" )
                .addPluginDefinition("apply plugin: 'net.minecraftforge.gradle.forge'" )
                .addPropertyDefinition("sourceCompatibility = targetCompatibility = '1.8'" )
                .addOther("minecraft {\n" +
                        "\tversion = project.mcVersion + \"-\" + project.forgeVersion\n" +
                        "\trunDir = \"run\"\n" +
                        "\tmappings = project.mcpMappings\n" +
                        "\n" +
                        "\tuseDepATs = project.useDepATs\n" +
                        "}" )
                .addOther("compileJava {\n" +
                        "\tsourceCompatibility = targetCompatibility = '1.8'\n" +
                        "}" );
    }

    static class Configuration {
        @NotNull
        final String mainClassName;
        @NotNull
        final String mcVersion;
        @NotNull
        final String forgeVersion;
        @NotNull
        final String mappingsVersion;
        @NotNull
        final String modName;
        final boolean useDepATs;

        Configuration(@NotNull String mainClassName,
                      @NotNull String mcVersion,
                      @NotNull String forgeVersion,
                      @NotNull String mappingsVersion,
                      @NotNull String modName,
                      boolean useDepATs) {
            this.mainClassName = mainClassName;
            this.mcVersion = mcVersion;
            this.forgeVersion = forgeVersion;
            this.mappingsVersion = mappingsVersion;
            this.modName = modName;
            this.useDepATs = useDepATs;
        }
    }
}
