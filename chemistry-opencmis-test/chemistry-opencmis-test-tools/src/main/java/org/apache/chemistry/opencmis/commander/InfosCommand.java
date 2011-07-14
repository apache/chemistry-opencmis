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
package org.apache.chemistry.opencmis.commander;

import java.io.PrintWriter;
import java.util.List;

import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.spi.CmisBinding;

public class InfosCommand implements Command {

    /*
     * (non-Javadoc)
     *
     * @see org.apache.opencmis.commander.Command#getCommandName()
     */
    public String getCommandName() {
        return "infos";
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.opencmis.commander.Command#getUsage()
     */
    public String getUsage() {
        return "INFOS";
    }

    public void execute(CmisBinding binding, String[] args, PrintWriter output) {
        List<RepositoryInfo> repositoryInfos = binding.getRepositoryService().getRepositoryInfos(null);

        for (RepositoryInfo repositoryInfo : repositoryInfos) {
            printRepositoryInfo(repositoryInfo, output);
        }
    }

    private static void printRepositoryInfo(RepositoryInfo repositoryInfo, PrintWriter output) {
        output.println("Id:           " + repositoryInfo.getId());
        output.println("Name:         " + repositoryInfo.getProductName());
        output.println("Description:  " + repositoryInfo.getDescription());
        output.println("Vendor:       " + repositoryInfo.getVendorName());
        output.println("Product:      " + repositoryInfo.getProductName() + " " + repositoryInfo.getProductVersion());
        output.println("Root Folder:  " + repositoryInfo.getRootFolderId());
        output.println("Capabilities: " + repositoryInfo.getCapabilities());
        output.println("------------------------------------------------------");
    }
}
