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
			mValueHandler.setValue(DataFormatUtil.formatDateForStorage(value));
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
