package interdroid.vdb.avro.control.handler.value;

import interdroid.vdb.avro.model.NotBoundException;
import android.net.Uri;

/**
 * This interface serves as a common API for the handling of values.
 *
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
public interface ValueHandler {
	/**
	 * @return the current value being held
	 */
	Object getValue();

	/**
	 * Sets the value being handled.
	 * @param value the value to set to
	 */
	void setValue(Object value);

	/**
	 * @return The uri for this value
	 * @throws NotBoundException if the record model is not bound
	 */
	Uri getValueUri() throws NotBoundException;

	/**
	 * @return the name of the field being managed.
	 */
	String getFieldName();
}
