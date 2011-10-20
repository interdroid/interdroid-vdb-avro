package interdroid.vdb.avro.view.factory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import interdroid.vdb.avro.control.handler.value.ValueHandler;
import interdroid.vdb.avro.model.AvroRecordModel;
import interdroid.vdb.avro.model.NotBoundException;
import interdroid.vdb.avro.view.DataFormatUtil;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A builder for immutable "timestamp" entries.
 *
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
public class AvroTimestampBuilder extends AvroTypedTextViewBuilder {

	/**
	 * Construct a builder for timestamps.
	 */
	protected AvroTimestampBuilder() {
		super(Type.LONG, "timestamp");
	}

	@Override
	protected final View buildEditView(final Activity activity,
			final AvroRecordModel dataModel, final ViewGroup viewGroup,
			final Schema schema, final Field field,
			final Uri uri, final ValueHandler valueHandler)
					throws NotBoundException {

		// Timestamps are immutable.
		DateFormat format = SimpleDateFormat.getDateTimeInstance();

		return buildTextView(activity, viewGroup,
				format.format(new Date((Long) valueHandler.getValue())));
	}

	@Override
	final void bindListView(final View view, final Cursor cursor,
			final Field field) {
		TextView text = (TextView) view.findViewWithTag(field.name());
		int index = cursor.getColumnIndex(field.name());
		text.setText(
				DataFormatUtil.formatTimestampForDisplay(
						cursor.getLong(index)));
	}

}
