package insilico.core.exception;

/**
 * Default class for database exceptions
 * 
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class DatabaseException extends Exception {
    
    private static final long serialVersionUID = 1L;

    
    public DatabaseException() {
            // TODO Auto-generated constructor stub
    }

    public DatabaseException(String message) {
            super(message);
            // TODO Auto-generated constructor stub
    }

    public DatabaseException(Throwable cause) {
            super(cause);
            // TODO Auto-generated constructor stub
    }

    public DatabaseException(String message, Throwable cause) {
            super(message, cause);
            // TODO Auto-generated constructor stub
    }

}
