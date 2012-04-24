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

	private boolean pictureTaken = false;

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
// TODO: What to do on 1.5?
//		Camera.Parameters params = mCamera.getParameters();
//		List<Camera.Size> supportedSizes = params.getSupportedPictureSizes();
//
//		if (supportedSizes != null && supportedSizes.size() > 0) {
//			Camera.Size chosenSize = supportedSizes.get(0);
//			for (Camera.Size supportedSize : supportedSizes) {
//				if (supportedSize.width > supportedSize.height
//						&& supportedSize.width < chosenSize.width
//						&& supportedSize.width >= MINIMUM_SIZE) {
//					chosenSize = supportedSize;
//				}
//			}
//			params.setPictureSize(chosenSize.width, chosenSize.height);
//			mCamera.setParameters(params);
//		}

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
// TODO: What to do on 1.5?
//		Camera.Parameters parameters = mCamera.getParameters();
//
//		List<Size> sizes = parameters.getSupportedPreviewSizes();
//		Size optimalSize = getOptimalPreviewSize(sizes, w, h);
//		parameters.setPreviewSize(optimalSize.width, optimalSize.height);
//
//		mCamera.setParameters(parameters);

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
	public synchronized final void takePicture(final PictureTakenCallback bitmapCallback,
			final double cropSelectionFractionX,
			final double cropSelectionFractionY,
			final File directory,
			final boolean autofocus) {
		if (!pictureTaken) {
			pictureTaken = true;
			LOG.debug("CameraPreview: take picture");
			bitmapCallback.onPrePictureTaken();

			// TODO Auto-generated method stub
			if (autofocus) {
				try {
					mCamera.autoFocus(new AutoFocusCallback() {

						@Override
						public void onAutoFocus(final boolean success,
								final Camera camera) {
							doTakePicture(bitmapCallback);
						}

					});
				} catch (Exception e) {
					LOG.warn("Auto focus failed. Taking without.", e);
					doTakePicture(bitmapCallback);
				}
			} else {
				doTakePicture(bitmapCallback);
			}
		}
	}

	/**
	 * Handles the taking of a picture.
	 * @param bitmapCallback the listener to send a bitmap to
	 */
	private void doTakePicture(final PictureTakenCallback bitmapCallback) {
		mCamera.takePicture(null, null, new PictureCallback() {

			@Override
			public void onPictureTaken(byte[] data, final Camera camera) {
				LOG.debug("CameraPreview: picture taken");
				mCamera.stopPreview();

				try {
					mCamera.setPreviewDisplay(mHolder);
				} catch (IOException e) {
					LOG.error("Error resetting display!", e);
				}
				// mCamera.startPreview();
				bitmapCallback.onPictureTaken(data);
				data = null;
				System.gc();
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
