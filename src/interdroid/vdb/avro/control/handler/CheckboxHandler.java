package interdroid.vdb.avro.control.handler;

import interdroid.vdb.avro.control.handler.value.ValueHandler;
import interdroid.vdb.avro.model.AvroRecordModel;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * A handler for a boolean represented by a checkbox.
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
public class CheckboxHandler implements OnCheckedChangeListener {

	/** The model we work with. */
	private final AvroRecordModel mDataModel;
	/** The value handler for the data. */
	private final ValueHandler mValueHandler;

	/**
	 * Construct a checkbox handler.
	 * @param dataModel the model to work in
	 * @param valueHandler the value handler with the data
	 * @param text the checkbox
	 */
	public CheckboxHandler(final AvroRecordModel dataModel,
			final ValueHandler valueHandler, final CheckBox text) {
		mDataModel = dataModel;
		mValueHandler = valueHandler;
		setWatched(text);
	}

	@Override
	public final void onCheckedChanged(final CompoundButton buttonView,
			final boolean isChecked) {
		mValueHandler.setValue(isChecked);
		mDataModel.onChanged();
	}

	/**
	 * Sets the onCheckedChangeListener for the checkbox.
	 * @param text the checkbox to listen to
	 */
	private void setWatched(final CheckBox text) {
		final Object value = mValueHandler.getValue();
		if (Boolean.TRUE.equals(value)) {
			text.setChecked(true);
		} else {
			text.setChecked(false);
		}
		text.setOnCheckedChangeListener(this);
	}

}
