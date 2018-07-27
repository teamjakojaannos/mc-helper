package jakojaannos.mchelper.util;

import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Properties;

public class TemplateUtil {
    private TemplateUtil() {
    }

    public static void applyJ2eeTemplate(final @NotNull Project project,
                                         final @NotNull String templateName,
                                         final @NotNull VirtualFile file,
                                         final @NotNull Properties properties,
                                         final boolean reformat) {
        final FileTemplateManager templateManager = FileTemplateManager.getInstance(project);
        final FileTemplate template = templateManager.getJ2eeTemplate(templateName);

        Properties projectProperties = templateManager.getDefaultProperties();
        projectProperties.putAll(properties);

        String fileContents;
        try {
            fileContents = template.getText(projectProperties);
            VfsUtil.saveText(file, fileContents);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (reformat) {
            reformatFile(project, file);
        }
    }

    public static void reformatFile(final @NotNull Project project,
                                    final @NotNull VirtualFile file) {
        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        if (psiFile != null) {
            new ReformatCodeProcessor(project, psiFile, null, false).run();
        }
    }
}
