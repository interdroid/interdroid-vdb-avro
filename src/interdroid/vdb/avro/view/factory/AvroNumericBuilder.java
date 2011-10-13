package interdroid.vdb.avro.view.factory;


import interdroid.vdb.avro.control.handler.EditTextHandler;
import interdroid.vdb.avro.control.handler.ValueHandler;
import interdroid.vdb.avro.model.AvroRecordModel;
import interdroid.vdb.avro.model.NotBoundException;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A builder for numeric data types.
 * This includes: Type.LONG, Type.INT, Type.DOUBLE, Type.FLOAT.
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
class AvroNumericBuilder extends AvroTypedTextViewBuilder {

	/**
	 * Construct a builder for numeric types.
	 */
	public AvroNumericBuilder() {
		super(getSupportedTypes());
	}

	/**
	 * @return the types this builder supports
	 */
	private static AvroViewType[] getSupportedTypes() {
		return new AvroViewType[] {
				new AvroViewType(Type.LONG),
				new AvroViewType(Type.INT),
				new AvroViewType(Type.DOUBLE),
				new AvroViewType(Type.FLOAT)
		};
	}


	@Override
	public final View buildEditView(final Activity activity,
			final AvroRecordModel dataModel,
			final ViewGroup viewGroup, final Schema schema,
			final Field field, final Uri uri,
			final ValueHandler valueHandler) throws NotBoundException {
		int flags;
		switch (schema.getType()) {
		case INT:
		case LONG:
			flags = InputType.TYPE_CLASS_NUMBER
			| InputType.TYPE_NUMBER_FLAG_DECIMAL
			| InputType.TYPE_NUMBER_FLAG_SIGNED;
			break;
		case DOUBLE:
		case FLOAT:
			flags = InputType.TYPE_CLASS_NUMBER
			| InputType.TYPE_NUMBER_FLAG_SIGNED;
			break;
		default:
			throw new RuntimeException("Unsupported type: " + schema);
		}

		return buildEditText(activity, viewGroup, schema, flags,
				new EditTextHandler(dataModel, schema.getType(), valueHandler)
				);
	}

	@Override
	final void bindListView(final View view, final Cursor cursor,
			final Field field) {
		TextView text = (TextView) view.findViewWithTag(field);
		int index = cursor.getColumnIndex(field.name());
		switch (field.schema().getType()) {
		case INT:
			text.setText(String.valueOf(cursor.getInt(index)));
			break;
		case LONG:
			text.setText(String.valueOf(cursor.getLong(index)));
			break;
		case DOUBLE:
			text.setText(String.valueOf(cursor.getDouble(index)));
			break;
		case FLOAT:
			text.setText(String.valueOf(cursor.getFloat(index)));
		default:
			throw new RuntimeException("Unsupported type: " + field);
		}
	}
}
