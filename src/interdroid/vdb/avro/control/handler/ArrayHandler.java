package interdroid.vdb.avro.control.handler;


import interdroid.util.view.AddListener;
import interdroid.util.view.DraggableAdapter;
import interdroid.vdb.avro.model.AvroRecordModel;
import interdroid.vdb.avro.model.AvroRecordModel.UriArray;
import interdroid.vdb.avro.view.AvroBaseEditor;
import interdroid.vdb.avro.view.AvroViewFactory;

import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;

public class ArrayHandler implements DraggableAdapter, AddListener  {

	private static final Logger logger = LoggerFactory.getLogger(ArrayHandler.class);

	private DataSetObservable observables = new DataSetObservable();

	private final AvroBaseEditor mActivity;
	private final UriArray<Object> mArray;
	private final Schema mElementSchema;
	private final AvroRecordModel mDataModel;
	private final ViewGroup mViewGroup;

	public ArrayHandler(AvroBaseEditor activity, AvroRecordModel dataModel, ViewGroup viewGroup, UriArray<Object> array, Schema elementSchema) {
		mActivity = activity;
		mArray = array;
		mElementSchema = elementSchema;
		mDataModel = dataModel;
		mViewGroup = viewGroup;
		observables.registerObserver(mDataModel);
	}

	private View getSubView(int offset) {
		return AvroViewFactory.buildArrayView(true, mActivity, mDataModel, mArray, mElementSchema, mArray.getUri(), offset);
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
		return getSubView(position);
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

}