package interdroid.vdb.avro.view.factory;

import interdroid.util.view.ViewUtil;
import interdroid.vdb.avro.control.handler.EnumHandler;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * A builder for Type.ENUM && widget == null.
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
class AvroEnumBuilder extends AvroTypedViewBuilder {

	/**
	 * Constructs an AvroEnumBuilder.
	 */
	protected AvroEnumBuilder() {
		super(Type.ENUM);
	}

	@Override
	public final View buildEditView(final Activity activity,
			final AvroRecordModel dataModel,
			final ViewGroup viewGroup, final Schema schema,
			final Field field, final Uri uri,
			final ValueHandler valueHandler) throws NotBoundException {

		// Build the view
		Button selectedText = new Button(activity);

		// Add it to the view group
		ViewUtil.addView(activity, viewGroup, selectedText);

		// Build the handler
		new EnumHandler(activity, dataModel, schema, selectedText,
				valueHandler);

		return selectedText;
	}

	@Override
	final View buildListView(final Context context, final Field field) {
		TextView text = new TextView(context);
		text.setTag(field.name());
		return text;
	}

	@Override
	final void bindListView(final View view, final Cursor cursor,
			final Field field) {
		TextView text = (TextView) view.findViewWithTag(field.name());
		int index = cursor.getColumnIndex(field.name());
		int value = cursor.getInt(index);
		String symbol =  field.schema().getEnumSymbols().get(value);
		text.setText(symbol);
	}

	@Override
	final List<String> getProjectionFields(final Field field) {
		return getFieldNameProjection(field);
	}

}
