package org.apache.chemistry.opencmis.fileshare;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import org.apache.chemistry.opencmis.commons.api.TypeDefinition;
import org.apache.chemistry.opencmis.commons.api.server.CallContext;
import org.apache.chemistry.opencmis.commons.api.server.CmisService;
import org.apache.chemistry.opencmis.commons.impl.Converter;
import org.apache.chemistry.opencmis.commons.impl.JaxBHelper;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisTypeDefinitionType;
import org.apache.chemistry.opencmis.commons.impl.server.AbstractServiceFactory;
import org.apache.chemistry.opencmis.server.spi.AbstractServicesFactory;
import org.apache.chemistry.opencmis.server.support.CmisServiceWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileShareServiceFactory extends AbstractServiceFactory {

    private static final String PREFIX_LOGIN = "login.";
    private static final String PREFIX_REPOSITORY = "repository.";
    private static final String PREFIX_TYPE = "type.";
    private static final String SUFFIX_READWRITE = ".readwrite";
    private static final String SUFFIX_READONLY = ".readonly";

    private static final BigInteger DEFAULT_MAX_ITEMS_TYPES = BigInteger.valueOf(50);
    private static final BigInteger DEFAULT_DEPTH_TYPES = BigInteger.valueOf(-1);
    private static final BigInteger DEFAULT_MAX_ITEMS_OBJECTS = BigInteger.valueOf(200);
    private static final BigInteger DEFAULT_DEPTH_OBJECTS = BigInteger.valueOf(10);

    private static final Log log = LogFactory.getLog(AbstractServicesFactory.class);

    private RepositoryMap repositoryMap;
    private TypeManager typeManager;

    private ThreadLocal<CmisServiceWrapper<FileShareService>> threadLocalService = new ThreadLocal<CmisServiceWrapper<FileShareService>>();

    public FileShareServiceFactory() {
    }

    @Override
    public void init(Map<String, String> parameters) {
        repositoryMap = new RepositoryMap();
        typeManager = new TypeManager();

        readConfiguration(parameters);
    }

    @Override
    public void destroy() {
        threadLocalService = null;
    }

    @Override
    public CmisService getService(CallContext context) {
        repositoryMap.getAuthenticatedRepository(context, context.getRepositoryId());

        CmisServiceWrapper<FileShareService> wrapperService = threadLocalService.get();
        if (wrapperService == null) {
            wrapperService = new CmisServiceWrapper<FileShareService>(new FileShareService(repositoryMap),
                    DEFAULT_MAX_ITEMS_TYPES, DEFAULT_DEPTH_TYPES, DEFAULT_MAX_ITEMS_OBJECTS, DEFAULT_DEPTH_OBJECTS);
            threadLocalService.set(wrapperService);
        }

        wrapperService.getWrappedService().setCallContext(context);

        return wrapperService;
    }

    // ---- helpers ----

    private void readConfiguration(Map<String, String> parameters) {
        List<String> keys = new ArrayList<String>(parameters.keySet());
        Collections.sort(keys);

        for (String key : keys) {
            if (key.startsWith(PREFIX_LOGIN)) {
                // get logins
                String usernameAndPassword = replaceSystemProperties(parameters.get(key));
                if (usernameAndPassword == null) {
                    continue;
                }

                String username = usernameAndPassword;
                String password = "";

                int x = usernameAndPassword.indexOf(':');
                if (x > -1) {
                    username = usernameAndPassword.substring(0, x);
                    password = usernameAndPassword.substring(x + 1);
                }

                repositoryMap.addLogin(username, password);

                log.info("Added login '" + username + "'.");
            } else if (key.startsWith(PREFIX_TYPE)) {
                // load type definition
                TypeDefinition type = loadType(replaceSystemProperties(parameters.get(key)));
                if (type != null) {
                    typeManager.addType(type);
                }
            } else if (key.startsWith(PREFIX_REPOSITORY)) {
                // configure repositories
                String repositoryId = key.substring(PREFIX_REPOSITORY.length()).trim();
                int x = repositoryId.lastIndexOf('.');
                if (x > 0) {
                    repositoryId = repositoryId.substring(0, x);
                }

                if (repositoryId.length() == 0) {
                    throw new IllegalArgumentException("No repository id!");
                }

                if (key.endsWith(SUFFIX_READWRITE)) {
                    // read-write users
                    FileShareRepository fsr = repositoryMap.getRepository(repositoryId);
                    for (String user : split(parameters.get(key))) {
                        fsr.addUser(replaceSystemProperties(user), false);
                    }
                } else if (key.endsWith(SUFFIX_READONLY)) {
                    // read-only users
                    FileShareRepository fsr = repositoryMap.getRepository(repositoryId);
                    for (String user : split(parameters.get(key))) {
                        fsr.addUser(replaceSystemProperties(user), true);
                    }
                } else {
                    // new repository
                    String root = replaceSystemProperties(parameters.get(key));
                    FileShareRepository fsr = new FileShareRepository(repositoryId, root, typeManager);

                    repositoryMap.addRepository(fsr);

                    log.info("Added repository '" + fsr.getRepositoryId() + "': " + root);
                }
            }
        }
    }

    private List<String> split(String csl) {
        if (csl == null) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<String>();
        for (String s : csl.split(",")) {
            result.add(s.trim());
        }

        return result;
    }

    private String replaceSystemProperties(String s) {
        if (s == null) {
            return null;
        }

        StringBuilder result = new StringBuilder();
        StringBuilder property = null;
        boolean inProperty = false;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (inProperty) {
                if (c == '}') {
                    String value = System.getProperty(property.toString());
                    if (value != null) {
                        result.append(value);
                    }
                    inProperty = false;
                } else {
                    property.append(c);
                }
            } else {
                if (c == '{') {
                    property = new StringBuilder();
                    inProperty = true;
                } else {
                    result.append(c);
                }
            }
        }

        return result.toString();
    }

    @SuppressWarnings("unchecked")
    private TypeDefinition loadType(String filename) {
        TypeDefinition result = null;

        try {
            Unmarshaller u = JaxBHelper.createUnmarshaller();
            JAXBElement<CmisTypeDefinitionType> type = (JAXBElement<CmisTypeDefinitionType>) u.unmarshal(new File(
                    filename));
            result = Converter.convert(type.getValue());
        } catch (Exception e) {
            log.info("Could not load type: '" + filename + "'", e);
        }

        return result;
    }
}
