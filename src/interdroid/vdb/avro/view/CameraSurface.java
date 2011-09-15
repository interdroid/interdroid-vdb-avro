package interdroid.vdb.avro.view;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.SurfaceHolder.Callback;

public class CameraSurface extends SurfaceView implements Callback {
	private static final Logger logger = LoggerFactory
			.getLogger(CameraSurface.class);

	SurfaceHolder mHolder;
	Camera mCamera;

	CameraSurface(Context context) {

		super(context);

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		logger.debug("CameraPreview: constructor");
	}

	public void surfaceCreated(SurfaceHolder holder) {
		logger.debug("CameraPreview: surface created");
		// The Surface has been created, acquire the camera and tell it where
		// to draw.
		mCamera = Camera.open();
		Camera.Parameters params = mCamera.getParameters();
		List<Camera.Size> supportedSizes = params.getSupportedPictureSizes();

		if (supportedSizes != null && supportedSizes.size() > 0) {
			Camera.Size chosenSize = supportedSizes.get(0);
			for (Camera.Size supportedSize : supportedSizes) {
				if (supportedSize.width > supportedSize.height
						&& supportedSize.width < chosenSize.width
						&& supportedSize.width >= 512) {
					chosenSize = supportedSize;
				}
			}
			params.setPictureSize(chosenSize.width, chosenSize.height);
			mCamera.setParameters(params);
		}

		try {
			mCamera.setPreviewDisplay(holder);
		} catch (IOException exception) {
			mCamera.release();
			mCamera = null;
			// TODO: add more exception handling logic here
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		logger.debug("CameraPreview: surface destroyed");
		// Surface will be destroyed when we return, so stop the preview.
		// Because the CameraDevice object is not a shared resource, it's very
		// important to release it when the activity is paused.
		mCamera.stopPreview();
		mCamera.release();
		mCamera = null;
	}

	private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
		final double ASPECT_TOLERANCE = 0.05;
		double targetRatio = (double) w / h;
		if (sizes == null)
			return null;

		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		int targetHeight = h;

		// Try to find an size match aspect ratio and size
		for (Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		// Cannot find the one match the aspect ratio, ignore the requirement
		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		logger.debug("CameraPreview: surface changed");
		// Now that the size is known, set up the camera parameters and begin
		// the preview.
		Camera.Parameters parameters = mCamera.getParameters();

		List<Size> sizes = parameters.getSupportedPreviewSizes();
		Size optimalSize = getOptimalPreviewSize(sizes, w, h);
		parameters.setPreviewSize(optimalSize.width, optimalSize.height);

		mCamera.setParameters(parameters);
		mCamera.startPreview();
	}

	public void takePicture(final PictureTakenCallback bitmapCallback,
			final double cropSelectionFractionX,
			final double cropSelectionFractionY,
			final File directory,
			boolean autofocus) {

		System.gc();
		logger.debug("CameraPreview: take picture");
		// TODO Auto-generated method stub
		if (autofocus) {
			mCamera.autoFocus(new AutoFocusCallback() {

				@Override
				public void onAutoFocus(boolean success, Camera camera) {
					doTakePicture(bitmapCallback);
				}

			});
		} else {
			doTakePicture(bitmapCallback);
		}
	}

	private void doTakePicture(final PictureTakenCallback bitmapCallback) {
		logger.debug("CameraPreview: picture taken");

		mCamera.takePicture(null, null, new PictureCallback() {

			@Override
			public void onPictureTaken(byte[] data, Camera camera) {
				mCamera.stopPreview();
				logger.debug("CameraPreview: preview stopped");

				bitmapCallback.onPrePictureTaken();
				bitmapCallback.onPictureTaken(data);

				try {
					mCamera.setPreviewDisplay(mHolder);
				} catch (IOException e) {
					logger.error("Error resetting display!", e);
				}
				mCamera.startPreview();
				logger.debug("CameraPreview: preview restarted");
			}
		});
	}

	interface PictureTakenCallback {

		public void onPrePictureTaken();

		void onPictureTaken(byte[] data);
	}

}
