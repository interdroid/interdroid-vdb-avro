package interdroid.vdb.avro.control.handler;


import org.apache.avro.Schema;
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
import interdroid.vdb.avro.model.AvroRecordModel;
import interdroid.vdb.avro.model.NotBoundException;
import interdroid.vdb.avro.view.AvroIntentUtil;
import interdroid.vdb.avro.view.UseCamera;

public class CameraHandler implements OnClickListener {
	private static final Logger logger = LoggerFactory
			.getLogger(CameraHandler.class);

	private final AvroRecordModel mDataModel;
	private final Activity mActivity;
	private final ValueHandler mValueHandler;
	private final Schema mSchema;
	private final boolean mIsVideo;
	private Button mButton;

	public CameraHandler(AvroRecordModel dataModel, Activity activity,
			Schema schema, ValueHandler valueHandler, Button cameraButton,
			ImageView image) {
		mDataModel = dataModel;
		mValueHandler = valueHandler;
		mSchema = schema;
		mActivity = activity;
		mIsVideo = false;
		setButton(cameraButton);
		setImageView(image);
	}

	private void setButton(Button takePhoto) {
		mButton = takePhoto;
		mButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View arg0) {
		Uri uri;
		try {
			uri = mValueHandler.getValueUri();

			logger.debug("Launching camera intent for URI: {} type: {}",
					uri, mActivity.getContentResolver().getType(uri));
			Intent cameraIntent = new Intent(
					MediaStore.ACTION_IMAGE_CAPTURE, uri);
			cameraIntent.setClassName(mActivity, UseCamera.class.getName());
			cameraIntent.putExtra("field", mValueHandler.getFieldName());
			AvroIntentUtil.launchDefaultIntent(mActivity, cameraIntent);
		} catch (NotBoundException e) {
			logger.error("Not bound!");
			ToastOnUI.show(mActivity, R.string.error_opening_camera,
					Toast.LENGTH_LONG);
		}
	}

	private void setImageView(final ImageView image) {
		if (mValueHandler.getValue() != null) {
			logger.debug("Setting bitmap.");
			try {
				byte[] data = (byte[]) mValueHandler.getValue();
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
				logger.error("Unable to set image.", e);
			}
		}
	}

}
