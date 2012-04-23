/*
 * Copyright (c) 2008-2012 Vrije Universiteit, The Netherlands All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the Vrije Universiteit nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ``AS IS''
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package interdroid.vdb.avro.view;

import interdroid.vdb.avro.AvroSchemaProperties;
import interdroid.vdb.avro.R;
import interdroid.vdb.avro.view.factory.AvroViewFactory;

import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TableLayout;

/**
 * This class provides a list adapter for data stored by an Avro VDB.
 *
 * @author nick <palmer@cs.vu.nl>
 *
 */
public class AvroListAdapter extends CursorAdapter {

	/** Our logging interface. */
	private static final Logger LOG =
			LoggerFactory.getLogger(AvroListAdapter.class);

	/**
	 * The schema for the data.
	 */
	private Schema mSchema;

	/**
	 * The title fields.
	 */
	private Field[] mTitleFields;

	/**
	 * The string to use to mean "This".
	 */
	private String	mThis;

	/**
	 * A field for when there is nothing in the list view.
	 */
	private static final Field sIdField =
			new Field("_id", Schema.create(Type.INT), null, null);


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
		mTitleFields = getTitleFields(schema);
	}

	/**
	 * Returns the title fields for a given schema.
	 * @param schema the schema to get the title fields from
	 * @return the title field names
	 */
	private Field[] getTitleFields(final Schema schema) {
		ArrayList<Field> title = new ArrayList<Field>();

		boolean foundOne = false;
		for (Field field : schema.getFields()) {
			if (propertyIsSet(field, AvroSchemaProperties.UI_TITLE)) {
				title.add(field);
				foundOne = true;
			}
		}
		// Add the _id field if we found none.
		if (!foundOne) {
			title.add(sIdField);
		}

		return title.toArray(new Field[title.size()]);
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
				List<String> fields =
						AvroViewFactory.getProjectionFields(field);
				if (fields != null) {
					listFields.addAll(fields);
				}
			} else {
				LOG.debug("Skipping field {} from list view.", field.name());
			}
		}
		if (listFields.isEmpty()) {
			LOG.debug("No list fields. Adding _id.");
			// No list fields. Add the _id field.
			listFields.add("_id");
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
		if (!schema.getType().equals(Type.UNION)) {
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
		TableLayout layout = new TableLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);

		boolean builtOne = false;
		for (Field field : mSchema.getFields()) {
			if (isListField(field)) {
				buildView(context, layout, field);
				builtOne = true;
			}
		}

		// There were no list fields so bind the _id field
		if (!builtOne) {
			buildView(context, layout, sIdField);
		}

		// Bind the data in the cursor.
		bindView(layout, context, cursor);

		return layout;
	}

	/**
	 * Build a view for a given field.
	 * @param context the activity we are working in
	 * @param layout the layout to add the view to
	 * @param field the field we are working on
	 */
	private void buildView(final Context context, final LinearLayout layout,
			final Field field) {
		View view = AvroViewFactory.buildListView(context, field);
		if (view != null) {
			layout.addView(view);
		}
	}

	@Override
	public final void bindView(final View recycleView, final Context context,
			final Cursor cursor) {
		View view = recycleView;

		if (view == null) {
			view = newView(context, cursor, null);
		}

		boolean boundOne = false;
		for (Field field : mSchema.getFields()) {
			if (isListField(field)) {
				bindView(view, cursor, field);
				boundOne = true;
			}
		}

		// We didn't bind one, so bind the _id field
		if (!boundOne) {
			bindView(view, cursor, sIdField);
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
		AvroViewFactory.bindListView(view, cursor, field);
	}

	/**
	 * Returns a title for the given cursor.
	 * @param cursor the cursor with the data
	 * @return the title
	 */
	public final CharSequence getTitle(final Cursor cursor) {
		if (mTitleFields.length > 0) {
			StringBuffer ret = new StringBuffer();
			for (Field field : mTitleFields) {
				AvroViewFactory.appendTitleField(ret, cursor, field);
			}
			if (ret.length() > 0) {
				return ret.toString();
			}
		}
		return mThis;
	}

}
