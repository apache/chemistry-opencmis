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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.Format;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.workbench.PropertyEditorFrame.UpdateStatus.StatusFlag;
import org.apache.chemistry.opencmis.workbench.model.ClientModel;

/**
 * Simple property editor.
 */
public class PropertyEditorFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final String WINDOW_TITLE = "Property Editor";

    private final ClientModel model;
    private final CmisObject object;
    private List<PropertyInputPanel> propertyPanels;

    public PropertyEditorFrame(ClientModel model, CmisObject object) {
        super();

        this.model = model;
        this.object = object;

        createGUI();
    }

    private void createGUI() {
        setTitle(WINDOW_TITLE);
        setPreferredSize(new Dimension(600, 600));
        setMinimumSize(new Dimension(200, 60));

        setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        JScrollPane scrollPane = new JScrollPane(panel);
        add(scrollPane, BorderLayout.CENTER);

        propertyPanels = new ArrayList<PropertyEditorFrame.PropertyInputPanel>();

        for (PropertyDefinition<?> propDef : object.getType().getPropertyDefinitions().values()) {
            boolean isUpdatable = (propDef.getUpdatability() == Updatability.READWRITE)
                    || (propDef.getUpdatability() == Updatability.WHENCHECKEDOUT && object.getAllowableActions()
                            .getAllowableActions().contains(Action.CAN_CHECK_IN));

            if (isUpdatable) {
                PropertyInputPanel propertyPanel = new PropertyInputPanel(propDef, object.getPropertyValue(propDef
                        .getId()));

                propertyPanels.add(propertyPanel);
                panel.add(propertyPanel);
            }
        }

        JButton updateButton = new JButton("Update");
        updateButton.setDefaultCapable(true);
        updateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (doUpdate()) {
                    dispose();
                }
            }
        });

        add(updateButton, BorderLayout.PAGE_END);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Performs the update.
     */
    private boolean doUpdate() {
        try {
            Map<String, Object> properties = new HashMap<String, Object>();
            for (PropertyInputPanel propertyPanel : propertyPanels) {
                if (propertyPanel.includeInUpdate()) {
                    properties.put(propertyPanel.getId(), propertyPanel.getValue());
                }
            }

            if (properties.isEmpty()) {
                return false;
            }

            ObjectId newId = object.updateProperties(properties, false);

            if (newId != null) {
                if (newId.getId().equals(model.getCurrentObject().getId())) {
                    try {
                        model.reloadObject();
                        model.reloadFolder();
                    } catch (Exception ex) {
                        ClientHelper.showError(null, ex);
                    }
                }
            }

            return true;
        } catch (Exception ex) {
            ClientHelper.showError(this, ex);
            return false;
        }
    }

    public interface UpdateStatus {
        enum StatusFlag {
            DontChange, Update, Unset
        }

        void setStatus(StatusFlag status);

        StatusFlag getStatus();
    }

    /**
     * Property input panel.
     */
    public static class PropertyInputPanel extends JPanel implements UpdateStatus {
        private static final long serialVersionUID = 1L;

        private final PropertyDefinition<?> propDef;
        private final Object value;
        private JComboBox changeBox;
        private List<JComponent> valueComponents;

        public PropertyInputPanel(PropertyDefinition<?> propDef, Object value) {
            super();
            this.propDef = propDef;
            this.value = value;
            createGUI();
        }

        protected void createGUI() {
            setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

            setBackground(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            Font labelFont = UIManager.getFont("Label.font");
            Font boldFont = labelFont.deriveFont(Font.BOLD, labelFont.getSize2D() * 1.2f);

            JPanel titlePanel = new JPanel();
            titlePanel.setLayout(new BorderLayout());
            titlePanel.setBackground(Color.WHITE);
            titlePanel.setToolTipText("<html><b>" + propDef.getPropertyType().value() + "</b> ("
                    + propDef.getCardinality().value() + " value)"
                    + (propDef.getDescription() != null ? "<br>" + propDef.getDescription() : ""));
            add(titlePanel);

            JLabel label = new JLabel(propDef.getDisplayName() + " (" + propDef.getId() + ")");
            label.setFont(boldFont);
            titlePanel.add(label, BorderLayout.LINE_START);

            changeBox = new JComboBox(new Object[] { "Don't change     ", "Update    ", "Unset     " });
            titlePanel.add(changeBox, BorderLayout.LINE_END);

            valueComponents = new ArrayList<JComponent>();
            if (propDef.getCardinality() == Cardinality.SINGLE) {
                JComponent valueField = createInputField(value);
                valueComponents.add(valueField);
                add(valueField);
            } else {
                if (value instanceof List<?>) {
                    for (Object v : (List<?>) value) {
                        JComponent valueField = createInputField(v);
                        valueComponents.add(valueField);
                        add(valueField);
                    }
                }
            }

            add(new JSeparator(SwingConstants.HORIZONTAL));

            setMaximumSize(new Dimension(Short.MAX_VALUE, getPreferredSize().height));
        }

        protected JComponent createInputField(Object value) {
            switch (propDef.getPropertyType()) {
            case INTEGER:
                return new IntegerPropertyInputField(value, this);
            case DECIMAL:
                return new DecimalPropertyInputField(value, this);
            case DATETIME:
                return new DateTimePropertyInputField(value, this);
            case BOOLEAN:
                return new BooleanPropertyInputField(value, this);
            default:
                return new StringPropertyInputField(value, this);
            }
        }

        public String getId() {
            return propDef.getId();
        }

        public Object getValue() {
            Object result = null;

            if (getStatus() == StatusFlag.Update) {
                try {
                    if (propDef.getCardinality() == Cardinality.SINGLE) {
                        result = ((PropertyValue) valueComponents.get(0)).getPropertyValue();
                    } else {
                        List<Object> list = new ArrayList<Object>();
                        for (JComponent comp : valueComponents) {
                            list.add(((PropertyValue) comp).getPropertyValue());
                        }
                        result = list;
                    }
                } catch (Exception ex) {
                    ClientHelper.showError(this, ex);
                }
            }

            return result;
        }

        public boolean includeInUpdate() {
            return getStatus() != StatusFlag.DontChange;
        }

        public void setStatus(StatusFlag status) {
            switch (status) {
            case Update:
                changeBox.setSelectedIndex(1);
                break;
            case Unset:
                changeBox.setSelectedIndex(2);
                break;
            default:
                changeBox.setSelectedIndex(0);
            }
        }

        public StatusFlag getStatus() {
            switch (changeBox.getSelectedIndex()) {
            case 1:
                return StatusFlag.Update;
            case 2:
                return StatusFlag.Unset;
            default:
                return StatusFlag.DontChange;
            }
        }
    }

    /**
     * Property value interface.
     */
    public interface PropertyValue {
        Object getPropertyValue() throws Exception;
    }

    /**
     * String property.
     */
    public static class StringPropertyInputField extends JTextField implements PropertyValue {
        private static final long serialVersionUID = 1L;

        public StringPropertyInputField(final Object value, final UpdateStatus status) {
            super(value == null ? "" : value.toString());

            addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent e) {
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    status.setStatus(StatusFlag.Update);
                }

                @Override
                public void keyPressed(KeyEvent e) {
                }
            });
        }

        public Object getPropertyValue() {
            return getText();
        }
    }

    /**
     * Formatted property.
     */
    public static class AbstractFormattedPropertyInputField extends JFormattedTextField implements PropertyValue {
        private static final long serialVersionUID = 1L;

        public AbstractFormattedPropertyInputField(final Object value, final Format format, final UpdateStatus status) {
            super(format);
            if (value != null) {
                setValue(value);
            }

            addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent e) {
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    status.setStatus(StatusFlag.Update);
                }

                @Override
                public void keyPressed(KeyEvent e) {
                }
            });
        }

        public Object getPropertyValue() throws Exception {
            commitEdit();
            return getValue();
        }
    }

    /**
     * Integer property.
     */
    public static class IntegerPropertyInputField extends AbstractFormattedPropertyInputField {
        private static final long serialVersionUID = 1L;

        public IntegerPropertyInputField(final Object value, final UpdateStatus status) {
            super(value, NumberFormat.getIntegerInstance(), status);
            setHorizontalAlignment(JTextField.RIGHT);
        }
    }

    /**
     * Decimal property.
     */
    public static class DecimalPropertyInputField extends AbstractFormattedPropertyInputField {
        private static final long serialVersionUID = 1L;

        public DecimalPropertyInputField(final Object value, final UpdateStatus status) {
            super(value, NumberFormat.getInstance(), status);
            setHorizontalAlignment(JTextField.RIGHT);
        }
    }

    /**
     * Boolean property.
     */
    public static class BooleanPropertyInputField extends JComboBox implements PropertyValue {
        private static final long serialVersionUID = 1L;

        public BooleanPropertyInputField(final Object value, final UpdateStatus status) {
            super(new Object[] { true, false });
            setSelectedItem(value == null ? true : value);

            addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    status.setStatus(StatusFlag.Update);
                }
            });
        }

        public Object getPropertyValue() {
            return getSelectedItem();
        }
    }

    /**
     * DateTime property.
     */
    public static class DateTimePropertyInputField extends JPanel implements PropertyValue {
        private static final long serialVersionUID = 1L;

        private static final String[] MONTH_STRINGS;

        static {
            String[] months = new java.text.DateFormatSymbols().getMonths();
            int lastIndex = months.length - 1;

            if (months[lastIndex] == null || months[lastIndex].length() <= 0) {
                String[] monthStrings = new String[lastIndex];
                System.arraycopy(months, 0, monthStrings, 0, lastIndex);
                MONTH_STRINGS = monthStrings;
            } else {
                MONTH_STRINGS = months;
            }
        }

        private SpinnerNumberModel day;
        private SpinnerListModel month;
        private SpinnerNumberModel year;
        private SpinnerNumberModel hour;
        private SpinnerNumberModel min;
        private SpinnerNumberModel sec;

        public DateTimePropertyInputField(final Object value, final UpdateStatus status) {
            setBackground(Color.WHITE);

            GregorianCalendar cal = (value == null ? new GregorianCalendar() : (GregorianCalendar) value);

            day = new SpinnerNumberModel(cal.get(Calendar.DATE), 1, 31, 1);
            addSpinner(new JSpinner(day), status);

            month = new SpinnerListModel(MONTH_STRINGS);
            month.setValue(MONTH_STRINGS[cal.get(Calendar.MONTH)]);
            JSpinner monthSpinner = new JSpinner(month);
            JComponent editor = monthSpinner.getEditor();
            if (editor instanceof JSpinner.DefaultEditor) {
                JFormattedTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
                tf.setColumns(6);
                tf.setHorizontalAlignment(JTextField.RIGHT);
            }
            addSpinner(monthSpinner, status);

            year = new SpinnerNumberModel(cal.get(Calendar.YEAR), 0, 9999, 1);
            JSpinner yearSpinner = new JSpinner(year);
            yearSpinner.setEditor(new JSpinner.NumberEditor(yearSpinner, "#"));
            editor = yearSpinner.getEditor();
            addSpinner(yearSpinner, status);

            add(new JLabel("  "));

            hour = new SpinnerNumberModel(cal.get(Calendar.HOUR_OF_DAY), 0, 23, 1);
            addSpinner(new JSpinner(hour), status);

            add(new JLabel(":"));

            min = new SpinnerNumberModel(cal.get(Calendar.MINUTE), 0, 59, 1);
            addSpinner(new JSpinner(min), status);

            add(new JLabel(":"));

            sec = new SpinnerNumberModel(cal.get(Calendar.SECOND), 0, 59, 1);
            addSpinner(new JSpinner(sec), status);
        }

        private void addSpinner(final JSpinner spinner, final UpdateStatus status) {
            spinner.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    status.setStatus(StatusFlag.Update);
                }
            });

            add(spinner);
        }

        public Object getPropertyValue() {
            GregorianCalendar result = new GregorianCalendar();

            result.set(Calendar.YEAR, year.getNumber().intValue());
            int mi = 0;
            String ms = month.getValue().toString();
            for (int i = 0; i < MONTH_STRINGS.length; i++) {
                if (MONTH_STRINGS[i].equals(ms)) {
                    mi = i;
                    break;
                }
            }
            result.set(Calendar.MONTH, mi);
            result.set(Calendar.DATE, day.getNumber().intValue());
            result.set(Calendar.HOUR_OF_DAY, hour.getNumber().intValue());
            result.set(Calendar.MINUTE, min.getNumber().intValue());
            result.set(Calendar.SECOND, sec.getNumber().intValue());

            return result;
        }
    }
}
