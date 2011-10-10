package interdroid.vdb.avro.control.handler;

import interdroid.util.ToastOnUI;
import interdroid.vdb.R;
import interdroid.vdb.avro.model.AvroRecordModel;
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

public class LocationHandler implements OnClickListener {
	private static final Logger logger = LoggerFactory
			.getLogger(LocationHandler.class);

	private final AvroRecordModel mDataModel;
	private final Activity mActivity;
	private final ValueHandler mValueHandler;
	private final Schema mSchema;
	private Button mButton;

	public LocationHandler(AvroRecordModel dataModel, Activity activity,
			Schema schema, ValueHandler valueHandler,
			Button cameraButton, ImageView image) {
		mDataModel = dataModel;
		mActivity = activity;
		mValueHandler = valueHandler;
		mSchema = schema;

		setButton(cameraButton);
		setImageView(image);
	}

	public void setButton(Button pickButton) {
		mButton = pickButton;
		mButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View arg0) {
		Uri uri;
		try {
			UriRecord record = (UriRecord) mValueHandler.getValue();
			if (record == null) {
				logger.debug("Building new location record: {}", Uri.withAppendedPath(mValueHandler.getValueUri(), mValueHandler.getFieldName()));
				uri = mActivity.getContentResolver().insert(Uri.withAppendedPath(mValueHandler.getValueUri(), mValueHandler.getFieldName()), null);
				logger.debug("Got value URI: {}", uri);
				record = new UriRecord(uri, mSchema);
				mValueHandler.setValue(record);
			} else {
				uri = record.getInstanceUri();
			}

			logger.debug("Launching location picker intent for URI: {} type: {}", uri, mActivity.getContentResolver().getType(uri));
			Intent locationIntent = new Intent(LocationPicker.ACTION_PICK_LOCATION, uri);
			locationIntent.setClassName(mActivity, LocationPicker.class.getName());
			AvroIntentUtil.launchDefaultIntent(mActivity, locationIntent);
		} catch (NotBoundException e) {
			logger.error("Not bound!");
			ToastOnUI.show(mActivity, R.string.error_picking_location,
					Toast.LENGTH_LONG);
		}
	}

	public final void setImageView(final ImageView image) {
		if (mValueHandler.getValue() != null) {
			logger.debug("Setting bitmap.");
			try {
				UriRecord record = (UriRecord) mValueHandler.getValue();
				byte[] data = (byte[]) record.get(LocationPicker.MAP_IMAGE);
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
