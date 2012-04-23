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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import interdroid.util.ToastOnUI;
import interdroid.vdb.avro.R;
import interdroid.vdb.avro.control.handler.value.ValueHandler;
import interdroid.vdb.avro.model.AvroRecordModel;
import interdroid.vdb.avro.model.NotBoundException;
import interdroid.vdb.avro.view.AvroIntentUtil;
import interdroid.vdb.avro.view.UseCamera;

/**
 * Handler for a photo field.
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
public class CameraHandler implements OnClickListener {
	/** Access to logger. */
	private static final Logger LOG = LoggerFactory
			.getLogger(CameraHandler.class);

	/** The data model we work in. */
	private final AvroRecordModel mDataModel;
	/** The activity we work for. */
	private final Activity mActivity;
	/** The value handler for the photo. */
	private final ValueHandler mValueHandler;

	/**
	 * Construct a camera handler.
	 * @param dataModel the data model to work with.
	 * @param activity the activity we work for
	 * @param valueHandler the value handler with the data
	 * @param cameraButton the button which triggers taking a photo
	 * @param image the image view to display the photo in
	 */
	public CameraHandler(final AvroRecordModel dataModel,
			final Activity activity, final ValueHandler valueHandler,
			final Button cameraButton, final ImageView image) {
		mDataModel = dataModel;
		mValueHandler = valueHandler;
		mActivity = activity;
		setButton(cameraButton);
		setImageView(image);
	}

	/**
	 * Sets the button to click for a photo.
	 * @param takePhoto the button
	 */
	private void setButton(final Button takePhoto) {
		takePhoto.setOnClickListener(this);
	}

	@Override
	public final void onClick(final View arg0) {
		Uri uri;
		try {
			uri = mValueHandler.getValueUri();

			LOG.debug("Launching camera intent for URI: {} type: {}",
					uri, mActivity.getContentResolver().getType(uri));
			final Intent cameraIntent = new Intent(
					MediaStore.ACTION_IMAGE_CAPTURE, uri);
			cameraIntent.setClassName(mActivity, UseCamera.class.getName());
			cameraIntent.putExtra("field", mValueHandler.getFieldName());
			AvroIntentUtil.launchDefaultIntent(mActivity, cameraIntent);
		} catch (NotBoundException e) {
			LOG.error("Not bound!");
			ToastOnUI.show(mActivity, R.string.error_opening_camera,
					Toast.LENGTH_LONG);
		}
	}

	/**
	 * Sets the image view to display the photo in.
	 * @param image the image view to use
	 */
	private void setImageView(final ImageView image) {
		if (mValueHandler.getValue() != null) {
			LOG.debug("Setting bitmap.");
			try {
				final byte[] data = (byte[]) mValueHandler.getValue();
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
				mDataModel.onChanged();
			} catch (Exception e) {
				LOG.error("Unable to set image.", e);
			}
		}
	}

}
