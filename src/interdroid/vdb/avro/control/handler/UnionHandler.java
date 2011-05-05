package interdroid.vdb.avro.control.handler;

import interdroid.vdb.avro.model.AvroRecordModel;

import java.util.HashMap;
import java.util.Map;

import org.apache.avro.Schema;

import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;

public class UnionHandler implements OnCheckedChangeListener {

	private final AvroRecordModel mDataModel;
	private final Map<RadioButton, Schema> buttonData = new HashMap<RadioButton, Schema>();
	private final ValueHandler mValueHandler;
	private final Map<RadioButton, ValueHandler>mHandlers = new HashMap<RadioButton, ValueHandler>();

	public UnionHandler(AvroRecordModel dataModel, ValueHandler valueHandler) {
		mDataModel = dataModel;
		mValueHandler = valueHandler;
	}

	public void addType(RadioButton radioButton, Schema innerType) {
		buttonData.put(radioButton, innerType);
		radioButton.setOnCheckedChangeListener(this);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// Uncheck the other radio buttons in the group.
		if (isChecked) {
			for (RadioButton button : buttonData.keySet()) {
				if (button != buttonView) {
					button.setChecked(false);
				}
			}
		}
		// Set the value based on this button
		ValueHandler innerHandler = mHandlers.get(buttonView);
		mValueHandler.setValue(innerHandler.getValue());
		mDataModel.notifyChanged();
	}

	public ValueHandler getHandler(final RadioButton radioButton) {
		ValueHandler handler = new ValueHandler() {

			Object mValue;

			@Override
			public Object getValue() {
				return mValue;
			}

			@Override
			public void setValue(Object value) {
				mValue = value;
				onCheckedChanged(radioButton, true);
			}
		};
		mHandlers.put(radioButton, handler);

		return handler;
	}

}
