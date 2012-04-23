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

import interdroid.util.view.DraggableListView;
import interdroid.util.view.LayoutUtil.LayoutParameters;
import interdroid.util.view.ViewUtil;
import interdroid.vdb.avro.R;
import interdroid.vdb.avro.control.handler.ArrayHandler;
import interdroid.vdb.avro.control.handler.value.ValueHandler;
import interdroid.vdb.avro.model.AvroRecordModel;
import interdroid.vdb.avro.model.NotBoundException;
import interdroid.vdb.avro.model.UriArray;

import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A builder which knows how to build array views.
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
class AvroArrayBuilder extends AvroTypedViewBuilder {

	/**
	 * Amount to indent array list by.
	 */
	protected static final int LEFT_INDENT = 3;

	/**
	 * Constructs a builder for Type.ARRAY widget == null.
	 */
	public AvroArrayBuilder() {
		super(Type.ARRAY);
	}

	@Override
	public final View buildEditView(final Activity activity,
			final AvroRecordModel dataModel,
			final ViewGroup viewGroup, final Schema schema,
			final Field field, final Uri uri,
			final ValueHandler valueHandler) throws NotBoundException {

		DraggableListView layout = new DraggableListView(activity);

		LayoutParameters.setViewGroupLayoutParams(
				LayoutParameters.W_FILL_H_WRAP, layout);
		layout.setPadding(LEFT_INDENT, 0, 0, 0);

		ArrayHandler adapter = new ArrayHandler(activity, dataModel, layout,
				getArray(uri, valueHandler, schema), field);
		layout.setAdapter(adapter);
		layout.setAddListener(adapter);

		TextView empty = (TextView) layout.findViewById(R.id.empty_text);
		empty.setGravity(Gravity.CENTER);
		empty.setText("Press the plus button to add to the list.");

		ViewUtil.addView(activity, viewGroup, layout);

		return layout;
	}

	/**
	 * Returns or constructs a UriArray setting in the value handler.
	 * @param uri the uri for the array
	 * @param valueHandler the value handler to get the array from
	 * @param schema the schema for the array
	 * @return a UriArray.
	 * @throws NotBoundException if the underlying model is not bound
	 */
	private UriArray<Object> getArray(final Uri uri,
			final ValueHandler valueHandler, final Schema schema)
					throws NotBoundException {

		@SuppressWarnings("unchecked")
		UriArray<Object> array = (UriArray<Object>) valueHandler.getValue();
		if (array == null) {
			array = new UriArray<Object>(
					Uri.withAppendedPath(valueHandler.getValueUri(),
							valueHandler.getFieldName()), schema);
			valueHandler.setValue(array);
		}

		return array;
	}

	@Override
	final View buildListView(final Context context, final Field field) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	final void bindListView(final View view, final Cursor cursor,
			final Field field) {
		// TODO Auto-generated method stub

	}

	@Override
	List<String> getProjectionFields(final Field field) {
		// TODO Auto-generated method stub
		return null;
	}

}
