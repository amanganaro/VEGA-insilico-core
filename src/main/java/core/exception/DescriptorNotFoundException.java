package core.exception;

/**
 * Default class for descriptor not found exception 
 * 
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class DescriptorNotFoundException extends Exception {
    
    private static final long serialVersionUID = 1L;

    
    public DescriptorNotFoundException() {
            // TODO Auto-generated constructor stub
    }

    public DescriptorNotFoundException(String message) {
            super(message);
            // TODO Auto-generated constructor stub
    }

    public DescriptorNotFoundException(Throwable cause) {
            super(cause);
            // TODO Auto-generated constructor stub
    }

    public DescriptorNotFoundException(String message, Throwable cause) {
            super(message, cause);
            // TODO Auto-generated constructor stub
    }

}
