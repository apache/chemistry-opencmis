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
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.tck.CmisTest;
import org.apache.chemistry.opencmis.tck.CmisTestGroup;
import org.apache.chemistry.opencmis.tck.CmisTestProgressMonitor;
import org.apache.chemistry.opencmis.tck.report.HtmlReport;
import org.apache.chemistry.opencmis.tck.runner.AbstractRunner;
import org.apache.chemistry.opencmis.workbench.model.ClientModel;

/**
 * TCK dialog and runner.
 */
public class TckDialog {

    private final Frame owner;
    private final TckDialogRunner runner;

    private JProgressBar groupsProgressBar;
    private JProgressBar testsProgressBar;

    public TckDialog(Frame owner, ClientModel model) {
        this.owner = owner;
        this.runner = new TckDialogRunner(model);

        try {
            runner.loadDefaultTckGroups();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(owner, "Error: " + e.getMessage(), "TCK Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // JOptionPane.showMessageDialog(owner,
        // "The TCK is brand new and incomplete. Don't trust the results, yet!",
        // "TCK Warning", JOptionPane.WARNING_MESSAGE);

        new TckSelectDialog();
    }

    private class TckSelectDialog extends JDialog {

        private static final long serialVersionUID = 1L;

        public TckSelectDialog() {
            super(owner, "TCK", true);

            createGUI();
        }

        private void createGUI() {
            setPreferredSize(new Dimension(300, 500));
            setMinimumSize(new Dimension(300, 500));

            setLayout(new BorderLayout());

            DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Groups");

            for (CmisTestGroup group : runner.getGroups()) {
                DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(group.getName());
                rootNode.add(groupNode);
                for (CmisTest test : group.getTests()) {
                    DefaultMutableTreeNode testNode = new DefaultMutableTreeNode(test.getName());
                    groupNode.add(testNode);
                }
            }

            JTree groupTree = new JTree(rootNode);
            groupTree.setRootVisible(false);

            for (int i = 0; i < groupTree.getRowCount(); i++) {
                groupTree.expandRow(i);
            }

            add(groupTree, BorderLayout.CENTER);

            JButton runButton = new JButton("Run TCK");
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
                        dispose();
                        new TckRunDialog();
                    }
                }
            });

            add(runButton, BorderLayout.PAGE_END);

            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            pack();
            setLocationRelativeTo(null);
            setVisible(true);
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
        public TckDialogRunner(ClientModel model) {
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
                // create report
                File tempReportFile = File.createTempFile("cmistck", ".html");
                tempReportFile.deleteOnExit();

                HtmlReport report = new HtmlReport();
                report.createReport(runner.getParameters(), runner.getGroups(), tempReportFile);

                // show report
                Desktop desktop = Desktop.getDesktop();
                if (!desktop.isSupported(Desktop.Action.OPEN)) {
                    JOptionPane.showMessageDialog(owner, "Report: " + tempReportFile.getAbsolutePath(), "Report",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    desktop.open(tempReportFile);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(owner, "Error: " + e.getMessage(), "Report Error",
                        JOptionPane.ERROR_MESSAGE);
            } finally {
                dialog.dispose();
            }
        }
    }
}
