package jakojaannos.mchelper.module.wizard;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.externalSystem.service.project.wizard.ExternalModuleSettingsStep;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.ui.HideableDecorator;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.util.ui.JBUI;
import jakojaannos.mchelper.module.ForgeModuleBuilder;
import lombok.NonNull;
import lombok.val;
import org.jetbrains.plugins.gradle.service.project.wizard.GradleParentProjectForm;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class ModWizardStep extends ModuleWizardStep {
    @NonNull private final ForgeModuleBuilder builder;
    private JPanel root;
    private JTextField modName;
    private JTextField modId;
    private JTextField defaultPackage;
    private JPanel advancedPlaceholder;
    private JPanel advancedPanel;
    private JTextField mainClassName;
    private JTextField mainClassPath;
    private JPanel advancedAdditionPanel;

    private GradleParentProjectForm parentProjectForm;

    private boolean defaultPackageIsDefault = true;
    private boolean mainClassNameIsDefault = true;

    private boolean autoEdit = false;

    public ModWizardStep(@NonNull ForgeModuleBuilder builder) {
        this.builder = builder;

        setupAdvancedPanel();

        setupFilters();
    }

    private void setupAdvancedPanel() {
        HideableDecorator decorator = new HideableDecorator(advancedPlaceholder, "Advanced Options", false);
        advancedPanel.setBorder(JBUI.Borders.empty(0, IdeBorderFactory.TITLED_BORDER_INDENT, 5, 0));
        decorator.setContentComponent(advancedPanel);

        if (builder.getWizardContext() == null) {
            throw new IllegalStateException("WizardContext cannot be null!");
        }

        parentProjectForm = new GradleParentProjectForm(builder.getWizardContext(), parent -> {
            if (parentProjectForm != null) {
                parentProjectForm.updateComponents();
            }

            builder.setParentProject(parent);
            if (parent == null) {
                builder.getWizardContext().putUserData(ExternalModuleSettingsStep.SKIP_STEP_KEY, Boolean.FALSE);
            } else {
                builder.getWizardContext().putUserData(ExternalModuleSettingsStep.SKIP_STEP_KEY, Boolean.TRUE);
            }
        });
        advancedAdditionPanel.add(parentProjectForm.getComponent());
    }

    private void setupFilters() {
        // Allow only lower case characters in mod ID
        ((AbstractDocument) modId.getDocument()).setDocumentFilter(new Filter() {
            @Override
            public String filter(String original) {
                return original.trim().toLowerCase().replaceAll("[^a-z]", "");
            }
        });

        // Relay edits to mod ID to default package, if default package hasn't been changed
        modId.getDocument().addDocumentListener((SimpleDocumentListener) e -> {
            if (defaultPackageIsDefault) {
                autoEdit = true;
                defaultPackage.setText(modId.getText());
                autoEdit = false;
            }
        });

        // Allow only lower case characters in package
        ((AbstractDocument) defaultPackage.getDocument()).setDocumentFilter(new Filter() {
            @Override
            public String filter(String original) {
                return original.trim().toLowerCase().replaceAll("[^a-z.]", "");
            }
        });

        // Update main class location string when default package changes
        defaultPackage.getDocument().addDocumentListener((SimpleDocumentListener) e -> updateMainClassPath());

        // Auto-generate default package if its text field loses focus while empty
        defaultPackage.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (defaultPackage.getDocument().getLength() == 0) {
                    autoEdit = true;
                    defaultPackage.setText(modId.getText());
                    autoEdit = false;

                    updateMainClassPath();
                }
            }
        });

        // Relay edits to mod name to main class name, if main class name hasn't been modified.
        modName.getDocument().addDocumentListener((SimpleDocumentListener) e -> {
            if (mainClassNameIsDefault) {
                autoEdit = true;
                mainClassName.setText(parseMainClassNameFromModName());
                autoEdit = false;
            }
        });

        // Re-enable auto-edit if class name is empty
        mainClassName.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!autoEdit) {
                    mainClassNameIsDefault = false;
                }

                updateMainClassPath();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (!autoEdit) {
                    mainClassNameIsDefault = e.getDocument().getLength() == 0;
                }

                updateMainClassPath();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (!autoEdit) {
                    mainClassNameIsDefault = e.getDocument().getLength() == 0;
                }

                updateMainClassPath();
            }
        });

        // Auto-generate main class name if text field loses focus while empty
        mainClassName.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (mainClassName.getDocument().getLength() == 0) {
                    autoEdit = true;
                    mainClassName.setText(parseMainClassNameFromModName());
                    autoEdit = false;

                    updateMainClassPath();
                }
            }
        });
    }

    private void updateMainClassPath() {
        val packageText = defaultPackage.getText().isEmpty() ? "<default package>" : defaultPackage.getText();
        val classNameText = mainClassName.getText().isEmpty() ? "<main class>" : mainClassName.getText();
        mainClassPath.setText(packageText + "." + classNameText);
    }

    @NonNull
    private String parseMainClassNameFromModName() {
        final Document doc = modName.getDocument();
        String text;
        try {
            text = doc.getText(doc.getStartPosition().getOffset(), doc.getLength());
        } catch (BadLocationException ignored) {
            return "";
        }

        return text.replaceAll("[^A-Za-z]", "");
    }

    @Override
    public JComponent getComponent() {
        return root;
    }

    @Override
    public void updateDataModel() {
        builder.setModInfo(new ForgeModuleBuilder.ModInfo(
                modName.getText(),
                modId.getText(),
                "1.0.0",
                defaultPackage.getText(),
                mainClassName.getText()));
    }

    @Override
    public boolean validate() throws ConfigurationException {
        if (modName.getText().isEmpty()) {
            throw new ConfigurationException("Mod name cannot be empty!");
        }

        if (modId.getText().isEmpty()) {
            throw new ConfigurationException("Mod ID cannot be empty!");
        }

        if (mainClassName.getText().isEmpty()) {
            throw new ConfigurationException("Main class name cannot be empty!");
        }

        return super.validate();
    }

    private abstract class Filter extends DocumentFilter {
        @Override
        public void insertString(DocumentFilter.FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            super.insertString(fb, offset, filter(string), attr);
        }

        @Override
        public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            super.replace(fb, offset, length, filter(text), attrs);
        }

        public abstract String filter(String original);
    }

    private interface SimpleDocumentListener extends DocumentListener {
        @Override
        default void insertUpdate(DocumentEvent e) {
            update(e);
        }

        @Override
        default void removeUpdate(DocumentEvent e) {
            update(e);
        }

        @Override
        default void changedUpdate(DocumentEvent e) {
            update(e);
        }

        void update(DocumentEvent e);
    }
}
