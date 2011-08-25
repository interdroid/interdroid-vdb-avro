package interdroid.vdb.avro.model;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;

public class UriUnion {

    private Object mValue;
    private Schema mSchema;
    private Type mType;
    private String mName;

    public UriUnion(Schema fieldSchema) {
        mSchema = fieldSchema;
    }

    public Object getValue() {
        return mValue;
    }

    public void setValue(Object v, Schema schema) {
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
                switch (type.getType()) {
                // TODO: Fixed is a named type
                case RECORD:
                case ENUM:
                    if (type.getName().equals(mName)) {
                        return type;
                    }
                    break;
                default:
                    return type;
                }
            }
        }
        return null;
    }

    public void save(ContentValues values, String fieldName) {
        // TODO Auto-generated method stub

    }

    public UriUnion load(Cursor cursor, String fieldName) {
        // TODO Auto-generated method stub
        return this;
    }

    public void save(Bundle outState, String fieldFullName) {
        // TODO Auto-generated method stub

    }

    public UriUnion delete(ContentValues values, String fieldName) {
        // TODO Auto-generated method stub
        return null;
    }

    public UriUnion load(Bundle saved, String fieldFullName) {
        // TODO Auto-generated method stub
        return this;
    }

    public void delete(ContentResolver resolver) throws NotBoundException {
        if (mValue != null && UriBoundAdapter.isBoundType(getType())) {
            ((UriBound) mValue).delete(resolver);
        }
    };

}
