package jakojaannos.mchelper.templates;

import com.intellij.ide.fileTemplates.FileTemplateDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory;
import icons.GradleIcons;
import jakojaannos.mchelper.util.McHelperIcons;

public class MinecraftForgeFileTemplateGroupDescriptorFactory implements FileTemplateGroupDescriptorFactory {
    @Override
    public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
        FileTemplateGroupDescriptor templateGroup = new FileTemplateGroupDescriptor("MinecraftForge", McHelperIcons.FORGE);

        templateGroup.addTemplate(new FileTemplateDescriptor(ForgeTemplates.MAIN_CLASS, McHelperIcons.MINECRAFT));
        templateGroup.addTemplate(new FileTemplateDescriptor(ForgeTemplates.COMMON_PROXY, McHelperIcons.MINECRAFT));
        templateGroup.addTemplate(new FileTemplateDescriptor(ForgeTemplates.CLIENT_PROXY, McHelperIcons.MINECRAFT));

        return templateGroup;
    }
}
