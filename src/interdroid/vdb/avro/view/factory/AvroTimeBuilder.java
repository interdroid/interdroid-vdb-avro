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

import interdroid.vdb.avro.control.handler.TimeHandler;
import interdroid.vdb.avro.control.handler.value.ValueHandler;
import interdroid.vdb.avro.model.AvroRecordModel;
import interdroid.vdb.avro.model.NotBoundException;
import interdroid.vdb.avro.view.DataFormatUtil;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.TimePicker;

/**
 * A builder for Type.LONG && widget == "time".
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
class AvroTimeBuilder extends AvroTypedTextViewBuilder {
	/**
	 * Access to logging.
	 */
	private static final Logger LOG =
			LoggerFactory.getLogger(AvroTimeBuilder.class);

	/**
	 * Build a time builder.
	 */
	protected AvroTimeBuilder() {
		super(Type.LONG, "time");
	}

	/**
	 * A holder for a TimePicker.
	 * @author nick &lt;palmer@cs.vu.nl&gt;
	 *
	 */
	private static final class TimeViewHolder {
		/**
		 * The TimePicker we hold.
		 */
		private TimePicker view = null;
		/**
		 * @return the time picker we are holding.
		 */
		public TimePicker getView() {
			return view;
		}
	};

	@Override
	public final View buildEditView(final Activity activity,
			final AvroRecordModel dataModel,
			final ViewGroup viewGroup, final Schema schema,
			final Field field, final Uri uri,
			final ValueHandler valueHandler) throws NotBoundException {

		// Unfortunately TimePicker needs a Handler so it has to be initialized
		// on the UI thread.
		final TimeViewHolder viewHolder = new TimeViewHolder();

		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				synchronized (viewHolder) {
					viewHolder.view = new TimePicker(activity);

					// Add it to the view group
					viewGroup.addView(viewHolder.view);

					// Build the timeHandler to manage the data
					new TimeHandler(viewHolder.view, valueHandler);

					viewHolder.notifyAll();
				}
			}
		});

		// Wait for it to finish on the UI thread.
		synchronized (viewHolder) {
			while (viewHolder.getView() == null) {
				try {
					viewHolder.wait();
				} catch (InterruptedException e) {
					LOG.error("Interrupted waiting for view holder.");
				}
			}
		}

		return viewHolder.view;
	}

	@Override
	final void bindListView(final View view, final Cursor cursor,
			final Field field) {
		TextView text = (TextView) view.findViewWithTag(field.name());
		int index = cursor.getColumnIndex(field.name());
		text.setText(
				DataFormatUtil.formatTimeForDisplay(cursor.getLong(index)));
	}

}
