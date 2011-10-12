package interdroid.vdb.avro.view.factory;

import interdroid.vdb.R;
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

/**
 * Builder for Type.NULL.
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
class AvroNullBuilder extends AvroViewBuilder {

	/**
	 * Construct a builder for type NULL.
	 */
	protected AvroNullBuilder() {
		super(Type.NULL);
	}

	@Override
	public final View buildEditView(final Activity activity,
			final AvroRecordModel dataModel,
			final ViewGroup viewGroup, final Schema schema,
			final Field field, final Uri uri,
			final ValueHandler valueHandler) throws NotBoundException {
		return buildTextView(activity, viewGroup, R.string.null_text);
	}

}
