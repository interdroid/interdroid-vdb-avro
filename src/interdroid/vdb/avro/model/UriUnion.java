package interdroid.vdb.avro.model;

import interdroid.vdb.content.EntityUriMatcher;
import interdroid.vdb.content.EntityUriMatcher.UriMatch;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

public class UriUnion {
    private static final Logger logger = LoggerFactory
            .getLogger(UriUnion.class);

    private Object mValue;
    private Schema mSchema;
    private Type mType;
    private String mName;

    public UriUnion(Schema fieldSchema) {
        logger.debug("UriUnion constructed with schema: {} {}", fieldSchema.getType(), fieldSchema);
        if (fieldSchema.getType() != Type.UNION) {
            logger.error("Wrong type for union.");
            throw new RuntimeException();
        }
        mSchema = fieldSchema;
    }

    public Object getValue() {
        return mValue;
    }

    public void setValue(Object v, Schema schema) {
        logger.debug("Union Value set to: {} {}", v, schema);
        mValue = v;
        mType = schema.getType();
        mName = schema.getFullName();
    }

    public Type getType() {
        return mType;
    }

    public String getTypeName() {
        return mName;
    }

    public Schema getValueSchema() {
        for(Schema type : mSchema.getTypes()) {
            if (type.getType() == mType) {
                if (UriBoundAdapter.isNamedType(mType)) {
                    if (type.getName().equals(mName)) {
                        return type;
                    }
                } else {
                    return type;
                }
            }
        }
        return null;
    }

    public void save(ContentResolver resolver, Uri rootUri, ContentValues values, String fieldName) throws NotBoundException {
        values.put(NameHelper.getTypeName(fieldName), mType == null ? null : mType.toString());
        values.put(NameHelper.getTypeNameName(fieldName), mName);
        if (mValue != null) {
            UriDataManager.storeDataToUri(resolver, rootUri, values, fieldName, getTypeSchema(), mValue);
            if (UriBoundAdapter.isBoundType(mType)) {
                values.put(fieldName, getInstanceId((UriBound<?>)mValue));
            }
        } else {
            values.put(fieldName, -1);
        }
        logger.debug("Values now has: {}", values);
    }

    private int getInstanceId(UriBound<?> value) throws NotBoundException {
       Uri uri = value.getInstanceUri();
       UriMatch match = EntityUriMatcher.getMatch(uri);
       logger.debug("Instance id for: {} : {}", uri, match.entityIdentifier);
       if (match.entityIdentifier != null) {
           return Integer.parseInt(match.entityIdentifier);
       } else {
           return -1;
       }
    }

    public UriUnion load(ContentResolver resolver, Uri rootUri, Cursor cursor, String fieldName) throws NotBoundException {
        String typeName = cursor.getString(cursor.getColumnIndex(NameHelper.getTypeName(fieldName)));

        if (typeName != null) {
            mType = Type.valueOf(typeName);
            mName = cursor.getString(cursor.getColumnIndex(
                    NameHelper.getTypeNameName(fieldName)));
            mValue = UriDataManager.loadDataFromUri(resolver, rootUri, cursor, fieldName, getTypeSchema());
        }
        return this;
    }

    public void save(Bundle outState, String fieldFullName) throws NotBoundException {
        outState.putString(NameHelper.getTypeName(fieldFullName), mType == null ? null : mType.toString());
        outState.putString(NameHelper.getTypeNameName(fieldFullName), mName);
        BundleDataManager.storeDataToBundle(outState, fieldFullName, getTypeSchema(), mValue);
        if (mValue != null && UriBoundAdapter.isBoundType(mType)) {
            outState.putParcelable(fieldFullName, ((UriBound<?>)mValue).getInstanceUri());
        }
    }

    public UriUnion delete(ContentValues values, String fieldName) {
        values.putNull(NameHelper.getTypeName(fieldName));
        values.putNull(NameHelper.getTypeNameName(fieldName));
        return null;
    }

    public UriUnion load(Bundle saved, String fieldName) throws NotBoundException {
        mType = Type.valueOf(saved.getString(NameHelper.getTypeName(fieldName)));
        mName = saved.getString(NameHelper.getTypeNameName(fieldName));

        Schema fieldType = getTypeSchema();
        setValue(BundleDataManager.loadDataFromBundle(saved, fieldName, fieldType), fieldType);
        return this;
    }

    private Schema getTypeSchema() {
        Schema fieldType = null;
        if (mType == null) {
            return null;
        }
        for (Schema unionType : mSchema.getTypes()) {
            if (unionType.getType().equals(mType) &&
                    (!UriBoundAdapter.isNamedType(mType) ||
                            (mName.equals(unionType.getFullName()) || mName.equals(unionType.getName()))
                     )) {
                fieldType = unionType;
                break;
            }
        }
        if (fieldType == null) {
            throw new RuntimeException("Unable to find union inner type: "
                    + mType + " : " + mName);
        }
        return fieldType;
    }

    public final void delete(final ContentResolver resolver) throws NotBoundException {
        if (mValue != null && UriBoundAdapter.isBoundType(getType())) {
            ((UriBound<?>) mValue).delete(resolver);
        }
    };

}
