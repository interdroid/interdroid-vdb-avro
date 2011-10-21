package interdroid.vdb.avro.model;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;

/**
 * The interface for data that is bound to a URI.
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 * @param <A> the type which this data is bound to
 */
public interface UriBound<A> {

	/**
	 *
	 * @return the uri for this instance of the type
	 * @throws NotBoundException if it is not bound
	 */
    Uri getInstanceUri() throws NotBoundException;

    /**
     * Sets the uri this data is bound to.
     * @param uri the uri to which this data is bound
     */
    void setInstanceUri(Uri uri);

    /**
     * Saves the data in the given field to the given resolver.
     * @param resolver the resolver to use
     * @param fieldName the name of the field
     * @throws NotBoundException if this data is not bound
     */
    void save(ContentResolver resolver, String fieldName)
    		throws NotBoundException;

    /**
     * Loads the data from the resolver.
     * @param resolver the resolver to use
     * @param fieldName the field to be loaded
     * @return the value for the field
     * @throws NotBoundException if this is not bound
     */
    A load(ContentResolver resolver, String fieldName) throws NotBoundException;

    /**
     * Save the data to the bundle.
     * @param outState the bundle to store to
     * @param prefix the prefix to store with
     * @throws NotBoundException if the data is not bound
     */
    void save(Bundle outState, String prefix) throws NotBoundException;

    /**
     * Load the data from the bundle.
     * @param saved the bundle to load from
     * @param prefix the prefix the data was stored with
     * @return the value for the data
     * @throws NotBoundException if the data is not properly bound
     */
    A load(Bundle saved, String prefix) throws NotBoundException;

    /**
     * Deletes the data from the content provider.
     * @param resolver the resolver to delete with
     * @throws NotBoundException if this is not bound properly
     */
    void delete(ContentResolver  resolver) throws NotBoundException;

}
