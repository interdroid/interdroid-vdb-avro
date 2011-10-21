package interdroid.vdb.avro.control.handler;

import interdroid.vdb.avro.control.handler.value.ValueHandler;
import interdroid.vdb.avro.view.DataFormatUtil;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;

/**
 * Handler for date values.
 *
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
public class DateHandler implements OnDateChangedListener {
	/** We use middle of the day so time zones don't move us across a date. */
	private static final int	DEFAULT_HOUR	= 12;

	/** Access to logger. */
	private static final Logger LOG = LoggerFactory
			.getLogger(DateHandler.class);

	/** The value handler we set and get data from. */
	private final ValueHandler mValueHandler;

	/**
	 * Construct a date handler.
	 * @param view the view we handle
	 * @param valueHandler the value handler we use for data access
	 */
	public DateHandler(final DatePicker view, final ValueHandler valueHandler) {
		mValueHandler = valueHandler;
		// Set the initial value
		final Calendar value = Calendar.getInstance();
		try {
			final Date date = DataFormatUtil.getDateAsDate(
					(Long) mValueHandler.getValue());
			value.setTime(date);
		} catch (ParseException e) {
			LOG.error("Error parsing date! Defaulting to now.", e);
		}
		LOG.debug("Initializing to: {} {} " + value.get(Calendar.YEAR),
				value.get(Calendar.MONTH), value.get(Calendar.DATE));
		view.init(value.get(Calendar.YEAR), value.get(Calendar.MONTH),
				value.get(Calendar.DATE), this);
	}

	@Override
	public final void onDateChanged(final DatePicker arg0,
			final int year, final int month, final int day) {
		LOG.debug("Updating date: {} {} " + day, year, month);
		// Update the model
		final Calendar value = Calendar.getInstance();
		value.set(year, month, day, DEFAULT_HOUR, 0, 0);
		final long parsed = DataFormatUtil.formatDateForStorage(value);
		LOG.debug("Setting date to: {}", parsed);
		mValueHandler.setValue(parsed);
	}

}
