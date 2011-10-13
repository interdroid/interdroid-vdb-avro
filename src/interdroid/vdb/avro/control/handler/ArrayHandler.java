package interdroid.vdb.avro.control.handler;


import interdroid.util.view.AddListener;
import interdroid.util.view.DraggableAdapter;
import interdroid.vdb.avro.model.AvroRecordModel;
import interdroid.vdb.avro.model.NotBoundException;
import interdroid.vdb.avro.model.UriArray;
import interdroid.vdb.avro.model.UriRecord;
import interdroid.vdb.avro.view.factory.AvroViewFactory;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;

public class ArrayHandler implements DraggableAdapter, AddListener  {

	private static final Logger logger = LoggerFactory.getLogger(ArrayHandler.class);

	private DataSetObservable observables = new DataSetObservable();

	private final Activity mActivity;
	private final UriArray<Object> mArray;
	private final AvroRecordModel mDataModel;
	private final ViewGroup mViewGroup;
	private final Field mField;

	public ArrayHandler(Activity activity, AvroRecordModel dataModel,
			ViewGroup viewGroup, UriArray<Object> array, Field field) {
		mActivity = activity;
		mArray = array;
		mDataModel = dataModel;
		mViewGroup = viewGroup;
		mField = field;
		observables.registerObserver(mDataModel);
	}

	private View getSubView(int offset) throws NotBoundException {
		return AvroViewFactory.buildArrayView(mActivity, mDataModel, this, mArray, mField, offset);
	}

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	@Override
	public boolean isEnabled(int position) {
		return true;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		observables.registerObserver(observer);
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		observables.unregisterObserver(observer);
	}

	@Override
	public int getCount() {
		return mArray.size();
	}

	@Override
	public Object getItem(int position) {
		return mArray.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		try {
			return getSubView(position);
		} catch (NotBoundException e) {
			logger.error("Shouldn't happen: {}", e);
			return null;
		}
	}

	@Override
	public int getItemViewType(int position) {
		return 0;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return mArray.isEmpty();
	}

	@Override
	public void onRemove(int offset) {
		mArray.remove(offset);
		observables.notifyChanged();
		mViewGroup.postInvalidate();
	}

	@Override
	public void onDrop(int from, int to) {
		mArray.add(from < to ? to - 1 : to, mArray.remove(from));
		observables.notifyChanged();
		mViewGroup.postInvalidate();
	}

	@Override
	public void onAddItem() {
		mArray.add(null);
		observables.notifyChanged();
		mViewGroup.postInvalidate();
	}

	public void setItem(int offset, Object value) {
		mArray.set(offset, value);
		observables.notifyChanged();
		mViewGroup.postInvalidate();
	}

}
