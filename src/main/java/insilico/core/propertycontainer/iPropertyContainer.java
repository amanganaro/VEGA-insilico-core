package insilico.core.propertycontainer;

import insilico.core.exception.PropertyNotFoundException;

import java.util.Set;

/**
 * Interface for a generic class containing a set of properties.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public interface iPropertyContainer {

    public void setBoolProperty(String PropertyName, boolean Value);
    public boolean getBoolProperty(String PropertyName) throws PropertyNotFoundException;
    public Set<String> getAllBoolPropertyNames();

    public void setNumericProperty(String PropertyName, double Value);
    public double getNumericProperty(String PropertyName) throws PropertyNotFoundException;
    public Set<String> getAllNumericPropertyNames();

}
