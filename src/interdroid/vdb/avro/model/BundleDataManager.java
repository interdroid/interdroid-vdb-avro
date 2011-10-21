package interdroid.vdb.avro.model;

import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.os.Bundle;

/**
 * A handler for persisting models to bundles.
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
public final class BundleDataManager { // NOPMD by nick
	/** Access to logger. */
    private static final Logger LOG = LoggerFactory
            .getLogger(BundleDataManager.class);

    /**
     * No construction.
     */
    private BundleDataManager() {
    	// No construction please.
    }

    /**
     * Loads data from a bundle.
     * @param saved the bundle to load from
     * @param fieldFullName the full name of the field
     * @param fieldSchema the schema for the field
     * @return the data
     * @throws NotBoundException if the record model is not bound
     */
    @SuppressWarnings("rawtypes") // NOPMD by nick
    static Object loadDataFromBundle(final Bundle saved, // NOPMD by nick
    		final String fieldFullName, final Schema fieldSchema)
    				throws NotBoundException {
        LOG.debug("Loading data from bundle: " + fieldFullName);
        Object value;
        switch (fieldSchema.getType()) {
        case ARRAY:
            value = new UriArray(fieldSchema, saved).load(saved, fieldFullName);
            break;
        case BOOLEAN:
            value = saved.getBoolean(fieldFullName);
            break;
        case BYTES:
            value = saved.getByteArray(fieldFullName);
            break;
        case DOUBLE:
            value = saved.getDouble(fieldFullName);
            break;
        case ENUM:
            value = saved.getString(fieldFullName);
            break;
        case FIXED:
            value = saved.getByteArray(fieldFullName);
            break;
        case FLOAT:
            value = saved.getFloat(fieldFullName);
            break;
        case INT:
            value = saved.getInt(fieldFullName);
            break;
        case LONG:
            value = saved.getLong(fieldFullName);
            break;
        case MAP:
            value = new UriMap(fieldSchema, saved).load(saved, fieldFullName);
            break;
        case NULL:
            value = null; // NOPMD by nick
            break;
        case RECORD:
            value = new UriRecord(fieldSchema, saved).load(
            		saved, fieldFullName);
            break;
        case STRING:
            value = saved.getString(fieldFullName);
            break;
        case UNION:
            value = new UriUnion(fieldSchema).load(saved, fieldFullName);
            break;
        default:
            throw new IllegalArgumentException(
            		"Unsupported type: " + fieldSchema);
        }
        return value;
    }

    /**
     * Stores data to a bundle.
     * @param outState the bundle to store to
     * @param fieldFullName the full name of the field to store
     * @param fieldSchema the schema for the field
     * @param data the data to store
     * @throws NotBoundException if the data is not bound properly
     */
    @SuppressWarnings("rawtypes") // NOPMD by nick
    static void storeDataToBundle(final Bundle outState, // NOPMD by nick
    		final String fieldFullName, final Schema fieldSchema,
    		final Object data) throws NotBoundException {
        if (data != null) {
            switch (fieldSchema.getType()) {
            case ARRAY:
                final UriArray array = (UriArray) data;
                array.save(outState, fieldFullName);
                break;
            case BOOLEAN:
                outState.putBoolean(fieldFullName, (Boolean) data);
                break;
            case BYTES:
                outState.putByteArray(fieldFullName, (byte[]) data);
                break;
            case DOUBLE:
                outState.putDouble(fieldFullName, (Double) data);
                break;
            case ENUM:
                outState.putInt(fieldFullName, (Integer) data);
                break;
            case FIXED:
                outState.putByteArray(fieldFullName, (byte[]) data);
                break;
            case FLOAT:
                outState.putFloat(fieldFullName, (Float) data);
                break;
            case INT:
                outState.putInt(fieldFullName, (Integer) data);
                break;
            case LONG:
                outState.putLong(fieldFullName, (Long) data);
                break;
            case MAP:
                final UriMap<?> map = (UriMap<?>) data;
                map.save(outState, fieldFullName);
                break;
            case NULL:
                // No need to do anything.
                break;
            case RECORD:
                final UriRecord record = (UriRecord) data;
                record.save(outState, fieldFullName);
                break;
            case STRING:
                outState.putString(fieldFullName, (String) data);
                break;
            case UNION:
                final UriUnion union = (UriUnion) data;
                union.save(outState, fieldFullName);
                break;
            default:
                throw new IllegalArgumentException(
                		"Unsupported type: " + fieldSchema);
            }
        }
    }

}
