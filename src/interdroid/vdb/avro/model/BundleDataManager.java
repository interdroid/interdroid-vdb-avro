package interdroid.vdb.avro.model;

import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.os.Bundle;

public class BundleDataManager {
    private static final Logger logger = LoggerFactory
            .getLogger(BundleDataManager.class);

    @SuppressWarnings("rawtypes")
    static Object loadDataFromBundle(Bundle saved, String fieldFullName,
            Schema fieldSchema) {
        logger.debug("Loading data from bundle: " + fieldFullName);
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
            value = null;
            break;
        case RECORD:
            value = new UriRecord(fieldSchema, saved).load(saved, fieldFullName);
            break;
        case STRING:
            value = saved.getString(fieldFullName);
            break;
        case UNION:
            value = new UriUnion(fieldSchema).load(saved, fieldFullName);
            break;
        default:
            throw new RuntimeException("Unsupported type: " + fieldSchema);
        }
        return value;
    }

    @SuppressWarnings("rawtypes")
    static void storeDataToBundle(Bundle outState, String fieldFullName,
            Schema fieldSchema, Object data) {
        if (data != null) {
            switch (fieldSchema.getType()) {
            case ARRAY:
                UriArray array = (UriArray) data;
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
                UriMap<?, ?> map = (UriMap<?, ?>) data;
                map.save(outState, fieldFullName);
                break;
            case NULL:
                // No need to do anything.
                break;
            case RECORD:
                UriRecord record = (UriRecord) data;
                record.save(outState, fieldFullName);
                break;
            case STRING:
                outState.putString(fieldFullName, (String) data);
                break;
            case UNION:
                UriUnion union = (UriUnion) data;
                union.save(outState, fieldFullName);
                break;
            default:
                throw new RuntimeException("Unsupported type: " + fieldSchema);
            }
        }
    }

}
