package interdroid.vdb.avro.view.factory;

import java.util.List;

import interdroid.util.view.LayoutUtil.LayoutWeight;
import interdroid.util.view.ViewUtil;
import interdroid.util.view.LayoutUtil.LayoutParameters;
import interdroid.vdb.avro.control.handler.UnionHandler;
import interdroid.vdb.avro.control.handler.ValueHandler;
import interdroid.vdb.avro.model.AvroRecordModel;
import interdroid.vdb.avro.model.NotBoundException;
import interdroid.vdb.avro.model.UriUnion;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.AbsListView.LayoutParams;

/**
 * A builder that knows how to handle Type.UNION.
 * @author nick
 *
 */
class AvroUnionBuilder extends AvroTypedViewBuilder {

	/**
	 * Construct a builder for Type.UNION.
	 */
	protected AvroUnionBuilder() {
		super(Type.UNION);
	}

	@Override
	public final View buildEditView(final Activity activity,
			final AvroRecordModel dataModel, final ViewGroup viewGroup,
			final Schema schema, final Field field, final Uri uri,
			final ValueHandler valueHandler) throws NotBoundException {
		return buildUnion(activity, dataModel, viewGroup, schema, field, uri,
				new UnionHandler(dataModel, valueHandler,
						getUnion(uri, valueHandler, schema)));
	}

	/**
	 * Builds a view for a union.
	 * @param activity the activity for the view
	 * @param dataModel the model to get data from
	 * @param viewGroup the view group to add to
	 * @param schema the schema for the view
	 * @param field the field for the view
	 * @param uri the uri for the field
	 * @param handler the value handler
	 * @return the view
	 * @throws NotBoundException if the model is not bound
	 */
	private static View buildUnion(final Activity activity,
			final AvroRecordModel dataModel, final ViewGroup viewGroup,
			final Schema schema, final Field field, final Uri uri,
			final UnionHandler handler)
					throws NotBoundException {
		LinearLayout layout = new LinearLayout(activity);
		layout.setOrientation(LinearLayout.VERTICAL);

		LayoutParams fillWrap = new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		layout.setLayoutParams(fillWrap);
		for (Schema innerType : schema.getTypes()) {
			LinearLayout row = new LinearLayout(activity);
			row.setOrientation(LinearLayout.HORIZONTAL);
			row.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
			RadioButton radioButton = new RadioButton(activity);
			radioButton.setFocusableInTouchMode(false);
			LayoutParameters.setLinearLayoutParams(
					LayoutParameters.W_WRAP_H_WRAP, LayoutWeight.Zero,
					radioButton);
			row.addView(radioButton);

			View view = AvroViewBuilder.getEditView(activity, dataModel, null,
					innerType, null, uri,
					handler.getHandler(radioButton, innerType));
			LayoutParameters.setLinearLayoutParams(
					LayoutParameters.W_WRAP_H_WRAP, LayoutWeight.Zero,
					view);
			row.addView(view);
			handler.addType(radioButton, innerType, view);

			layout.addView(row);
		}
		ViewUtil.addView(activity, viewGroup, layout);
		return layout;
	}


	/**
	 * Returns a UriUnion or constructs an empty one.
	 * @param uri the uri to fetch
	 * @param valueHandler the value handler to get the value from
	 * @param schema the schema for the union
	 * @return the UriUnion
	 */
	private static UriUnion getUnion(final Uri uri,
			final ValueHandler valueHandler, final Schema schema) {
		UriUnion value = (UriUnion) valueHandler.getValue();
		if (value == null) {
			value = new UriUnion(schema);
		}
		return value;
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
	final List<String> getProjectionFields(final Field field) {
		// TODO Auto-generated method stub
		return null;
	}
}
