package com.quylaptrinh.jbplugin.pywsl.toolWindow;

import com.intellij.execution.wsl.WSLDistribution;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.quylaptrinh.jbplugin.pywsl.MyBundle;
import com.quylaptrinh.jbplugin.pywsl.services.WslService;

import java.awt.FlowLayout;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;

public class MyToolWindowFactory implements ToolWindowFactory {

    private static final Logger LOG = Logger.getInstance(MyToolWindowFactory.class);

    public MyToolWindowFactory() {
        LOG.warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.");
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        MyToolWindow myToolWindow = new MyToolWindow(toolWindow);
        Content content = ContentFactory.getInstance().createContent(myToolWindow.getContent(), null, false);
        toolWindow.getContentManager().addContent(content);
    }

    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        return true;
    }

    public static class MyToolWindow {

        private final WslService service;

        public MyToolWindow(ToolWindow toolWindow) {
            this.service = ServiceManager.getService(toolWindow.getProject(), WslService.class);
        }

        public JComponent getContent() {
            List<WSLDistribution> ls = service.listDistributions();
            JBPanel<?> panel = (JBPanel<?>) createCenterPanel(ls);

//            JBLabel label = new JBLabel(MyBundle.message("randomLabel", "?"));
//            JButton button = new JButton(MyBundle.message("shuffle"));

//            button.addActionListener(e -> label.setText(MyBundle.message("randomLabel", service.getRandomNumber())));
//
//            panel.add(label);
//            panel.add(button);

//            // Dropdown options
//            String[] options = {"Option 1", "Option 2", "Option 3", "Option 4"};
//
//            // Create JComboBox
//            <String> comboBox = new ComboBox<>(options);
//
//            // Label to show selection
//            JBLabel label = new JBLabel("Selected: None");
//
//            // Add ActionListener to handle selection
//            comboBox.addActionListener(e -> {
//                String selected = (String) comboBox.getSelectedItem();
//                label.setText("Selected: " + selected);
//            });
//
//            // Add components to frame
//            frame.add(comboBox);
//            frame.add(label);

            return panel;
        }


        private JComponent createCenterPanel(List<WSLDistribution> distList) {
            JBPanel<?> panel = new JBPanel<>(new FlowLayout());

            String[] distListStr = distList.stream().map(dist ->
                dist.getId() + ", " + dist.getMsId()).toArray(String[]::new);
            ComboBox<String> comboBox = new ComboBox<>(new DefaultComboBoxModel<>(distListStr));

            JBLabel label = new JBLabel("Selected: none");

            comboBox.addActionListener(e ->
                label.setText("Selected: " + comboBox.getSelectedItem()));

            panel.add(comboBox);
            panel.add(label);

            return panel;
        }
    }
}
