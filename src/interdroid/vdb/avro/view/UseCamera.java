package interdroid.vdb.avro.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import interdroid.util.ToastOnUI;
import interdroid.vdb.avro.R;
import interdroid.vdb.avro.view.CameraSurface.PictureTakenCallback;
import android.app.Activity;
import android.content.ContentValues;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

public class UseCamera extends Activity {
	private static final Logger logger = LoggerFactory
			.getLogger(UseCamera.class);

	CameraSurface mPreview;
	Uri mUri;
	String mField;

	protected void onCreate() {

	}

	protected void onStart() {
		mPreview = new CameraSurface(this);
		mPreview.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				logger.debug("Taking picture!");
				mPreview.takePicture(pictureCallback, 0, 0, getFilesDir(), true);
			}

		});
		mUri = getIntent().getData();
		mField = getIntent().getStringExtra("field");

		if (mUri == null || mField == null) {
			ToastOnUI.show(this, R.string.error_opening_camera, Toast.LENGTH_LONG);
			finish();
		}

		logger.debug("Taking picture for: {} {}", mUri, mField);

		LinearLayout layout = new LinearLayout(this);
		layout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		layout.addView(mPreview);
		this.setContentView(layout);

		super.onStart();
	}

	protected void onStop() {
		// mPreview.mCamera.stopPreview();
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		mPreview = null;
		super.onDestroy();
	}

	PictureTakenCallback pictureCallback = new PictureTakenCallback() {

		@Override
		public void onPrePictureTaken() {
			getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}

		@Override
		public void onPictureTaken(byte[] data) {
			ContentValues values = new ContentValues();
			values.put(mField, data);
			getContentResolver().update(mUri, values, null, null);
			finish();
		}

	};
}
