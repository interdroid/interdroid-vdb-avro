package interdroid.vdb.avro.view.factory;

import java.text.BreakIterator;

import interdroid.util.view.ViewUtil;
import interdroid.vdb.R;
import interdroid.vdb.avro.AvroSchemaProperties;
import interdroid.vdb.avro.control.handler.ArrayValueHandler;
import interdroid.vdb.avro.control.handler.RecordValueHandler;
import interdroid.vdb.avro.control.handler.ValueHandler;
import interdroid.vdb.avro.model.AvroRecordModel;
import interdroid.vdb.avro.model.NotBoundException;
import interdroid.vdb.avro.model.UriRecord;
import interdroid.vdb.avro.model.UriArray;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * This class knows how to build various views for editing data
 * represented by an avro schema.
 *
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
public final class AvroViewFactory {

	/**
	 * The logger we use to write to the log.
	 */
	private static final Logger LOG =
			LoggerFactory.getLogger(AvroViewFactory.class);

	/**
	 * Static factory. No construction.
	 */
	private AvroViewFactory() {
		// No construction please.
	}

	/**
	 * Constructs the root record scroll view and all sub-views.
	 * @param activity The activity the view will be placed in
	 * @param dataModel The data to be viewed
	 * @throws NotBoundException If the data model is not bound
	 */
	public static void buildRootView(final Activity activity,
			final AvroRecordModel dataModel) throws NotBoundException {

		LOG.debug("Constructing root view: " + dataModel.schema());
		ViewGroup viewGroup = (ViewGroup)
				ViewUtil.getLayoutInflater(activity).inflate(
						R.layout.avro_base_editor, null);
		final ScrollView scroll = new ScrollView(activity);
		scroll.setId(Integer.MAX_VALUE);
		scroll.setLayoutParams(
				new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT,
						LayoutParams.FILL_PARENT));
		scroll.addView(viewGroup);
		activity.runOnUiThread(new Runnable() {

			public void run() {
				activity.setContentView(scroll);
			}
		});

		buildRecordView(activity, dataModel, dataModel.getCurrentModel(),
				viewGroup);
	}

	/**
	 * Builds a root record view.
	 * @param activity the activity the views go in
	 * @param dataModel the model of the data
	 * @param record the record we are building for
	 * @param viewGroup the view group to put views into
	 * @return the record view
	 * @throws NotBoundException if the model is not bound
	 */
	private static View buildRecordView(final Activity activity,
			final AvroRecordModel dataModel, final UriRecord record,
			final ViewGroup viewGroup) throws NotBoundException {

		LOG.debug("Building record view. {}", record.getSchema().getName());

		// Construct a view for each field
		for (Field field : record.getSchema().getFields()) {
			LOG.debug("Building view for: " + field.name() + " in: "
					+ record.getSchema() + " schema:" + field.schema());
			buildFieldView(activity, dataModel, record, viewGroup, field);
		}

		return viewGroup;
	}

	/**
	 * Builds a view for a given field in a schema.
	 * @param activity the activity to build for
	 * @param dataModel the data model with the data
	 * @param viewGroup the view group to add views to
	 * @param schema the schema for the field
	 * @param field the field we are building
	 * @param uri the uri for the record or field
	 * @param valueHandler the value handler for the data
	 * @return a view for the data
	 * @throws NotBoundException if the model is not bound.
	 */
	private static View buildView(final Activity activity,
			final AvroRecordModel dataModel, final ViewGroup viewGroup,
			final Schema schema, final String field,
			final Uri uri, final ValueHandler valueHandler)
					throws NotBoundException {

		View view = AvroViewBuilder.getEditView(activity, dataModel,
				viewGroup, schema, field, uri, valueHandler);

		if (schema.getProp(AvroSchemaProperties.UI_VISIBLE) != null) {
			LOG.debug("Hiding view.");
			view.setVisibility(View.GONE);
		}
		if (schema.getProp(AvroSchemaProperties.UI_ENABLED) != null) {
			LOG.debug("Disabling view.");
			view.setEnabled(false);
		}

		return view;
	}

	/**
	 * Constructs a view for the given field.
	 * @param activity the activity with the views
	 * @param dataModel the model for the record
	 * @param record the record instance
	 * @param viewGroup the view group to add to
	 * @param field the field we are processing
	 * @return the constructed view
	 * @throws NotBoundException if the model isn't bound.
	 */
	private static View buildFieldView(final Activity activity,
			final AvroRecordModel dataModel, final UriRecord record,
			final ViewGroup viewGroup, final Field field)
					throws NotBoundException {

		// TODO: Add field comment as tiny text under the view?
		if (field.getProp(AvroSchemaProperties.UI_VISIBLE) == null) {
			TextView label = new TextView(activity);
			label.setText(toTitle(field));
			label.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT));
			label.setGravity(Gravity.LEFT);
			ViewUtil.addView(activity, viewGroup, label);
		}

		buildView(activity, dataModel, viewGroup, field.schema(), field.name(),
				record.getInstanceUri(),
				new RecordValueHandler(dataModel, record, field.name()));

		return viewGroup;
	}

	/**
	 * Constructs a view inside an array.
	 * @param activity the activity with the views
	 * @param dataModel the model for the data
	 * @param array the array with the data
	 * @param elementSchema the schema for the element
	 * @param field the field with the array
	 * @param uri the uri for the array
	 * @param offset the offset into the array
	 * @return the built view
	 * @throws NotBoundException if the array is not bound
	 */
	public static View buildArrayView(final Activity activity,
			final AvroRecordModel dataModel, final UriArray<Object> array,
			final Schema elementSchema, final String field,
			final Uri uri, final int offset)
					throws NotBoundException {

		View layout = LayoutInflater.from(activity)
				.inflate(R.layout.avro_array_item, null);
		buildView(activity, dataModel,
				(ViewGroup) layout.findViewById(R.id.array_layout),
				elementSchema, field, uri,
				new ArrayValueHandler(dataModel, field, array, offset));
		return layout;
	}

	// =-=-=-=- Some title helpers -=-=-=-=

	/**
	 * Returns the title for a given schema.
	 * @param schema the schema
	 * @return the title
	 */
	public static String toTitle(final Schema schema) {
		return toTitle(schema.getProp(AvroSchemaProperties.UI_LABEL),
				schema.getName(), false);
	}

	/**
	 * Returns the title for a given field.
	 * @param theField the field
	 * @return the title
	 */
	public static String toTitle(final Field theField) {
		return toTitle(theField.getProp(AvroSchemaProperties.UI_LABEL),
				theField.name(), true);
	}

	/**
	 * Returns the title. Performs '_' to ' ' conversion.
	 * @param label the label to put before the title. May be null
	 * @param name the name portion of the title
	 * @param includeColon true if a colon should be placed after the title
	 * @return the title
	 */
	private static String toTitle(final String label, final String name,
			final boolean includeColon) {

		StringBuffer sb = new StringBuffer();
		if (label != null) {
			sb.append(label);
		} else {
			String field = name.toLowerCase().replace('_', ' ');
			BreakIterator boundary = BreakIterator.getWordInstance();
			boundary.setText(field);
			boolean first = true;
			int start = boundary.first();
			for (int end = boundary.next();
					end != BreakIterator.DONE;
					start = end, end = boundary.next()) {
				if (first) {
					first = false;
				} else {
					sb.append(" ");
				}
				sb.append(field.substring(start, start + 1).toUpperCase());
				sb.append(field.substring(start + 1, end));
			}
		}
		if (includeColon) {
			sb.append(":");
		}
		return sb.toString();
	}

	/**
	 * Returns a title with a static label in front of it.
	 * @param activity the activity to get the label from
	 * @param label the label to get
	 * @param schema the schema to get the title from
	 * @return the title with label in front
	 */
	public static CharSequence toTitle(
			final Activity activity, final int label, final Schema schema) {
		return activity.getString(label) + " " + toTitle(schema);
	}

}
