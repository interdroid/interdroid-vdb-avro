package interdroid.vdb.avro.control.handler;

import org.apache.avro.generic.GenericData.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecordValueHandler implements ValueHandler {
	private static final Logger logger = LoggerFactory.getLogger(RecordValueHandler.class)
	;
	private final Record mRecord;
	private final String mFieldName;

	public RecordValueHandler(Record record, String fieldName) {
		mFieldName = fieldName;
		mRecord = record;
		logger.debug("Constructed for: " + mRecord + "[" + fieldName + "]");
	}

	@Override
	public Object getValue() {
		return mRecord.get(mFieldName);
	}

	@Override
	public void setValue(Object value) {
		mRecord.put(mFieldName, value);
	}

	public String toString() {
		return "RecordValueHandler: " + mRecord + "[" + mFieldName +"]";
	}

}
