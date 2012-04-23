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
package interdroid.vdb.avro.view;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;

/**
 * Utilities for dealing with how we store some data types in the database.
 *
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
public final class DataFormatUtil {
	/**
	 * Access to logger.
	 */
	private static final Logger LOG =
			LoggerFactory.getLogger(DataFormatUtil.class);

	/**
	 * No instances please.
	 */
	private DataFormatUtil() {
		// No consutrction
	}

	/**
	 * The format used to store data in the database.
	 */
	private static final SimpleDateFormat DATE_STORE_FORMAT =
			new SimpleDateFormat("yyyyMMdd");
	static {
		DATE_STORE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	/**
	 * The format used to format data stored in the database for display.
	 */
	private static final DateFormat DATE_DISPLAY_FORMAT =
			SimpleDateFormat.getDateInstance();

	/**
	 * The format used to store data in the database.
	 */
	private static final SimpleDateFormat TIME_STORE_FORMAT =
			new SimpleDateFormat("HHmmss");

	static {
		TIME_STORE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	/**
	 * The format used to display timestamps.
	 */
	private static final DateFormat TIMESTAMP_DISPLAY_FORMAT =
			SimpleDateFormat.getInstance();

	/**
	 * The format used to format data stored in the database for display.
	 */
	private static final DateFormat TIME_DISPLAY_FORMAT;
	static {
		TIME_DISPLAY_FORMAT = SimpleDateFormat.getTimeInstance();
	}

	/**
	 * @param timestamp the unix timestamp to format
	 * @return the timestamp formatted for display
	 */
	public static CharSequence formatTimestampForDisplay(final long timestamp) {
		return TIMESTAMP_DISPLAY_FORMAT.format(new Date(timestamp));
	}

	/**
	 * @param dateAsLong the date as a long
	 * @return the human readable date.
	 */
	public static CharSequence formatDateForDisplay(final long dateAsLong) {
		String ret = String.valueOf(dateAsLong);
		try {
			Date date = DataFormatUtil.DATE_STORE_FORMAT.parse(ret);
			ret = DataFormatUtil.DATE_DISPLAY_FORMAT.format(date);
		} catch (ParseException e) {
			LOG.error("Error parsing date.", e);
		}
		return ret;
	}

	/**
	 * @param timeAsLong the time as a long
	 * @return the human readable time
	 */
	public static CharSequence formatTimeForDisplay(final long timeAsLong) {
		String ret = String.valueOf(timeAsLong);
		try {
			Date date = DataFormatUtil.TIME_STORE_FORMAT.parse(ret);
			ret = DataFormatUtil.TIME_DISPLAY_FORMAT.format(date);
		} catch (ParseException e) {
			LOG.error("Error parsing time.", e);
		}
		return ret;
	}

	/**
	 * @param value the calendar value to format
	 * @return the time as a long
	 */
	public static long formatTimeForStorage(final Calendar value) {
		return Long.valueOf(TIME_STORE_FORMAT.format(value.getTime()));
	}

	/**
	 * @param value the calendar value to format
	 * @return the time as a long
	 */
	public static long formatDateForStorage(final Calendar value) {
		return Long.valueOf(DATE_STORE_FORMAT.format(value.getTime()));
	}

	/**
	 *
	 * @param value the value to convert
	 * @return the time as a Date
	 * @throws ParseException if the time can not be converted
	 */
	public static Date getTimeAsDate(final long value) throws ParseException {
		return TIME_STORE_FORMAT.parse(String.valueOf(value));
	}

	/**
	 * @param value the value to convert
	 * @return the date as a Date
	 * @throws ParseException if the time can not be converted
	 */
	public static Date getDateAsDate(final long value) throws ParseException {
		return DATE_STORE_FORMAT.parse(String.valueOf(value));
	}

	/**
	 * Constructs a bitmap from the given byte array resizing if required.
	 * @param data the byte array with the bitmap data
	 * @param maxSize the maximum size. Use <= 0 to not resize
	 * @return a preview bitmap.
	 */
	public static Bitmap getBitmap(final byte[] data, final int maxSize) {
		// NULL pointer here.
		Bitmap bitmap = null;
		if (data != null) {
			bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
			if (bitmap != null) {
				if (maxSize > 0) {
					// 200 x 100 -> 100 / 50
					// 100 x 200 -> 50 / 100
					int width, height;
					LOG.debug("Original: {} {}", bitmap.getWidth(), bitmap.getHeight());
					float aspect = (float) bitmap.getHeight()
							/ (float) bitmap.getWidth();
					LOG.debug("Aspect: {}", aspect);

					if (bitmap.getHeight() > bitmap.getWidth()) {
						height = maxSize;
						width = (int) (height * aspect);
					} else {
						width = maxSize;
						height = (int) (width * aspect);
					}
					LOG.debug("Scaled: {} {}", width, height);
					bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
				}
			}
		}
		return bitmap;
	}

	public static byte[] fromatBitmapForStorage(byte[] data) {
		Bitmap bitmap = getBitmap(data, 500);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.JPEG, 9, out);
		return out.toByteArray();
	}
}
