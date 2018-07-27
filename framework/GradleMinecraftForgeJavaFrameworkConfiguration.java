package jakojaannos.mchelper.framework;

import com.intellij.ui.HideableDecorator;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class GradleMinecraftForgeJavaFrameworkConfiguration {
    private JPanel root;
    private JComboBox mcVersionComboBox;
    private JComboBox forgeVersionComboBox;
    private JComboBox mappingsComboBox;
    private JCheckBox depAtCheckBox;
    private JPanel moreSettingsPanel;
    private JPanel moreSettingsPlaceholder;
    private JTextField modNameTextField;
    private JTextField mainClassNameTextField;

    private boolean mainClassFieldIsDefault = true;
    private boolean isAutoEdit = false;

    GradleMinecraftForgeJavaFrameworkConfiguration() {
        HideableDecorator moreSettingsDecorator = new HideableDecorator(moreSettingsPlaceholder, "Mor&e settings", false);
        moreSettingsPanel.setBorder(JBUI.Borders.empty(0, IdeBorderFactory.TITLED_BORDER_INDENT, 5, 0));
        moreSettingsDecorator.setContentComponent(moreSettingsPanel);

        final String mcVersion = "1.12.2";
        final String forgeVersion = "14.23.4.2739";
        final String mappingsVersion = "snapshot_20171003";

        mcVersionComboBox.addItem(mcVersion);
        forgeVersionComboBox.addItem(forgeVersion);
        mappingsComboBox.addItem(mappingsVersion);

        modNameTextField.getDocument().addDocumentListener((DocumentListenerAdapter) e -> {
            if (mainClassFieldIsDefault) {
                try {
                    final Document document = modNameTextField.getDocument();
                    String text = document.getText(document.getStartPosition().getOffset(), document.getLength());
                    isAutoEdit = true;
                    mainClassNameTextField.setText(text.replaceAll("[^A-Za-z]", ""));
                    isAutoEdit = false;
                } catch (BadLocationException ignored) {
                }
            }
        });

        mainClassNameTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!isAutoEdit) {
                    mainClassFieldIsDefault = false;
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (!isAutoEdit) {
                    mainClassFieldIsDefault = e.getDocument().getLength() == 0;
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });

        mainClassNameTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (mainClassNameTextField.getDocument().getLength() == 0) {
                    try {
                        final Document document = modNameTextField.getDocument();
                        String text = document.getText(document.getStartPosition().getOffset(), document.getLength());
                        isAutoEdit = true;
                        mainClassNameTextField.setText(text.replaceAll("[^A-Za-z]", ""));
                        isAutoEdit = false;
                    } catch (BadLocationException ignored) {
                    }
                }
            }
        });

        mainClassNameTextField.addActionListener(e -> {
            if (mainClassNameTextField.getDocument().getLength() == 0) {
                try {
                    final Document document = modNameTextField.getDocument();
                    String text = document.getText(document.getStartPosition().getOffset(), document.getLength());
                    isAutoEdit = true;
                    mainClassNameTextField.setText(text.replaceAll("[^A-Za-z]", ""));
                    isAutoEdit = false;
                } catch (BadLocationException ignored) {
                }
            }
        });
    }

    @NotNull
    GradleMinecraftForgeJavaFrameworkSupportProvider.Configuration toConfiguration() {
        return new GradleMinecraftForgeJavaFrameworkSupportProvider.Configuration(
                mainClassNameTextField.getText(),
                (String) mcVersionComboBox.getSelectedItem(),
                (String) forgeVersionComboBox.getSelectedItem(),
                (String) mappingsComboBox.getSelectedItem(),
                modNameTextField.getText(),
                depAtCheckBox.isSelected()
        );
    }

    JComponent getRootComponent() {
        return root;
    }

    private interface DocumentListenerAdapter extends DocumentListener {
        @Override
        default void insertUpdate(DocumentEvent e) {
            updated(e);
        }

        @Override
        default void removeUpdate(DocumentEvent e) {
            updated(e);
        }

        @Override
        default void changedUpdate(DocumentEvent e) {
            updated(e);
        }

        void updated(DocumentEvent e);
    }
}
