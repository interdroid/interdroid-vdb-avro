package interdroid.vdb.avro.control.handler;

import java.util.ArrayList;

import interdroid.vdb.avro.model.AvroRecordModel;
import interdroid.vdb.avro.view.AvroBaseEditor;
import interdroid.vdb.avro.view.AvroViewFactory;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData.Array;

import android.R;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;

public class ArrayHandler implements OnClickListener, OnFocusChangeListener {

	private static final String TAG = "ArrayHandler";

	private final AvroBaseEditor mActivity;
	private final Array<Object> mArray;
	private final Schema mElementSchema;
	private final AvroRecordModel mDataModel;
	private final ViewGroup mViewGroup;

	private class FocusBackgroundChanger implements OnFocusChangeListener {

		public View mSubView;

		public FocusBackgroundChanger(View subView) {
			mSubView = subView;
		}

		public void onFocusChange(View v, boolean hasFocus) {
			// TODO Auto-generated method stub
			if (hasFocus) {
				mSubView.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.screen_background_light));
			} else {
				mSubView.setBackgroundColor(Color.BLACK);
			}
			mSubView.postInvalidate();
		}
	}

	public ArrayHandler(AvroBaseEditor activity, AvroRecordModel dataModel, ViewGroup viewGroup, Array<Object> array, Schema elementSchema) {
		mActivity = activity;
		mArray = array;
		mElementSchema = elementSchema;
		mDataModel = dataModel;
		mViewGroup = viewGroup;
		buildSubViews();
	}

	private void buildSubViews() {
		int size = (int) mArray.size();
		for (int i = 0; i < size; i++) {
			addSubView(i);
		}
	}


	private void addSubView(int offset) {
		View subView = AvroViewFactory.buildArrayView(true, mActivity, mDataModel, mArray, mElementSchema, offset, mViewGroup);
		Log.d(TAG, "Adding view: " + subView);
		if (subView != null) {
			ArrayList<View> focusables = new ArrayList<View>();
			subView.addFocusables(focusables, View.FOCUS_DOWN, View.FOCUSABLES_ALL);

			for (View focusable: focusables) {
				Log.d(TAG, "Adding focus listener to: " + focusable);
				focusable.setOnFocusChangeListener(new FocusBackgroundChanger(subView));
			}
		}
	}

	@Override
	public void onClick(View arg0) {
		if ("add".equals(arg0.getTag())) {
			mArray.add(null);
			addSubView((int) (mArray.size() - 1));
			mDataModel.notifyChanged();
		} else {
			try {
				Integer offset = (Integer)arg0.getTag();
				if (offset != null) {
					mArray.remove(offset);
					mViewGroup.removeAllViews();
					buildSubViews();
					mViewGroup.postInvalidate();
					mDataModel.notifyChanged();
				}
			} catch (ClassCastException e) {
				// Ignore
			}
		}
	}

	@Override
	public void onFocusChange(View subView, boolean focused) {
		Log.d(TAG, "Focus changed called: " + focused);
		if (focused) {
			mViewGroup.setBackgroundColor(R.color.background_light);
		} else {
			mViewGroup.setBackgroundColor(R.color.background_dark);
		}
		mViewGroup.postInvalidate();
	}

}