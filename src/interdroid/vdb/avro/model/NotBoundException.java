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
	 * the version id for serialization.
	 */
	private static final long	serialVersionUID	= 1L;

}
