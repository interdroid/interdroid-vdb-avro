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

import interdroid.util.ToastOnUI;
import interdroid.vdb.avro.R;
import interdroid.vdb.avro.control.handler.value.ValueHandler;
import interdroid.vdb.avro.model.NotBoundException;
import interdroid.vdb.avro.model.UriRecord;
import interdroid.vdb.avro.view.AvroIntentUtil;
import interdroid.vdb.avro.view.LocationPicker;

import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * A handler for location data.
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
public class LocationHandler implements OnClickListener {
	/** Access to logger. */
	private static final Logger LOG = LoggerFactory
			.getLogger(LocationHandler.class);

	/** The activity we work in. */
	private final Activity mActivity;
	/** The value handler for the data. */
	private final ValueHandler mValueHandler;
	/** The schema for the data. */
	private final Schema mSchema;

	/**
	 * Construct a location handler.
	 * @param activity the activity to work in
	 * @param schema the schema for the data
	 * @param valueHandler the value handler
	 * @param cameraButton the button to trigger the picker
	 * @param image the image view to display location in
	 */
	public LocationHandler(final Activity activity,
			final Schema schema, final ValueHandler valueHandler,
			final Button cameraButton, final ImageView image) {
		mActivity = activity;
		mValueHandler = valueHandler;
		mSchema = schema;

		setButton(cameraButton);
		setImageView(image);
	}

	/**
	 * Set the button to use to trigger picks.
	 * @param pickButton the button to trigger picking.
	 */
	private void setButton(final Button pickButton) {
		pickButton.setOnClickListener(this);
	}

	@Override
	public final void onClick(final View arg0) {
		Uri uri;
		try {
			UriRecord record = (UriRecord) mValueHandler.getValue();
			if (record == null) {
				LOG.debug("Building new location record: {}",
						Uri.withAppendedPath(mValueHandler.getValueUri(),
								mValueHandler.getFieldName()));
				uri = mActivity.getContentResolver().insert(
						Uri.withAppendedPath(mValueHandler.getValueUri(),
								mValueHandler.getFieldName()), null);
				LOG.debug("Got value URI: {}", uri);
				record = new UriRecord(uri, mSchema);
				mValueHandler.setValue(record);
			} else {
				uri = record.getInstanceUri();
			}

			LOG.debug("Launching location picker intent for URI: {} type: {}",
					uri, mActivity.getContentResolver().getType(uri));
			final Intent locationIntent = new Intent(
					LocationPicker.ACTION_PICK_LOCATION, uri);
			locationIntent.setClassName(mActivity,
					LocationPicker.class.getName());
			AvroIntentUtil.launchDefaultIntent(mActivity, locationIntent);
		} catch (NotBoundException e) {
			LOG.error("Not bound!");
			ToastOnUI.show(mActivity, R.string.error_picking_location,
					Toast.LENGTH_LONG);
		}
	}

	/**
	 * Sets the image view to display the location with.
	 * @param image the image view to use
	 */
	private void setImageView(final ImageView image) {
		if (mValueHandler.getValue() != null) {
			LOG.debug("Setting bitmap.");
			try {
				final UriRecord record = (UriRecord) mValueHandler.getValue();
				final byte[] data =
						(byte[]) record.get(LocationPicker.MAP_IMAGE);
				if (data != null && data.length > 0) {
					final Bitmap bitmap =
							BitmapFactory.decodeByteArray(data, 0, data.length);
					mActivity.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							image.setVisibility(View.VISIBLE);
							image.setImageBitmap(bitmap);
						}

					});
				} else {
					mActivity.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							image.setImageBitmap(null);
							image.setVisibility(View.INVISIBLE);
						}

					});
				}
			} catch (Exception e) {
				LOG.error("Unable to set image.", e);
			}
		}
	}

}
