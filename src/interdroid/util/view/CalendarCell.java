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

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;


/**
 * A view which displays a clickable calendar.
 * @author nick <palmer@cs.vu.nl>
 *
 */
public class CalendarCell extends ImageView {
//	/**
//	 * Access to logger.
//	 */
//	private static final Logger LOG =
//			LoggerFactory.getLogger(CalendarCell.class);

	/** Transparency of the pressed overlay. */
	private static final int	TRANSPARENCY	= 128;
	/** Alpha for no transparency. */
	private static final int	NO_TRANSPARENCY	= 255;


	/** The size of font to use. */
	private static final int	FONT_SIZE	= 25;

	/** How much to inset the text from the top right corner. */
	private static final int	TEXT_INSET	= 7;

	/**
	 * The default weekend color.
	 */
	private static final int	WEEKEND_COLOR	= 0xffffaaaa; // Red
	/**
	 * The default day color.
	 */
	private static final int	DAY_COLOR	= Color.WHITE;
	/**
	 * The default out of month day color.
	 */
	private static final int	OUT_COLOR	= Color.DKGRAY;
	/**
	 * The default font color.
	 */
	private static final int	FONT_COLOR	= Color.BLACK;
	/**
	 * The default color of the border.
	 */
	private static final int	BORDER_COLOR	= Color.BLACK;
	/**
	 * Default color when a cell is pressed.
	 */
	private static final int	PRESSED_COLOR	= Color.RED;
	/**
	 * Default color to highlight today.
	 */
	private static final int	TODAY_COLOR	= Color.RED;
	/** Width of the stroke for the today circle. */
	private static final float	TODAY_STROKE_WIDTH	= 3;



	/** The number this cell is showing. */
	private int mDay;
	/** The is the month this cell is showing. */
	private int	mMonth;
	/** The year this cell is showing. */
	private int mYear;
	/** True if this cell is in the displayed month. */
	private boolean mInMonth;
	/** Is this a weekend day? */
	private boolean	mIsWeekend;
	/** Is this day really today. */
	private boolean	mIsToday;

	/** The height of this cell. */
	private int	mHeight;
	/** The width of this cell. */
	private int	mWidth;

	/** Is this cell being pressed? */
	private boolean	mPressed;


	/**
	 * Construct a CalendarCell.
	 * @param context context to work in
	 * @param attrs the attributes for the view
	 */
	public CalendarCell(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		this.setClickable(true);
		// For some reason setPressed is not being called when this is pressed
		// This on touch listener handles calling setPressed properly.
		setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(final View view, final MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					setPressed(true);
					break;
				case MotionEvent.ACTION_MOVE:
					// Build the view bounds.
					int width = CalendarCell.this.getWidth();
					int height = CalendarCell.this.getHeight();
					Rect bounds = new Rect(0, 0, width, height);
					// Is this a hit?
					if (bounds.contains((int) event.getX(),
							(int) event.getY())) {
						setPressed(true);
					} else {
						setPressed(false);
					}
					break;
				case MotionEvent.ACTION_UP:
					setPressed(false);
					break;
				default:
					break;
				}
				return false;
			}
		});
	}

	/**
	 * Construct a CalendarCell.
	 * @param context context to work in
	 */
	public CalendarCell(final Context context) {
		this(context, null);
	}

	/**
	 * Set the day this cell is showing.
	 * @param day the day the cell is showing
	 * @param month the month the cell is showing
	 * @param year the year the cell is showing
	 * @param inMonth true if this day is in the month.
	 * @param isWeekend is this a weekend day.
	 * @param isToday
	 */
	public final void setDay(final int day, final int month, final int year,
			final boolean inMonth, final boolean isWeekend,
			final boolean isToday) {
		mDay = day;
		mMonth = month;
		mYear = year;
		mInMonth = inMonth;
		mIsWeekend = isWeekend;
		mIsToday = isToday;
	}


	/**
	 * @return the background color for this cell.
	 */
	private int getDayColor() {
		int color;

		if (mIsWeekend) {
			color = WEEKEND_COLOR;
		} else {
			color = DAY_COLOR;
		}
		return color;
	}

	@Override
	protected final void onMeasure(final int widthMeasureSpec,
			final int heightMeasureSpec) {
		this.setMeasuredDimension(mWidth, mHeight);
	}

	@Override
	public final void onDraw(final Canvas canvas) {
		super.onDraw(canvas);

		final Paint paint = new Paint();

		final Rect border = new Rect();
		getDrawingRect(border);

		// Draw the background
		paint.setColor(BORDER_COLOR);
		canvas.drawRect(border, paint);
		paint.setColor(getDayColor());

		// Now resize the border down to the cell size.
		border.left += 1;
		border.top += 1;
		border.right -= 1;
		border.bottom -= 1;
		canvas.drawRect(border, paint);
		// If it is out of the month then draw an out overlay
		if (!mInMonth) {
			paint.setColor(OUT_COLOR);
			paint.setAlpha(TRANSPARENCY);
			canvas.drawRect(border, paint);
			paint.setAlpha(NO_TRANSPARENCY);
		}

		// Draw the text
		paint.setColor(FONT_COLOR);
		paint.setTextSize(FONT_SIZE);
		paint.setTextAlign(Paint.Align.RIGHT);
		String text = String.valueOf(mDay);
		Rect bounds = new Rect();
		paint.getTextBounds(text, 0, text.length(), bounds);
		canvas.drawText(text, border.right - TEXT_INSET,
				border.top + TEXT_INSET + bounds.height(),
				paint);

		// If it is today add a marker for that.
		if (mIsToday) {
			paint.setColor(TODAY_COLOR);
			paint.setStyle(Paint.Style.STROKE);
			RectF oval = new RectF(border);
			// Take off the day height.
			oval.top += (bounds.bottom - bounds.top);
			oval.top += TODAY_STROKE_WIDTH + ( 2 * TEXT_INSET);
			oval.bottom -= TODAY_STROKE_WIDTH;
			oval.left += TODAY_STROKE_WIDTH;
			oval.right -= TODAY_STROKE_WIDTH;
			// Now I want a cool looking stroke from
			// fat to thin running counter clockwise
			// just to make the code more complicated. ;)
			paint.setStrokeWidth(TODAY_STROKE_WIDTH + 3);
			canvas.drawArc(oval, 270, 30, false, paint);
			paint.setStrokeWidth(TODAY_STROKE_WIDTH + 2);
			canvas.drawArc(oval, 180, 90, false, paint);
			paint.setStrokeWidth(TODAY_STROKE_WIDTH + 1);
			canvas.drawArc(oval, 90, 90, false, paint);
			paint.setStrokeWidth(TODAY_STROKE_WIDTH - 1);
			oval.top += TEXT_INSET;
			oval.left += TEXT_INSET / 4;
			oval.right -= TEXT_INSET / 4;
			canvas.drawArc(oval, 270, 90, false, paint);
			paint.setStrokeWidth(TODAY_STROKE_WIDTH);
			canvas.drawArc(oval, 0, 90, false, paint);
		}

		// If it is pressed draw a white overlay.
		if (mPressed) {
			paint.setStyle(Paint.Style.FILL_AND_STROKE);
			paint.setColor(PRESSED_COLOR);
			paint.setAlpha(TRANSPARENCY);
			paint.setStrokeWidth(1);
			canvas.drawRect(border, paint);
		}

	}

	/**
	 * Called from the calendar view to set the size of this view.
	 * @param cellHeight the height for this cell
	 * @param cellWidth the width for this cell
	 */
	public final void setCellSize(final int cellHeight, final int cellWidth) {
		mHeight = cellHeight;
		mWidth = cellWidth;
	}

	/**
	 * Fires a CalendarClick event to the listener with this cells date.
	 * @param mListener the listener to fire the event to
	 */
	public final void fireCalendarClick(final CalendarClickListener mListener) {
		mListener.onCalendarClicked(mDay, mMonth, mYear);
	}

	@Override
	public final void setPressed(final boolean pressed) {
		if (mPressed != pressed) {
			mPressed = pressed;
			invalidate();
		}
	}

}
