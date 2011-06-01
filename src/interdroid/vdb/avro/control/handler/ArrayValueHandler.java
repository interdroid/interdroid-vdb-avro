package interdroid.vdb.avro.control.handler;

import org.apache.avro.generic.GenericData.Array;

public class ArrayValueHandler implements ValueHandler {
	private final Array<Object>mArray;
	private final int mOffset;

	public ArrayValueHandler(Array<Object> array, int offset) {
		mArray = array;
		mOffset = offset;
	}

	@Override
	public Object getValue() {
		return mArray.get(mOffset);
	}

	@Override
	public void setValue(Object value) {
		mArray.set(mOffset, value);
	}

	public String toString() {
		return "ArrayValueHandler: " + mArray + "[" + mOffset + "]";
	}

}
