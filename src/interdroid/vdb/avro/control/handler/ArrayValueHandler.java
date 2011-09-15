package interdroid.vdb.avro.control.handler;

import android.net.Uri;
import interdroid.vdb.avro.model.AvroRecordModel;
import interdroid.vdb.avro.model.NotBoundException;
import interdroid.vdb.avro.model.UriArray;

public class ArrayValueHandler implements ValueHandler {

	private final UriArray<Object>mArray;
	private final int mOffset;
	private final AvroRecordModel mDataModel;
	private final String mField;

	public ArrayValueHandler(AvroRecordModel model, String field, UriArray<Object> array, int offset) {
		mArray = array;
		mOffset = offset;
		mDataModel = model;
		mField = field;
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
	public Uri getValueUri() throws NotBoundException {
		return mArray.getInstanceUri();
	}

	@Override
	public String getFieldName() {
		return mField;
	}

}
