package insilico.core.exception;

/**
 * Default class for property not found exception in a PropertyContainer object
 * 
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class PropertyNotFoundException extends Exception {
    
    private static final long serialVersionUID = 1L;

    
    public PropertyNotFoundException() {
            // TODO Auto-generated constructor stub
    }

    public PropertyNotFoundException(String message) {
            super(message);
            // TODO Auto-generated constructor stub
    }

    public PropertyNotFoundException(Throwable cause) {
            super(cause);
            // TODO Auto-generated constructor stub
    }

    public PropertyNotFoundException(String message, Throwable cause) {
            super(message, cause);
            // TODO Auto-generated constructor stub
    }

}
