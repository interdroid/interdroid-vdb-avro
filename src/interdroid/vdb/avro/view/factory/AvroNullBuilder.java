package interdroid.vdb.avro.view.factory;

import java.util.List;

import interdroid.vdb.avro.R;
import interdroid.vdb.avro.control.handler.value.ValueHandler;
import interdroid.vdb.avro.model.AvroRecordModel;
import interdroid.vdb.avro.model.NotBoundException;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Builder for Type.NULL.
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
class AvroNullBuilder extends AvroTypedViewBuilder {

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

	@Override
	final View buildListView(final Context context, final Field field) {
		TextView view = new TextView(context);
		view.setText(R.string.null_text);
		return view;
	}

	@Override
	final void bindListView(final View view, final Cursor cursor,
			final Field field) {
		// Nothing to do
	}

	@Override
	final List<String> getProjectionFields(final Field field) {
		// Nothing to do
		return null;
	}

}
