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

import interdroid.vdb.avro.R;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * A DraggableListView which can handle drag and drop operations.
 *
 * @author nick <palmer@cs.vu.nl>
 *
 */
public class DraggableListView extends ListView {
	/** Logger access */
	private static final Logger logger = LoggerFactory
	.getLogger(DraggableListView.class);

	/** Are we currently dragging? */
	private boolean mDragMode;
	/** Are we currently removing? */
	private boolean mRemoving;

	/** Should the dragged item be allowed to move left and right? */
	private boolean mAllowLeftRightMovement = false;
	/** Should we allow items to be added? */
	private boolean mAllowAdd = true;
	/** The resource id for the add button */
	private int mAddResource = R.layout.draggable_add;

	/** The starting position for a drag */
	int mStartPosition;
	/** The ending position for a drag */
	int mEndPosition;
	/** The offset for the drag */
	int mDragOffset;
	/* The top of the removed view */
	int mRemoveTop;
	/* The bottom of the removed view */
	int mRemoveBottom;

	/* The view being dragged */
	ImageView mDragView;

	/* The listener we notify when we are adding an item */
	private AddListener mAddListener;

	/**
	 * List views do not properly measure their height.
	 * We thus implement it correctly to gets around the problem.
	 */
	protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
		// Let our parent figure it out most measurements for us
		super.onMeasure( widthMeasureSpec, heightMeasureSpec );
		logger.debug("onMeasure "+this+
				": width: "+decodeMeasureSpec( widthMeasureSpec )+
				"; height: "+decodeMeasureSpec( heightMeasureSpec )+
				"; measuredHeight: "+getMeasuredHeight()+
				"; measuredWidth: "+getMeasuredWidth() );

		int height = 0; // getMeasuredHeight();
		// logger.debug("Header height is: {}", height);
		ListAdapter adapter = getAdapter();
		int count = adapter.getCount();
		for (int i = 0; i < count; i++) {
			View child =  adapter.getView(i, null, null);
			child.measure(widthMeasureSpec, heightMeasureSpec);
			height += child.getMeasuredHeight();
		}

		logger.debug("Setting measured dimension to: {}x{}", getMeasuredWidth(), height);

		setMeasuredDimension( getMeasuredWidth(), height );
	}

	/**
	 * A helper so we can log measure specs easily
	 * @param measureSpec the measure spect to decode
	 * @return A string representation
	 */
	private String decodeMeasureSpec( int measureSpec ) {
		int mode = View.MeasureSpec.getMode( measureSpec );
		String modeString = "<> ";
		switch( mode ) {
		case View.MeasureSpec.UNSPECIFIED:
			modeString = "UNSPECIFIED ";
			break;

		case View.MeasureSpec.EXACTLY:
			modeString = "EXACTLY ";
			break;

		case View.MeasureSpec.AT_MOST:
			modeString = "AT_MOST ";
			break;
		}
		return modeString+Integer.toString( View.MeasureSpec.getSize( measureSpec ) );
	}

	/** Our listener for drop operations */
	private DropListener mInnerDropListener =
		new DropListener() {
		public void onDrop(int from, int to) {
			ListAdapter adapter = getAdapter();
			if (mAllowAdd) {
				if (from > 0) from -= 1;
				if (to > 0) to -= 1;
				adapter = ((HeaderViewListAdapter) adapter).getWrappedAdapter();
			}

			logger.debug("Adapter: {}", adapter);
			if (adapter instanceof DraggableAdapter) {
				logger.debug("Firing onDrop: {} {}", from, to);
				((DraggableAdapter)adapter).onDrop(from, to);
				invalidateViews();
			} else {
				logger.debug("Not a draggable adapter.");
			}
		}
	};

	/** Our handler for remove actions */
	private RemoveListener mInnerRemoveListener =
		new RemoveListener() {
		public void onRemove(int which) {
			ListAdapter adapter = getAdapter();
			if (mAllowAdd) {
				if (which > 0) which -= 1;
				adapter = ((HeaderViewListAdapter) adapter).getWrappedAdapter();
			}

			if (adapter instanceof DraggableAdapter) {
				logger.debug("Firing onRemove: {}", which);
				((DraggableAdapter)adapter).onRemove(which);
				invalidateViews();
			} else {
				logger.debug("Not a removable adapter.");
			}
		}
	};

	/** Our handler for drag actions */
	private DragListener mInnerDragListener =
		new DragListener() {

		// TODO: This should come from style or something.
		int backgroundColor = 0xe0103010;
		int defaultBackgroundColor;

		public void onDragStart(View itemView) {
			itemView.setVisibility(View.INVISIBLE);
			defaultBackgroundColor = itemView.getDrawingCacheBackgroundColor();
			itemView.setBackgroundColor(backgroundColor);
			ImageView iv = (ImageView)itemView.findViewById(R.id.drag_handle);
			if (iv != null) iv.setVisibility(View.INVISIBLE);
		}

		public void onDragStop(View itemView) {
			itemView.setVisibility(View.VISIBLE);
			itemView.setBackgroundColor(defaultBackgroundColor);
			ImageView iv = (ImageView)itemView.findViewById(R.id.drag_handle);
			if (iv != null) iv.setVisibility(View.VISIBLE);
		}

	};

	/**
	 * Construct the list view. Called by the android inflate system
	 * @param context the context the view will run in
	 * @param attrs the attributes we will take on
	 */
	public DraggableListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * Construct the list view. Called by the android inflate system
	 * @param context the context the view will run in
	 */
	public DraggableListView(Context context) {
		super(context);
	}

	/**
	 * Toggle if views should be allowed to move left and right while dragging
	 * @param b true if views should be able to move left and right
	 */
	public void setAllowLeftRightMovement(boolean b) {
		mAllowLeftRightMovement = b;
	}

	/**
	 *
	 */
	/**
	 * Sets the resource to inflate for the add button. Note that
	 * the resource must include a Button with id interdroid.util.R.add_button
	 * @param resc the resource to inflate for the add button
	 */
	public void setAddResource(int resc) {
		mAddResource = resc;
	}

	/**
	 * Sets if we should allow items to be added
	 * @param b true if allow buttons should be shown
	 */
	public void setAllowAdd(boolean b) {
		if(getAdapter() != null) {
			throw new IllegalStateException(
					"You must set allow before setting the adapter.");
		}
		mAllowAdd = b;
	}

	/**
	 * The listener we will notify when an add button is clicked.
	 * @param l the listener to notify
	 */
	public void setAddListener(AddListener l) {
		mAllowAdd = true;
		mAddListener = l;
	}

	/**
	 * Sets the adapter this view will use to construct views. The adapter
	 * must be an instance of DraggableAdapter or an exception will be thrown.
	 * @param adpater the adapter for the list views
	 */
	@Override
	public void setAdapter(ListAdapter adapter) {
		if (!(adapter instanceof DraggableAdapter)) {
			throw new IllegalArgumentException("Adapter for a DraggableListView must be a DraggableAdapter");
		}
		if (mAllowAdd) {
			View header = inflate(getContext(), mAddResource, null);
			ImageButton addButton = (ImageButton) header.findViewById(R.id.add_button);
			addButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					logger.debug("Add button clicked.");
					if (mAddListener != null) {
						logger.debug("Firing add event.");
						mAddListener.onAddItem();
					}
				}

			});
			addHeaderView(header);


			View footer = inflate(getContext(), mAddResource, null);
			ImageButton footerAddButton = (ImageButton) footer.findViewById(R.id.add_button);
			footerAddButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					logger.debug("Add button clicked.");
					if (mAddListener != null) {
						logger.debug("Firing add event.");
						mAddListener.onAddItem();
					}
				}

			});
			addFooterView(footer);
		}
		super.setAdapter(adapter);
	}

	/**
	 * Handles drag touch events.
	 * @param ev the touch events while dragging
	 */
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		final int action = ev.getAction();
		final int x = (int) ev.getX();
		final int y = (int) ev.getY();

		// How wide is our drag target?
		int touched = pointToPosition(x, y);
		int minX = 0;
		int maxX = 0;


		// Break out if they touched the add view
		if (!mDragMode && mAllowAdd && touched == 0) {
			return false;
		}

		if (touched != INVALID_POSITION && touched != 0) {
			View tView = getChildAt(touched);
			if (tView != null) {
				tView = tView.findViewById(R.id.drag_handle);
				if (tView == null) {
					return false;
				}
				minX = tView.getLeft();
				maxX = tView.getRight();
			}
		}

		if (!mRemoving && action == MotionEvent.ACTION_DOWN && x >= minX && x <= maxX) {
			mDragMode = true;
		}

		if (!mDragMode) {
			// Check if we are pressing the remove button
			if (touched != INVALID_POSITION) {
				ImageView button = (ImageView) getChildAt(touched).findViewById(R.id.remove_button);
				switch (action) {
				case MotionEvent.ACTION_UP:
					logger.debug("Releasing: {} {}", x, y);
					logger.debug("{} {}", button.getLeft(), button.getRight());
					logger.debug("{} {}", mRemoveTop, mRemoveBottom);
					if (mRemoving && x >= button.getLeft() && x <= button.getRight() && y >= mRemoveTop && y <= mRemoveBottom) {
						logger.debug("Remove button pressed.");
						if (mInnerRemoveListener != null) {
							mInnerRemoveListener.onRemove(touched);
						}
					}
					button.setImageResource(R.drawable.remove_button);
					button.postInvalidate();
					mRemoving = false;
					break;
				case MotionEvent.ACTION_MOVE:
					if (mRemoving) {
						logger.debug("Remove button moved: {} {}", x, y);
						logger.debug("{} {}", button.getLeft(), button.getRight());
						logger.debug("{} {}", mRemoveTop, mRemoveBottom);
						if (x >= button.getLeft() && x <= button.getRight() && y >= mRemoveTop && y <= mRemoveBottom) {
							logger.debug("Showing as pressed.");
							button.setImageResource(R.drawable.remove_button_pressed);
							button.postInvalidate();
						} else {
							button.setImageResource(R.drawable.remove_button);
							button.postInvalidate();
						}
					}
					break;
				case MotionEvent.ACTION_DOWN:
					if (!mRemoving && x >= button.getLeft() && x <= button.getRight()) {
						mRemoving = true;
						mRemoveTop = getChildAt(touched).getTop();
						mRemoveBottom = getChildAt(touched).getBottom();
						logger.debug("Remove button pressed: {} {}", mRemoveTop, mRemoveBottom);
						button.setImageResource(R.drawable.remove_button_pressed);
						button.postInvalidate();
					}
					break;
				}
			}
		} else {
			switch (action) {
			case MotionEvent.ACTION_DOWN: {
				mStartPosition = touched;
				int mItemPosition = mStartPosition - getFirstVisiblePosition();
				logger.debug("Drag: {}", mItemPosition);
				if (mStartPosition != INVALID_POSITION) {
					mDragOffset = y - getChildAt(mItemPosition).getTop();
					mDragOffset -= ((int)ev.getRawY()) - y;
					startDrag(mItemPosition,y);
					logger.debug("Drag Start: {} {} :" + y, getTop(), getBottom());
					drag(mAllowLeftRightMovement ? x : 0,y);

					// Now we need to try to turn off interception
					requestDisallowInterceptRecursive(getRootView(), true);
				}
			}
			break;
			case MotionEvent.ACTION_MOVE: {
				logger.debug("Drag: {} {} :", y, getBottom() - getTop());
				if ( y >= 0 && y <= getBottom() - getTop())
					drag(mAllowLeftRightMovement ? x : 0, y);
			}
			break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
			default: {
				mDragMode = false;
				mEndPosition = touched;
				logger.debug("Checking end: {} {}", mEndPosition, getCount() - 1);
				if (mEndPosition == getCount() - 1) {
					View child = getChildAt(mEndPosition);
					int top = y - (mDragView.getHeight() / 2);
					logger.debug("Checking top: {} {}", top, child.getTop());
					if (top > child.getTop()) {
						logger.debug("After end.");
						mEndPosition += 1;
					}
				}
				logger.debug("Dropped: {} {}", mStartPosition, mEndPosition);
				stopDrag(mStartPosition - getFirstVisiblePosition());
				if (mStartPosition != INVALID_POSITION && mEndPosition != INVALID_POSITION && mStartPosition != mEndPosition)
					mInnerDropListener.onDrop(mStartPosition, mEndPosition);

				// Now we need to try to turn on interception again
				requestDisallowInterceptRecursive(getRootView(), false);
			}
			break;
			}
		}
		return true;
	}

	/**
	 * Hack to disallow intercepts of touch events on all sub views so
	 * that we can drag properly. This is required because PhoneDecore
	 * doesn't pass the request to children properly.
	 * @param root the root view
	 * @param disallow true if we should disallow intercepts
	 */
	private void requestDisallowInterceptRecursive(View root, boolean disallow) {
		if (root instanceof ViewGroup) {
			ViewGroup rootGroup = (ViewGroup)root;
			rootGroup.requestDisallowInterceptTouchEvent(disallow);
			for (int i = 0; i < rootGroup.getChildCount(); i++) {
				requestDisallowInterceptRecursive(rootGroup.getChildAt(i), disallow);
			}
		}
	}

	/**
	 * Updates the position of the dragged view.
	 * @param x the x position for the view
	 * @param y the y position for the view
	 */
	private void drag(int x, int y) {
		if (mDragView != null) {
			WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) mDragView.getLayoutParams();
			layoutParams.x = x;
			layoutParams.y = y - mDragOffset;
			WindowManager mWindowManager = (WindowManager) getContext()
			.getSystemService(Context.WINDOW_SERVICE);
			mWindowManager.updateViewLayout(mDragView, layoutParams);
		}
	}

	/**
	 * Starts a drag operation on the given item
	 * @param itemIndex the index of the dragged item
	 * @param y the y offset of the touch which started the drag
	 */
	private void startDrag(int itemIndex, int y) {
		stopDrag(itemIndex);

		View item = getChildAt(itemIndex);
		if (item == null) return;
		item.setDrawingCacheEnabled(true);
		mInnerDragListener.onDragStart(item);

		// Create a copy of the drawing cache so that it does not get recycled
		// by the framework when the list tries to clean up memory
		Bitmap bitmap = Bitmap.createBitmap(item.getDrawingCache());

		WindowManager.LayoutParams mWindowParams = new WindowManager.LayoutParams();
		mWindowParams.gravity = Gravity.TOP;
		mWindowParams.x = 0;
		mWindowParams.y = y - mDragOffset;

		mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		mWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
		| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
		| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
		| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
		| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
		mWindowParams.format = PixelFormat.TRANSLUCENT;
		mWindowParams.windowAnimations = 0;

		Context context = getContext();
		ImageView v = new ImageView(context);
		v.setImageBitmap(bitmap);

		WindowManager mWindowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		mWindowManager.addView(v, mWindowParams);
		mDragView = v;
	}

	/**
	 * Handles stopping a drag of the given item
	 * @param itemIndex the index of the item which is being dragged
	 */
	private void stopDrag(int itemIndex) {
		if (mDragView != null) {
			mInnerDragListener.onDragStop(getChildAt(itemIndex));
			mDragView.setVisibility(GONE);
			WindowManager wm = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
			wm.removeView(mDragView);
			mDragView.setImageDrawable(null);
			mDragView = null;
		}
	}
}
