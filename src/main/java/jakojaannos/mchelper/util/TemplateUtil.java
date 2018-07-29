package jakojaannos.mchelper.util;

import com.intellij.application.options.CodeStyle;
import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.impl.LoadTextUtil;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import lombok.NonNull;
import lombok.experimental.var;
import lombok.val;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

public class TemplateUtil {
    private static final Logger LOG = Logger.getInstance(TemplateUtil.class);

    private TemplateUtil() {
    }

    public static void applyJ2eeTemplate(final @NonNull Project project,
                                         final @NonNull String templateName,
                                         final @NonNull VirtualFile file,
                                         final @NonNull Properties properties)
            throws IOException {
        val templateManager = FileTemplateManager.getDefaultInstance();//.getInstance(project);
        val template = templateManager.getJ2eeTemplate(templateName);

        val projectProperties = templateManager.getDefaultProperties();
        projectProperties.putAll(properties);

        val fileContents = template.getText(projectProperties);
        VfsUtil.saveText(file, fileContents);
    }

    public static void saveFile(@NonNull VirtualFile file,
                                @NonNull String templateName,
                                @Nullable Map attributes)
            throws ConfigurationException {
        val templateManager = FileTemplateManager.getDefaultInstance();
        val template = templateManager.getInternalTemplate(templateName);

        try {
            appendToFile(file, attributes != null ? template.getText(attributes) : template.getText());
        } catch (IOException e) {
            LOG.warn("Unexpected exception saving template config", e);
            throw new ConfigurationException(
                    e.getMessage(), "Can't apply template config text");
        }
    }

    public static void appendToFile(@NonNull VirtualFile file,
                                    @NonNull String text)
            throws IOException {
        val lineSeparator = lineSeparator(file);
        val existingText = StringUtil.trimTrailing(VfsUtilCore.loadText(file));
        val content = (StringUtil.isNotEmpty(existingText) ? existingText + lineSeparator : "")
                + StringUtil.convertLineSeparators(text, lineSeparator);
        VfsUtil.saveText(file, content);
    }

    @NonNull
    public static String lineSeparator(@NonNull VirtualFile file) {
        var lineSeparator = LoadTextUtil.detectLineSeparator(file, true);
        if (lineSeparator == null) {
            lineSeparator = CodeStyle.getDefaultSettings().getLineSeparator();
        }
        return lineSeparator;
    }

    public static VirtualFile getOrCreateExternalProjectConfigFile(@NonNull String parent,
                                                                   @NonNull String fileName) {
        val file = new File(parent, fileName);
        FileUtilRt.createIfNotExists(file);
        return LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
    }
}
