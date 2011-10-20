package interdroid.vdb.avro.view.factory;

import interdroid.util.view.LayoutUtil.LayoutParameters;
import interdroid.util.view.ViewUtil;
import interdroid.vdb.avro.control.handler.CheckboxHandler;
import interdroid.vdb.avro.control.handler.value.ValueHandler;
import interdroid.vdb.avro.model.AvroRecordModel;
import interdroid.vdb.avro.model.NotBoundException;

import java.util.List;

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
import android.widget.CheckBox;

/**
 * A builder for Type.BOOLEAN.
 *
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
class AvroBooleanBuilder extends AvroTypedViewBuilder {

	/**
	 * A builder for Type.BOOLEAN && widget == null.
	 */
	AvroBooleanBuilder() {
		super(Type.BOOLEAN);
	}

	@Override
	public final View buildEditView(final Activity activity,
			final AvroRecordModel dataModel,
			final ViewGroup viewGroup, final Schema schema,
			final Field field, final Uri uri,
			final ValueHandler valueHandler) throws NotBoundException {

		// Build the view
		CheckBox text = new CheckBox(activity);
		LayoutParameters.setViewGroupLayoutParams(
				LayoutParameters.W_FILL_H_WRAP, text);
		text.setGravity(Gravity.FILL_HORIZONTAL);

		// Build the handler
		new CheckboxHandler(dataModel, valueHandler, text);

		ViewUtil.addView(activity, viewGroup, text);

		return text;
	}

	@Override
	final View buildListView(final Context context, final Field field) {
		CheckBox view = new CheckBox(context);
		view.setTag(field.name());
		view.setEnabled(false);
		return view;
	}

	@Override
	final void bindListView(final View view, final Cursor cursor,
			final Field field) {
		CheckBox box = (CheckBox) view.findViewWithTag(field.name());
		int index = cursor.getColumnIndex(field.name());
		boolean value = Boolean.valueOf(cursor.getString(index));
		box.setChecked(value);
	}

	@Override
	final List<String> getProjectionFields(final Field field) {
		return getFieldNameProjection(field);
	}

}
