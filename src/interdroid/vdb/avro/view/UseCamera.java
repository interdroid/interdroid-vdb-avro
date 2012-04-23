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
package interdroid.vdb.avro.view;

import interdroid.util.ToastOnUI;
import interdroid.util.view.LayoutUtil.LayoutParameters;
import interdroid.vdb.avro.R;
import interdroid.vdb.avro.view.CameraSurface.PictureTakenCallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.ContentValues;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * An activity which lets the user take a photo. It stores the resulting
 * photo to a content provider uri.
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
public class UseCamera extends Activity {
	/** Access to logger. */
	private static final Logger LOG = LoggerFactory
			.getLogger(UseCamera.class);

	/** The camera surface used to show the preview. */
	private CameraSurface mPreview;
	/** The uri we are going to store to. */
	private Uri mUri;
	/** The field to store to in the content provider. */
	private String mField;

	@Override
	protected final void onStart() {
		ToastOnUI.show(this, "Tap to take a photo!", Toast.LENGTH_LONG);

		mPreview = new CameraSurface(this);
		mPreview.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(final View arg0) {
				LOG.debug("Taking picture!");
				mPreview.takePicture(
						pictureCallback, 0, 0, getFilesDir(), true);
			}

		});
		mUri = getIntent().getData();
		mField = getIntent().getStringExtra("field");

		if (mUri == null || mField == null) {
			ToastOnUI.show(this,
					R.string.error_opening_camera, Toast.LENGTH_LONG);
			finish();
		}

		LOG.debug("Taking picture for: {} {}", mUri, mField);

		final LinearLayout layout = new LinearLayout(this);
		LayoutParameters.setViewGroupLayoutParams(
				LayoutParameters.W_FILL_H_FILL, layout);
		layout.addView(mPreview);
		this.setContentView(layout);

		super.onStart();
	}

	@Override
	protected final void onStop() {
		// mPreview.mCamera.stopPreview();
		super.onStop();
	}

	@Override
	protected final void onDestroy() {
		super.onDestroy();
	}

	/**
	 * A class which holds our handlers for photos which are taken.
	 */
	private final PictureTakenCallback pictureCallback =
			new PictureTakenCallback() {

		@Override
		public void onPrePictureTaken() {
			getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}

		@Override
		public void onPictureTaken(final byte[] data) {
			final ContentValues values = new ContentValues();
			values.put(mField, data);
			getContentResolver().update(mUri, values, null, null);
			finish();
		}

	};
}
