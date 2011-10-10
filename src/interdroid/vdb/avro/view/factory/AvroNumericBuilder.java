package interdroid.vdb.avro.view.factory;

import interdroid.vdb.avro.control.handler.EditTextHandler;
import interdroid.vdb.avro.control.handler.ValueHandler;
import interdroid.vdb.avro.model.AvroRecordModel;
import interdroid.vdb.avro.model.NotBoundException;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;

import android.app.Activity;
import android.net.Uri;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;

/**
 * A builder for numeric data types.
 * This includes: Type.LONG, Type.INT, Type.DOUBLE, Type.FLOAT.
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
class AvroNumericBuilder extends AvroViewBuilder {

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
			final String field, final Uri uri,
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
}
