package interdroid.vdb.avro.control.handler.value;

import interdroid.vdb.avro.model.AvroRecordModel;
import interdroid.vdb.avro.model.NotBoundException;
import interdroid.vdb.avro.model.UriRecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.net.Uri;

/**
 * Handles a field in a record.
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
public class RecordValueHandler implements ValueHandler {
	/** Access to logger. */
	private static final Logger LOG =
			LoggerFactory.getLogger(RecordValueHandler.class);

	/** The record we are handling. */
	private final UriRecord mRecord;
	/** The name of the field we are handling. */
	private final String mFieldName;
	/** The data model for the record. */
	private final AvroRecordModel mDataModel;

	/**
	 * Construct a handler for record fields.
	 * @param model the model to work with
	 * @param record the record we are representing
	 * @param fieldName the name of the field being managed
	 */
	public RecordValueHandler(final AvroRecordModel model,
			final UriRecord record, final String fieldName) {
		mFieldName = fieldName;
		mRecord = record;
		mDataModel = model;
		LOG.debug("Constructed for: " + mRecord + "[" + fieldName + "]");
	}

	@Override
	public final Object getValue() {
		return mRecord.get(mFieldName);
	}

	@Override
	public final void setValue(final Object value) {
		LOG.debug("Record Value Handler Setting {} to {}", mFieldName, value);
		mRecord.put(mFieldName, value);
		mDataModel.onChanged();
	}

	@Override
	public final String toString() {
		return "RecordValueHandler: " + mRecord + "[" + mFieldName + "]";
	}

	@Override
	public final Uri getValueUri() throws NotBoundException {
		return mRecord.getInstanceUri();
	}

	@Override
	public final String getFieldName() {
		return mFieldName;
	}

}
