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
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.workbench.ClientHelper.FileEntry;
import org.apache.chemistry.opencmis.workbench.model.ClientSession;

public class LoginDialog extends JDialog {

    public static final String SYSPROP_URL = ClientSession.WORKBENCH_PREFIX + "url";
    public static final String SYSPROP_BINDING = ClientSession.WORKBENCH_PREFIX + "binding";
    public static final String SYSPROP_AUTHENTICATION = ClientSession.WORKBENCH_PREFIX + "authentication";
    public static final String SYSPROP_USER = ClientSession.WORKBENCH_PREFIX + "user";
    public static final String SYSPROP_PASSWORD = ClientSession.WORKBENCH_PREFIX + "password";

    private static final String CONFIGS_FOLDER = "/configs/";
    private static final String CONFIGS_LIBRARY = "config-library.properties";

    private static final long serialVersionUID = 1L;

    private JTabbedPane loginTabs;
    private JTextField urlField;
    private JRadioButton bindingAtomButton;
    private JRadioButton bindingWebServicesButton;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JRadioButton authenticationNoneButton;
    private JRadioButton authenticationStandardButton;
    private JRadioButton authenticationNTLMButton;
    private JTextArea sessionParameterTextArea;
    private JButton loadRepositoryButton;
    private JButton loginButton;
    private JComboBox repositoryBox;

    private List<FileEntry> sessionConfigurations;

    private boolean expertLogin = false;

    private boolean canceled = true;

    private ClientSession clientSession;

    public LoginDialog(Frame owner) {
        super(owner, "Login", true);
        createGUI();
    }

    private void createGUI() {
        setMinimumSize(new Dimension(700, 500));
        setPreferredSize(new Dimension(700, 500));

        Container pane = getContentPane();
        pane.setLayout(new BorderLayout());

        loginTabs = new JTabbedPane();
        add(loginTabs, BorderLayout.CENTER);

        // basic panel
        JPanel basicPanel = new JPanel(new SpringLayout());

        urlField = createTextField(basicPanel, "URL:");
        urlField.setText(System.getProperty(SYSPROP_URL, ""));

        createBindingButtons(basicPanel);

        usernameField = createTextField(basicPanel, "Username:");
        usernameField.setText(System.getProperty(SYSPROP_USER, ""));

        passwordField = createPasswordField(basicPanel, "Password:");
        passwordField.setText(System.getProperty(SYSPROP_PASSWORD, ""));

        createAuthenticationButtons(basicPanel);

        makeCompactGrid(basicPanel, 5, 2, 5, 10, 5, 5);

        loginTabs.addTab("Basic", basicPanel);

        // expert panel
        final JPanel expertPanel = new JPanel(new BorderLayout());
        expertPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        sessionConfigurations = ClientHelper.readFileProperties(CONFIGS_FOLDER + CONFIGS_LIBRARY, CONFIGS_FOLDER);

        final JComboBox configs = new JComboBox();
        configs.setMaximumRowCount(20);

        configs.addItem(new FileEntry("", null));
        if (sessionConfigurations != null) {
            for (FileEntry fe : sessionConfigurations) {
                configs.addItem(fe);
            }
        }
        expertPanel.add(configs, BorderLayout.PAGE_START);

        sessionParameterTextArea = new JTextArea();
        sessionParameterTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        expertPanel.add(new JScrollPane(sessionParameterTextArea), BorderLayout.CENTER);

        loginTabs.addTab("Expert", expertPanel);

        // repository
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        add(buttonPanel, BorderLayout.PAGE_END);

        loadRepositoryButton = createButton("Load Repositories");
        buttonPanel.add(loadRepositoryButton);

        createRepositoryBox(buttonPanel);

        loginButton = createButton("Login");
        buttonPanel.add(loginButton);
        loginButton.setEnabled(false);

        // listeners
        loadRepositoryButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                repositoryBox.removeAllItems();

                try {
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    createClientSession();

                    List<Repository> repositories = clientSession.getRepositories();
                    if (repositories.size() > 0) {

                        for (Repository repository : repositories) {
                            repositoryBox.addItem(repository);
                        }

                        repositoryBox.setEnabled(true);
                        loginButton.setEnabled(true);
                    } else {
                        repositoryBox.setEnabled(false);
                        loginButton.setEnabled(false);
                    }
                } catch (Exception ex) {
                    repositoryBox.setEnabled(false);
                    loginButton.setEnabled(false);

                    ClientHelper.showError(getOwner(), ex);
                } finally {
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                    clientSession.createSession(repositoryBox.getSelectedIndex());
                    canceled = false;
                    hideDialog();
                } catch (Exception ex) {
                    repositoryBox.setEnabled(false);
                    loginButton.setEnabled(false);

                    ClientHelper.showError(getOwner(), ex);

                } finally {
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });

        loginTabs.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                expertLogin = (loginTabs.getSelectedComponent() == expertPanel);

                if (expertLogin) {
                    configs.setSelectedIndex(0);
                    
                    StringBuilder sb = new StringBuilder();
                    for (Map.Entry<String, String> parameter : createBasicSessionParameters().entrySet()) {
                        sb.append(parameter.getKey());
                        sb.append("=");
                        sb.append(parameter.getValue());
                        sb.append("\n");
                    }

                    sessionParameterTextArea.setText(sb.toString());
                    sessionParameterTextArea.setCaretPosition(0);
                }
            }
        });

        configs.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                FileEntry fe = (FileEntry) e.getItem();

                sessionParameterTextArea.setText(ClientHelper.readFileAndRemoveHeader(fe.getFile()));
                sessionParameterTextArea.setCaretPosition(0);
            }
        });

        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    private JTextField createTextField(Container pane, String label) {
        JTextField textField = new JTextField(60);
        JLabel textLabel = new JLabel(label, JLabel.TRAILING);
        textLabel.setLabelFor(textField);

        pane.add(textLabel);
        pane.add(textField);

        return textField;
    }

    private JPasswordField createPasswordField(Container pane, String label) {
        JPasswordField textField = new JPasswordField(60);
        JLabel textLabel = new JLabel(label, JLabel.TRAILING);
        textLabel.setLabelFor(textField);

        pane.add(textLabel);
        pane.add(textField);

        return textField;
    }

    private void createBindingButtons(Container pane) {
        JPanel bindingContainer = new JPanel();
        bindingContainer.setLayout(new BoxLayout(bindingContainer, BoxLayout.LINE_AXIS));
        boolean atom = (System.getProperty(SYSPROP_BINDING, "atom").toLowerCase().charAt(0) == 'a');
        bindingAtomButton = new JRadioButton("AtomPub", atom);
        bindingWebServicesButton = new JRadioButton("Web Services", !atom);
        ButtonGroup bindingGroup = new ButtonGroup();
        bindingGroup.add(bindingAtomButton);
        bindingGroup.add(bindingWebServicesButton);
        bindingContainer.add(bindingAtomButton);
        bindingContainer.add(Box.createRigidArea(new Dimension(10, 0)));
        bindingContainer.add(bindingWebServicesButton);
        JLabel bindingLabel = new JLabel("Binding:", JLabel.TRAILING);

        pane.add(bindingLabel);
        pane.add(bindingContainer);
    }

    private void createAuthenticationButtons(Container pane) {
        JPanel authenticationContainer = new JPanel();
        authenticationContainer.setLayout(new BoxLayout(authenticationContainer, BoxLayout.LINE_AXIS));
        boolean standard = (System.getProperty(SYSPROP_AUTHENTICATION, "standard").toLowerCase().equals("standard"));
        boolean ntlm = (System.getProperty(SYSPROP_AUTHENTICATION, "").toLowerCase().equals("ntlm"));
        boolean none = !standard && !ntlm;
        authenticationNoneButton = new JRadioButton("None", none);
        authenticationStandardButton = new JRadioButton("Standard", standard);
        authenticationNTLMButton = new JRadioButton("NTLM", ntlm);
        ButtonGroup authenticationGroup = new ButtonGroup();
        authenticationGroup.add(authenticationNoneButton);
        authenticationGroup.add(authenticationStandardButton);
        authenticationGroup.add(authenticationNTLMButton);
        authenticationContainer.add(authenticationNoneButton);
        authenticationContainer.add(Box.createRigidArea(new Dimension(10, 0)));
        authenticationContainer.add(authenticationStandardButton);
        authenticationContainer.add(Box.createRigidArea(new Dimension(10, 0)));
        authenticationContainer.add(authenticationNTLMButton);
        JLabel authenticatioLabel = new JLabel("Authentication:", JLabel.TRAILING);

        pane.add(authenticatioLabel);
        pane.add(authenticationContainer);
    }

    private JButton createButton(String title) {
        JButton button = new JButton(title);
        button.setPreferredSize(new Dimension(Short.MAX_VALUE, 30));
        button.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);

        return button;
    }

    private void createRepositoryBox(Container pane) {
        repositoryBox = new JComboBox();
        repositoryBox.setEnabled(false);
        repositoryBox.setRenderer(new RepositoryRenderer());
        repositoryBox.setPreferredSize(new Dimension(Short.MAX_VALUE, 60));
        repositoryBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        pane.add(repositoryBox);
    }

    private SpringLayout.Constraints getConstraintsForCell(int row, int col, Container parent, int cols) {
        SpringLayout layout = (SpringLayout) parent.getLayout();
        Component c = parent.getComponent(row * cols + col);
        return layout.getConstraints(c);
    }

    private void makeCompactGrid(Container parent, int rows, int cols, int initialX, int initialY, int xPad, int yPad) {
        SpringLayout layout = (SpringLayout) parent.getLayout();

        Spring x = Spring.constant(initialX);
        for (int c = 0; c < cols; c++) {
            Spring width = Spring.constant(0);
            for (int r = 0; r < rows; r++) {
                width = Spring.max(width, getConstraintsForCell(r, c, parent, cols).getWidth());
            }
            for (int r = 0; r < rows; r++) {
                SpringLayout.Constraints constraints = getConstraintsForCell(r, c, parent, cols);
                constraints.setX(x);
                constraints.setWidth(width);
            }
            x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
        }

        Spring y = Spring.constant(initialY);
        for (int r = 0; r < rows; r++) {
            Spring height = Spring.constant(0);
            for (int c = 0; c < cols; c++) {
                height = Spring.max(height, getConstraintsForCell(r, c, parent, cols).getHeight());
            }
            for (int c = 0; c < cols; c++) {
                SpringLayout.Constraints constraints = getConstraintsForCell(r, c, parent, cols);
                constraints.setY(y);
                constraints.setHeight(height);
            }
            y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
        }

        layout.getConstraints(parent).setConstraint(SpringLayout.EAST, x);
    }

    private Map<String, String> createBasicSessionParameters() {
        String url = urlField.getText();
        BindingType binding = bindingAtomButton.isSelected() ? BindingType.ATOMPUB : BindingType.WEBSERVICES;
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        ClientSession.Authentication authentication = ClientSession.Authentication.NONE;
        if (authenticationStandardButton.isSelected()) {
            authentication = ClientSession.Authentication.STANDARD;
        } else if (authenticationNTLMButton.isSelected()) {
            authentication = ClientSession.Authentication.NTLM;
        }

        return ClientSession.createSessionParameters(url, binding, username, password, authentication);
    }

    private Map<String, String> createExpertSessionParameters() {
        Map<String, String> result = new HashMap<String, String>();

        for (String line : sessionParameterTextArea.getText().split("\n")) {
            line = line.trim();
            if (line.startsWith("#") || (line.length() == 0)) {
                continue;
            }

            int x = line.indexOf('=');
            if (x < 0) {
                result.put(line.trim(), "");
            } else {
                result.put(line.substring(0, x).trim(), line.substring(x + 1).trim());
            }
        }

        return result;
    }

    public void createClientSession() {
        if (expertLogin) {
            clientSession = new ClientSession(createExpertSessionParameters());
        } else {
            clientSession = new ClientSession(createBasicSessionParameters());
        }
    }

    public void showDialog() {
        clientSession = null;
        canceled = true;

        repositoryBox.removeAllItems();
        repositoryBox.setEnabled(false);
        loginButton.setEnabled(false);

        setLocationRelativeTo(getOwner());
        setVisible(true);
    }

    public void hideDialog() {
        setVisible(false);
    }

    public ClientSession getClientSession() {
        return clientSession;
    }

    public boolean isCanceled() {
        return canceled;
    }

    class RepositoryRenderer extends JPanel implements ListCellRenderer {
        private static final long serialVersionUID = 1L;

        private JLabel nameLabel;
        private JLabel idLabel;
        private JLabel descriptionLabel;

        public RepositoryRenderer() {
            super();
            setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
            setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

            Font labelFont = UIManager.getFont("Label.font");

            nameLabel = new JLabel();
            nameLabel.setFont(labelFont.deriveFont(Font.BOLD));
            add(nameLabel);

            idLabel = new JLabel();
            add(idLabel);

            descriptionLabel = new JLabel();
            add(descriptionLabel);
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            Repository repository = (Repository) value;

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            if (repository == null) {
                nameLabel.setText("");
                idLabel.setText("");
                descriptionLabel.setText("");
            } else {
                nameLabel.setText(repository.getName());
                idLabel.setText(repository.getId());
                descriptionLabel.setText(repository.getDescription());
            }

            return this;
        }
    }
}
