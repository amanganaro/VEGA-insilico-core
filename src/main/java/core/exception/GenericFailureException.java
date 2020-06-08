package core.exception;

/**
 * Generic Exception thrown for a generic failure.
 * 
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class GenericFailureException extends Exception {

	public GenericFailureException() {
		// TODO Auto-generated constructor stub
	}

	public GenericFailureException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public GenericFailureException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public GenericFailureException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
