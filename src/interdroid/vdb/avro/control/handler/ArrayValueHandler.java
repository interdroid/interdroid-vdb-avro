package interdroid.vdb.avro.control.handler;

import android.net.Uri;
import interdroid.vdb.avro.model.AvroRecordModel;
import interdroid.vdb.avro.model.UriArray;

public class ArrayValueHandler implements ValueHandler {

    private final UriArray<Object>mArray;
    private final int mOffset;
    private AvroRecordModel mDataModel;

    public ArrayValueHandler(AvroRecordModel model, UriArray<Object> array, int offset) {
        mArray = array;
        mOffset = offset;
        mDataModel = model;
    }

    @Override
    public Object getValue() {
        return mArray.get(mOffset);
    }

    @Override
    public void setValue(Object value) {
        mArray.set(mOffset, value);
        mDataModel.onChanged();
    }

    public String toString() {
        return "ArrayValueHandler: " + mArray + "[" + mOffset + "]";
    }

    @Override
    public Uri getValueUri(Uri uri) {
        return uri;
    }

}
