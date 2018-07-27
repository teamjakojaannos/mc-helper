package jakojaannos.mchelper.templates;

import com.intellij.ide.fileTemplates.FileTemplateDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory;
import icons.GradleIcons;
import jakojaannos.mchelper.util.McHelperIcons;

public class MinecraftForgeFileTemplateGroupDescriptorFactory implements FileTemplateGroupDescriptorFactory {
    public static final String MAIN_CLASS_TEMPLATE = "minecraftforge_main_class.java";
    public static final String COMMON_PROXY_TEMPLATE = "minecraftforge_commonproxy.java";
    public static final String CLIENT_PROXY_TEMPLATE = "minecraftforge_clientproxy.java";

    public static final String GRADLE_PROPERTIES_TEMPLATE = "minecraftforge_gradle.properties";

    @Override
    public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
        FileTemplateGroupDescriptor templateGroup = new FileTemplateGroupDescriptor("MinecraftForge", McHelperIcons.FORGE);

        templateGroup.addTemplate(new FileTemplateDescriptor(MAIN_CLASS_TEMPLATE, McHelperIcons.MINECRAFT));
        templateGroup.addTemplate(new FileTemplateDescriptor(COMMON_PROXY_TEMPLATE, McHelperIcons.MINECRAFT));
        templateGroup.addTemplate(new FileTemplateDescriptor(CLIENT_PROXY_TEMPLATE, McHelperIcons.MINECRAFT));
        templateGroup.addTemplate(new FileTemplateDescriptor(GRADLE_PROPERTIES_TEMPLATE, GradleIcons.Gradle));

        return templateGroup;
    }
}
