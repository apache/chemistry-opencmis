package org.apache.chemistry.opencmis.client.bindings.spi.http;

import java.io.OutputStream;

/**
 * Output interface.
 */
public interface Output {
    void write(OutputStream out) throws Exception;
}