package insilico.core.exception;

/**
 * Default class for descriptor having a missing value exception 
 * 
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class MissingValueException extends Exception {
    
    private static final long serialVersionUID = 1L;

    
    public MissingValueException() {
            // TODO Auto-generated constructor stub
    }

    public MissingValueException(String message) {
            super(message);
            // TODO Auto-generated constructor stub
    }

    public MissingValueException(Throwable cause) {
            super(cause);
            // TODO Auto-generated constructor stub
    }

    public MissingValueException(String message, Throwable cause) {
            super(message, cause);
            // TODO Auto-generated constructor stub
    }

}
