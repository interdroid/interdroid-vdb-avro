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
package interdroid.util.view;


import interdroid.util.view.LayoutUtil.LayoutParameters;
import interdroid.util.view.LayoutUtil.LayoutWeight;

import java.text.DateFormatSymbols;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.util.AttributeSet;
import android.util.MonthDisplayHelper;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * A view which displays a clickable calendar.
 * @author nick <palmer@cs.vu.nl>
 *
 */
public class CalendarView extends LinearLayout {
	/**
	 * Access to logger.
	 */
	private static final Logger LOG =
			LoggerFactory.getLogger(CalendarView.class);

	/**
	 * Date format symbols used to get day and month names.
	 */
	private static final DateFormatSymbols DATE_FORMAT =
			new DateFormatSymbols();

	/** A day which is in the middle of the month. */
	private static final int	MIDDLE_OF_THE_MONTH	= 15;

	/**
	 * The number of rows possible in a calendar.
	 */
	public static final int	CALENDAR_ROWS		= 6;
	/**
	 * The number of columns in the calendar == days of week.
	 */
	public static final int	CALENDAR_COLUMNS	= 7;

	/**
	 * The size of the month text.
	 */
	private static final float	MONTH_TEXT_SIZE	= 30;

	/**
	 * What is today?
	 */
	private final Calendar	mToday;

	/**
	 * Helper to help us format a calendar.
	 */
	private MonthDisplayHelper	mMonthHelper;

	/**
	 * The cells of the calendar.
	 */
	private final CalendarCell[][]	mCells;
	/**
	 * The attributes for the calendar.
	 */
	private final AttributeSet	mAttributes;

	/**
	 * The table layout that holds the cells.
	 */
	private final TableLayout mCalendar;

	/** The view with the month name at the top. */
	private TextView	mMonthName;

	/** The row with the month days. */
	private TableRow	mDaysRow;
	/** The year number. */
	private TextView	mYearName;
	/** The year we are displaying. */
	private int	mYear;

	/**
	 * The listener for clicks on this calendar.
	 */
	private CalendarClickListener	mListener;

	/**
	 * The listener for clicks on individual cells we use to notify the
	 * calendar click listener.
	 */
	private final OnClickListener	mCellClickListener = new OnClickListener() {

		@Override
		public void onClick(final View view) {
			if (mListener != null) {
				CalendarCell cell = (CalendarCell) view;
				// Ask the cell to tell our listener they were clicked.
				cell.fireCalendarClick(mListener);
			}
		}

	};

	/**
	 * Construct a CalendarView.
	 * @param context context to work in
	 * @param attrs the attributes for the view
	 */
	public CalendarView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		mAttributes = attrs;
		// Make sure we run in vertical mode.
		this.setOrientation(LinearLayout.VERTICAL);

		mToday = Calendar.getInstance();
		mMonthHelper = new MonthDisplayHelper(
				mToday.get(Calendar.YEAR), mToday.get(Calendar.MONTH));
		mCalendar = new TableLayout(getContext(), mAttributes);
		mCells = new CalendarCell[CALENDAR_ROWS][CALENDAR_COLUMNS];
		initHeader();
		initCells();
		LayoutParameters.setLinearLayoutParams(
				LayoutParameters.W_FILL_H_WRAP, mCalendar);
		addView(mCalendar);
	}

	/**
	 * Construct a CalendarView.
	 * @param context context to work in
	 */
	public CalendarView(final Context context) {
		this(context, null);
	}

	/**
	 * Initialize the header objects.
	 */
	private void initHeader() {

		// Add the month title.
		LinearLayout header = new LinearLayout(getContext(), mAttributes);
		header.setOrientation(LinearLayout.HORIZONTAL);

		mMonthName = new TextView(getContext(), mAttributes);
		mMonthName.setTextSize(MONTH_TEXT_SIZE);
		mMonthName.setClickable(true);
		mMonthName.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				today();
			}
		});

		TextView leftArrow = new TextView(getContext(), mAttributes);
		leftArrow.setText("  <");
		leftArrow.setTextSize(MONTH_TEXT_SIZE);
		leftArrow.setClickable(true);
		leftArrow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				previousMonth();
			}

		});
		TextView rightArrow = new TextView(getContext(), mAttributes);
		rightArrow.setText(">  ");
		rightArrow.setTextSize(MONTH_TEXT_SIZE);
		rightArrow.setClickable(true);
		rightArrow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				nextMonth();
			}
		});

		// Set gravity and layout for everything
		mMonthName.setGravity(Gravity.CENTER);
		LayoutParameters.setLinearLayoutParams(
				LayoutParameters.W_WRAP_H_WRAP, LayoutWeight.Half, mMonthName);
		leftArrow.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
		LayoutParameters.setLinearLayoutParams(
				LayoutParameters.W_WRAP_H_WRAP, LayoutWeight.Quarter,
				leftArrow);
		rightArrow.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
		LayoutParameters.setLinearLayoutParams(
				LayoutParameters.W_WRAP_H_WRAP, LayoutWeight.Quarter,
				rightArrow);

		// Add it all together
		header.addView(leftArrow);
		header.addView(mMonthName);
		header.addView(rightArrow);
		addView(header);

		// Add a smaller year under that
		mYearName = new TextView(getContext(), mAttributes);
		mYear = mToday.get(Calendar.YEAR);
		mYearName.setText(String.valueOf(mYear));
		mYearName.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
		LayoutParameters.setLinearLayoutParams(
				LayoutParameters.W_FILL_H_WRAP,
				mYearName);
		addView(mYearName);

		// Now add the row of day names.
		mDaysRow = new TableRow(getContext(), mAttributes);
		LayoutParameters.setViewGroupLayoutParams(
				LayoutParameters.W_FILL_H_WRAP, mDaysRow);
		mCalendar.addView(mDaysRow);

		// Now build the views with all the names
		float cellWeight = 1f / CALENDAR_COLUMNS;
		for (int column = 0; column < CALENDAR_COLUMNS; column++) {
			TextView day = new TextView(getContext(), mAttributes);
			day.setText(getDayName(column + 1));
			day.setGravity(Gravity.CENTER);
			LOG.debug("Added day: {}", day.getText());
			// Add it to the row
			LayoutParameters.setTableRowParams(
					LayoutParameters.W_FILL_H_FILL, cellWeight, day);
			mDaysRow.addView(day);
		}
	}

	/**
	 * @param column the day to get the name of
	 * @return the short name for a given day
	 */
	private CharSequence getDayName(final int column) {
		return DATE_FORMAT.getShortWeekdays()[column];
	}

	/**
	 * @return the name of the month the mMonthHelper is currently showing.
	 */
	private String getMonthName() {
		return DATE_FORMAT.getMonths()[mMonthHelper.getMonth()];
	}

	/**
	 * Initializes the CalendarCell objects.
	 */
	private void initCells() {
		float cellWeight = 1f / CALENDAR_COLUMNS;
		for (int row = 0; row < CALENDAR_ROWS; row++) {
			TableRow tableRow = new TableRow(getContext(), mAttributes);
			LayoutParameters.setViewGroupLayoutParams(
					LayoutParameters.W_FILL_H_FILL, tableRow);
			mCalendar.addView(tableRow);
			for (int column = 0; column < CALENDAR_COLUMNS; column++) {
				CalendarCell cell =
						new CalendarCell(getContext(), mAttributes);
				cell.setOnClickListener(mCellClickListener);
				mCells[row][column] = cell;
				LayoutParameters.setTableRowParams(
						LayoutParameters.W_FILL_H_FILL, cellWeight, cell);
				tableRow.addView(cell);
			}
		}

		// Now set all the days in the calendar.
		setCellDays();
	}

	/**
	 * Sets the days for all the cells based on the mMonthHelper value.
	 */
	private void setCellDays() {
		mMonthName.setText(getMonthName());
		for (int row = 0; row < CALENDAR_ROWS; row++) {
			final int[] rowDigits = mMonthHelper.getDigitsForRow(row);
			for (int column = 0; column < CALENDAR_COLUMNS; column++) {
				int month;
				month = getMonthNumber(rowDigits, row, column);
				mCells[row][column].setDay(rowDigits[column],
						month, mYear,
						mMonthHelper.isWithinCurrentMonth(row, column),
						// Days are 1 indexed
						isWeekend(column + 1),
						isToday(rowDigits[column], month, mYear));
			}
		}
	}

	/**
	 * @param day the day to check
	 * @return true if this day is a weekend day
	 */
	private boolean isWeekend(final int day) {
		boolean ret = false;
		if (day == Calendar.SUNDAY || day == Calendar.SATURDAY) {
			ret = true;
		}
		return ret;
	}

	/**
	 * @param day the day to check
	 * @param month the month to check
	 * @param year the year to check
	 * @return true if the date matches mToday.
	 */
	private boolean isToday(
			final int day, final int month, final int year) {
		return day == mToday.get(Calendar.DATE)
				&& month == mToday.get(Calendar.MONTH)
				&& year == mToday.get(Calendar.YEAR);
	}

	/**
	 * This should be in MonthHelper but it isn't.
	 * @param rowDigits the digits for the row
	 * @param row the row number
	 * @param column the column number
	 * @return the 0 indexed month number
	 */
	private int getMonthNumber(final int[] rowDigits,
			final int row, final int column) {
		int month;
		month = mMonthHelper.getMonth();
		if (!mMonthHelper.isWithinCurrentMonth(row, column)) {
			if (rowDigits[column] > MIDDLE_OF_THE_MONTH) {
				month -= 1;
			}
			if (rowDigits[column] < MIDDLE_OF_THE_MONTH) {
				month += 1;
			}
			if (month < 0) {
				month = Calendar.DECEMBER;
			}
			if (month > Calendar.DECEMBER) {
				month = Calendar.JANUARY;
			}
		}
		return month;
	}

	/**
	 * Rewind to the previous month.
	 */
	public final void previousMonth() {
		mMonthHelper.previousMonth();
		if (mMonthHelper.getMonth() == Calendar.DECEMBER) {
			mYear -= 1;
			mYearName.setText(String.valueOf(mYear));
		}
		setCellDays();
		invalidate();
	}

	/**
	 * Advance to the next month.
	 */
	public final void nextMonth() {
		mMonthHelper.nextMonth();
		if (mMonthHelper.getMonth() == Calendar.JANUARY) {
			mYear += 1;
			mYearName.setText(String.valueOf(mYear));
		}
		setCellDays();
		invalidate();
	}

	/**
	 * Set the calendar so it shows today.
	 */
	public final void today() {
		mMonthHelper = new MonthDisplayHelper(mToday.get(Calendar.YEAR),
				mToday.get(Calendar.MONTH));
		mYear = mToday.get(Calendar.YEAR);
		mYearName.setText(String.valueOf(mYear));
		setCellDays();
		invalidate();
	}

	@Override
	protected final void onLayout(final boolean changed, final int top,
			final int left, final int right, final int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		// Now figure out how tall the celendar part can be.
		int height = (bottom - top)
				- (mMonthName.getHeight()
						+ mDaysRow.getHeight()
						+ mYearName.getHeight());
		setCellSizes(height, right - left);
	}

	/**
	 * Tells all the cells what size they should insist on being.
	 * @param height the height to tell the cells
	 * @param width the width to tell the cells
	 */
	private void setCellSizes(final int height, final int width) {
		int cellHeight = height / CALENDAR_ROWS;
		int cellWidth = width / CALENDAR_COLUMNS;
		for (int row = 0; row < CALENDAR_ROWS; row++) {
			for (int column = 0; column < CALENDAR_COLUMNS; column++) {
				mCells[row][column].setCellSize(cellHeight, cellWidth);
			}
		}
	}

	/**
	 * Registers the listener for clicks on the calendar.
	 * @param listener the listener to register
	 */
	public final void setOnCalendarClickListener(
			final CalendarClickListener listener) {
		mListener = listener;
	}
}
