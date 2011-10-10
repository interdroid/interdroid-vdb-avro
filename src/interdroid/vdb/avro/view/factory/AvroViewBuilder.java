package interdroid.vdb.avro.view.factory;

import java.util.HashMap;
import java.util.Map;

import interdroid.util.view.ViewUtil;
import interdroid.vdb.avro.AvroSchemaProperties;
import interdroid.vdb.avro.control.handler.EditTextHandler;
import interdroid.vdb.avro.control.handler.ValueHandler;
import interdroid.vdb.avro.model.AvroRecordModel;
import interdroid.vdb.avro.model.NotBoundException;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.AbsListView.LayoutParams;

/**
 * This class knows how to build view of various types using
 * subclasses.
 *
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
abstract class AvroViewBuilder {
	/**
	 * Access to logging interface.
	 */
	private static Logger logger =
			LoggerFactory.getLogger(AvroViewBuilder.class);

	// =-=-=-=- Static Interface -=-=-=-=

	/**
	 * A hash of builders by type so we can find them quickly.
	 */
	private static Map<AvroViewType, AvroViewBuilder> sBuilders =
			new HashMap<AvroViewType, AvroViewBuilder>();

	/**
	 * The various builder subclasses we can use to assist us.
	 */
	private static AvroViewBuilder[] sBuilderInstances = new AvroViewBuilder[] {
		new AvroArrayBuilder(),
		new AvroBooleanBuilder(),
		new AvroDateBuilder(),
		new AvroEnumBuilder(),
		new AvroLocationBuilder(),
		new AvroNullBuilder(),
		new AvroNumericBuilder(),
		new AvroPhotoBuilder(),
		new AvroRecordBuilder(),
		new AvroStringBuilder(),
		new AvroTimeBuilder(),
		new AvroUnionBuilder(),
	};

	static {
		// Initialize the hash for quick access to builders
		for (AvroViewBuilder builder : sBuilderInstances) {
			for (AvroViewType type : builder.mTypes) {
				sBuilders.put(type, builder);
			}
		}
	}


	/**
	 * Builds an edit view.
	 * @param activity the activity the view goes in
	 * @param dataModel the data model to get data from
	 * @param viewGroup the view group to add the view to
	 * @param schema the schema for the data
	 * @param field the field
	 * @param uri the uri for the field
	 * @param valueHandler the value handler to set data with
	 * @return The view.
	 * @throws NotBoundException if the model is not bound
	 */
	public static View getEditView(final Activity activity,
			final AvroRecordModel dataModel, final ViewGroup viewGroup,
			final Schema schema, final String field, final Uri uri,
			final ValueHandler valueHandler)
					throws NotBoundException {

		if (schema.getProp(AvroSchemaProperties.UI_RESOURCE) != null) {

			logger.debug("Inflating custom resource: {}",
					schema.getProp(AvroSchemaProperties.UI_RESOURCE));
			try {

				View view = ViewUtil.getLayoutInflater(activity).inflate(
						Integer.valueOf(
								schema.getProp(
										AvroSchemaProperties.UI_RESOURCE)),
										null);
				return view;

			} catch (Exception e) {
				logger.error("Unable to inflate resource: {}",
						schema.getProp(AvroSchemaProperties.UI_RESOURCE));
				throw new RuntimeException("Unable to inflate UI resource: "
						+ schema.getProp(AvroSchemaProperties.UI_RESOURCE), e);
			}

		} else {

			// Find the builder for this type
			AvroViewBuilder builder = sBuilders.get(new AvroViewType(schema));
			if (builder == null) {
				logger.error("No builder for schema: {}", schema);
				throw new RuntimeException(
						"Don't know how to build a view for: " + schema);
			}

			return builder.buildEditView(activity, dataModel, viewGroup,
					schema, field, uri, valueHandler);
		}
	}

	// =-=-=-=- instance variables setup by the subclass -=-=-=-=

	/**
	 * The type this builder knows how to build.
	 */
	private final AvroViewType[] mTypes;

	// =-=-=-=- Constructors used by subclasses -=-=-=-=

	/**
	 * Construct a view builder which supports the given types.
	 * @param types The types this builder supports
	 */
	protected AvroViewBuilder(final AvroViewType[] types) {
		mTypes = types;
	}

	/**
	 * Construct a view builder which support just one type.
	 * @param type The type this builder supports
	 * @param widget The widget this builder supports
	 */
	protected AvroViewBuilder(final Type type, final String widget) {
		mTypes = new AvroViewType[] { new AvroViewType(type, widget) };
	}

	/**
	 * Construct a view builder which support just one type.
	 * @param type The type this builder supports
	 */
	protected AvroViewBuilder(final Type type) {
		mTypes = new AvroViewType[] { new AvroViewType(type) };
	}

	// =-=-=-=- protected utility classes used by subclasses =-=-=-=-

	/**
	 * @return the type this builder knows how to build
	 */
	protected final AvroViewType[] getViewTypes() {
		return mTypes;
	}

	/**
	 * Builds an edit text view.
	 * @param activity the activity to build for
	 * @param viewGroup the view group to add to
	 * @param schema the schema to work with
	 * @param inputType the input type parameters
	 * @param textWatcher the text watcher
	 * @return the built view
	 */
	protected static View buildEditText(final Activity activity,
			final ViewGroup viewGroup, final Schema schema, final int inputType,
			final EditTextHandler textWatcher) {
		logger.debug("Building edit text for: " + schema);
		EditText text = null;

		text = new EditText(activity);
		text.setLayoutParams(new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		text.setGravity(Gravity.FILL_HORIZONTAL);
		text.setInputType(inputType);

		ViewUtil.addView(activity, viewGroup, text);
		textWatcher.setWatched(text);
		return text;
	}

	/**
	 * Builds a text view with the given text resource.
	 * @param activity the activity to build in
	 * @param viewGroup the view group to add to
	 * @param textId the id of the text to set the text to
	 * @return the built view
	 */
	protected static View buildTextView(final Activity activity,
			final ViewGroup viewGroup, final int textId) {
		TextView text = new TextView(activity);
		text.setText(textId);
		ViewUtil.addView(activity, viewGroup, text);
		return text;
	}

	// =-=-=-=- Subclass interface -=-=-=-=

	/**
	 * Builds an edit view.
	 * @param activity the activity the view goes in
	 * @param dataModel the data model to get data from
	 * @param viewGroup the view group to add the view to
	 * @param schema the schema for the data
	 * @param field the field
	 * @param uri the uri for the field
	 * @param valueHandler the value handler to set data with
	 * @return The view.
	 * @throws NotBoundException if the model is not bound
	 */
	protected abstract View buildEditView(final Activity activity,
			final AvroRecordModel dataModel, final ViewGroup viewGroup,
			final Schema schema, final String field, final Uri uri,
			final ValueHandler valueHandler) throws NotBoundException;

}
