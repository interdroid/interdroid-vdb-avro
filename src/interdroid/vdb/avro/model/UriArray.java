package interdroid.vdb.avro.model;

import interdroid.vdb.avro.model.UriBoundAdapter.UriBoundAdapterImpl;
import interdroid.vdb.content.EntityUriMatcher;
import interdroid.vdb.content.EntityUriMatcher.UriMatch;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.GenericData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;


public class UriArray<A> extends GenericData.Array<A> implements UriBound<UriArray<A>> {
    private static final Logger logger = LoggerFactory
            .getLogger(UriArray.class);

    private UriBoundAdapter<UriArray<A>> mUriBinder;

    private static final int DEFAULT_ARRAY_SIZE = 10;

    private UriBoundAdapterImpl<UriArray<A>> mBinderImpl =
            new UriBoundAdapterImpl<UriArray<A>>() {

        @Override
        public void saveImpl(ContentResolver resolver, String fieldName) throws NotBoundException {
            logger.debug("Saving array: {} : {}", getInstanceUri(), fieldName);

            deleteImpl(resolver, false);

            ContentValues values = new ContentValues();
            for (Object value : UriArray.this) {
                values.clear();
                // First insert a null row
                Uri idUri = UriDataManager.insertUri(resolver, getInstanceUri(), values);
                logger.debug("Got id uri for array row: " + idUri);
                Uri dataUri = UriDataManager.storeDataToUri(resolver, idUri, values, fieldName,
                        getSchema().getElementType(), value);
                if (dataUri != null) {
                    UriMatch match = EntityUriMatcher.getMatch(dataUri);
                    values.put(fieldName, match.entityIdentifier);
                }
                UriDataManager.updateUriOrThrow(resolver, idUri, values);
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public UriArray<A> loadImpl(ContentResolver resolver, String fieldName) throws NotBoundException {
            logger.debug("Loading array from uri: " + getInstanceUri() + " : " + getSchema());
            Cursor cursor = resolver.query(getInstanceUri(), null, null, null, null);
            try {
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        add((A)UriDataManager.loadDataFromUri(resolver, getInstanceUri(), cursor, fieldName, getSchema().getElementType()));
                    }
                } else {
                    throw new RuntimeException("Unable to load: " + getInstanceUri());
                }
            } finally {
                UriDataManager.safeClose(cursor);
            }
            return UriArray.this;
        }

        @Override
        public void deleteImpl(ContentResolver resolver) throws NotBoundException {
            deleteImpl(resolver, true);
        }

        public void deleteImpl(ContentResolver resolver, boolean recursion)
                throws NotBoundException {
            logger.debug("Deleting Array: " + getInstanceUri());
            if (recursion) {
                logger.debug("Handling recursive delete of array.");
                if (UriBoundAdapter.isBoundType(getSchema().getElementType().getType())) {
                    for (Object element : UriArray.this) {
                        if (element != null) {
                            ((UriBound) element).delete(resolver);
                        }
                    }
                } else if(getSchema().getElementType().getType() == Type.UNION) {
                    for (Object element : UriArray.this) {
                        ((UriUnion) element).delete(resolver);
                    }
                }
            }
            resolver.delete(getInstanceUri(), null, null);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void saveImpl(Bundle outState, String fieldFullName) throws NotBoundException {
            logger.debug("Storing to bundle: " + outState + " field: " + fieldFullName);
            switch (getSchema().getElementType().getType()) {
            case ARRAY: {
                outState.putInt(NameHelper.getCountName(fieldFullName), (int) size());
                Schema subSchema = getSchema().getElementType();
                int i = 0;
                for (Object element : UriArray.this) {
                    ((UriArray<?>) element).save(outState, NameHelper.getIndexedFieldName(fieldFullName, i++));
                }
                break;
            }
            case BOOLEAN: {
                boolean[] bools = new boolean[(int) size()];
                int i = 0;
                for (Object element : UriArray.this) {
                    bools[i++] = (Boolean) element;
                }
                outState.putBooleanArray(fieldFullName, bools);
                break;
            }
            case BYTES: {
                outState.putInt(NameHelper.getCountName(fieldFullName), (int) size());
                int i = 0;
                for (Object element : UriArray.this) {
                    outState.putByteArray(NameHelper.getIndexedFieldName(fieldFullName, i++),
                            (byte[]) element);
                }
                break;
            }
            case DOUBLE: {
                double[] doubles = new double[(int) size()];
                int i = 0;
                for (Object element : UriArray.this) {
                    doubles[i++] = (Double) element;
                }
                outState.putDoubleArray(fieldFullName, doubles);
                break;
            }
            case ENUM: {
                String[] enums = new String[(int) size()];
                int i = 0;
                for (Object element : UriArray.this) {
                    enums[i++] = (String) element;
                }
                outState.putStringArray(fieldFullName, enums);
                break;
            }
            case FIXED: {
                outState.putInt(NameHelper.getCountName(fieldFullName), (int) size());
                int i = 0;
                for (Object element : UriArray.this) {
                    outState.putByteArray(NameHelper.getIndexedFieldName(fieldFullName, i++),
                            (byte[]) element);
                }
                break;
            }
            case FLOAT: {
                float[] floats = new float[(int) size()];
                int i = 0;
                for (Object element : UriArray.this) {
                    floats[i++] = (Float) element;
                }
                outState.putFloatArray(fieldFullName, floats);
                break;
            }
            case INT: {
                int[] ints = new int[(int) size()];
                int i = 0;
                for (Object element : UriArray.this) {
                    ints[i++] = (Integer) element;
                }
                outState.putIntArray(fieldFullName, ints);
                break;
            }
            case LONG: {
                long[] longs = new long[(int) size()];
                int i = 0;
                for (Object element : UriArray.this) {
                    longs[i++] = (Long) element;
                }
                outState.putLongArray(fieldFullName, longs);
                break;
            }
            case MAP: {
                outState.putInt(NameHelper.getCountName(fieldFullName), (int) size());
                Schema subSchema = getSchema().getElementType();
                int i = 0;
                for (Object element : UriArray.this) {
                    ((UriMap<?,?>) element).save(outState,
                            NameHelper.getIndexedFieldName(fieldFullName, i++));
                }
                break;
            }
            case NULL: {
                outState.putInt(NameHelper.getCountName(fieldFullName), (int) size());
                break;
            }
            case RECORD: {
                outState.putInt(NameHelper.getCountName(fieldFullName), (int) size());
                int i = 0;
                for (Object element : UriArray.this) {
                   ((UriRecord) element).save(outState, fieldFullName);
                }
                break;
            }
            case STRING: {
                String[] strings = new String[(int) size()];
                int i = 0;
                for (Object element : UriArray.this) {
                    strings[i++] = (String) element;
                }
                outState.putStringArray(fieldFullName, strings);
                break;
            }
            case UNION: {
                outState.putInt(NameHelper.getCountName(fieldFullName), (int) size());
                int i = 0;
                for (Object element : UriArray.this) {
                    ((UriUnion) element).save(outState,
                            NameHelper.getIndexedFieldName(fieldFullName, i++));
                }
                break;
            }
            default:
                throw new RuntimeException("Unsupported array type: "
                        + getSchema());
            }
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public UriArray<A> loadImpl(Bundle saved, String fieldName) throws NotBoundException {
            switch (getSchema().getElementType().getType()) {
            case ARRAY: {
                int count = saved.getInt(NameHelper.getCountName(fieldName));
                for (int i = 0; i < count; i++) {
                    add((A) new UriArray<A>(getInstanceUri(),
                            getSchema().getElementType()).load(saved, NameHelper.getIndexedFieldName(fieldName, i)));
                }
                break;
            }
            case BOOLEAN: {
                boolean[] savedData = saved.getBooleanArray(fieldName);
                if (savedData != null) {
                    for (boolean value : savedData) {
                        add((A) Boolean.valueOf(value));
                    }
                }
                break;
            }
            case BYTES: {
                int count = saved.getInt(NameHelper.getCountName(fieldName));
                for (int i = 0; i < count; i++) {
                    byte[] data = saved.getByteArray(NameHelper.getIndexedFieldName(
                            fieldName, i));
                    add((A) data);
                }
                break;
            }
            case DOUBLE: {
                double[] savedData = saved.getDoubleArray(fieldName);
                if (savedData != null) {
                    for (double value : savedData) {
                        add((A) Double.valueOf(value));
                    }
                }
                break;
            }
            case ENUM: {
                String[] savedData = saved.getStringArray(fieldName);
                if (savedData != null) {
                    for (String value : savedData) {
                        add((A) value);
                    }
                }
                break;
            }
            case FIXED: {
                int count = saved.getInt(NameHelper.getCountName(fieldName));
                for (int i = 0; i < count; i++) {
                    byte[] data = saved.getByteArray(NameHelper.getIndexedFieldName(
                            fieldName, i));
                    add((A) data);
                }
                break;
            }
            case FLOAT: {
                float[] savedData = saved.getFloatArray(fieldName);
                if (savedData != null) {
                    for (float value : savedData) {
                        add((A) Float.valueOf(value));
                    }
                }
                break;
            }
            case INT: {
                int[] savedData = saved.getIntArray(fieldName);
                if (savedData != null) {
                    for (int value : savedData) {
                        add((A) Integer.valueOf(value));
                    }
                }
                break;
            }
            case LONG: {
                long[] savedData = saved.getLongArray(fieldName);
                if (savedData != null) {
                    for (long value : savedData) {
                        add((A) Long.valueOf(value));
                    }
                }
                break;
            }
            case MAP: {
                int count = saved.getInt(NameHelper.getCountName(fieldName));
                for (int i = 0; i < count; i++) {
                    add((A) new UriMap(getInstanceUri(), getSchema().getElementType()).load(saved, NameHelper.getIndexedFieldName(fieldName, i)));
                }
                break;
            }
            case NULL: {
                int count = saved.getInt(NameHelper.getCountName(fieldName));
                for (int i = 0; i < count; i++) {
                    add(null);
                }
                break;
            }
            case RECORD: {
                int count = saved.getInt(NameHelper.getCountName(fieldName));
                for (int i = 0; i < count; i++) {
                    add((A) new UriRecord(getInstanceUri(), getSchema().getElementType()).load(saved, NameHelper.getIndexedFieldName(fieldName, i)));
                }
                break;
            }
            case STRING: {
                String[] savedData = saved.getStringArray(fieldName);
                if (savedData != null) {
                    for (String value : savedData) {
                        add((A) value);
                    }
                }
                break;
            }
            case UNION: {
                int count = saved.getInt(NameHelper.getCountName(fieldName));
                for (int i = 0; i < count; i++) {
                    add((A) new UriUnion(getSchema().getElementType()).load(saved,
                            NameHelper.getIndexedFieldName(fieldName, i)));
                }
                break;
            }
            default:
                throw new RuntimeException("Unsupported array type: "
                        + getSchema());
            }

            return UriArray.this;
        }

    };

    public UriArray(final Schema schema, Bundle saved) {
        super(DEFAULT_ARRAY_SIZE, schema);
        mUriBinder = new UriBoundAdapter<UriArray<A>>(saved, mBinderImpl);
    }

    public UriArray(Uri uri, final Schema schema) {
        super(DEFAULT_ARRAY_SIZE, schema);
        logger.debug("UriArray built and bound to: {}", uri);
        mUriBinder = new UriBoundAdapter<UriArray<A>>(uri, mBinderImpl);
    }

    @Override
    public Uri getInstanceUri() throws NotBoundException {
        return mUriBinder.getInstanceUri();
    }

    @Override
    public void setInstanceUri(Uri uri) {
        mUriBinder.setInstanceUri(uri);
    }

    @Override
    public void save(ContentResolver resolver, String fieldName)
            throws NotBoundException {
        mUriBinder.save(resolver, fieldName);
    }

    @Override
    public UriArray<A> load(ContentResolver resolver, String fieldName)
            throws NotBoundException {
        return mUriBinder.load(resolver, fieldName);
    }

    @Override
    public void save(Bundle outState, String prefix) throws NotBoundException {
        mUriBinder.save(outState, prefix);
    }

    @Override
    public UriArray<A> load(Bundle b, String prefix) throws NotBoundException{
        return mUriBinder.load(b, prefix);
    }

    @Override
    public void delete(ContentResolver resolver) throws NotBoundException {
        mUriBinder.delete(resolver);
    }

}
