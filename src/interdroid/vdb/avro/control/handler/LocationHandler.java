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
