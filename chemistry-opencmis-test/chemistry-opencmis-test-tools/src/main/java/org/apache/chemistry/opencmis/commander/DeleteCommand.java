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

import org.apache.chemistry.opencmis.commons.spi.CmisBinding;

public class DeleteCommand implements Command {

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.opencmis.commander.Command#getCommandName()
     */
    public String getCommandName() {
        return "delete";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.opencmis.commander.Command#getUsage()
     */
    public String getUsage() {
        return "DELETE <repository id> <object id> [all versions: true/false]";
    }

    public void execute(CmisBinding binding, String[] args, PrintWriter output) {
        if (args.length < 2) {
            output.println(getUsage());
            return;
        }

        String repositoryId = args[0];
        String objectId = args[1];
        Boolean allVersions = (args.length > 2 ? Boolean.valueOf(args[2]) : null);

        output.println("Deleting " + objectId + " ...");
        output.flush();

        binding.getObjectService().deleteObject(repositoryId, objectId, allVersions, null);

        output.println("done.");
    }
}
