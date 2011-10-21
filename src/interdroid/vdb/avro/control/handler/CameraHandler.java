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
import interdroid.vdb.R;
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
