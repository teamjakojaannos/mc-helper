package jakojaannos.mchelper.module;

import com.intellij.ide.highlighter.ModuleFileType;
import com.intellij.ide.projectWizard.ProjectSettingsStep;
import com.intellij.ide.util.projectWizard.ModuleNameLocationSettings;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.externalSystem.ExternalSystemModulePropertyManager;
import com.intellij.openapi.externalSystem.importing.ImportSpec;
import com.intellij.openapi.externalSystem.importing.ImportSpecBuilder;
import com.intellij.openapi.externalSystem.model.ExternalSystemDataKeys;
import com.intellij.openapi.externalSystem.model.ProjectSystemId;
import com.intellij.openapi.externalSystem.model.project.ProjectData;
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode;
import com.intellij.openapi.externalSystem.service.project.wizard.AbstractExternalModuleBuilder;
import com.intellij.openapi.externalSystem.service.project.wizard.ExternalModuleSettingsStep;
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil;
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.*;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdkType;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ThreeState;
import com.intellij.util.containers.ContainerUtil;
import jakojaannos.mchelper.forge.ForgeVersionEntry;
import jakojaannos.mchelper.forge.McpMappingEntry;
import jakojaannos.mchelper.forge.MinecraftVersionEntry;
import jakojaannos.mchelper.module.wizard.ForgeWizardStep;
import jakojaannos.mchelper.module.wizard.ModWizardStep;
import jakojaannos.mchelper.templates.ForgeTemplates;
import jakojaannos.mchelper.util.TemplateUtil;
import lombok.*;
import lombok.experimental.var;
import org.gradle.util.GradleVersion;
import org.jetbrains.plugins.gradle.service.settings.GradleProjectSettingsControl;
import org.jetbrains.plugins.gradle.settings.DistributionType;
import org.jetbrains.plugins.gradle.settings.GradleProjectSettings;
import org.jetbrains.plugins.gradle.util.GradleConstants;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * <p>
 * Builds ForgeGradle projects. Setups project files to use gradle, with forge setup by default.
 * (This allows creating forge projects without the extra gradle import step adding feel of complexity
 * to the project creation.)
 * </p>
 * <p>
 * NOTE:
 * {@link org.jetbrains.plugins.gradle.service.project.wizard.GradleModuleBuilder GradleModuleBuilder}
 * cannot feasibly be extended due to its constructor implementation preventing overriding system IDs etc.
 * Thus, this class is largely VERY similar to the original GradleModuleBuilder as both essentially just
 * construct modules with Gradle support. This gives us nice amount of control over how project files are
 * created without having to worry about how original implementation would like to create them.
 * </p>
 */
public class ForgeModuleBuilder extends AbstractExternalModuleBuilder<GradleProjectSettings> {
    private static final Logger LOG = Logger.getInstance(ForgeModuleBuilder.class);

    private static final String TEMPLATE_GRADLE_SETTINGS = "Forge settings.gradle";
    private static final String TEMPLATE_GRADLE_SETTINGS_MERGE = "Forge merge settings.gradle";
    private static final String TEMPLATE_GRADLE_BUILD_WRAPPER_TASK = "Forge wrapper build.gradle";
    private static final String TEMPLATE_GRADLE_BUILD = "Forge build.gradle";
    private static final String TEMPLATE_GRADLE_PROPERTIES = "Forge gradle.properties";

    private static final String DEFAULT_PROPERTIES_NAME = "gradle.properties";

    @Nullable @Setter @Getter
    private ModInfo modInfo;
    @Nullable @Setter @Getter
    private ForgeSettings forgeSettings;

    @Nullable @Getter
    private WizardContext wizardContext;

    @Nullable @Setter
    private ProjectData parentProject;
    @Nullable
    private String rootProjectPath;


    public ForgeModuleBuilder() {
        super(new ProjectSystemId(/*"FORGE"*/GradleConstants.SYSTEM_ID.getId(), "Minecraft (Forge)"), new GradleProjectSettings());
    }

    @NonNull
    @Override
    public Module createModule(@NonNull ModifiableModuleModel moduleModel) throws InvalidDataException, ConfigurationException {
        // Make sure we have path to create the module to
        val originModuleFilePath = getModuleFilePath();
        LOG.assertTrue(wizardContext != null);
        LOG.assertTrue(originModuleFilePath != null);

        // Resolve module name. By default, modID is used (qualified with default package, respecting gradle settings)
        // If mod info is invalid, fall back to using getName()
        String moduleName;
        if (modInfo != null) {
            moduleName = getExternalProjectSettings().isUseQualifiedModuleNames() && StringUtil.isNotEmpty(modInfo.getDefaultPackage())
                    ? (modInfo.getDefaultPackage() + "." + modInfo.getModId())
                    : modInfo.getModId();
        } else {
            moduleName = getName();
        }

        // Resolve project file directory (where .iml or /.idea/ should reside)
        val contextProject = wizardContext.getProject();
        String projectFileDirectory = null;
        if (wizardContext.isCreatingNewProject() || contextProject == null || contextProject.getBasePath() == null) {
            projectFileDirectory = wizardContext.getProjectFileDirectory();
        } else if (wizardContext.getProjectStorageFormat() == StorageScheme.DEFAULT) {
            var moduleFileDirectory = getModuleFileDirectory();
            if (moduleFileDirectory != null) {
                projectFileDirectory = moduleFileDirectory;
            }
        }

        if (projectFileDirectory == null) {
            projectFileDirectory = contextProject.getBasePath();
        }

        if (wizardContext.getProjectStorageFormat() == StorageScheme.DIRECTORY_BASED) {
            projectFileDirectory += "/.idea/modules";
        }

        // Purge the possible existing module file and create the new module
        var moduleFilePath = projectFileDirectory + "/" + moduleName + ModuleFileType.DOT_DEFAULT_EXTENSION;
        deleteModuleFile(moduleFilePath);
        var moduleType = getModuleType();
        var module = moduleModel.newModule(moduleFilePath, moduleType.getId());

        setupModule(module);

        return module;
    }

    @Override
    public void setupRootModel(ModifiableRootModel rootModel) throws ConfigurationException {
        LOG.assertTrue(wizardContext != null);
        val contentEntryPath = getContentEntryPath();
        if (StringUtil.isEmpty(contentEntryPath)) {
            LOG.warn("Content Entry Path was empty!");
            return;
        }

        // Create root directory
        val contentRootDir = new File(contentEntryPath);
        FileUtilRt.createDirectory(contentRootDir);
        val fileSystem = LocalFileSystem.getInstance();
        val modelContentRootDir = fileSystem.refreshAndFindFileByIoFile(contentRootDir);
        if (modelContentRootDir == null) {
            LOG.warn("Model Content Root Directory could not be created or found!");
            return;
        }

        rootModel.addContentEntry(modelContentRootDir);
        if (myJdk != null) {
            rootModel.setSdk(myJdk);
        } else {
            rootModel.inheritSdk();
        }

        // If we are creating a new module to another project, setup root project path
        val project = rootModel.getProject();
        if (parentProject != null) {
            rootProjectPath = parentProject.getLinkedExternalProjectPath();
        } else {
            rootProjectPath = FileUtil.toCanonicalPath(
                    wizardContext.isCreatingNewProject()
                            ? project.getBasePath()
                            : modelContentRootDir.getPath());
        }
        LOG.assertTrue(rootProjectPath != null);

        // Setup buildscript files
        setupGradleBuildFile(
                modelContentRootDir,
                getExternalProjectSettings(),
                modInfo);
        setupGradlePropertiesFile(
                modelContentRootDir,
                forgeSettings,
                modInfo);
        setupGradleSettingsFile(
                rootProjectPath,
                modelContentRootDir,
                project.getName(),
                modInfo == null ? rootModel.getModule().getName() : modInfo.getModId(),
                wizardContext.isCreatingNewProject() || parentProject == null);

        // Setup default classes
        setupModMain(
                project,
                modelContentRootDir,
                modInfo);
    }

    @Override
    protected void setupModule(Module module) throws ConfigurationException {
        super.setupModule(module);
        LOG.assertTrue(rootProjectPath != null);
        LOG.assertTrue(wizardContext != null);

        ExternalSystemModulePropertyManager.getInstance(module).setExternalId(GradleConstants.SYSTEM_ID);

        val project = module.getProject();

        if (wizardContext.isCreatingNewProject()) {
            getExternalProjectSettings().setExternalProjectPath(rootProjectPath);
            val settings = ExternalSystemApiUtil.getSettings(project, GradleConstants.SYSTEM_ID);
            project.putUserData(ExternalSystemDataKeys.NEWLY_CREATED_PROJECT, Boolean.TRUE);
            //noinspection unchecked
            settings.linkProject(getExternalProjectSettings());

            Runnable task = () -> {
                ImportSpec importSpec = new ImportSpecBuilder(project, GradleConstants.SYSTEM_ID)
                        .use(ProgressExecutionMode.IN_BACKGROUND_ASYNC)
                        //.createDirectoriesForEmptyContentRoots()
                        .useDefaultCallback()
                        .build();
                ExternalSystemUtil.refreshProject(rootProjectPath, importSpec);
            };

            ExternalSystemUtil.invokeLater(project, ModalityState.NON_MODAL, task);
        } else {
            FileDocumentManager.getInstance().saveAllDocuments();
            val projectSettings = getExternalProjectSettings();
            Runnable task = () -> {
                if (parentProject == null) {
                    projectSettings.setExternalProjectPath(rootProjectPath);
                    val settings = ExternalSystemApiUtil.getSettings(project, GradleConstants.SYSTEM_ID);
                    //noinspection unchecked
                    settings.linkProject(projectSettings);
                }

                ImportSpec importSpec = new ImportSpecBuilder(project, GradleConstants.SYSTEM_ID)
                        .use(ProgressExecutionMode.IN_BACKGROUND_ASYNC)
                        .createDirectoriesForEmptyContentRoots()
                        .useDefaultCallback()
                        .build();
                ExternalSystemUtil.refreshProject(rootProjectPath, importSpec);
            };

            ExternalSystemUtil.invokeLater(project, ModalityState.NON_MODAL, task);
        }
    }

    @Override
    public ModuleWizardStep[] createWizardSteps(@NonNull WizardContext wizardContext, @NonNull ModulesProvider modulesProvider) {
        this.wizardContext = wizardContext;

        val settings = getExternalProjectSettings().clone();
        settings.setStoreProjectFilesExternally(ThreeState.UNSURE);

        return new ModuleWizardStep[]{
                new ModWizardStep(this),
                new ExternalModuleSettingsStep<>(wizardContext, this, new GradleProjectSettingsControl(settings))
        };
    }

    @Nullable
    @Override
    public ModuleWizardStep getCustomOptionsStep(WizardContext context, Disposable parentDisposable) {
        return new ForgeWizardStep(this);
    }

    @Nullable
    @Override
    public ModuleWizardStep modifySettingsStep(@NonNull SettingsStep settingsStep) {
        if (settingsStep instanceof ProjectSettingsStep) {
            final ProjectSettingsStep projectSettingsStep = (ProjectSettingsStep) settingsStep;
            final ModuleNameLocationSettings nameLocationSettings = settingsStep.getModuleNameLocationSettings();

            if (nameLocationSettings != null && modInfo != null) {
                nameLocationSettings.setModuleName(modInfo.getModId());
            }
            projectSettingsStep.bindModuleSettings();
        }
        return super.modifySettingsStep(settingsStep);
    }

    @Override
    public boolean isSuitableSdkType(SdkTypeId sdkType) {
        return sdkType instanceof JavaSdkType;
    }

    @Override
    public String getParentGroup() {
        return JavaModuleType.JAVA_GROUP;
    }

    @Override
    public int getWeight() {
        return 999;
    }

    @Override
    public ModuleType getModuleType() {
        return StdModuleTypes.JAVA;
    }

    @Override
    public String getDescription() {
        return "Module for Minecraft mod development using the Forge API.";
    }


    private static void setupGradleBuildFile(@NonNull VirtualFile modelContentRootDir,
                                             @NonNull GradleProjectSettings externalProjectSettings,
                                             @Nullable ModInfo modInfo)
            throws ConfigurationException {
        val file = TemplateUtil.getOrCreateExternalProjectConfigFile(modelContentRootDir.getPath(), GradleConstants.DEFAULT_SCRIPT_NAME);
        if (file == null) {
            LOG.warn("Could not create or find build.gradle");
            return;
        }

        val attributes = ContainerUtil.newHashMap();
        if (modInfo != null) {
            attributes.put("MOD_MAIN_CLASS", modInfo.mainClassName);
        }
        TemplateUtil.saveFile(file, TEMPLATE_GRADLE_BUILD, attributes);

        // Append wrapper task if wrapper desired
        if (externalProjectSettings.getDistributionType() == DistributionType.WRAPPED) {
            attributes.put("GRADLE_VERSION", GradleVersion.current().getVersion());
            TemplateUtil.saveFile(file, TEMPLATE_GRADLE_BUILD_WRAPPER_TASK, attributes);
        }
    }

    private static void setupGradlePropertiesFile(@NonNull VirtualFile modelContentRootDir,
                                                  @Nullable ForgeSettings forgeSettings,
                                                  @Nullable ModInfo modInfo)
            throws ConfigurationException {
        val file = TemplateUtil.getOrCreateExternalProjectConfigFile(modelContentRootDir.getPath(), DEFAULT_PROPERTIES_NAME);
        if (file == null) {
            LOG.warn("Could not create or find gradle.properties");
            return;
        }


        val attributes = ContainerUtil.newHashMap();
        if (modInfo != null) {
            attributes.put("MOD_ID", modInfo.getModId());
            attributes.put("MOD_GROUP", modInfo.getDefaultPackage());
            attributes.put("MOD_VERSION", modInfo.getVersion());
        }

        if (forgeSettings != null) {
            attributes.put("MC_VERSION", forgeSettings.getMinecraftVersion().getVersion());
            attributes.put("FORGE_VERSION", forgeSettings.getForgeVersion().getVersion());
            attributes.put("MCP_MAPPINGS", forgeSettings.getMcpMappings().getVersion());
        }

        TemplateUtil.saveFile(file, TEMPLATE_GRADLE_PROPERTIES, attributes);
    }

    private static void setupGradleSettingsFile(@NonNull String rootProjectPath,
                                                @NonNull VirtualFile modelContentRootDir,
                                                String projectName,
                                                String moduleName,
                                                boolean renderNewFile)
            throws ConfigurationException {
        val file = TemplateUtil.getOrCreateExternalProjectConfigFile(rootProjectPath, GradleConstants.SETTINGS_FILE_NAME);
        LOG.assertTrue(file != null);

        val attributes = ContainerUtil.newHashMap();
        attributes.put("MODULE_NAME", moduleName);

        if (renderNewFile) {
            val moduleDirName = VfsUtilCore.getRelativePath(modelContentRootDir, file.getParent(), '/');

            attributes.put("PROJECT_NAME", projectName);
            attributes.put("MODULE_PATH", moduleDirName);
            TemplateUtil.saveFile(file, TEMPLATE_GRADLE_SETTINGS, attributes);
        } else {
            val separator =
                    (file.getParent() == null || !VfsUtilCore.isAncestor(file.getParent(), modelContentRootDir, true))
                            ? '/'
                            : ':';
            val modulePath = VfsUtilCore.findRelativePath(file, modelContentRootDir, separator);

            val flatStructureModulePath =
                    modulePath != null && StringUtil.startsWith(modulePath, "../")
                            ? StringUtil.trimStart(modulePath, "../")
                            : null;

            if (StringUtil.equals(flatStructureModulePath, modelContentRootDir.getName())) {
                attributes.put("MODULE_FLAT_DIR", "true");
                attributes.put("MODULE_PATH", flatStructureModulePath);
            } else {
                attributes.put("MODULE_PATH", modulePath);
            }

            TemplateUtil.saveFile(file, TEMPLATE_GRADLE_SETTINGS_MERGE, attributes);
        }
    }


    private void setupModMain(@NonNull Project project,
                              @NonNull VirtualFile modelContentRootDir,
                              @Nullable ModInfo modInfo)
            throws ConfigurationException {
        LOG.assertTrue(modInfo != null);

        val path = modelContentRootDir.getPath() + ("/src/main/java/" + modInfo.defaultPackage).replaceAll("\\.", "/");

        val properties = new Properties();
        properties.put("PACKAGE", modInfo.getDefaultPackage());
        properties.put("MAIN_CLASS_NAME", modInfo.getMainClassName());
        properties.put("MOD_ID", modInfo.getModId());
        properties.put("MOD_NAME", modInfo.getModName());

        val mainClassFile = TemplateUtil.getOrCreateExternalProjectConfigFile(path, modInfo.getMainClassName() + ".java");
        val commonProxyFile = TemplateUtil.getOrCreateExternalProjectConfigFile(path, "CommonProxy.java");
        val clientProxyFile = TemplateUtil.getOrCreateExternalProjectConfigFile(path + "/client", "ClientProxy.java");

        try {
            TemplateUtil.applyJ2eeTemplate(
                    project,
                    ForgeTemplates.MAIN_CLASS,
                    mainClassFile,
                    properties);

            TemplateUtil.applyJ2eeTemplate(
                    project,
                    ForgeTemplates.COMMON_PROXY,
                    commonProxyFile,
                    properties);

            TemplateUtil.applyJ2eeTemplate(
                    project,
                    ForgeTemplates.CLIENT_PROXY,
                    clientProxyFile,
                    properties);
        } catch (IOException e) {
            LOG.warn("Error applying class templates!", e);
            throw new ConfigurationException("Error applying class templates!");
        }
    }


    @Value
    public static class ModInfo {
        String modName;
        String modId;
        String version;

        String defaultPackage;
        String mainClassName;
    }

    @Value
    public static class ForgeSettings {
        @NonNull
        ForgeVersionEntry forgeVersion;
        @NonNull
        MinecraftVersionEntry minecraftVersion;
        @NonNull
        McpMappingEntry mcpMappings;

        boolean useDependencyAts;
    }
}
