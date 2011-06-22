package interdroid.vdb.avro.control.handler;

import interdroid.vdb.avro.model.AvroRecordModel;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class CheckboxHandler implements OnCheckedChangeListener {

	private final AvroRecordModel mDataModel;
	private final ValueHandler mValueHandler;

	public CheckboxHandler(AvroRecordModel dataModel, ValueHandler valueHandler) {
		mDataModel = dataModel;
		mValueHandler = valueHandler;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView,
			boolean isChecked) {
		mValueHandler.setValue(isChecked);
		mDataModel.onChanged();
	}

	public void setWatched(CheckBox text) {
		Object value = mValueHandler.getValue();
		if (Boolean.TRUE.equals(value)) {
			text.setChecked(true);
		} else {
			text.setChecked(false);
		}
		text.setOnCheckedChangeListener(this);
	}

}
