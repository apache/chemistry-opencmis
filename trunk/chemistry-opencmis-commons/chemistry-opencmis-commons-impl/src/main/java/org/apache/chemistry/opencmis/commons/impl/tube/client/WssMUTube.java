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
package org.apache.chemistry.opencmis.commons.impl.tube.client;

import javax.xml.namespace.QName;

import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.chemistry.opencmis.commons.impl.tube.AbstractWssTube;

import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;

public class WssMUTube extends AbstractWssTube {

    private final SOAPVersion soapVersion;

    public WssMUTube(WSBinding binding, Tube next) {
        super(next);
        soapVersion = binding.getSOAPVersion();
    }

    protected WssMUTube(WssMUTube that, TubeCloner cloner) {
        super(that, cloner);
        soapVersion = that.soapVersion;
    }

    public WssMUTube copy(TubeCloner cloner) {
        return new WssMUTube(this, cloner);
    }

    @Override
    public NextAction processResponse(Packet response) {
        if (response.getMessage() == null) {
            return super.processResponse(response);
        }

        HeaderList headers = response.getMessage().getHeaders();

        for (int i = 0; i < headers.size(); i++) {
            if (!headers.isUnderstood(i)) {
                Header header = headers.get(i);
                if (!header.isIgnorable(soapVersion, soapVersion.implicitRoleSet)) {
                    QName qName = new QName(header.getNamespaceURI(), header.getLocalPart());
                    if (WSSE.equals(qName)) {
                        checkSecurityHeader(header);
                    } else {
                        throw new CmisConnectionException("MustUnderstand header is not understood: " + qName);
                    }
                }
            }
        }

        return super.processResponse(response);
    }

    private void checkSecurityHeader(Header header) {
        // TODO
    }
}
