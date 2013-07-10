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
package org.apache.chemistry.opencmis.commons.impl.tube;

import org.apache.chemistry.opencmis.commons.impl.tube.client.WssMUTube;
import org.apache.chemistry.opencmis.commons.impl.tube.server.WssTube;

import com.sun.xml.ws.api.pipe.ClientTubeAssemblerContext;
import com.sun.xml.ws.api.pipe.ServerTubeAssemblerContext;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubelineAssembler;

/**
 * Assembler for OpenCMIS client and server tubelines.
 */
public class OpenCMISTubeAssembler implements TubelineAssembler {

    public Tube createClient(ClientTubeAssemblerContext context) {
        Tube head = context.createTransportTube();
        head = context.createSecurityTube(head);
        head = context.createWsaTube(head);
        head = new WssMUTube(context.getBinding(), head);

        return context.createHandlerTube(head);
    }

    public Tube createServer(ServerTubeAssemblerContext context) {
        Tube head = context.getTerminalTube();
        head = new WssTube(head);
        head = context.createHandlerTube(head);
        head = context.createMonitoringTube(head);
        head = context.createServerMUTube(head);
        head = context.createWsaTube(head);
        head = context.createSecurityTube(head);

        return head;
    }
}
