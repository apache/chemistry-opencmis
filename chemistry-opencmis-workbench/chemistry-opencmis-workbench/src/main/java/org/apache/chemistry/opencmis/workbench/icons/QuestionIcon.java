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
package org.apache.chemistry.opencmis.workbench.icons;

import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;

public class QuestionIcon extends AbstractWorkbenchIcon {

    public QuestionIcon() {
        super();
    }

    public QuestionIcon(int width, int height) {
        super(width, height);
    }

    public QuestionIcon(int width, int height, boolean enabled) {
        super(width, height, enabled);
    }

    @Override
    protected int getOrginalHeight() {
        return 64;
    }

    @Override
    protected int getOrginalWidth() {
        return 64;
    }

    @Override
    protected void paint(Graphics2D g) {
        GeneralPath shape = new GeneralPath();

        shape = new GeneralPath();
        shape.moveTo(32.0, 0.0);
        shape.curveTo(14.327, 0.0, 0.0, 14.327, 0.0, 32.0);
        shape.curveTo(0.0, 49.673, 14.327, 64.0, 32.0, 64.0);
        shape.curveTo(49.673, 64.0, 64.0, 49.673, 64.0, 32.0);
        shape.curveTo(64.0, 14.327, 49.673, 0.0, 32.0, 0.0);
        shape.closePath();
        shape.moveTo(32.0, 58.0);
        shape.curveTo(17.641, 58.0, 6.0, 46.359, 6.0, 32.0);
        shape.curveTo(6.0, 17.64, 17.641, 6.0, 32.0, 6.0);
        shape.curveTo(46.359, 6.0, 58.0, 17.64, 58.0, 32.0);
        shape.curveTo(58.0, 46.359, 46.359, 58.0, 32.0, 58.0);
        shape.closePath();

        g.setPaint(getColor());
        g.fill(shape);

        shape = new GeneralPath();
        shape.moveTo(31.95612, 40.470966);
        shape.lineTo(29.742579, 40.470966);
        shape.lineTo(29.742579, 38.87591);
        shape.curveTo(29.742579, 36.53216, 29.954166, 35.23008, 30.442448, 33.83034);
        shape.curveTo(31.093489, 32.07253, 31.093489, 32.07253, 34.80443, 27.026955);
        shape.curveTo(35.797264, 25.578388, 36.39948, 23.674091, 36.39948, 21.62331);
        shape.curveTo(36.39948, 17.782164, 34.348698, 15.470966, 30.80052, 15.470966);
        shape.curveTo(28.407944, 15.470966, 26.1944, 16.675394, 26.1944, 17.977476);
        shape.curveTo(26.1944, 18.368101, 26.243229, 18.482035, 26.991928, 19.067972);
        shape.curveTo(27.952213, 19.881773, 28.456772, 20.728128, 28.456772, 21.720966);
        shape.curveTo(28.456772, 23.26719, 27.203516, 24.471617, 25.592188, 24.471617);
        shape.curveTo(23.752995, 24.471617, 22.353256, 22.827738, 22.353256, 20.630472);
        shape.curveTo(22.353256, 16.31732, 26.454819, 13.224873, 32.102604, 13.224873);
        shape.curveTo(37.799217, 13.224873, 41.65664, 16.724222, 41.65664, 21.932554);
        shape.curveTo(41.65664, 25.431904, 40.54987, 27.12461, 35.65078, 31.470314);
        shape.curveTo(32.4444, 34.220966, 31.907291, 35.571877, 31.95612, 40.470966);
        shape.closePath();
        shape.moveTo(30.751692, 44.5237);
        shape.curveTo(32.4444, 44.5237, 33.84414, 45.92344, 33.84414, 47.632423);
        shape.curveTo(33.84414, 49.37396, 32.4444, 50.7737, 30.702864, 50.7737);
        shape.curveTo(28.993881, 50.7737, 27.594141, 49.37396, 27.594141, 47.632423);
        shape.curveTo(27.594141, 45.92344, 28.993881, 44.5237, 30.751692, 44.5237);
        shape.closePath();

        g.setPaint(getColor());
        g.fill(shape);
    }
}
