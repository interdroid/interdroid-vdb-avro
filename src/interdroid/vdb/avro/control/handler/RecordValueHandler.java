package interdroid.vdb.avro.control.handler;

import interdroid.vdb.avro.model.AvroRecordModel;
import interdroid.vdb.avro.model.NotBoundException;
import interdroid.vdb.avro.model.UriRecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.net.Uri;

public class RecordValueHandler implements ValueHandler {
	private static final Logger logger = LoggerFactory.getLogger(RecordValueHandler.class)
	;
	private final UriRecord mRecord;
	private final String mFieldName;
	private AvroRecordModel mDataModel;

	public RecordValueHandler(AvroRecordModel model, UriRecord record, String fieldName) {
		mFieldName = fieldName;
		mRecord = record;
		mDataModel = model;
		logger.debug("Constructed for: " + mRecord + "[" + fieldName + "]");
	}

	@Override
	public Object getValue() {
		return mRecord.get(mFieldName);
	}

	@Override
	public void setValue(Object value) {
		logger.debug("Record Value Handler Setting {} to {}", mFieldName, value);
		mRecord.put(mFieldName, value);
		mDataModel.onChanged();
	}

	public String toString() {
		return "RecordValueHandler: " + mRecord + "[" + mFieldName +"]";
	}

	@Override
	public Uri getValueUri() throws NotBoundException {
		return mRecord.getInstanceUri();
	}

	@Override
	public String getFieldName() {
		return mFieldName;
	}

}
