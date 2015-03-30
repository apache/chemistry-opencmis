package org.apache.chemistry.opencmis.commons.spi;

import java.util.HashMap;
import java.util.Map;

/**
 * Holder for IN/OUT parameters that can hold extra values.
 */
public class ExtendedHolder<T> extends Holder<T> {

    private Map<String, Object> extraValues = new HashMap<String, Object>();

    /**
     * Constructs a holder with a {@code null} value.
     */
    public ExtendedHolder() {
        super();
    }

    /**
     * Constructs a holder with the given value.
     */
    public ExtendedHolder(T value) {
        super(value);
    }

    /**
     * Sets an extra value.
     * 
     * @param name
     *            the name of the value
     * @param value
     *            the value
     */
    public void setExtraValue(String name, Object value) {
        extraValues.put(name, value);
    }

    /**
     * Gets an extra value,
     * 
     * @param name
     *            the name of the value
     * @return the value or {@code null} if a value with the given name doesn't
     *         exist
     */
    public Object getExtraValue(String name) {
        return extraValues.get(name);
    }

    @Override
    public String toString() {
        return "ExtendedHolder(" + getValue() + ", " + extraValues.toString() + ")";
    }
}
