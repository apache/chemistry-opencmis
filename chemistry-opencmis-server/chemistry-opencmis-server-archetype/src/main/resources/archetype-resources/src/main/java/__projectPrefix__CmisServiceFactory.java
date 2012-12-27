#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import java.math.BigInteger;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.impl.server.AbstractServiceFactory;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.server.support.CmisServiceWrapper;

/**
 * CMIS Service Factory.
 */
public class ${projectPrefix}CmisServiceFactory extends AbstractServiceFactory {

    /** Default maxItems value for getTypeChildren()}. */
    private static final BigInteger DEFAULT_MAX_ITEMS_TYPES = BigInteger.valueOf(50);

    /** Default depth value for getTypeDescendants(). */
    private static final BigInteger DEFAULT_DEPTH_TYPES = BigInteger.valueOf(-1);

    /**
     * Default maxItems value for getChildren() and other methods returning
     * lists of objects.
     */
    private static final BigInteger DEFAULT_MAX_ITEMS_OBJECTS = BigInteger.valueOf(200);

    /** Default depth value for getDescendants(). */
    private static final BigInteger DEFAULT_DEPTH_OBJECTS = BigInteger.valueOf(10);

    @Override
    public void init(Map<String, String> parameters) {
    }

    @Override
    public void destroy() {
    }

    @Override
    public CmisService getService(CallContext context) {
        // authentication can go here
        String user = context.getUsername();
        String password = context.getPassword();

        // if the authentication fails, throw a CmisPermissionDeniedException

        // create a new service object
        // (can also be pooled or stored in a ThreadLocal)
        ${projectPrefix}CmisService service = new ${projectPrefix}CmisService();

        // add the CMIS service wrapper
        // (The wrapper catches invalid CMIS requests and sets default values
        // for parameters that have not been provided by the client.)
        CmisServiceWrapper<${projectPrefix}CmisService> wrapperService = 
                new CmisServiceWrapper<${projectPrefix}CmisService>(service,
                DEFAULT_MAX_ITEMS_TYPES, DEFAULT_DEPTH_TYPES, DEFAULT_MAX_ITEMS_OBJECTS, DEFAULT_DEPTH_OBJECTS);

        // hand over the call context to the service object
        service.setCallContext(context);

        return wrapperService;
    }

}
