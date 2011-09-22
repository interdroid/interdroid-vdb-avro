package interdroid.vdb.avro.control.handler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;

public class DateHandler implements OnDateChangedListener {
	private static final SimpleDateFormat DATE_FORMAT;
	static {
		DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
		DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	private static final Logger logger = LoggerFactory
			.getLogger(DateHandler.class);

	private DatePicker mView;
	private ValueHandler mValueHandler;

	public DateHandler(DatePicker view, ValueHandler valueHandler) {
		mView = view;
		mValueHandler = valueHandler;
		// Set the initial value
		Calendar value = Calendar.getInstance();
		try {
			Date d = DATE_FORMAT.parse(String.valueOf(mValueHandler.getValue()));
			value.setTime(d);
		} catch (ParseException e) {
			logger.error("Error parsing date! Defaulting to now.", e);
		}
		logger.debug("Initializing to: {} {} " + value.get(Calendar.YEAR), value.get(Calendar.MONTH), value.get(Calendar.DATE));
		mView.init(value.get(Calendar.YEAR), value.get(Calendar.MONTH), value.get(Calendar.DATE), this);
	}

	@Override
	public void onDateChanged(DatePicker arg0, int year, int month, int day) {
		logger.debug("Updating date: {} {} " + day, year, month);
		// Update the model
		Calendar value = Calendar.getInstance();
		value.set(year, month, day, 12, 0, 0);
		String parsed = DATE_FORMAT.format(value.getTime());
		logger.debug("Setting date to: {}", parsed);
		mValueHandler.setValue(Long.valueOf(parsed));
	}

}
