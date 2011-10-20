package interdroid.vdb.avro.control.handler.value;

import android.net.Uri;
import interdroid.vdb.avro.model.AvroRecordModel;
import interdroid.vdb.avro.model.NotBoundException;
import interdroid.vdb.avro.model.UriArray;

/**
 * The ValueHandler for a single value in an array.
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
public class ArrayValueHandler implements ValueHandler {

	/** The array with the data. */
	private final UriArray<Object>mArray;
	/** The offset we are handling. */
	private final int mOffset;
	/** The model we are working for. */
	private final AvroRecordModel mDataModel;
	/** The field with the array. */
	private final String mField;

	/**
	 * Construct an ArrayValueHandler.
	 * @param model the model to work in
	 * @param field the field with the array
	 * @param array the array with the data
	 * @param offset the offset into the array
	 */
	public ArrayValueHandler(final AvroRecordModel model, final String field,
			final UriArray<Object> array, final int offset) {
		mArray = array;
		mOffset = offset;
		mDataModel = model;
		mField = field;
	}

	@Override
	public final Object getValue() {
		return mArray.get(mOffset);
	}

	@Override
	public final void setValue(final Object value) {
		mArray.set(mOffset, value);
		mDataModel.onChanged();
	}

	@Override
	public final String toString() {
		return "ArrayValueHandler: " + mArray + "[" + mOffset + "]";
	}

	@Override
	public final Uri getValueUri() throws NotBoundException {
		return mArray.getInstanceUri();
	}

	@Override
	public final String getFieldName() {
		return mField;
	}

}
