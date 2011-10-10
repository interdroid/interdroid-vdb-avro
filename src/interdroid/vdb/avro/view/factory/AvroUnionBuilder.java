package interdroid.vdb.avro.view.factory;

import interdroid.util.view.ViewUtil;
import interdroid.vdb.avro.control.handler.UnionHandler;
import interdroid.vdb.avro.control.handler.ValueHandler;
import interdroid.vdb.avro.model.AvroRecordModel;
import interdroid.vdb.avro.model.NotBoundException;
import interdroid.vdb.avro.model.UriUnion;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;

import android.app.Activity;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TableLayout;
import android.widget.AbsListView.LayoutParams;

/**
 * A builder that knows how to handle Type.UNION.
 * @author nick
 *
 */
class AvroUnionBuilder extends AvroViewBuilder {

	/**
	 * A heavy weight component.
	 */
	private static final int WEIGHT_HEAVY = 10;

	/**
	 * A zero weight component.
	 */
	private static final int WEIGHT_NONE = 0;

	/**
	 * Construct a builder for Type.UNION.
	 */
	protected AvroUnionBuilder() {
		super(Type.UNION);
	}

	@Override
	public final View buildEditView(final Activity activity,
			final AvroRecordModel dataModel, final ViewGroup viewGroup,
			final Schema schema, final String field, final Uri uri,
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
			final Schema schema, final String field, final Uri uri,
			final UnionHandler handler)
					throws NotBoundException {
		LinearLayout layout = new TableLayout(activity);
		layout.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams wrapHeavy =
				new LinearLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT,
						LayoutParams.WRAP_CONTENT, WEIGHT_HEAVY);
		LinearLayout.LayoutParams wrapLight =
				new LinearLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT,
						LayoutParams.WRAP_CONTENT, WEIGHT_NONE);

		LayoutParams fillWrap = new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		layout.setLayoutParams(fillWrap);
		for (Schema innerType : schema.getTypes()) {
			LinearLayout row = new LinearLayout(activity);
			row.setOrientation(LinearLayout.HORIZONTAL);
			row.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
			RadioButton radioButton = new RadioButton(activity);
			radioButton.setFocusableInTouchMode(false);
			radioButton.setLayoutParams(wrapLight);
			row.addView(radioButton);

			View view = AvroViewBuilder.getEditView(activity, dataModel, null,
					innerType, field, uri,
					handler.getHandler(radioButton, innerType));
			view.setLayoutParams(wrapHeavy);
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
}
