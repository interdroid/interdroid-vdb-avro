/*
 * Copyright (c) 2008-2012 Vrije Universiteit, The Netherlands All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the Vrije Universiteit nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ``AS IS''
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package interdroid.vdb.avro.control.handler;

import interdroid.vdb.avro.control.handler.value.ValueHandler;
import interdroid.vdb.avro.view.DataFormatUtil;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

/**
 * Handler for time widgets.
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
public class TimeHandler implements OnTimeChangedListener {
	/** Access to logger. */
	private static final Logger LOG = LoggerFactory
			.getLogger(TimeHandler.class);

	/** The year of the start of the epoch time. */
	private static final int	EPOCH_START_YEAR	= 1970;

	/** The value handler to get data from. */
	private final ValueHandler mValueHandler;

	/**
	 * Construct a time handler.
	 * @param view the picker we handle
	 * @param valueHandler the value handler to get and set data with
	 */
	public TimeHandler(final TimePicker view, final ValueHandler valueHandler) {
		mValueHandler = valueHandler;
		// Set the initial value
		final Calendar value = Calendar.getInstance();
		try {
			final Date date = DataFormatUtil.getTimeAsDate(
					(Long) mValueHandler.getValue());
			value.set(EPOCH_START_YEAR, Calendar.JANUARY, 1,
					date.getHours(), date.getMinutes(), 0);
		} catch (ParseException e) {
			LOG.error("Error parsing time! Defaulting to now.", e);
			mValueHandler.setValue(DataFormatUtil.formatTimeForStorage(value));
		}
		LOG.debug("Initializing time to: {} {}",
				value.get(Calendar.HOUR_OF_DAY), value.get(Calendar.MINUTE));
		view.setCurrentHour(value.get(Calendar.HOUR_OF_DAY));
		view.setCurrentMinute(value.get(Calendar.MINUTE));
		view.setOnTimeChangedListener(this);
	}

	@Override
	public final void onTimeChanged(final TimePicker view, final int hourOfDay,
			final int minute) {
		LOG.debug("Updating time: {} {}", hourOfDay, minute);
		// Update the model
		final Calendar value = Calendar.getInstance();
		value.set(EPOCH_START_YEAR, Calendar.JANUARY, 1, hourOfDay, minute, 0);
		final long format = DataFormatUtil.formatTimeForStorage(value);
		LOG.debug("Setting time to: {}", format);
		mValueHandler.setValue(format);
	}

}
