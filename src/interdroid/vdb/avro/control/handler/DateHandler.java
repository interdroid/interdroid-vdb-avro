package interdroid.vdb.avro.control.handler;

import interdroid.vdb.avro.view.DataFormatUtil;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;

public class DateHandler implements OnDateChangedListener {

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
			Date d = DataFormatUtil.getDateAsDate(
					(Long) mValueHandler.getValue());
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
		long parsed = DataFormatUtil.formatDateForStorage(value);
		logger.debug("Setting date to: {}", parsed);
		mValueHandler.setValue(parsed);
	}

}
