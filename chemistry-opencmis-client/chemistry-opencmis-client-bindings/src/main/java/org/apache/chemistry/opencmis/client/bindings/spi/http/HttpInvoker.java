package org.apache.chemistry.opencmis.client.bindings.spi.http;

import java.math.BigInteger;
import java.util.Map;

import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;

/**
 * HTTP Invoker Interface.
 */
public interface HttpInvoker {

    /**
     * Executes a HTTP GET request.
     */
    Response invokeGET(UrlBuilder url, BindingSession session);

    /**
     * Executes a HTTP GET request.
     */
    Response invokeGET(UrlBuilder url, BindingSession session, BigInteger offset, BigInteger length);

    /**
     * Executes a HTTP POST request.
     */
    Response invokePOST(UrlBuilder url, String contentType, Output writer, BindingSession session);

    /**
     * Executes a HTTP PUT request.
     */
    Response invokePUT(UrlBuilder url, String contentType, Map<String, String> headers, Output writer,
            BindingSession session);

    /**
     * Executes a HTTP DELETE request.
     */
    Response invokeDELETE(UrlBuilder url, BindingSession session);
}
