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
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

/**
 * A surface for display camera previews.
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
public class CameraSurface extends SurfaceView implements Callback {
	/** Access to logger. */
	private static final Logger LOG = LoggerFactory
			.getLogger(CameraSurface.class);

	/** The minimum size we would like to have. */
	private static final int	MINIMUM_SIZE	= 512;

	/** The aspect tolerance we support. */
	private static final double ASPECT_TOLERANCE = 0.05;


	/** The holder for this surface. */
	private final SurfaceHolder mHolder;
	/** The camera we are using. */
	private Camera mCamera;

	/**
	 * Construct a CameraSurface for the given context.
	 * @param context the context in which to work.
	 */
	public CameraSurface(final Context context) {

		super(context);

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		LOG.debug("CameraPreview: constructor");
	}

	@Override
	public final void surfaceCreated(final SurfaceHolder holder) {
		LOG.debug("CameraPreview: surface created");
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
						&& supportedSize.width >= MINIMUM_SIZE) {
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

	@Override
	public final void surfaceDestroyed(final SurfaceHolder holder) {
		LOG.debug("CameraPreview: surface destroyed");
		// Surface will be destroyed when we return, so stop the preview.
		// Because the CameraDevice object is not a shared resource, it's very
		// important to release it when the activity is paused.
		mCamera.stopPreview();
		mCamera.release();
		mCamera = null;
	}

	/**
	 * @param sizes the available sizes
	 * @param w the width
	 * @param h the height
	 * @return the optimal preview size
	 */
	private Size getOptimalPreviewSize(final List<Size> sizes,
			final int w, final int h) {

		if (sizes == null) {
			return null;
		}

		double targetRatio = (double) w / h;

		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		int targetHeight = h;

		// Try to find an size match aspect ratio and size
		for (Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
				continue;
			}
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

	@Override
	public final void surfaceChanged(
			final SurfaceHolder holder, final int format,
			final int w, final int h) {
		LOG.debug("CameraPreview: surface changed");
		// Now that the size is known, set up the camera parameters and begin
		// the preview.
		Camera.Parameters parameters = mCamera.getParameters();

		List<Size> sizes = parameters.getSupportedPreviewSizes();
		Size optimalSize = getOptimalPreviewSize(sizes, w, h);
		parameters.setPreviewSize(optimalSize.width, optimalSize.height);

		mCamera.setParameters(parameters);
		mCamera.startPreview();
	}

	/**
	 * Take a picture.
	 * @param bitmapCallback the callback to call with a bitmap
	 * @param cropSelectionFractionX the amount to crop in x
	 * @param cropSelectionFractionY the amount to crop in y
	 * @param directory the directory to write to
	 * @param autofocus should we use autofocus
	 */
	public final void takePicture(final PictureTakenCallback bitmapCallback,
			final double cropSelectionFractionX,
			final double cropSelectionFractionY,
			final File directory,
			final boolean autofocus) {

		LOG.debug("CameraPreview: take picture");
		// TODO Auto-generated method stub
		if (autofocus) {
			mCamera.autoFocus(new AutoFocusCallback() {

				@Override
				public void onAutoFocus(final boolean success,
						final Camera camera) {
					doTakePicture(bitmapCallback);
				}

			});
		} else {
			doTakePicture(bitmapCallback);
		}
	}

	/**
	 * Handles the taking of a picture.
	 * @param bitmapCallback the listner to send a bitmap to
	 */
	private void doTakePicture(final PictureTakenCallback bitmapCallback) {
		LOG.debug("CameraPreview: picture taken");

		mCamera.takePicture(null, null, new PictureCallback() {

			@Override
			public void onPictureTaken(final byte[] data, final Camera camera) {
				mCamera.stopPreview();
				LOG.debug("CameraPreview: preview stopped");

				bitmapCallback.onPrePictureTaken();
				bitmapCallback.onPictureTaken(data);

				try {
					mCamera.setPreviewDisplay(mHolder);
				} catch (IOException e) {
					LOG.error("Error resetting display!", e);
				}
				mCamera.startPreview();
				LOG.debug("CameraPreview: preview restarted");
			}
		});
	}

	/**
	 * The callback for when a picture is taken.
	 * @author nick &lt;palmer@cs.vu.nl&gt;
	 *
	 */
	interface PictureTakenCallback {

		/**
		 * Called just before a picture is taken.
		 */
		void onPrePictureTaken();

		/**
		 * Called when a picture is taken.
		 * @param data the bytes of the photo.
		 */
		void onPictureTaken(byte[] data);
	}

}
