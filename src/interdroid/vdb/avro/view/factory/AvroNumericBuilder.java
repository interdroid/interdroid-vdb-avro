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


import interdroid.util.DbUtil;
import interdroid.vdb.avro.control.handler.EditTextHandler;
import interdroid.vdb.avro.control.handler.value.ValueHandler;
import interdroid.vdb.avro.model.AvroRecordModel;
import interdroid.vdb.avro.model.NotBoundException;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

/**
 * A builder for numeric data types.
 * This includes: Type.LONG, Type.INT, Type.DOUBLE, Type.FLOAT.
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
class AvroNumericBuilder extends AvroTypedTextViewBuilder {

	/**
	 * Construct a builder for numeric types.
	 */
	public AvroNumericBuilder() {
		super(getSupportedTypes());
	}

	/**
	 * @return the types this builder supports
	 */
	private static AvroViewType[] getSupportedTypes() {
		return new AvroViewType[] {
				new AvroViewType(Type.LONG),
				new AvroViewType(Type.INT),
				new AvroViewType(Type.DOUBLE),
				new AvroViewType(Type.FLOAT)
		};
	}


	@Override
	public final View buildEditView(final Activity activity,
			final AvroRecordModel dataModel,
			final ViewGroup viewGroup, final Schema schema,
			final Field field, final Uri uri,
			final ValueHandler valueHandler) throws NotBoundException {
		int flags;
		switch (schema.getType()) {
		case INT:
		case LONG:
			flags = InputType.TYPE_CLASS_NUMBER
			| InputType.TYPE_NUMBER_FLAG_DECIMAL
			| InputType.TYPE_NUMBER_FLAG_SIGNED;
			break;
		case DOUBLE:
		case FLOAT:
			flags = InputType.TYPE_CLASS_NUMBER
			| InputType.TYPE_NUMBER_FLAG_SIGNED;
			break;
		default:
			throw new IllegalArgumentException("Unsupported type: " + schema);
		}

		EditText view = buildEditText(activity, viewGroup, schema, flags);
		new EditTextHandler(dataModel, schema.getType(), valueHandler, view);
		return view;
	}

	@Override
	final void bindListView(final View view, final Cursor cursor,
			final Field field) {
		TextView text = (TextView) view.findViewWithTag(field.name());
		int index = DbUtil.getFieldIndex(cursor, field.name());
		switch (field.schema().getType()) {
		case INT:
			text.setText(String.valueOf(cursor.getInt(index)));
			break;
		case LONG:
			text.setText(String.valueOf(cursor.getLong(index)));
			break;
		case DOUBLE:
			text.setText(String.valueOf(cursor.getDouble(index)));
			break;
		case FLOAT:
			text.setText(String.valueOf(cursor.getFloat(index)));
		default:
			throw new IllegalArgumentException("Unsupported type: " + field);
		}

	}
}
