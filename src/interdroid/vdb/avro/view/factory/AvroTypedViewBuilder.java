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

import interdroid.util.view.LayoutUtil.LayoutParameters;
import interdroid.util.view.ViewUtil;
import interdroid.vdb.avro.control.handler.value.ValueHandler;
import interdroid.vdb.avro.model.AvroRecordModel;
import interdroid.vdb.avro.model.NotBoundException;

import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

/**
 * The base class for all view builder instances. Subclasses know how
 * to build a view for a particular type or types.
 *
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
public abstract class AvroTypedViewBuilder {
	/**
	 * Access to LOG.
	 */
	private static final Logger LOG =
			LoggerFactory.getLogger(AvroTypedViewBuilder.class);

	// =-=-=-=- instance variables setup by the subclass -=-=-=-=

	/**
	 * The type this builder knows how to build.
	 */
	private final AvroViewType[] mTypes;

	/**
	 * Return a string version of this builder.
	 */
	public String toString() {
		return "Builder for: " + mTypes[0];
	}

	// =-=-=-=- Constructors used by subclasses -=-=-=-=

	/**
	 * Construct a view builder which supports the given types.
	 * @param types The types this builder supports
	 */
	protected AvroTypedViewBuilder(final AvroViewType[] types) {
		mTypes = types;
	}

	/**
	 * Construct a view builder which support just one type.
	 * @param type The type this builder supports
	 * @param widget The widget this builder supports
	 */
	protected AvroTypedViewBuilder(final Type type, final String widget) {
		mTypes = new AvroViewType[] { new AvroViewType(type, widget) };
	}

	/**
	 * Construct a view builder which support just one type.
	 * @param type The type this builder supports
	 */
	protected AvroTypedViewBuilder(final Type type) {
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
	 * @return the built view
	 */
	protected static EditText buildEditText(final Activity activity,
			final ViewGroup viewGroup, final Schema schema,
			final int inputType) {
		LOG.debug("Building edit text for: " + schema);
		EditText text = null;

		text = new EditText(activity);
		LayoutParameters.setViewGroupLayoutParams(
				LayoutParameters.W_FILL_H_WRAP, text);
		text.setGravity(Gravity.FILL_HORIZONTAL);
		text.setInputType(inputType);

		ViewUtil.addView(activity, viewGroup, text);
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

	/**
	 * Builds a text view with the given text string.
	 * @param activity the activity to build in
	 * @param viewGroup the view group to add to
	 * @param text the text to set
	 * @return the built view
	 */
	protected static View buildTextView(final Activity activity,
			final ViewGroup viewGroup, final String text) {
		TextView textView = new TextView(activity);
		textView.setText(text);
		ViewUtil.addView(activity, viewGroup, textView);
		return textView;
	}

	/**
	 * Builds a text view for a list, setting the tag to the field name.
	 * @param context the context to build in
	 * @param field the field to build for
	 * @return the built TextView
	 */
	protected static View buildTextView(final Context context,
			final Field field) {
		TextView view = new TextView(context);
		view.setTag(field.name());
		return view;
	}

	/**
	 * @param field the field the projection is needed for
	 * @return the single field name in a list
	 */
	protected static List<String> getFieldNameProjection(final Field field) {
		List<String> ret = new ArrayList<String>(1);
		ret.add(field.name());
		return ret;
	}

	// =-=-=-=- Subclass methods -=-=-=-=

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
	abstract View buildEditView(final Activity activity,
			final AvroRecordModel dataModel, final ViewGroup viewGroup,
			final Schema schema, final Field field, final Uri uri,
			final ValueHandler valueHandler) throws NotBoundException;

	/**
	 * Builds a list view.
	 * @param context the context to build for
	 * @param field the field to build a view for
	 * @return the built view.
	 */
	abstract View buildListView(Context context, Field field);

	/**
	 * Binds the given view to the data held in the cursor for the given field.
	 * @param view the view to bind to
	 * @param cursor the cursor to get data from
	 * @param field the field to bind for
	 */
	abstract void bindListView(View view, Cursor cursor, Field field);

	/**
	 * @param field the field we need the projection for
	 * @return the list of projection fields required to bind this field
	 */
	abstract List<String> getProjectionFields(Field field);
}
