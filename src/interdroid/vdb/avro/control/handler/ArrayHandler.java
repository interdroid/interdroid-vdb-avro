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

import interdroid.util.view.AddListener;
import interdroid.util.view.DraggableAdapter;
import interdroid.vdb.avro.model.AvroRecordModel;
import interdroid.vdb.avro.model.NotBoundException;
import interdroid.vdb.avro.model.UriArray;
import interdroid.vdb.avro.view.factory.AvroViewFactory;

import org.apache.avro.Schema.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;

/**
 * Handles array values.
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
public class ArrayHandler implements DraggableAdapter, AddListener  {
	/**
	 * Access to logger.
	 */
	private static final Logger LOG =
			LoggerFactory.getLogger(ArrayHandler.class);

	/** The observable data sets. */
	private final DataSetObservable mObservables = new DataSetObservable();

	/** The activity we work for. */
	private final Activity mActivity;
	/** The array with data. */
	private final UriArray<Object> mArray;
	/** The model this array sits in. */
	private final AvroRecordModel mDataModel;
	/** The view group we are working in. */
	private final ViewGroup mViewGroup;
	/** The field which the array represents. */
	private final Field mField;

	/**
	 * Construct a new array handler.
	 * @param activity the activity to work in
	 * @param dataModel the model for the data
	 * @param viewGroup the view group to add views to
	 * @param array the array with data
	 * @param field the field with the array
	 */
	public ArrayHandler(final Activity activity,
			final AvroRecordModel dataModel, final ViewGroup viewGroup,
			final UriArray<Object> array, final Field field) {
		mActivity = activity;
		mArray = array;
		mDataModel = dataModel;
		mViewGroup = viewGroup;
		mField = field;
		mObservables.registerObserver(mDataModel);
	}

	/**
	 * @param offset the offset into the data array
	 * @return returns a view for the data at the given offset
	 * @throws NotBoundException if the model is not bound
	 */
	private View getSubView(final int offset) throws NotBoundException {
		return AvroViewFactory.buildArrayView(mActivity, mDataModel,
				this, mArray, mField, offset);
	}

	@Override
	public final boolean areAllItemsEnabled() {
		return true;
	}

	@Override
	public final boolean isEnabled(final int position) {
		return true;
	}

	@Override
	public final void registerDataSetObserver(final DataSetObserver observer) {
		mObservables.registerObserver(observer);
	}

	@Override
	public final void unregisterDataSetObserver(
			final DataSetObserver observer) {
		mObservables.unregisterObserver(observer);
	}

	@Override
	public final int getCount() {
		return mArray.size();
	}

	@Override
	public final Object getItem(final int position) {
		return mArray.get(position);
	}

	@Override
	public final long getItemId(final int position) {
		return position;
	}

	@Override
	public final boolean hasStableIds() {
		return false;
	}

	@Override
	public final View getView(final int position, final View convertView,
			final ViewGroup parent) {
		View ret = null;
		try {
			ret = getSubView(position);
		} catch (NotBoundException e) {
			LOG.error("Shouldn't happen: {}", e);
		}
		return ret;
	}

	@Override
	public final int getItemViewType(final int position) {
		return 0;
	}

	@Override
	public final int getViewTypeCount() {
		return 1;
	}

	@Override
	public final boolean isEmpty() {
		return mArray.isEmpty();
	}

	@Override
	public final void onRemove(final int offset) {
		mArray.remove(offset);
		mObservables.notifyChanged();
		mViewGroup.postInvalidate();
	}

	@Override
	public final void onDrop(final int origin, final int destination) {
		if (origin < destination) {
			mArray.add(destination - 1, mArray.remove(origin));
		} else {
			mArray.add(destination, mArray.remove(origin));
		}
		mObservables.notifyChanged();
		mViewGroup.postInvalidate();
	}

	@Override
	public final void onAddItem() {
		mArray.add(null);
		mObservables.notifyChanged();
		mViewGroup.postInvalidate();
	}

	/**
	 * Sets the value at the given offset.
	 * @param offset the offset to set
	 * @param value the value to set to
	 */
	public final void setItem(final int offset, final Object value) {
		mArray.set(offset, value);
		mObservables.notifyChanged();
		mViewGroup.postInvalidate();
	}

}
