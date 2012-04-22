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
