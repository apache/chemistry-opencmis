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

import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;

/**
 * Abstract Login Tab.
 * 
 * To add a new login tab, derive a class from this class, create a file
 * <code>META-INF/services/org.apache.chemistry.opencmis.workbench.AbstractLoginTab</code>
 * , and put the name of the fully qualified name of your class into this file.
 */
public abstract class AbstractLoginTab extends JPanel {

    private static final long serialVersionUID = 1L;

    /**
     * Returns the title of the tab.
     */
    public abstract String getTabTitle();

    /**
     * Returns the session parameters when the user hits the "Load Repositories"
     * button.
     */
    public abstract Map<String, String> getSessionParameters();

    /**
     * Defines if the session parameters should be transfered to the expert tab
     * when the user switches from the this tab to the expert tab. The default
     * is <code>false</code>.
     */
    public boolean transferSessionParametersToExpertTab() {
        return false;
    }

    /**
     * Called after the list of repositories has been loaded.
     */
    public void repositoriesLoaded(List<Repository> repositories) {
    }

    /**
     * Called after the Workbench session has been created.
     */
    public void loggedIn(Session session) {
    }
}
