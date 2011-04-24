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

import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.chemistry.opencmis.client.bindings.CmisBindingFactory;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.spi.CmisBinding;

/**
 * Commander tool main.
 *
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 *
 */
public class Commander {

    private static final Map<String, Command> COMMAND_MAP = new LinkedHashMap<String, Command>();
    static {
        addCommand(new InfosCommand());
        addCommand(new ListCommand());
        addCommand(new DeleteCommand());
    }

    private final PrintWriter fPW;

    /**
     * Constructor.
     */
    public Commander(String[] args) {
        fPW = new PrintWriter(System.out);

        if (args.length < 2) {
            printUsage(fPW);
            return;
        }

        try {
            // get the command object
            Command command = COMMAND_MAP.get(args[1].toLowerCase());
            if (command == null) {
                printUsage(fPW);
                return;
            }

            // get provider object
            CmisBinding binding = createBinding(args[0]);

            // prepare args
            String[] commandArgs = new String[args.length - 2];
            System.arraycopy(args, 2, commandArgs, 0, commandArgs.length);

            // execute
            command.execute(binding, commandArgs, fPW);
        } catch (Exception e) {
            fPW.println("Exception:");

            if (e instanceof CmisBaseException) {
                fPW.println(e);
            } else {
                e.printStackTrace(fPW);
            }
        } finally {
            fPW.flush();
        }
    }

    /**
     * Prints usage.
     */
    private static void printUsage(PrintWriter output) {
        output.println("CMIS Commander\n");
        output.println("Usage: Commander <config file> <command>\n");
        output.println("Available commands:");
        for (Command command : COMMAND_MAP.values()) {
            output.println("  " + command.getUsage());
        }

        output.flush();
    }

    /**
     * Creates the provider object
     */
    private static CmisBinding createBinding(String configFile) throws Exception {
        Properties properties = new Properties();
        properties.load(new FileInputStream(configFile));

        Map<String, String> sessionParameters = new HashMap<String, String>();

        for (Enumeration<?> e = properties.propertyNames(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            String value = properties.getProperty(key);
            sessionParameters.put(key, value);
        }

        CmisBindingFactory factory = CmisBindingFactory.newInstance();

        CmisBinding result = null;
        if (sessionParameters.containsKey(SessionParameter.ATOMPUB_URL)) {
            result = factory.createCmisAtomPubBinding(sessionParameters);
        } else if (sessionParameters.containsKey(SessionParameter.WEBSERVICES_REPOSITORY_SERVICE)) {
            result = factory.createCmisWebServicesBinding(sessionParameters);
        } else {
            throw new IllegalArgumentException("Cannot find CMIS binding information in config file!");
        }

        return result;
    }

    /**
     * Adds a command
     */
    private static final void addCommand(Command command) {
        if ((command == null) || (command.getCommandName() == null)) {
            return;
        }

        COMMAND_MAP.put(command.getCommandName().toLowerCase(), command);
    }

    /**
     * Main.
     */
    public static void main(String[] args) {
        new Commander(args);
    }
}
