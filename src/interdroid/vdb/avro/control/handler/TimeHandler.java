package interdroid.vdb.avro.control.handler;

import interdroid.vdb.avro.view.DataFormatUtil;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

public class TimeHandler implements OnTimeChangedListener {

	private static final Logger logger = LoggerFactory
			.getLogger(TimeHandler.class);

	private TimePicker mView;
	private ValueHandler mValueHandler;

	public TimeHandler(TimePicker view, ValueHandler valueHandler) {
		mView = view;
		mValueHandler = valueHandler;
		// Set the initial value
		Calendar value = Calendar.getInstance();
		try {
			Date d = DataFormatUtil.getTimeAsDate((Long) mValueHandler.getValue());
			value.set(1970, Calendar.JANUARY, 1, d.getHours(), d.getMinutes(), 0);
//			value.setTimeZone(TimeZone.getDefault());
		} catch (ParseException e) {
			logger.error("Error parsing time! Defaulting to now.", e);
		}
		logger.debug("Initializing time to: {} {}", value.get(Calendar.HOUR_OF_DAY), value.get(Calendar.MINUTE));
		mView.setCurrentHour(value.get(Calendar.HOUR_OF_DAY));
		mView.setCurrentMinute(value.get(Calendar.MINUTE));
		mView.setOnTimeChangedListener(this);
	}

	@Override
	public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
		logger.debug("Updating time: {} {}", hourOfDay, minute);
		// Update the model
		Calendar value = Calendar.getInstance();
		value.set(1970, Calendar.JANUARY, 1, hourOfDay, minute, 0);
		long format = DataFormatUtil.formatTimeForStorage(value);
		logger.debug("Setting time to: {}", format);
		mValueHandler.setValue(format);
	}

}
