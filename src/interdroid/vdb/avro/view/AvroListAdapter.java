package interdroid.vdb.avro.view;

import interdroid.vdb.avro.R;

import java.util.ArrayList;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * This class provides a list adapter for data stored by an Avro VDB.
 *
 * @author nick <palmer@cs.vu.nl>
 *
 */
public class AvroListAdapter extends CursorAdapter {

	/**
	 * The largest size image to show in the image view.
	 */
	private static final int	MAX_LIST_IMAGE_SIZE			= 150;

	/**
	 * The default font size for elements in a list.
	 */
	private static final int	DEFAULT_ELEMENT_FONT_SIZE	= 12;

	/**
	 * The default font size for labels in a list.
	 */
	private static final float	DEFAULT_LABEL_FONT_SIZE		= 9;

	/** Our logging interface. */
	private static final Logger LOG =
			LoggerFactory.getLogger(AvroListAdapter.class);

	/**
	 * The schema for the data.
	 */
	private Schema mSchema;

	/**
	 * The Uri we are listing.
	 */
	private Uri mUri;

	/**
	 * The title fields.
	 */
	private String[] mTitleFields;

	/**
	 * The string to use to mean "This".
	 */
	private String	mThis;

	/**
	 * Build a list adapter to work for the given AvroBaseList.
	 *
	 * @param context the base list this will work for
	 * @param schema the schema for the data being listed
	 * @param uri the uri for the data to show
	 */
	public AvroListAdapter(final AvroBaseList context, final Schema schema,
			final Uri uri) {
		super(context, getCursor(context, schema, uri));
		mThis = context.getString(R.string.title_this) + " " + schema.getName();
		mSchema = schema;
		mUri = uri;
		mTitleFields = getTitleFields(schema);
	}

	/**
	 * Returns the title fields for a given schema.
	 * @param schema the schema to get the title fields from
	 * @return the title field names
	 */
	private String[] getTitleFields(final Schema schema) {
		ArrayList<String> title = new ArrayList<String>();

		for (Field field : schema.getFields()) {
			if (propertyIsSet(field, "ui.title")) {
				title.add(field.name());
			}
		}

		return title.toArray(new String[title.size()]);
	}

	/**
	 *
	 * @param context the AvroBaseList this will run in
	 * @param schema the schema for the data
	 * @param uri the uri for the data
	 * @return a cursor for the given uri
	 */
	private static Cursor getCursor(final AvroBaseList context,
			final Schema schema, final Uri uri) {
		return context.managedQuery(uri,
				getProjection(schema), null, null,
				getSortOrder(schema));
	}

	/**
	 * Returns the default sort order for the given schema.
	 * @param schema the schema for which the sort order is desired
	 * @return the sort order portion of the query.
	 */
	private static String getSortOrder(final Schema schema) {
		// TODO: This should come from the sort order on the fields
		return schema.getProp("ui.default_sort");
	}

	/**
	 * Returns an array of field names which are required to list data
	 * for the given schema.
	 * @param schema the schema to list data for
	 * @return an array of field names
	 */
	private static String[] getProjection(final Schema schema) {
		ArrayList<String> listFields = new ArrayList<String>();
		// Add the _id field be in the PROJECTION.
		// Since this is synthetic we add it here.
		listFields.add("_id");

		for (Field field : schema.getFields()) {
			if (isListField(field)) {
				LOG.debug("List field includes: {}", field.name());
				listFields.add(field.name());
				// TODO: For unions we need additional fields
				// TODO: For subrecords we would need to do some kind of join
			} else {
				LOG.debug("Skipping field {} from list view.", field.name());
			}
		}

		return listFields.toArray(new String[listFields.size()]);
	}

	/**
	 *
	 * @param schema the schema to check
	 * @return true if the type can be listed
	 */
	private static boolean isValidListType(final Schema schema) {
		return isBasicType(schema)
				|| isValidUnionType(schema)
				|| isValidRecordType(schema);
	}

	/**
	 *
	 * @param schema the schema to check
	 * @return true if the type is a union type that can be listed
	 */
	private static boolean isValidUnionType(final Schema schema) {
		if (schema.getType().equals(Type.UNION)) {
			return false;
		}
		for (Schema branch : schema.getTypes()) {
			if (!isBasicType(branch)
					&& !isValidUnionType(branch)
					&& !isValidRecordType(branch)) {
				return false;
			}
		}
		return true;
	}

	/**
	 *
	 * @param schema the schema
	 * @return true if this record can be in a list
	 */
	private static boolean isValidRecordType(final Schema schema) {
		// TODO: We would like to support location here but does it make sense?
		// The content provider would need a subquery and that would
		// totally kill performance. We want to join the image...
		// So maybe how we store locations needs to change...
		return false;
	}

	/**
	 *
	 * @param schema the schema for the field
	 * @return true if the field is a basic type
	 */
	private static boolean isBasicType(final Schema schema) {
		switch (schema.getType()) {
		case BOOLEAN:
		case DOUBLE:
		case ENUM:
		case FLOAT:
		case INT:
		case LONG:
		case STRING:
			return true;
		case BYTES:
			if (schema.getProp("ui.widget") != null
					&& (schema.getProp("ui.widget").equals("photo")
					|| schema.getProp("ui.widget").equals("video"))) {
				return true;
			}
		default:
			return false;
		}
	}

	/**
	 *
	 * @param field the field to check
	 * @return true if the field is in the list
	 */
	private static boolean isListField(final Field field) {
		return isListedField(field) && isValidListType(field.schema());
	}

	/**
	 *
	 * @param field the field to check
	 * @return true if the field is marked with ui.list
	 */
	private static boolean isListedField(final Field field) {
		return propertyIsSet(field, "ui.list");
	}

	/**
	 *
	 * @param field the field to check
	 * @param property the property to check
	 * @return true if the property in the field is set to true
	 */
	private static boolean propertyIsSet(final Field field,
			final String property) {
		boolean isSet = field.getProp(property) != null
				&& Boolean.TRUE.equals(
						Boolean.parseBoolean(field.getProp(property)));
		return isSet;
	}

	@Override
	public final View newView(final Context context, final Cursor cursor,
			final ViewGroup parent) {
		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);

		for (Field field : mSchema.getFields()) {
			if (isListField(field)) {
				buildView(context, layout, cursor, field);
			}
		}

		// Do we need to call this?
		bindView(layout, context, cursor);

		return layout;
	}

	/**
	 * Build a view for a given field.
	 * @param context the activity we are working in
	 * @param layout the layout to add the view to
	 * @param cursor the cursor with the data in it
	 * @param field the field we are working on
	 */
	private void buildView(final Context context, final LinearLayout layout,
			final Cursor cursor, final Field field) {
		LinearLayout row = new LinearLayout(context);
		row.setOrientation(LinearLayout.HORIZONTAL);
		LinearLayout.LayoutParams zeroWeight =
				new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.WRAP_CONTENT, 0);
		LinearLayout.LayoutParams oneWeight =
				new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.WRAP_CONTENT, 1);
		TextView label = new TextView(context);
		label.setText(field.name() + ": ");
		label.setTextSize(TypedValue.COMPLEX_UNIT_PT, DEFAULT_LABEL_FONT_SIZE);
		row.addView(label, zeroWeight);
		layout.addView(row);
		switch (field.schema().getType()) {
		case BOOLEAN:
			CheckBox booleanView =  new CheckBox(context);
			booleanView.setTag(field.name());
			row.addView(booleanView, oneWeight);
			break;
		case DOUBLE:
		case FLOAT:
		case INT:
		case LONG:
		case STRING:
		case ENUM:
			TextView view = new TextView(context);
			view.setTextSize(TypedValue.COMPLEX_UNIT_PT, DEFAULT_ELEMENT_FONT_SIZE);
			view.setTag(field.name());
			row.addView(view, oneWeight);
			break;
		case BYTES:
			ImageView image = new ImageView(context);
			image.setTag(field.name());
			row.addView(image, oneWeight);
			break;
		case RECORD:

			break;
		case UNION:

			break;
		default:
			break;
		}
	}

	@Override
	public final void bindView(final View recycleView, final Context context,
			final Cursor cursor) {
		View view = recycleView;

		if (view == null) {
			view = newView(context, cursor, null);
		}

		for (Field field : mSchema.getFields()) {
			if (isListField(field)) {
				bindView(view, cursor, field);
			}
		}
	}

	/**
	 * Binds a view to the cursor for the given field.
	 * @param view the view to fill
	 * @param cursor the cursor with the data
	 * @param field the field we ware binding
	 */
	private void bindView(final View view, final Cursor cursor,
			final Field field) {
		View value = view.findViewWithTag(field.name());
		int index = cursor.getColumnIndex(field.name());

		switch (field.schema().getType()) {
		case BOOLEAN:
			CheckBox booleanView =  (CheckBox) value;
			booleanView.setChecked(cursor.getInt(index) == 0);
			break;
		case DOUBLE:
		case FLOAT:
		case INT:
		case LONG:
		case STRING:
		case ENUM:
			TextView textView = (TextView) value;
			textView.setText(cursor.getString(index));
			break;
		case BYTES:
			ImageView image = (ImageView) value;
			byte[] data = cursor.getBlob(index);
			Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
			// 200 x 100 -> 100 / 50
			// 100 x 200 -> 50 / 100
			int width, height;
			float aspect = (float) bitmap.getHeight()
					/ (float) bitmap.getWidth();
			if (bitmap.getHeight() > bitmap.getWidth()) {
				height = MAX_LIST_IMAGE_SIZE;
				width = (int) (bitmap.getWidth() / aspect);
			} else {
				width = MAX_LIST_IMAGE_SIZE;
				height = (int) (bitmap.getHeight() * aspect);
			}

			image.setImageBitmap(
					Bitmap.createScaledBitmap(bitmap, width, height, true));
			break;
		case RECORD:

			break;
		case UNION:

			break;
		default:
			break;
		}
	}

	/**
	 * Returns a title for the given cursor.
	 * @param cursor the cursor with the data
	 * @return the title
	 */
	public final CharSequence getTitle(final Cursor cursor) {
		if (mTitleFields.length > 0) {
			StringBuffer ret = new StringBuffer();
			for (String field : mTitleFields) {
				switch (mSchema.getField(field).schema().getType()) {
				case STRING:
					cursor.getString(cursor.getColumnIndex(field));
					break;
				// TODO: We could do other fields, but why?
				default:
					break;
				}
			}
			if (ret.length() > 0) {
				return ret.toString();
			}
		}
		return mThis;
	}

}
