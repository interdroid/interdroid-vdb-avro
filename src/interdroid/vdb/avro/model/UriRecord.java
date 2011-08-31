package interdroid.vdb.avro.model;

import interdroid.vdb.avro.model.UriBoundAdapter.UriBoundAdapterImpl;
import interdroid.vdb.content.EntityUriMatcher;
import interdroid.vdb.content.EntityUriMatcher.UriMatch;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.GenericData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

public class UriRecord extends GenericData.Record implements UriBound<UriRecord> {
    private static final Logger logger = LoggerFactory
            .getLogger(UriRecord.class);

    private UriBoundAdapter<UriRecord> mUriBinder;

    private UriBoundAdapterImpl<UriRecord> mBinderImpl =
            new UriBoundAdapterImpl<UriRecord>() {

        @Override
        public void saveImpl(ContentResolver resolver, String fieldFullName) throws NotBoundException {
            ContentValues values = new ContentValues();

            for (Field field : getSchema().getFields()) {
                String fieldName = field.name();
                // Store the data to either the values or the right table
                Uri dataUri = UriDataManager.storeDataToUri(resolver,
                        getInstanceUri(), values, field.name(), field.schema(),
                        get(fieldName));
                // Update our reference if this is a record
                if (field.schema().getType() == Type.RECORD
                        && dataUri != null) {
                    UriMatch match = EntityUriMatcher.getMatch(dataUri);
                    values.put(fieldName, match.entityIdentifier);
                }
            }
            // Now we can update the data for this record.
            UriDataManager.updateUriOrThrow(resolver, getInstanceUri(), values);
        }

        @Override
        public UriRecord loadImpl(ContentResolver resolver, String fullFieldName) throws NotBoundException {
            logger.debug("Loading record from uri: {} : {}",
                    getInstanceUri(), getSchema());

            Cursor cursor = resolver.query(getInstanceUri(),
                    null, null, null, null);

            try {
                logger.debug("Cursor is: {}", cursor);
                if (cursor != null && cursor.getCount() == 1) {
                    cursor.moveToFirst();

                    for (Field field : getSchema().getFields()) {
                        String fieldName = field.name();
                        // Load the data for this field
                        Object value = UriDataManager.loadDataFromUri(resolver,
                                getInstanceUri(), cursor, fieldName,
                                field.schema());
                        logger.debug("Loaded: {} : {}", fieldName, value);
                        // And store it in the record
                        put(fieldName, value);
                    }
                }
            } finally {
                UriDataManager.safeClose(cursor);
            }
            return UriRecord.this;
        }

        @Override
        public void deleteImpl(ContentResolver resolver)
                throws NotBoundException {
            logger.debug("Deleting Record: {}", getInstanceUri());

            // TODO: The fields here may not reflect what we
            // really need to do to delete if this is not
            // loaded to match the DB. For now we
            // assume this is a clean record but we really
            // should have loaded and dirty flags all through
            // the model. This is dangerous but good enough for
            // now.

            for (Field field : getSchema().getFields()) {
                String fieldName = field.name();
                if (UriBoundAdapter.isBoundType(field.schema().getType())) {
                    UriBound<?> data = (UriBound<?>) get(fieldName);
                    if (data != null) {
                        data.delete(resolver);
                    }
                }
            }

            resolver.delete(getInstanceUri(), null, null);
        }

        @Override
        public void saveImpl(Bundle outState, String prefix) throws NotBoundException {
            logger.debug("Saving record to bundle: {} : {}", prefix, getSchema().getFullName());
            String dataFullName = NameHelper.getPrefixName(prefix, getSchema()
                    .getFullName());

            outState.putParcelable(NameHelper.getTypeNameUri(dataFullName),
                    getInstanceUri());

            for (Field field : getSchema().getFields()) {
                String fieldName = field.name();
                String fieldFullName =
                        NameHelper.getFieldFullName(dataFullName, fieldName);
                BundleDataManager.storeDataToBundle(outState,
                        fieldFullName, field.schema(), get(fieldName));
            }
        }

        @Override
        public UriRecord loadImpl(Bundle saved, String prefix) throws NotBoundException {
            logger.debug(
                    "Loading data from bundle: {} : {}",
                    prefix, getSchema().getFullName());
            String dataFullName =
                    NameHelper.getPrefixName(prefix, getSchema().getFullName());

            for (Field field : getSchema().getFields()) {
                String fieldName = field.name();
                logger.debug("Loading field: " + fieldName);
                String fieldFullName = NameHelper.getFieldFullName(dataFullName, fieldName);
                put(fieldName, BundleDataManager.loadDataFromBundle(saved, fieldFullName, field.schema()));
            }

            return UriRecord.this;
        }

    };

    public UriRecord(final Schema schema, Bundle saved) {
        super(schema);
        mUriBinder = new UriBoundAdapter<UriRecord>(saved, mBinderImpl);
    }

    public UriRecord(Uri uri, final Schema schema) {
        super(schema);
        logger.debug("UriRecord built and bound to: {}", uri);
        mUriBinder = new UriBoundAdapter<UriRecord>(uri, mBinderImpl);
    }

    @Override
    public Uri getInstanceUri() throws NotBoundException {
        return mUriBinder.getInstanceUri();
    }

    @Override
    public void setInstanceUri(Uri uri) {
        logger.debug("UriRecord now bound to: {}", uri);
        mUriBinder.setInstanceUri(uri);
    }

    @Override
    public void save(ContentResolver resolver, String fieldName)
            throws NotBoundException {
        mUriBinder.save(resolver, fieldName);
    }

    @Override
    public UriRecord load(ContentResolver resolver, String fieldName)
            throws NotBoundException {
        return mUriBinder.load(resolver, fieldName);
    }

    @Override
    public void save(Bundle outState, String prefix) throws NotBoundException {
        mUriBinder.save(outState, prefix);
    }

    @Override
    public UriRecord load(Bundle b, String prefix) throws NotBoundException {
        return mUriBinder.load(b, prefix);
    }

    @Override
    public void delete(ContentResolver resolver) throws NotBoundException {
        mUriBinder.delete(resolver);
    }

    public UriRecord load(Bundle savedInstanceState) throws NotBoundException {
        return mUriBinder.load(savedInstanceState, null);
    }

    public void save(ContentResolver resolver) throws NotBoundException {
        mUriBinder.save(resolver, null);
    }

    public UriRecord load(ContentResolver resolver) throws NotBoundException {
        return mUriBinder.load(resolver, null);
    }

    public void save(Bundle outState) throws NotBoundException {
        mUriBinder.save(outState, null);
    }


}
