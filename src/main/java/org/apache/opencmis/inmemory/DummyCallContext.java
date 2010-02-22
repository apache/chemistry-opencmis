package org.apache.opencmis.inmemory;

import java.util.HashMap;
import java.util.Map;

import org.apache.opencmis.server.spi.CallContext;

public class DummyCallContext implements CallContext {
  private Map<String, String> fParameter = new HashMap<String, String>();

  public DummyCallContext() {
    fParameter.put( USERNAME, "TestUser");
    fParameter.put( PASSWORD, "secret");
    fParameter.put( LOCALE, "en");
  }
  
  public String get(String key) {
    return fParameter.get(key);
  }

  public String getBinding() {
    return BINDING_ATOMPUB;
  }

  public String getLocale() {
    return get(LOCALE);
  }

  public String getPassword() {
    return get(PASSWORD);
  }

  public String getUsername() {
    return get(USERNAME);
  }

  public void put(String key, String value) {
    fParameter.put(key, value);
  }
}
