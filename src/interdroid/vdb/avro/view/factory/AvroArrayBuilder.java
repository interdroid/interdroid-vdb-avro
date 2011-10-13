package interdroid.vdb.avro.view.factory;

import interdroid.util.view.DraggableListView;
import interdroid.util.view.LayoutUtil.LayoutParameters;
import interdroid.util.view.ViewUtil;
import interdroid.vdb.avro.control.handler.ArrayHandler;
import interdroid.vdb.avro.control.handler.ValueHandler;
import interdroid.vdb.avro.model.AvroRecordModel;
import interdroid.vdb.avro.model.NotBoundException;
import interdroid.vdb.avro.model.UriArray;

import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;

/**
 * A builder which knows how to build array views.
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
class AvroArrayBuilder extends AvroTypedViewBuilder {

	/**
	 * Amount to indent array list by.
	 */
	protected static final int LEFT_INDENT = 3;

	/**
	 * Constructs a builder for Type.ARRAY widget == null.
	 */
	public AvroArrayBuilder() {
		super(Type.ARRAY);
	}

	@Override
	public final View buildEditView(final Activity activity,
			final AvroRecordModel dataModel,
			final ViewGroup viewGroup, final Schema schema,
			final Field field, final Uri uri,
			final ValueHandler valueHandler) throws NotBoundException {

		DraggableListView layout = new DraggableListView(activity);
		LayoutParameters.setViewGroupLayoutParams(
				LayoutParameters.W_FILL_H_WRAP, layout);
		layout.setPadding(LEFT_INDENT, 0, 0, 0);

		ArrayHandler adapter = new ArrayHandler(activity, dataModel, layout,
				getArray(uri, valueHandler, schema), field);
		layout.setAdapter(adapter);
		layout.setAddListener(adapter);

		ViewUtil.addView(activity, viewGroup, layout);

		return layout;
	}

	/**
	 * Returns or constructs a UriArray setting in the value handler.
	 * @param uri the uri for the array
	 * @param valueHandler the value handler to get the array from
	 * @param schema the schema for the array
	 * @return a UriArray.
	 * @throws NotBoundException if the underlying model is not bound
	 */
	private UriArray<Object> getArray(final Uri uri,
			final ValueHandler valueHandler, final Schema schema)
					throws NotBoundException {

		@SuppressWarnings("unchecked")
		UriArray<Object> array = (UriArray<Object>) valueHandler.getValue();
		if (array == null) {
			array = new UriArray<Object>(
					Uri.withAppendedPath(valueHandler.getValueUri(),
							valueHandler.getFieldName()), schema);
			valueHandler.setValue(array);
		}

		return array;
	}

	@Override
	final View buildListView(final Context context, final Field field) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	final void bindListView(final View view, final Cursor cursor,
			final Field field) {
		// TODO Auto-generated method stub

	}

	@Override
	List<String> getProjectionFields(final Field field) {
		// TODO Auto-generated method stub
		return null;
	}

}
