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
package org.apache.chemistry.opencmis.commons.impl.tube.server;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.chemistry.opencmis.commons.impl.tube.AbstractWssTube;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.message.Headers;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractFilterTubeImpl;

public class WssTube extends AbstractWssTube {

    public WssTube(Tube next) {
        super(next);
    }

    protected WssTube(AbstractFilterTubeImpl that, TubeCloner cloner) {
        super(that, cloner);
    }

    public WssTube copy(TubeCloner cloner) {
        return new WssTube(this, cloner);
    }

    @Override
    public NextAction processResponse(Packet packet) {
        Message message = packet.getMessage();
        if (message == null) {
            return super.processResponse(packet);
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            long created = System.currentTimeMillis();
            long expires = created + 24 * 60 * 60 * 1000; // 24 hours

            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

            Element wsseSecurityElement = document.createElementNS(WSSE_NAMESPACE, "Security");

            Element wsuTimestampElement = document.createElementNS(WSU_NAMESPACE, "Timestamp");
            wsseSecurityElement.appendChild(wsuTimestampElement);

            Element tsCreatedElement = document.createElementNS(WSU_NAMESPACE, "Created");
            tsCreatedElement.setTextContent(sdf.format(created));
            wsuTimestampElement.appendChild(tsCreatedElement);

            Element tsExpiresElement = document.createElementNS(WSU_NAMESPACE, "Expires");
            tsExpiresElement.setTextContent(sdf.format(expires));
            wsuTimestampElement.appendChild(tsExpiresElement);

            HeaderList headers = message.getHeaders();
            headers.add(Headers.create(wsseSecurityElement));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return super.processResponse(packet);
    }
}
