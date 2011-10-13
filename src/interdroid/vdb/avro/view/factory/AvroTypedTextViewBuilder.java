package interdroid.vdb.avro.view.factory;

import java.util.List;

import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;

import android.content.Context;
import android.view.View;

/**
 * This class provides implementations for buildListView and getProjectionFields
 * for types which are using a single field name value and a text view to
 * display it.
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
public abstract class AvroTypedTextViewBuilder extends AvroTypedViewBuilder {

	/**
	 * Construct a builder.
	 * @param type the type to support
	 * @param widget the widget to support
	 */
	protected AvroTypedTextViewBuilder(final Type type, final String widget) {
		super(type, widget);
	}

	/**
	 * Construct a builder.
	 * @param type the type to support
	 */
	protected AvroTypedTextViewBuilder(final Type type) {
		super(type);
	}

	/**
	 * Construct a builder.
	 * @param types the types to support.
	 */
	protected AvroTypedTextViewBuilder(final AvroViewType[] types) {
		super(types);
	}

	@Override
	final View buildListView(final Context context, final Field field) {
		return buildTextView(context, field);
	}


	@Override
	final List<String> getProjectionFields(final Field field) {
		return getFieldNameProjection(field);
	}

}
