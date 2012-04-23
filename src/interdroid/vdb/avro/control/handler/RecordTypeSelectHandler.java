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
package interdroid.vdb.avro.control.handler;

import interdroid.vdb.avro.control.handler.value.ValueHandler;
import interdroid.vdb.avro.model.NotBoundException;
import interdroid.vdb.avro.model.UriRecord;
import interdroid.vdb.avro.view.AvroBaseEditor;
import interdroid.vdb.avro.view.AvroIntentUtil;
import interdroid.vdb.content.EntityUriBuilder;
import interdroid.vdb.avro.AvroSchema;

import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * Handles selection of a Type.
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
public class RecordTypeSelectHandler implements OnClickListener {
	/** Access to logger. */
	private static final Logger LOG =
			LoggerFactory.getLogger(RecordTypeSelectHandler.class);

	/** The activity we work for. */
	private final Activity mActivity;
	/** The schema for the type. */
	private final Schema mSchema;
	/** The value handler to get and set data with. */
	private final ValueHandler mValueHandler;

	/**
	 * Construct a selection handler.
	 * @param activity the activity to work in
	 * @param schema the schema for the data
	 * @param handler the handler to get and set data with
	 */
	public RecordTypeSelectHandler(final Activity activity,
			final Schema schema, final ValueHandler handler) {
		mActivity = activity;
		mSchema = schema;
		mValueHandler = handler;
	}

	@Override
	public final void onClick(final View view) {
		Uri uri = null;
		if (mValueHandler.getValue() == null) {
			// Do an insert so we can stash away the URI for the record
			final Uri storeUri = Uri.withAppendedPath(
					EntityUriBuilder.branchUri(mSchema.getNamespace(),
						AvroSchema.NAMESPACE, "master"), mSchema.getName());
			uri = mActivity.getContentResolver().insert(storeUri,
					new ContentValues());
			mValueHandler.setValue(new UriRecord(uri, mSchema));
		} else {
			try {
				uri = ((UriRecord) mValueHandler.getValue()).getInstanceUri();
			} catch (NotBoundException e) {
				LOG.error(
					"Unable to get record URI because record is not bound.");
			}
		}
		LOG.debug("Launching edit on URI: {} type: {}",
				uri, mActivity.getContentResolver().getType(uri));
		final Intent editIntent = new Intent(Intent.ACTION_EDIT, uri);
		editIntent.putExtra(AvroBaseEditor.SCHEMA, mSchema.toString());
		AvroIntentUtil.launchEditIntent(mActivity, editIntent);
	}

}
