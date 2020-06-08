package core.exception;

/**
 * Generic Exception thrown when initialization of some object fails.
 * 
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class InitFailureException extends Exception {

	public InitFailureException() {
		// TODO Auto-generated constructor stub
	}

	public InitFailureException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public InitFailureException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public InitFailureException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
