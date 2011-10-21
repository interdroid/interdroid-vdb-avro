package interdroid.vdb.avro.model;

/**
 * Exception which is thrown when an operation is attempted on a
 * bound data type that is not yet bound to a URI.
 *
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
public class NotBoundException extends Exception {

	/**
	 * Construct a new NotBoundException.
	 */
	public NotBoundException() {
		super();
	}

	/**
	 * Construct a new NotBoundException.
	 * @param message the message for the exception
	 */
	public NotBoundException(final String message) {
		super(message);
	}

	/**
	 * the version id for serialization.
	 */
	private static final long	serialVersionUID	= 1L;

}
