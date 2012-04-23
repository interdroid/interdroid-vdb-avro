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
