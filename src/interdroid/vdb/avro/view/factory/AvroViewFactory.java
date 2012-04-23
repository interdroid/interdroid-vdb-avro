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
package interdroid.vdb.avro.view.factory;

import java.text.BreakIterator;
import java.util.List;

import interdroid.util.view.LayoutUtil.LayoutParameters;
import interdroid.util.view.LayoutUtil.LayoutWeight;
import interdroid.util.view.ViewUtil;
import interdroid.vdb.avro.R;
import interdroid.vdb.avro.AvroSchemaProperties;
import interdroid.vdb.avro.control.handler.ArrayHandler;
import interdroid.vdb.avro.control.handler.value.ArrayValueHandler;
import interdroid.vdb.avro.control.handler.value.RecordValueHandler;
import interdroid.vdb.avro.control.handler.value.ValueHandler;
import interdroid.vdb.avro.model.AvroRecordModel;
import interdroid.vdb.avro.model.NotBoundException;
import interdroid.vdb.avro.model.UriRecord;
import interdroid.vdb.avro.model.UriArray;
import interdroid.vdb.content.EntityUriBuilder;
import interdroid.vdb.content.EntityUriMatcher;
import interdroid.vdb.content.EntityUriMatcher.UriMatch;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableRow;
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
	 * The largest size image to show in the image view.
	 */
	static final int	MAX_LIST_IMAGE_SIZE			= 150;

	/**
	 * The default font size for labels in a list.
	 */
	private static final float	DEFAULT_LABEL_FONT_SIZE		= 9;

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
		LayoutParameters.setViewGroupLayoutParams(
				LayoutParameters.W_FILL_H_FILL, scroll);
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
			final Schema schema, final Field field,
			final Uri uri, final ValueHandler valueHandler)
					throws NotBoundException {

		View view = AvroViewBuilder.getEditView(activity, dataModel,
				viewGroup, schema, field, uri, valueHandler);

		if (field != null) {
			if (field.schema().getProp(
					AvroSchemaProperties.UI_VISIBLE) != null) {
				LOG.debug("Hiding view.");
				view.setVisibility(View.GONE);
			}
			if (field.schema().getProp(
					AvroSchemaProperties.UI_ENABLED) != null) {
				LOG.debug("Disabling view.");
				view.setEnabled(false);
			}
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
			LayoutParameters.setViewGroupLayoutParams(
					LayoutParameters.W_WRAP_H_WRAP, label);
			label.setGravity(Gravity.LEFT);
			ViewUtil.addView(activity, viewGroup, label);
		}

		buildView(activity, dataModel, viewGroup, field.schema(), field,
				record.getInstanceUri(),
				new RecordValueHandler(dataModel, record, field.name()));

		return viewGroup;
	}

	/**
	 * Returns or constructs UriRecord and sets in valueHandler.
	 * @param activity the activity we are working in
	 * @param arrayHandler the value handler with the data
	 * @param uri the uri for the record
	 * @param offset the offset into the array
	 * @oaram uri the uri for the array
	 * @param schema the schema for the record
	 * @return a UriRecord.
	 */
	private static UriRecord getRecord(final Activity activity,
			final ArrayHandler arrayHandler, final int offset, final Uri uri,
			final Schema schema) {

		UriRecord subRecord = (UriRecord) arrayHandler.getItem(offset);
		if (subRecord == null) {
			UriMatch match = EntityUriMatcher.getMatch(uri);
			Uri pathUri = Uri.withAppendedPath(
					EntityUriBuilder.branchUri(match.authority,
							match.repositoryName, match.reference),
							schema.getName());
			LOG.debug("Storing to path: {}", pathUri);
			pathUri = activity.getContentResolver().insert(pathUri,
					new ContentValues());
			subRecord = new UriRecord(pathUri, schema);
			arrayHandler.setItem(offset, subRecord);
		}
		return subRecord;
	}

	/**
	 * Constructs a view inside an array.
	 * @param activity the activity with the views
	 * @param dataModel the model for the data
	 * @param array the array with the data
=	 * @param field the field of the array
	 * @param offset the offset into the array
	 * @param arrayHandler the handler for the array data
	 * @return the built view
	 * @throws NotBoundException if the array is not bound
	 */
	public static View buildArrayView(final Activity activity,
			final AvroRecordModel dataModel, final ArrayHandler arrayHandler,
			final UriArray<Object> array, final Field field,
final int offset)
					throws NotBoundException {

		View layout = LayoutInflater.from(activity)
				.inflate(R.layout.avro_array_item, null);
		ViewGroup viewGroup = (ViewGroup)
				layout.findViewById(R.id.array_layout);

		// Records are special since we want to allow them instead of doing
		// a button here which is what the default builder does.
		if (array.getSchema().getElementType().getType().equals(Type.RECORD)) {
			UriRecord subRecord = getRecord(activity, arrayHandler,
					offset, array.getInstanceUri(),
					array.getSchema().getElementType());
			buildRecordView(activity, dataModel, subRecord, viewGroup);
		} else {
			buildView(activity, dataModel, viewGroup,
					array.getSchema().getElementType(), null,
					array.getInstanceUri(),
					new ArrayValueHandler(dataModel, field.name(), array,
							offset));
		}
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

	/**
	 * Builds a view for use in a list, including the label.
	 * @param context the context to build in
	 * @param field the field to build a view for
	 * @return the built list view
	 */
	public static View buildListView(final Context context, final Field field) {
		TableRow row = null;
		View view = AvroViewBuilder.getListView(context, field);
		if (view != null) {
			row = new TableRow(context);
			row.setOrientation(LinearLayout.HORIZONTAL);

			TextView label = new TextView(context);
			label.setText(toTitle(field));
			label.setTextSize(TypedValue.COMPLEX_UNIT_PT,
					DEFAULT_LABEL_FONT_SIZE);
			LayoutParameters.setTableRowParams(
					LayoutParameters.W_WRAP_H_WRAP, LayoutWeight.Quarter,
					label);
			row.addView(label);

			if (field.schema().getProp(AvroSchemaProperties.UI_VISIBLE)
					!= null) {
				LOG.debug("Hiding view.");
				row.setVisibility(View.GONE);
			}
			if (field.schema().getProp(AvroSchemaProperties.UI_ENABLED)
					!= null) {
				LOG.debug("Disabling view.");
				row.setEnabled(false);
			}

			LayoutParameters.setTableRowParams(
					LayoutParameters.W_WRAP_H_WRAP, LayoutWeight.ThreeQuarters,
					view);
			row.addView(view);
		}
		return row;
	}

	/**
	 * Binds a list view to the data in the cursor for the given field.
	 * @param view the view to bind
	 * @param cursor the cursor with data
	 * @param field the field to bind
	 */
	public static void bindListView(final View view, final Cursor cursor,
			final Field field) {
		AvroViewBuilder.bindListView(view, cursor, field);
	}

	/**
	 * @param field the field to get projection fields for
	 * @return the list of projection column names
	 */
	public static List<String> getProjectionFields(final Field field) {
		return AvroViewBuilder.getProjectionFields(field);
	}

	/**
	 * Appends a field as a title.
	 * @param ret the string buffer to append to
	 * @param cursor the cursor to get data from
	 * @param field the field to append
	 */
	public static void appendTitleField(final StringBuffer ret,
			final Cursor cursor, final Field field) {
		String title = toTitle(field);
		if (title != null) {
			if (ret.length() > 0) {
				ret.append(' ');
			}
			ret.append(title);
		}
	}

}
