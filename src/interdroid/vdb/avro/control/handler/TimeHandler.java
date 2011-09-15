package interdroid.vdb.avro.control.handler;

import java.util.Calendar;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TimePicker;

public class TimeHandler implements OnClickListener {

	private TimePicker mView;
	private ValueHandler mValueHandler;

	public TimeHandler(TimePicker view, ValueHandler valueHandler) {
		mView = view;
		mValueHandler = valueHandler;
		// Set the initial value
		Calendar value = Calendar.getInstance();
		value.setTimeInMillis((Long)mValueHandler.getValue() / 1000L);
		mView.setCurrentHour(value.get(Calendar.HOUR_OF_DAY));
		mView.setCurrentMinute(value.get(Calendar.MINUTE));
	}

	@Override
	public void onClick(View v) {
		// Update the model
		Calendar value = Calendar.getInstance();
		value.set(Calendar.HOUR_OF_DAY, mView.getCurrentHour());
		value.set(Calendar.MINUTE, mView.getCurrentMinute());
		mValueHandler.setValue(value.getTimeInMillis()/1000);
	}

}
