package interdroid.vdb.avro.view.factory;

import interdroid.util.view.ViewUtil;
import interdroid.vdb.avro.control.handler.EnumHandler;
import interdroid.vdb.avro.control.handler.ValueHandler;
import interdroid.vdb.avro.model.AvroRecordModel;
import interdroid.vdb.avro.model.NotBoundException;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;

import android.app.Activity;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * A builder for Type.ENUM && widget == null.
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
class AvroEnumBuilder extends AvroViewBuilder {

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

}
