package insilico.core.exception;

/**
 * Generic Exception thrown when a wrong matrix type is requested
 * 
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class MatrixNotSupportedException extends Exception {

	public MatrixNotSupportedException() {
		super("A wrong matrix type has been requested");
	}

	public MatrixNotSupportedException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public MatrixNotSupportedException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public MatrixNotSupportedException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
