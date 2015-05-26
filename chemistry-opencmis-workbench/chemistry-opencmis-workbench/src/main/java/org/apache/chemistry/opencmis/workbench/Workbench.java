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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.net.Authenticator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.plaf.FontUIResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Workbench {

    private static final Logger LOG = LoggerFactory.getLogger(Workbench.class);

    public Workbench() {
        // set Mac OS X name
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "CMIS Workbench");

        // turn off existing Authenticators (-> Web Start)
        Authenticator.setDefault(null);

        // set up Swing
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    try {
                        boolean nimbus = false;

                        for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                            if ("Nimbus".equals(info.getName())) {
                                if (WorkbenchScale.isScaling()) {
                                    UIManager.setLookAndFeel(new WorkbenchScale.ScaledNimbusLookAndFeel());
                                } else {
                                    UIManager.setLookAndFeel(info.getClassName());
                                }
                                nimbus = true;
                                break;
                            }
                        }

                        if (!nimbus) {
                            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                        }
                    } catch (Exception e) {
                        LOG.warn("Nimbus not available: {}", e.getMessage(), e);
                    }
                }
            });
        } catch (Exception e) {
            LOG.warn("Nimbus not available: {}", e.getMessage(), e);
        }

        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);
        ClientHelper.installKeyBindings();

        // show client frame
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ClientFrame();
            }
        });
    }

    private static class Nimbus extends javax.swing.plaf.nimbus.NimbusLookAndFeel {

        private double factor = 3.0;

        @Override
        public UIDefaults getDefaults() {
            UIDefaults ret = super.getDefaults();

            Map<String, Object> newFonts = new HashMap<String, Object>();

            Enumeration<Object> e = (Enumeration<Object>) ret.keys();
            while (e.hasMoreElements()) {
                String key = e.nextElement().toString();

                Font font = ret.getFont(key);
                if (font != null) {
                    Font newFont = font.deriveFont((float) (font.getSize() * factor));
                    newFonts.put(key, newFont);
                }

                Insets insets = ret.getInsets(key);
                if (insets != null) {
                    Insets newInsets = new Insets((int) (insets.top * factor), (int) (insets.left * factor),
                            (int) (insets.bottom * factor), (int) (insets.right * factor));
                    newFonts.put(key, newInsets);
                }

                Dimension dimension = ret.getDimension(key);
                if (dimension != null) {
                    Dimension newdimension = new Dimension((int) (dimension.width * factor),
                            (int) (dimension.height * factor));
                    newFonts.put(key, newdimension);
                }

            }

            for (Map.Entry<String, Object> nf : newFonts.entrySet()) {
                ret.put(nf.getKey(), nf.getValue());
            }

            return ret;
        }

        private void scaleFont(UIDefaults defs, String fontName) {
            Font font = (Font) defs.get(fontName);
            Font newFont = font.deriveFont((float) (font.getSize() * factor));
            defs.put(fontName, newFont);
        }

    }

    public static void main(String[] args) {
        new Workbench();
    }
}
