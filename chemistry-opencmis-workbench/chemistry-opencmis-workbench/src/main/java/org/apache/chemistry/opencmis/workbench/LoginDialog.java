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

import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.workbench.model.ClientSession;

public class LoginDialog extends JDialog {

    public static final String SYSPROP_URL = ClientSession.WORKBENCH_PREFIX + "url";
    public static final String SYSPROP_BINDING = ClientSession.WORKBENCH_PREFIX + "binding";
    public static final String SYSPROP_AUTHENTICATION = ClientSession.WORKBENCH_PREFIX + "authentication";
    public static final String SYSPROP_USER = ClientSession.WORKBENCH_PREFIX + "user";
    public static final String SYSPROP_PASSWORD = ClientSession.WORKBENCH_PREFIX + "password";

    private static final long serialVersionUID = 1L;

    private JTextField urlField;
    private JRadioButton bindingAtomButton;
    private JRadioButton bindingWebServicesButton;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JRadioButton authenticationNoneButton;
    private JRadioButton authenticationStandardButton;
    private JRadioButton authenticationNTLMButton;
    private JButton connectButton;
    private JButton loginButton;
    private JComboBox repositoryBox;

    private boolean canceled = true;

    private ClientSession clientSession;

    public LoginDialog(Frame owner) {
        super(owner, "Login", true);
        createGUI();
    }

    private void createGUI() {
        setPreferredSize(new Dimension(520, 280));

        Container pane = getContentPane();

        pane.setLayout(new GridBagLayout());

        urlField = createTextField(pane, "URL:", 1);
        urlField.setText(System.getProperty(SYSPROP_URL, ""));

        createBindingButtons(pane, 2);

        usernameField = createTextField(pane, "Username:", 3);
        usernameField.setText(System.getProperty(SYSPROP_USER, ""));

        passwordField = createPasswordField(pane, "Password:", 4);
        passwordField.setText(System.getProperty(SYSPROP_PASSWORD, ""));

        createAuthenticationButtons(pane, 5);

        connectButton = createButton(pane, "Connect", 6);

        createRepositoryBox(pane, 7);

        loginButton = createButton(pane, "Login", 8);
        loginButton.setEnabled(false);

        connectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                repositoryBox.removeAllItems();

                try {
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    createClientSession();

                    List<Repository> repositories = clientSession.getRepositories();
                    if (repositories.size() > 0) {

                        for (Repository repository : repositories) {
                            repositoryBox.addItem(repository.getName() + " (" + repository.getId() + ")");
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
                    clientSession.createSession(repositoryBox.getSelectedIndex());
                    canceled = false;
                    hideDialog();
                } catch (Exception ex) {
                    repositoryBox.setEnabled(false);
                    loginButton.setEnabled(false);

                    ClientHelper.showError(getOwner(), ex);
                }
            }
        });

        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    private JTextField createTextField(Container pane, String label, int row) {
        JTextField textField = new JTextField(60);
        JLabel textLabel = new JLabel(label);
        textLabel.setLabelFor(textField);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.LINE_START;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = row;
        pane.add(textLabel, c);
        c.gridx = 1;
        c.ipadx = 400;
        pane.add(textField, c);

        return textField;
    }

    private JPasswordField createPasswordField(Container pane, String label, int row) {
        JPasswordField textField = new JPasswordField(60);
        JLabel textLabel = new JLabel(label);
        textLabel.setLabelFor(textField);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.LINE_START;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = row;
        pane.add(textLabel, c);
        c.gridx = 1;
        pane.add(textField, c);

        return textField;
    }

    private void createBindingButtons(Container pane, int row) {
        JPanel bindingContainer = new JPanel();
        boolean atom = (System.getProperty(SYSPROP_BINDING, "atom").toLowerCase().charAt(0) == 'a');
        bindingAtomButton = new JRadioButton("AtomPub", atom);
        bindingWebServicesButton = new JRadioButton("Web Services", !atom);
        ButtonGroup bindingGroup = new ButtonGroup();
        bindingGroup.add(bindingAtomButton);
        bindingGroup.add(bindingWebServicesButton);
        bindingContainer.add(bindingAtomButton);
        bindingContainer.add(bindingWebServicesButton);
        JLabel bindingLabel = new JLabel("Binding:");

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.LINE_START;
        c.gridx = 0;
        c.gridy = row;
        pane.add(bindingLabel, c);
        c.gridx = 1;
        pane.add(bindingContainer, c);
    }

    private void createAuthenticationButtons(Container pane, int row) {
        JPanel authenticationContainer = new JPanel();
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
        authenticationContainer.add(authenticationStandardButton);
        authenticationContainer.add(authenticationNTLMButton);
        JLabel authenticatioLabel = new JLabel("Authentication:");

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.LINE_START;
        c.gridx = 0;
        c.gridy = row;
        pane.add(authenticatioLabel, c);
        c.gridx = 1;
        pane.add(authenticationContainer, c);
    }

    private JButton createButton(Container pane, String label, int row) {
        JButton button = new JButton(label);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = row;
        pane.add(button, c);

        return button;
    }

    private void createRepositoryBox(Container pane, int row) {
        repositoryBox = new JComboBox();
        repositoryBox.setEnabled(false);
        JLabel boxLabel = new JLabel("Repository:");
        boxLabel.setLabelFor(repositoryBox);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.LINE_START;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = row;
        pane.add(boxLabel, c);
        c.gridx = 1;
        pane.add(repositoryBox, c);
    }

    public void createClientSession() {
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

        clientSession = new ClientSession(url, binding, username, password, authentication);
    }

    public void showDialog() {
        clientSession = null;
        canceled = true;

        repositoryBox.removeAllItems();
        repositoryBox.setEnabled(false);
        loginButton.setEnabled(false);

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
}
