package interdroid.vdb.avro.model;

import interdroid.vdb.avro.model.UriBoundAdapter.UriBoundAdapterImpl;
import interdroid.vdb.content.EntityUriMatcher;
import interdroid.vdb.content.EntityUriMatcher.UriMatch;
import interdroid.vdb.content.avro.AvroContentProvider;

import java.util.HashMap;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

public class UriMap<K, V> extends HashMap<K, V> implements UriBound<UriMap<K, V>> {
    private static final Logger logger = LoggerFactory.getLogger(UriMap.class);

    private static final long serialVersionUID = 1L;
    private final Schema mSchema;

    private static final String _KEY = AvroContentProvider.KEY_COLUMN_NAME;

    public Schema getSchema() {
        return mSchema;
    }

    private UriBoundAdapter<UriMap<K,V>> mUriBinder;

    private UriBoundAdapterImpl<UriMap<K, V>> mBinderImpl =
            new UriBoundAdapterImpl<UriMap<K, V>>() {

        @SuppressWarnings("unchecked")
        @Override
        public UriMap<K, V> loadImpl(Bundle saved, String fieldFullName) {
            String keyName = NameHelper.getMapKeyName(fieldFullName);
            String valueName = NameHelper.getMapValueName(fieldFullName);

            int count = saved.getInt(NameHelper.getCountName(fieldFullName));
            for (int i = 0; i < count; i++) {
                String key = saved.getString(NameHelper.getIndexedFieldName(keyName, i));
                put((K) key, (V) BundleDataManager.loadDataFromBundle(saved, valueName, getSchema().getValueType()));
            }

            return UriMap.this;
        }

        @Override
        public void saveImpl(Bundle outState, String fieldFullName) {
            String keyName = NameHelper.getMapKeyName(fieldFullName);
            String valueName = NameHelper.getMapValueName(fieldFullName);
            outState.putParcelable(NameHelper.getTypeNameUri(fieldFullName), getInstanceUri());
            outState.putInt(NameHelper.getCountName(fieldFullName), size());
            int i = 0;
            for (K key : keySet()) {
                String keyId = NameHelper.getIndexedFieldName(keyName, i);
                String valueId = NameHelper.getIndexedFieldName(valueName, i++);

                outState.putString(keyId, (String) key);
                BundleDataManager.storeDataToBundle(outState, valueId, getSchema().getValueType(), get(key));
            }
        }

        @Override
        public void deleteImpl(ContentResolver resolver)
                throws NotBoundException {
            logger.debug("Deleting Map: " + getInstanceUri());
            if (UriBoundAdapter.isBoundType(getSchema().getValueType().getType())) {
                for (Object element : UriMap.this.values()) {
                    ((UriBound<?>) element).delete(resolver);
                }
            } else if(getSchema().getValueType().getType() == Type.UNION) {
                for (Object element : UriMap.this.values()) {
                    ((UriUnion) element).delete(resolver);
                }
            }
            resolver.delete(getInstanceUri(), null, null);
        }

        @Override
        public void saveImpl(ContentResolver resolver, String fieldName)
                throws NotBoundException {

            delete(resolver);

            ContentValues values = new ContentValues();

            for (Object key : UriMap.this.keySet()) {
                values.clear();
                values.put(_KEY, (String)key);
                // First insert a row with just the key so we can get the ID of the
                // row in
                // case the row is really an array or some other table based row
                Uri idUri = UriDataManager.insertUri(resolver, getInstanceUri(), values);
                logger.debug("Got id uri for map row: " + idUri);

                Uri dataUri = UriDataManager.storeDataToUri(resolver, idUri, values, fieldName,
                        getSchema().getValueType(), get(key));
                if (dataUri != null) {
                    UriMatch match = EntityUriMatcher.getMatch(dataUri);
                    values.put(fieldName, match.entityIdentifier);
                }
                UriDataManager.updateUriOrThrow(resolver, idUri, values);
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public UriMap<K, V> loadImpl(ContentResolver resolver,
                String fieldName) throws NotBoundException {

            logger.debug("Loading map from uri: " + getInstanceUri() + " : " + fieldName + " : " + getSchema());
            Cursor cursor = resolver.query(getInstanceUri(), null, null, null, null);
            try {
                if (cursor != null) {
                    int keyIndex = cursor
                            .getColumnIndex(AvroContentProvider.KEY_COLUMN_NAME);
                    int valueIndex = cursor
                            .getColumnIndex(fieldName);
                    while (cursor.moveToNext()) {
                        Uri dataUri = Uri.withAppendedPath(getInstanceUri(), cursor.getString(keyIndex));
                        Cursor dataCursor = cursor;
                        try {
                            if (UriBoundAdapter.isBoundType(getSchema().getValueType().getType())) {
                                int recordId = cursor.getInt(valueIndex);
                                if (recordId > 0) {
                                    dataUri = Uri.withAppendedPath(
                                            UriDataManager.getRecordUri(getInstanceUri(), getSchema().getValueType()),
                                            String.valueOf(recordId));
                                    dataCursor = resolver.query(dataUri, null, null, null, null);
                                    if (dataCursor != null) {
                                        dataCursor.moveToFirst();
                                    }
                                }
                            }
                            put((K) cursor.getString(keyIndex),
                                    (V) UriDataManager.loadDataFromUri(resolver, dataUri,
                                            dataCursor, fieldName, getSchema().getValueType()));
                        } finally {
                            if (UriBoundAdapter.isBoundType(getSchema().getValueType().getType())) {
                                UriDataManager.safeClose(dataCursor);
                            }
                        }
                    }
                } else {
                    throw new RuntimeException("Unable to load: " + getInstanceUri());
                }
            } finally {
                UriDataManager.safeClose(cursor);
            }

            return UriMap.this;
        }

    };

    public UriMap(final Schema schema, Bundle saved) {
        mSchema = schema;
        mUriBinder = new UriBoundAdapter<UriMap<K, V>>(saved, mBinderImpl);
    }

    public UriMap(Uri uri, final Schema schema) {
        mSchema = schema;
        mUriBinder = new UriBoundAdapter<UriMap<K, V>>(uri, mBinderImpl);
    }

    @Override
    public Uri getInstanceUri() {
        return mUriBinder.getInstanceUri();
    }

    @Override
    public void save(Bundle outState, String fieldName) {
        mUriBinder.save(outState, fieldName);
    }

    @Override
    public void delete(final ContentResolver resolver)
            throws NotBoundException {
        mUriBinder.delete(resolver);
    }

    @Override
    public UriMap<K, V> load(final Bundle b, final String prefix) {
        return mUriBinder.load(b, prefix);
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
    public UriMap<K, V> load(ContentResolver resolver, String fieldName)
            throws NotBoundException {
        // TODO Auto-generated method stub
        return null;
    }
}
