package insilico.core.propertycontainer;

import insilico.core.exception.PropertyNotFoundException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

/**
 * Ancestor class for a generic class containing a set of properties.
 * Properties can be boolean or numerical (double), each type has its getter
 * and setter methods.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class PropertyContainer implements iPropertyContainer, Serializable, Cloneable {

    protected HashMap<String, String> TextProperties;
    protected HashMap<String, Boolean> BoolProperties;
    protected HashMap<String, Double> NumericProperties;


    public PropertyContainer() {
        TextProperties = new HashMap<>();
        BoolProperties = new HashMap<>();
        NumericProperties = new HashMap<>();
    }


    /**
     * Sets a text property given its key (name) as String.
     *
     * @param PropertyName key (name) of the property
     * @param Value value (string) to be set
     */
    @Override
    public void setTextProperty(String PropertyName, String Value) {
        TextProperties.put(PropertyName, Value);
    }


    /**
     * Gets a text property value given its key (name) as String.
     * If the property has not been set, return value is false.
     *
     * @param PropertyName key (name) of the property
     * @return value (string) of the property
     */
    @Override
    public String getTextProperty(String PropertyName) throws PropertyNotFoundException {
        String retVal =  TextProperties.get(PropertyName);
        if (retVal == null)
            throw new PropertyNotFoundException("No text property found for the key " + PropertyName);
        else
            return retVal;
    }


    /**
     * Gets all the keys (names) set in the current text properties list.
     *
     * @return set of all keys
     */
    @Override
    public Set<String> getAllTextPropertyNames() {
        return TextProperties.keySet();
    }


    /**
     * Sets a boolean property given its key (name) as String.
     *
     * @param PropertyName key (name) of the property
     * @param Value value (boolean) to be set
     */
    @Override
    public void setBoolProperty(String PropertyName, boolean Value) {
        BoolProperties.put(PropertyName, Value);
    }


    /**
     * Gets a boolean property value given its key (name) as String.
     * If the property has not been set, return value is false.
     *
     * @param PropertyName key (name) of the property
     * @return value (boolean) of the property
     */
    @Override
    public boolean getBoolProperty(String PropertyName) {
        Boolean retVal =  BoolProperties.get(PropertyName);
        if (retVal == null)
            return false;
        else
            return retVal;
    }


    /**
     * Gets all the keys (names) set in the current boolean properties list.
     *
     * @return set of all keys
     */
    @Override
    public Set<String> getAllBoolPropertyNames() {
        return BoolProperties.keySet();
    }


    /**
     * Sets a numeric property given its key (name) as String.
     *
     * @param PropertyName key (name) of the property
     * @param Value value (double) to be set
     */
    @Override
    public void setNumericProperty(String PropertyName, double Value) {
        NumericProperties.put(PropertyName, Value);
    }


    /**
     * Gets a numeric property value given its key (name) as String.
     * If the property has not been set, return value is false.
     *
     * @param PropertyName key (name) of the property
     * @return value (double) of the property
     * @throws PropertyNotFoundException
     */
    @Override
    public double getNumericProperty(String PropertyName) throws PropertyNotFoundException {
        Double retVal =  NumericProperties.get(PropertyName);
        if (retVal == null)
            throw new PropertyNotFoundException("No numeric property found for the key " + PropertyName);
        else
            return retVal;
    }


    /**
     * Gets all the keys (names) set in the current numeric properties list.
     *
     * @return set of all keys
     */
    @Override
    public Set<String> getAllNumericPropertyNames() {
        return NumericProperties.keySet();
    }
}
