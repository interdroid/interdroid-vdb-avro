package interdroid.vdb.avro.view.factory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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
 * A builder for immutable "timestamp" entries.
 *
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
public class AvroTimestampBuilder extends AvroViewBuilder {

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

}
