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
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.UIDefaults;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

public class WorkbenchScale {
    public static final String WORKBENCH_SCALE = "cmis.workbench.scale";

    private static boolean scale = false;
    private static Float scaleFactor = null;

    static {
        String scaleStr = System.getProperty(WORKBENCH_SCALE);
        if (scaleStr != null) {
            try {
                scaleFactor = Float.parseFloat(scaleStr.trim());
                scale = true;
            } catch (Exception e) {
                // ignore
            }
        }
    }

    public static boolean isScaling() {
        return scale;
    }

    public static float getScaleFactor() {
        return (scaleFactor == null ? 1.0f : (float) scaleFactor);
    }

    public static int scaleInt(int x) {
        if (scale) {
            return (int) (x * getScaleFactor());
        } else {
            return x;
        }
    }

    public static Font scaleFont(Font font) {
        if (scale) {
            return font.deriveFont(font.getSize() * getScaleFactor());
        } else {
            return font;
        }
    }

    public static Insets scaleInsets(Insets insets) {
        if (scale) {
            return new Insets(scaleInt(insets.top), scaleInt(insets.left), scaleInt(insets.bottom),
                    scaleInt(insets.right));
        } else {
            return insets;
        }
    }

    public static Dimension scaleDimension(Dimension dim) {
        if (scale) {
            return new Dimension(scaleInt(dim.width), scaleInt(dim.height));
        } else {
            return dim;
        }
    }

    public static ImageIcon scaleIcon(ImageIcon icon) {
        if (scale) {
            int newWidth = (int) (icon.getIconWidth() * getScaleFactor());
            int newHeight = (int) (icon.getIconHeight() * getScaleFactor());

            BufferedImage img = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(icon.getImage(), 0, 0, newWidth, newHeight, 0, 0, icon.getIconWidth(), icon.getIconHeight(),
                    null);
            g.dispose();

            return new ImageIcon(img);
        } else {
            return icon;
        }
    }

    public static class ScaledNimbusLookAndFeel extends NimbusLookAndFeel {
        private static final long serialVersionUID = 1L;

        private UIDefaults defs;
        private boolean isScaled;

        public ScaledNimbusLookAndFeel() {
            isScaled = false;
        }

        @Override
        public synchronized UIDefaults getDefaults() {
            if (isScaled) {
                return defs;
            }

            defs = super.getDefaults();

            Map<String, Object> newDefs = new HashMap<String, Object>();

            Enumeration<Object> enumeration = defs.keys();
            while (enumeration.hasMoreElements()) {
                String key = enumeration.nextElement().toString();

                Font font = defs.getFont(key);
                if (font != null) {
                    newDefs.put(key, scaleFont(font));
                }

                Dimension dim = defs.getDimension(key);
                if (dim != null) {
                    newDefs.put(key, scaleDimension(dim));
                }

                Insets insets = defs.getInsets(key);
                if (insets != null) {
                    newDefs.put(key, scaleInsets(insets));
                }
            }

            for (Map.Entry<String, Object> entry : newDefs.entrySet()) {
                defs.put(entry.getKey(), entry.getValue());
            }

            isScaled = true;

            return defs;
        }
    }
}
