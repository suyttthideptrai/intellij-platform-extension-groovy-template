package com.quylaptrinh.jbplugin.pywsl.toolWindow;

import com.intellij.ui.components.JBLabel;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.execution.wsl.WSLDistribution;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.quylaptrinh.jbplugin.pywsl.MyBundle;
import com.quylaptrinh.jbplugin.pywsl.exception.DistroNotLoadedException;
import com.quylaptrinh.jbplugin.pywsl.services.WslService;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;
import org.jetbrains.annotations.NotNull;

public class MyToolWindowFactory implements ToolWindowFactory {

  private static final Logger LOG = Logger.getInstance(MyToolWindowFactory.class);

  public MyToolWindowFactory() {}

  @Override
  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
    MyToolWindow myToolWindow = new MyToolWindow(toolWindow);
    Content content = ContentFactory.getInstance()
        .createContent(myToolWindow.getContent(), null, false);
    toolWindow.getContentManager().addContent(content);
  }

  @Override
  public boolean shouldBeAvailable(@NotNull Project project) {
    return true;
  }

  public static class MyToolWindow {

    /**
     * Layouts
     */
    private final JPanel rootPanel;
    private JPanel wslFsExplorerPanel;
    private final CardLayout cardLayout;

    private final WslService service;

    private boolean isDistroSelected() {
      return this.service.isWslLoaded();
    }

    public MyToolWindow(ToolWindow toolWindow) {
      this.service = ServiceManager.getService(toolWindow.getProject(), WslService.class);

      // init data
      List<WSLDistribution> ls = service.listDistributions();
      WSLDistribution wslDist = this.service.getDefaultWslDistro().orElse(null);
      String defaultName = UUID.randomUUID().toString();
      if (Objects.nonNull(wslDist)) {
        defaultName = wslDist.getId();
      }

      this.cardLayout = new CardLayout();
      this.rootPanel = new JPanel(this.cardLayout);

      JPanel panel1 = new JBPanel<>(new BorderLayout());
      panel1.add(new JBLabel("Select wsl distro"), BorderLayout.CENTER);
      panel1.add(this.createDistroSelectPanel(ls, defaultName));

      this.rootPanel.add(panel1, "select");
    }

    public JPanel getContent() {
      return this.rootPanel;
    }


    private JComponent createDistroSelectPanel(List<WSLDistribution> distList, String defaultName) {
      JBPanel<?> panel = new JBPanel<>(new FlowLayout());
      JBLabel compTitle = new JBLabel("Select wsl distro");

      Map<String, WSLDistribution> options = distList
          .stream()
          .map(dist -> {
            String id = dist.getId();
            if (Objects.equals(id, defaultName)) {
              return Map.entry(id + "(default)", dist);
            }
            return Map.entry(id, dist);
          })
          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

      JButton button = new JButton(MyBundle.message("Select"));
      ComboBox<String> comboBox = new ComboBox<>(options.keySet().toArray(new String[0]));
      comboBox.setSelectedItem(defaultName);
      button.addActionListener(e -> {
        String selectedText = (String) comboBox.getSelectedItem();
        this.service.setDistro(options.get(selectedText));
        try {
          initializeWslFsExplorer();
        } catch (DistroNotLoadedException ex) {
          JOptionPane
              .showMessageDialog(panel, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
      });
      panel.add(compTitle);
      panel.add(button);
      panel.add(comboBox);

      return panel;
    }

    private void initializeWslFsExplorer() throws DistroNotLoadedException {
      if (Objects.isNull(this.wslFsExplorerPanel)) {
        this.wslFsExplorerPanel = new JBPanel<>(new BorderLayout());
        this.wslFsExplorerPanel.add(new JLabel("Open folder as Project"), BorderLayout.CENTER);
        String userHome = this.service.getDistro().getUserHome();
        this.wslFsExplorerPanel.add(createFileTree(userHome), BorderLayout.CENTER);
        this.rootPanel.add(this.wslFsExplorerPanel, "lazy");
      }
      cardLayout.show(rootPanel, "lazy");
    }

    public static Tree createFileTree(String rootPath) {
      VirtualFile root = LocalFileSystem.getInstance().findFileByIoFile(new File(rootPath));
      if (root == null) return new Tree();

      DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(root.getName());
      for (VirtualFile file : root.getChildren()) {
        rootNode.add(new DefaultMutableTreeNode(file.getName()));
      }
      return new Tree(rootNode);
    }
  }
}
