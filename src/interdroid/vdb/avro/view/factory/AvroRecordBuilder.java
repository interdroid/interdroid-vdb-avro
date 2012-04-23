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

import java.util.List;

import interdroid.util.view.ViewUtil;
import interdroid.vdb.avro.R;
import interdroid.vdb.avro.control.handler.RecordTypeSelectHandler;
import interdroid.vdb.avro.control.handler.value.ValueHandler;
import interdroid.vdb.avro.model.AvroRecordModel;
import interdroid.vdb.avro.model.NotBoundException;
import interdroid.vdb.avro.model.UriRecord;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * A builder which knows how to build Type.RECORD fields.
 * @author nick
 *
 */
class AvroRecordBuilder extends AvroTypedViewBuilder {

	/**
	 * Constructs a builder for Type.RECORD && widget == null.
	 */
	protected AvroRecordBuilder() {
		super(Type.RECORD);
	}

	@Override
	public final View buildEditView(final Activity activity,
			final AvroRecordModel dataModel, final ViewGroup viewGroup,
			final Schema schema, final Field field, final Uri uri,
			final ValueHandler valueHandler) throws NotBoundException {

		final Button button = new Button(activity);
		UriRecord record = (UriRecord) valueHandler.getValue();
		if (record == null) {
			button.setText(
					AvroViewFactory.toTitle(activity,
							R.string.label_create, schema));
		} else {
			button.setText(
					AvroViewFactory.toTitle(activity,
							R.string.label_edit, schema));
		}
		button.setOnClickListener(
				getRecordTypeSelectorHandler(activity, schema, valueHandler));
		ViewUtil.addView(activity, viewGroup, button);
		return button;
	}

	/**
	 * Returns an on click listener for the given data.
	 * @param activity the activity to work in
	 * @param schema the schema for the data
	 * @param valueHandler the value handler for the data
	 * @return the on click listener
	 */
	private static OnClickListener getRecordTypeSelectorHandler(
			final Activity activity, final Schema schema,
			final ValueHandler valueHandler) {
		return new RecordTypeSelectHandler(activity, schema, valueHandler);
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
	final List<String> getProjectionFields(final Field field) {
		// TODO Auto-generated method stub
		return null;
	}
}
