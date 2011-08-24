/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.chemistry.opencmis.workbench;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.ToolTipManager;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;

import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.tck.CmisTest;
import org.apache.chemistry.opencmis.tck.CmisTestGroup;
import org.apache.chemistry.opencmis.tck.CmisTestProgressMonitor;
import org.apache.chemistry.opencmis.tck.impl.TestParameters;
import org.apache.chemistry.opencmis.tck.runner.AbstractRunner;
import org.apache.chemistry.opencmis.workbench.checks.SwingReport;
import org.apache.chemistry.opencmis.workbench.model.ClientModel;

import sun.swing.DefaultLookup;

/**
 * TCK dialog and runner.
 */
public class TckDialog {

    private final Frame owner;
    private final ClientModel model;
    private final TckDialogRunner runner;

    private JProgressBar groupsProgressBar;
    private JProgressBar testsProgressBar;

    public TckDialog(Frame owner, ClientModel model) {
        this.owner = owner;
        this.model = model;
        this.runner = new TckDialogRunner(model, this);

        try {
            runner.loadDefaultTckGroups();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(owner, "Error: " + e.getMessage(), "TCK Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        new TckSelectDialog();
    }

    private class TckSelectDialog extends JDialog {
        private static final long serialVersionUID = 1L;

        public TckSelectDialog() {
            super(owner, "TCK", true);

            createGUI();
        }

        private void createGUI() {
            setPreferredSize(new Dimension(600, 500));
            setMinimumSize(new Dimension(600, 500));

            setLayout(new BorderLayout());

            // tree
            final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Groups");
            final JTree groupTree = new JTree(rootNode);

            for (CmisTestGroup group : runner.getGroups()) {
                final TestTreeNode groupNode = new TestTreeNode(groupTree, group);
                rootNode.add(groupNode);
                for (CmisTest test : group.getTests()) {
                    final TestTreeNode testNode = new TestTreeNode(groupTree, test);
                    groupNode.add(testNode);
                }
            }

            ((DefaultTreeModel) groupTree.getModel()).reload();

            groupTree.setRootVisible(false);
            groupTree.setCellRenderer(new TestTreeNodeRender());
            groupTree.setCellEditor(new TestTreeNodeEditor());
            groupTree.setEditable(true);
            ToolTipManager.sharedInstance().registerComponent(groupTree);

            for (int i = 0; i < groupTree.getRowCount(); i++) {
                groupTree.expandRow(i);
            }

            final JPopupMenu treePopup = new JPopupMenu();

            final JMenuItem selectItem = new JMenuItem("Select all");
            selectItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    selectAll(groupTree, true);
                }
            });
            treePopup.add(selectItem);

            final JMenuItem deselectItem = new JMenuItem("Deselect all");
            deselectItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    selectAll(groupTree, false);
                }
            });
            treePopup.add(deselectItem);

            groupTree.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    maybeShowPopup(e);
                }

                public void mouseReleased(MouseEvent e) {
                    maybeShowPopup(e);
                }

                private void maybeShowPopup(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        treePopup.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            });

            // config panel
            final JPanel configPanel = new JPanel();
            configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.PAGE_AXIS));
            configPanel.setPreferredSize(new Dimension(getWidth() / 2, 500));
            configPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

            final JComboBox folderComboBox = addComboBox(configPanel, "Test folder type:",
                    BaseTypeId.CMIS_FOLDER.value(), TestParameters.DEFAULT_FOLDER_TYPE_VALUE);
            configPanel.add(Box.createRigidArea(new Dimension(1, 10)));

            final JComboBox documentComboBox = addComboBox(configPanel, "Test document type:",
                    BaseTypeId.CMIS_DOCUMENT.value(), TestParameters.DEFAULT_DOCUMENT_TYPE_VALUE);
            configPanel.add(Box.createRigidArea(new Dimension(1, 10)));

            configPanel.add(new JLabel("Test folder path:"));
            final JTextField testParentFolderField = new JTextField(TestParameters.DEFAULT_TEST_FOLDER_PARENT_VALUE);
            testParentFolderField.setMaximumSize(new Dimension(Short.MAX_VALUE, 10));
            testParentFolderField.setAlignmentX(Component.LEFT_ALIGNMENT);
            configPanel.add(testParentFolderField);

            configPanel.add(Box.createVerticalGlue());

            add(configPanel);

            final JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            mainPanel.add(new JScrollPane(groupTree), BorderLayout.CENTER);
            mainPanel.add(configPanel, BorderLayout.LINE_END);
            add(mainPanel, BorderLayout.CENTER);

            final JButton runButton = new JButton("Run TCK");
            runButton.setDefaultCapable(true);
            runButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    int answer = JOptionPane
                            .showConfirmDialog(
                                    owner,
                                    "Running the TCK may take a long time and may add, remove and alter data in the repository!\n"
                                            + "It also puts at a strain on the repository, performing several thousand calls!\n"
                                            + "\nAre you sure you want to proceed?", "TCK", JOptionPane.YES_NO_OPTION,
                                    JOptionPane.WARNING_MESSAGE);

                    if (answer == JOptionPane.YES_OPTION) {
                        Map<String, String> parameters = runner.getParameters();
                        parameters.put(TestParameters.DEFAULT_FOLDER_TYPE, (String) folderComboBox.getSelectedItem());
                        parameters.put(TestParameters.DEFAULT_DOCUMENT_TYPE,
                                (String) documentComboBox.getSelectedItem());
                        parameters.put(TestParameters.DEFAULT_TEST_FOLDER_PARENT, testParentFolderField.getText());

                        runner.setParameters(parameters);

                        dispose();
                        new TckRunDialog();
                    }
                }
            });

            final JPanel runbuttonPanel = new JPanel();
            runbuttonPanel.setLayout(new BoxLayout(runbuttonPanel, BoxLayout.PAGE_AXIS));
            runbuttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 3, 3, 3));
            runButton.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
            runbuttonPanel.add(runButton);

            add(runbuttonPanel, BorderLayout.PAGE_END);

            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            pack();
            setLocationRelativeTo(null);
            setVisible(true);
        }

        private void selectAll(final JTree tree, boolean select) {
            for (CmisTestGroup group : runner.getGroups()) {
                group.setEnabled(select);
                for (CmisTest test : group.getTests()) {
                    test.setEnabled(select);
                }
            }

            DefaultTreeModel model = ((DefaultTreeModel) tree.getModel());
            model.nodeChanged((TreeNode) model.getRoot());
        }

        private JComboBox addComboBox(JPanel panel, String title, String rootTypeId, String defaultTypeId) {
            final JLabel label = new JLabel(title);
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(label);

            List<ObjectType> types = model.getCreateableTypes(rootTypeId);
            String[] typeIds = new String[types.size()];

            int i = 0;
            for (ObjectType type : types) {
                typeIds[i++] = type.getId();
            }

            final JComboBox comboBox = new JComboBox(typeIds);
            comboBox.setSelectedItem(defaultTypeId);
            comboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
            comboBox.setMaximumSize(new Dimension(Short.MAX_VALUE, 10));
            panel.add(comboBox);

            return comboBox;
        }
    }

    private class TestTreeNode extends DefaultMutableTreeNode {
        private static final long serialVersionUID = 1L;

        private final JTree tree;
        private final CmisTestGroup group;
        private final CmisTest test;

        public TestTreeNode(JTree tree, CmisTestGroup group) {
            this.tree = tree;
            this.group = group;
            this.test = null;
        }

        public TestTreeNode(JTree tree, CmisTest test) {
            this.tree = tree;
            this.test = test;
            this.group = null;
        }

        public CmisTestGroup getGroup() {
            return group;
        }

        public String getName() {
            if (group != null) {
                return group.getName();
            }

            return test.getName();
        }

        public String getDescription() {
            if (group != null) {
                return group.getDescription();
            }

            return test.getDescription();
        }

        public boolean isEnabled() {
            if (group != null) {
                return group.isEnabled();
            }

            return test.isEnabled();
        }

        public void setEnabled(boolean enabled) {
            DefaultTreeModel model = ((DefaultTreeModel) tree.getModel());

            if (group != null) {
                group.setEnabled(enabled);

                for (int i = 0; i < getChildCount(); i++) {
                    TestTreeNode node = (TestTreeNode) getChildAt(i);
                    node.setEnabled(enabled);
                    model.nodeChanged(node);
                }

                return;
            }

            test.setEnabled(enabled);

            if (enabled) {
                TestTreeNode node = (TestTreeNode) getParent();
                node.getGroup().setEnabled(true);
                model.nodeChanged(node);
            }
        }
    }

    private class TestTreeNodeRender extends JCheckBox implements TreeCellRenderer {
        private static final long serialVersionUID = 1L;

        private final Color textSelectionColor;
        private final Color textNonSelectionColor;
        private final Color backgroundSelectionColor;
        private final Color backgroundNonSelectionColor;

        public TestTreeNodeRender() {
            textSelectionColor = DefaultLookup.getColor(this, ui, "Tree.selectionForeground");
            textNonSelectionColor = DefaultLookup.getColor(this, ui, "Tree.textForeground");
            backgroundSelectionColor = DefaultLookup.getColor(this, ui, "Tree.selectionBackground");
            backgroundNonSelectionColor = DefaultLookup.getColor(this, ui, "Tree.textBackground");

            Insets margins = DefaultLookup.getInsets(this, ui, "Tree.rendererMargins");
            if (margins != null) {
                setBorder(new EmptyBorder(margins.top, margins.left, margins.bottom, margins.right));
            }
        }

        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
                boolean leaf, int row, boolean hasFocus) {

            if (sel) {
                setForeground(textSelectionColor);
                setBackground(backgroundSelectionColor);
            } else {
                setForeground(textNonSelectionColor);
                setBackground(backgroundNonSelectionColor);
            }

            if (value instanceof TestTreeNode) {
                TestTreeNode node = (TestTreeNode) value;
                setText(node.getName());
                setSelected(node.isEnabled());
                setToolTipText(node.getDescription());
            } else {
                setText(value == null ? "" : value.toString());
                setToolTipText(null);
            }

            return this;
        }

        public void validate() {
        }

        public void invalidate() {
        }

        public void revalidate() {
        }

        public void repaint(long tm, int x, int y, int width, int height) {
        }

        public void repaint(Rectangle r) {
        }

        public void repaint() {
        }
    }

    private class TestTreeNodeEditor extends AbstractCellEditor implements TreeCellEditor {
        private static final long serialVersionUID = 1L;

        private TestTreeNodeRender lastObject;

        @Override
        public Object getCellEditorValue() {
            return lastObject;
        }

        @Override
        public Component getTreeCellEditorComponent(JTree tree, final Object value, boolean isSelected,
                boolean expanded, boolean leaf, int row) {

            lastObject = new TestTreeNodeRender();
            lastObject.getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, false);
            lastObject.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent itemEvent) {
                    ((TestTreeNode) value).setEnabled(((JCheckBox) itemEvent.getItem()).isSelected());
                    fireEditingStopped();
                }
            });

            return lastObject;
        }
    }

    private class TckRunDialog extends JDialog {
        private static final long serialVersionUID = 1L;

        private final TckTask task;

        public TckRunDialog() {
            super(owner, "TCK");

            createGUI();

            task = new TckTask(this, runner);
            task.execute();
        }

        private void createGUI() {
            setPreferredSize(new Dimension(500, 200));
            setMinimumSize(new Dimension(500, 200));

            setLayout(new BorderLayout());

            JPanel progressPanel = new JPanel();
            progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));
            progressPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            progressPanel.add(Box.createRigidArea(new Dimension(0, 10)));

            JLabel groupsLabel = new JLabel("Groups:");
            groupsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            progressPanel.add(groupsLabel);

            groupsProgressBar = new JProgressBar();
            groupsProgressBar.setMinimumSize(new Dimension(500, 30));
            groupsProgressBar.setPreferredSize(new Dimension(500, 30));
            groupsProgressBar.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
            groupsProgressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
            groupsProgressBar.setAlignmentY(Component.CENTER_ALIGNMENT);
            progressPanel.add(groupsProgressBar);

            progressPanel.add(Box.createRigidArea(new Dimension(0, 10)));

            JLabel testsLabel = new JLabel("Tests:");
            testsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            progressPanel.add(testsLabel);

            testsProgressBar = new JProgressBar();
            testsProgressBar.setMinimumSize(new Dimension(500, 30));
            testsProgressBar.setPreferredSize(new Dimension(500, 30));
            testsProgressBar.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
            testsProgressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
            testsProgressBar.setAlignmentY(Component.CENTER_ALIGNMENT);
            progressPanel.add(testsProgressBar);

            progressPanel.add(Box.createRigidArea(new Dimension(0, 10)));

            add(progressPanel, BorderLayout.CENTER);

            JButton cancelButton = new JButton("Cancel");
            cancelButton.setDefaultCapable(true);
            cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    task.cancel(true);
                }
            });

            add(cancelButton, BorderLayout.PAGE_END);

            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            pack();
            setLocationRelativeTo(null);
            setVisible(true);
        }
    }

    private static class TckDialogRunner extends AbstractRunner {
        public TckDialogRunner(ClientModel model, TckDialog tckDialog) {
            Map<String, String> parameters = new HashMap<String, String>(model.getClientSession()
                    .getSessionParameters());
            parameters.put(SessionParameter.REPOSITORY_ID, model.getClientSession().getSession().getRepositoryInfo()
                    .getId());

            setParameters(parameters);
        }
    }

    private class DialogProgressMonitor implements CmisTestProgressMonitor {

        public DialogProgressMonitor(int numberOfGroups) {
            groupsProgressBar.setStringPainted(true);
            groupsProgressBar.setMinimum(0);
            groupsProgressBar.setMaximum(numberOfGroups);
            groupsProgressBar.setValue(0);
        }

        public void startGroup(CmisTestGroup group) {
            groupsProgressBar.setString(group.getName());

            testsProgressBar.setStringPainted(true);
            testsProgressBar.setMinimum(0);
            testsProgressBar.setMaximum(group.getTests().size());
            testsProgressBar.setValue(0);
        }

        public void endGroup(CmisTestGroup group) {
            groupsProgressBar.setString("");
            groupsProgressBar.setValue(groupsProgressBar.getValue() + 1);
        }

        public void startTest(CmisTest test) {
            testsProgressBar.setString(test.getName());
        }

        public void endTest(CmisTest test) {
            testsProgressBar.setString("");
            testsProgressBar.setValue(testsProgressBar.getValue() + 1);
        }

        public void message(String msg) {
        }
    }

    class TckTask extends SwingWorker<Void, Void> {
        private final JDialog dialog;
        private final TckDialogRunner runner;

        public TckTask(JDialog dialog, TckDialogRunner runner) {
            this.dialog = dialog;
            this.runner = runner;
        }

        @Override
        public Void doInBackground() {
            try {
                runner.run(new DialogProgressMonitor(runner.getGroups().size()));
            } catch (InterruptedException ie) {
                runner.cancel();
            } catch (Exception e) {
                JOptionPane
                        .showMessageDialog(owner, "Error: " + e.getMessage(), "TCK Error", JOptionPane.ERROR_MESSAGE);
            }

            return null;
        }

        @Override
        public void done() {
            try {
                SwingReport report = new SwingReport(null, 700, 500);
                report.createReport(runner.getParameters(), runner.getGroups(), (Writer) null);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(owner, "Error: " + e.getMessage(), "Report Error",
                        JOptionPane.ERROR_MESSAGE);
            } finally {
                dialog.dispose();
            }
        }
    }
}
