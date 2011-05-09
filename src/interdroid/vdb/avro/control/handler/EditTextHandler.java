package interdroid.vdb.avro.control.handler;

import org.apache.avro.Schema.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import interdroid.vdb.avro.model.AvroRecordModel;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class EditTextHandler implements TextWatcher {
	private static final Logger logger = LoggerFactory.getLogger(EditTextHandler.class);

	private final AvroRecordModel mDataModel;
	private final Type mType;
	private final ValueHandler mValueHandler;

	public EditTextHandler(AvroRecordModel dataModel, Type type, ValueHandler valueHandler) {
		mDataModel = dataModel;
		mType = type;
		mValueHandler = valueHandler;
	}

	public Object getValue() {
		return mValueHandler.getValue();
	}

	@Override
	public void afterTextChanged(Editable s) {
		if (s.length() == 0) {
			mValueHandler.setValue(null);
		} else {
			switch (mType) {
			case FLOAT:
				mValueHandler.setValue(Float.valueOf(s.toString()));
				break;
			case INT:
				mValueHandler.setValue(Integer.valueOf(s.toString()));
				break;
			case LONG:
				mValueHandler.setValue(Long.valueOf(s.toString()));
				break;
			case NULL:
				mValueHandler.setValue(null);
				break;
			case STRING:
				mValueHandler.setValue(s.toString());
				break;
			default:
				throw new RuntimeException("Unsupported type: " + mType);
			}
		}
		mDataModel.notifyChanged();
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// Do Nothing
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before,
			int count) {
		// Do Nothing
	}

	public String toString() {
		return "EditTextHandler: " + mType + " : " + mValueHandler;
	}


	public void setWatched(EditText text) {

		if (getValue() != null) {
			logger.debug("Setting value: " + getValue() + " for: " + this);
			text.setText(String.valueOf(getValue()));
		} else {
			logger.debug("Text watcher has null value: " + this);
		}

		// Must come after the setText.
		text.addTextChangedListener(this);

	}
}
