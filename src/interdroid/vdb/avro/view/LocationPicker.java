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

import interdroid.vdb.avro.AvroSchemaProperties;
import interdroid.vdb.avro.R;

import java.io.ByteArrayOutputStream;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.api.IMapView;
import org.osmdroid.api.IMyLocationOverlay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


/**
 * A picker for a location (with radius).
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
public class LocationPicker extends MapActivity {
	/** Access to logging. */
	private static final Logger LOG = LoggerFactory
			.getLogger(LocationPicker.class);

	private static final String API_KEY =
			"0OomkHbNcEYGHtSAw6TIIuX6q1a3iJIT0qSEZwA";

	/** Power of six factor for dealing with lat and long values. */
	private static final double	POWER_OF_SIX	= 1E6;

	private static final int DEFAULT_ZOOM = 15;

	/** The alpha value for the radius circle. */
	private static final int	RADIUS_ALPHA	= 30;

	/** The quality passed to compress the image. */
	protected static final int	MAP_IMAGE_QUALITY	= 25;

	/** The action that triggers this activity. */
	public static final String ACTION_PICK_LOCATION =
			Intent.ACTION_PICK + "_LOCATION";

	private static final int GOOGLE_MAP_VIEW_ID = 1;
	private static final int OSM_MAP_VIEW_ID = 2;

	private MenuItem mGoogleMenuItem;
	private MenuItem mOsmMenuItem;

	private enum MapViewSelection { Google, OSM };

	private MapViewSelection mMapViewSelection = MapViewSelection.OSM;

	private IMapView mMapView;
	private IMyLocationOverlay mMyLocationOverlay;

	/** The overlay with our picked point. */
	private com.google.android.maps.Overlay mGoogleLocationOverlay;
	private org.osmdroid.views.overlay.Overlay mOsmLocationOverlay;

	/** The centered point. */
	private IGeoPoint mCenterGeoPoint;
	/** The radius point. */
	private IGeoPoint mRadiusGeoPoint;
	/** Are we showing a radius currently. */
	private boolean mRadiusMode = false;

	/** The radius calculated into meters. */
	private float mRadiusInMeters;

	private org.osmdroid.views.MapView mOsmView;
	private com.google.android.maps.MapView mGoogleView;

	private String mField;

	@Override
	protected void onResume() {
		super.onResume();
		setMapView();
	}

	@Override
	protected void onPause() {
		done();
		super.onPause();
		mMyLocationOverlay.disableMyLocation();
	}
//
//
//	@Override
//	public boolean onCreateOptionsMenu(final Menu pMenu) {
//		mGoogleMenuItem = pMenu.add(0, GOOGLE_MAP_VIEW_ID, Menu.NONE, "Google");
//		mOsmMenuItem = pMenu.add(0, OSM_MAP_VIEW_ID, Menu.NONE, "Open Street Maps");
//		return true;
//	}
//
//	@Override
//	public boolean onPrepareOptionsMenu(final Menu pMenu) {
//		mGoogleMenuItem.setVisible(mMapViewSelection == MapViewSelection.OSM);
//		mOsmMenuItem.setVisible(mMapViewSelection == MapViewSelection.Google);
//		return super.onPrepareOptionsMenu(pMenu);
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(final MenuItem pItem) {
//		if (pItem == mGoogleMenuItem) {
//			// switch to google
//			mMapViewSelection = MapViewSelection.Google;
//			setMapView();
//			return true;
//		}
//		if (pItem == mOsmMenuItem) {
//			// switch to osm
//			mMapViewSelection = MapViewSelection.OSM;
//			setMapView();
//			return true;
//		}
//
//		return false;
//	}

	private void setMapView() {
		setupOverlay();

		setContentView(R.layout.locationpicker);

		// Setup OSM view
		if (mMapViewSelection == MapViewSelection.OSM) {
			if (mOsmView == null) {
				final org.osmdroid.views.MapView mapView =
						new org.osmdroid.views.MapView(this, 256);
				mOsmView = mapView;

				LinearLayout container =
						(LinearLayout) findViewById(R.id.MapContainer);
				container.addView(mOsmView,
						new LayoutParams(LayoutParams.FILL_PARENT,
								LayoutParams.FILL_PARENT));
				container.requestLayout();

				mapView.setBuiltInZoomControls(true);
				mapView.setAlwaysDrawnWithCacheEnabled(true);
				mapView.setDrawingCacheEnabled(true);

				final org.osmdroid.views.overlay.MyLocationOverlay mlo =
						new org.osmdroid.views.overlay.MyLocationOverlay(this, mapView);
				mMyLocationOverlay = mlo;
				mOsmView.getOverlays().add(mlo);
				mOsmView.getOverlays().add(mOsmLocationOverlay);

				IMapController mapController = mOsmView.getController();

				LOG.debug("Setting zoom to: {}", DEFAULT_ZOOM);
				mapController.setZoom(DEFAULT_ZOOM);

				if (mCenterGeoPoint != null) {
					LOG.debug("Setting center to: {}", mCenterGeoPoint);
					mapController.setCenter(mCenterGeoPoint);

					if (mRadiusGeoPoint != null) {
						LOG.debug("Setting zoom to span: {}", mRadiusGeoPoint);
						mapController.zoomToSpan(mRadiusGeoPoint.getLatitudeE6(),
								mRadiusGeoPoint.getLongitudeE6());
					}
				}
			}
			if (mGoogleView != null) {
				mGoogleView.setVisibility(View.GONE);
			}
			mOsmView.setVisibility(View.VISIBLE);
			mMapView = mOsmView;
			mOsmView.requestLayout();

		} else if (mMapViewSelection == MapViewSelection.Google) {
			if (mGoogleView == null) {
				final com.google.android.maps.MapView mapView =
						new com.google.android.maps.MapView(this,
								API_KEY);
				mGoogleView = mapView;

				LinearLayout container =
						(LinearLayout) findViewById(R.id.MapContainer);
				container.addView(mGoogleView);
				container.requestLayout();

				mapView.setBuiltInZoomControls(true);
				mapView.displayZoomControls(true);
				mapView.setDrawingCacheEnabled(true);

				final org.osmdroid.google.wrapper.MyLocationOverlay mlo =
						new org.osmdroid.google.wrapper.MyLocationOverlay(this, mGoogleView);
//				mGoogleView.getOverlays().add(mlo);
				mMyLocationOverlay = mlo;
//				mGoogleView.getOverlays().add(mGoogleLocationOverlay);

				MapController mapController = mGoogleView.getController();

				LOG.debug("Setting default google zoom to: {}", DEFAULT_ZOOM);
				mapController.setZoom(DEFAULT_ZOOM);

				if (mCenterGeoPoint != null) {
					LOG.debug("Setting center to: {}", mCenterGeoPoint);
					GeoPoint center = new GeoPoint(mCenterGeoPoint.getLatitudeE6(),
							mCenterGeoPoint.getLongitudeE6());
					mapController.setCenter(center);

					if (mRadiusGeoPoint != null) {
						LOG.debug("Setting zoom to span: {}", mRadiusGeoPoint);
						mapController.zoomToSpan(mRadiusGeoPoint.getLatitudeE6(),
								mRadiusGeoPoint.getLongitudeE6());
					}
				}
			}
			mMapView = new org.osmdroid.google.wrapper.MapView(mGoogleView);

			if (mOsmView != null) {
				mOsmView.setVisibility(View.GONE);
			}
			mGoogleView.setVisibility(View.VISIBLE);
			mGoogleView.requestLayout();
		}

		findViewById(R.id.MapContainer).requestLayout();

		mMyLocationOverlay.enableMyLocation();

		setupButtons();

		updateStatusText();
	}

	@Override
	public final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();

		mField = intent.getStringExtra("field");

		setupPoints(intent);
	}

	private void setupPoints(Intent intent) {
		if (intent.getData() == null) {
			if (intent.hasExtra(AvroSchemaProperties.LATITUDE)
					&& intent.hasExtra(AvroSchemaProperties.LONGITUDE)) {
				int latitudeE6 = intent.getIntExtra(AvroSchemaProperties.LATITUDE, 0);
				int longitudeE6 = intent.getIntExtra(AvroSchemaProperties.LONGITUDE, 0);
				mCenterGeoPoint = new org.osmdroid.google.wrapper.GeoPoint(
						new GeoPoint(latitudeE6, longitudeE6));
			}
			if (intent.hasExtra(AvroSchemaProperties.RADIUS_LATITUDE)
					&& intent.hasExtra(AvroSchemaProperties.RADIUS_LONGITUDE)) {
				int latitudeE6 = intent.getIntExtra(AvroSchemaProperties.RADIUS_LATITUDE, 0);
				int longitudeE6 = intent.getIntExtra(AvroSchemaProperties.RADIUS_LONGITUDE, 0);
				mRadiusGeoPoint = new org.osmdroid.google.wrapper.GeoPoint(
						new GeoPoint(latitudeE6, longitudeE6));
			}
		} else {
			Cursor c = null;
			try {
				c = getContentResolver().query(intent.getData(),
						new String[]
								{mField + AvroSchemaProperties.LATITUDE,
					mField + AvroSchemaProperties.LONGITUDE,
					mField + AvroSchemaProperties.RADIUS_LATITUDE,
					mField + AvroSchemaProperties.RADIUS_LONGITUDE}, null, null, null);
				if (c != null && c.moveToFirst()) {
					if (c.getInt(0) != 0 ||
							c.getInt(0) != 0) {
						mCenterGeoPoint = new org.osmdroid.google.wrapper.GeoPoint(
								new GeoPoint(c.getInt(0), c.getInt(1)));
						// CHECKSTYLE:OFF 3 is not so magic since it is just an
						// index into the array above I think it is clear enough.
						mRadiusGeoPoint = new org.osmdroid.google.wrapper.GeoPoint(
								new GeoPoint(c.getInt(2), c.getInt(3)));
					} else {
						LOG.debug("Defaulting to Vancouver since zero.");
						// Vancouver, CA
						mCenterGeoPoint = new org.osmdroid.google.wrapper.GeoPoint(
								new GeoPoint((int) (49286675),
										(int) (-123116097)));
					}
					// CHECKSTYLE:ON
				} else {
					LOG.debug("Defaulting to Vancouver.");
					// Vancouver, CA
					mCenterGeoPoint = new org.osmdroid.google.wrapper.GeoPoint(
							new GeoPoint((int) (49286675),
									(int) (-123116097)));
				}
			} catch (Exception e) {
				LOG.error("Got error fetching data.", e);
			} finally {
				if (c != null) {
					try {
						c.close();
					} catch (Exception e2) {
						LOG.error("Error closing cursor.", e2);
					}
				}
			}
		}
	}

	/**
	 * Sets up the buttons which the user uses to interact.
	 */
	private void setupButtons() {
		Button resetButton = (Button) findViewById(R.id.ResetButton);
		resetButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				mCenterGeoPoint = null;
				mRadiusMode = false;
				updateStatusText();
			}
		});

//		Button doneButton = (Button) findViewById(R.id.DoneButton);
//		doneButton.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(final View v) {
//				done();
//			}
//		});
	}

	private void done() {
		if (getIntent().getData() == null) {
			Intent intent = new Intent();
			intent.putExtra(mField + AvroSchemaProperties.LONGITUDE,
					mCenterGeoPoint.getLongitudeE6());
			intent.putExtra(mField + AvroSchemaProperties.LATITUDE,
					mCenterGeoPoint.getLatitudeE6());
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			if (mMapViewSelection == MapViewSelection.Google) {
				mGoogleView.getDrawingCache().compress(
						CompressFormat.JPEG, MAP_IMAGE_QUALITY, bos);
			} else {
				mOsmView.getDrawingCache().compress(
						CompressFormat.JPEG, MAP_IMAGE_QUALITY, bos);
			}

			intent.putExtra(mField, bos.toByteArray());
			bos = null;
			if (mRadiusMode) {
				intent.putExtra(mField + AvroSchemaProperties.RADIUS_LONGITUDE,
						mRadiusGeoPoint.getLongitudeE6());
				intent.putExtra(mField + AvroSchemaProperties.RADIUS_LATITUDE,
						mRadiusGeoPoint.getLatitudeE6());
				intent.putExtra(mField + AvroSchemaProperties.RADIUS_IN_METERS, mRadiusInMeters);
			}
			setResult(RESULT_OK, intent);
			finish();
		} else {
			ContentValues values = new ContentValues();
			if (mCenterGeoPoint != null) {
				values.put(mField + AvroSchemaProperties.LONGITUDE, mCenterGeoPoint.getLongitudeE6());
				values.put(mField + AvroSchemaProperties.LATITUDE, mCenterGeoPoint.getLatitudeE6());
				if (mRadiusGeoPoint != null) {
					values.put(mField + AvroSchemaProperties.RADIUS_LONGITUDE,
							mRadiusGeoPoint.getLongitudeE6());
					values.put(mField + AvroSchemaProperties.RADIUS_LATITUDE,
							mRadiusGeoPoint.getLatitudeE6());
				} else {
					values.put(mField + AvroSchemaProperties.RADIUS_LONGITUDE, 0);
					values.put(mField + AvroSchemaProperties.RADIUS_LATITUDE, 0);
				}
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				if (mMapViewSelection == MapViewSelection.Google) {
					mGoogleView.getDrawingCache().compress(
							CompressFormat.JPEG, MAP_IMAGE_QUALITY, bos);
				} else {
					mOsmView.getDrawingCache().compress(
							CompressFormat.JPEG, MAP_IMAGE_QUALITY, bos);
				}
				values.put(mField, bos.toByteArray());
				// Let GC collect this since it is kind of big.
				bos = null;
				getContentResolver().update(
						getIntent().getData(), values, null, null);
				setResult(RESULT_OK);
			} else {
				setResult(RESULT_CANCELED);
			}
			finish();
		}
	}

	/**
	 * Sets up the radius overlay.
	 * @return
	 */
	private void setupOverlay() {
		setupOsmOverlay();
		setupGoogleOverlay();
	}

	private void drawMarker(Canvas canvas, Point centerPoint) {
		Drawable marker = getResources().getDrawable(R.drawable.marker);
		int hh = marker.getIntrinsicHeight();
		int hw = marker.getIntrinsicWidth() / 2;
		marker.setBounds(centerPoint.x - hw, centerPoint.y - hh,
				centerPoint.x + hw, centerPoint.y);
		marker.draw(canvas);
	}

	private void setupOsmOverlay() {
		mOsmLocationOverlay = new org.osmdroid.views.overlay.Overlay(this) {

			@Override
			public boolean onSingleTapUp(final MotionEvent e,
					final org.osmdroid.views.MapView mapView) {
				LOG.debug("SingleTapUp clicked.");

				float[] results = new float[1];
				if (!mRadiusMode) {
					mCenterGeoPoint =
							mapView.getProjection().fromPixels(
									e.getX(), e.getY());
					mRadiusGeoPoint =
							mapView.getProjection().fromPixels(
									e.getX(), e.getY());
					mRadiusMode = true;
				} else {
					mRadiusGeoPoint =
							mapView.getProjection().fromPixels(
									e.getX(), e.getY());
					Location.distanceBetween(
							mCenterGeoPoint.getLatitudeE6() / POWER_OF_SIX,
							mCenterGeoPoint.getLongitudeE6() / POWER_OF_SIX,
							mRadiusGeoPoint.getLatitudeE6() / POWER_OF_SIX,
							mRadiusGeoPoint.getLongitudeE6() / POWER_OF_SIX,
							results);

					mRadiusInMeters = results[0];
					mRadiusMode = false;

				}
				updateStatusText();

				return super.onSingleTapUp(e, mapView);
			}

			@Override
			public void draw(final Canvas canvas,
					final org.osmdroid.views.MapView mapView,
					final boolean shadow) {
				if (mCenterGeoPoint != null) {
					Paint paint = new Paint();
					paint.setStyle(Paint.Style.FILL);
					paint.setAlpha(RADIUS_ALPHA);

					org.osmdroid.views.MapView.Projection projection =
							mapView.getProjection();
					Point centerPoint = new Point();
					Point radiusPoint = new Point();
					projection.toPixels(mCenterGeoPoint, centerPoint);

					drawMarker(canvas, centerPoint);

					if (mRadiusGeoPoint != null) {
						projection.toPixels(mRadiusGeoPoint, radiusPoint);

						float radiusInPixels;
						radiusInPixels =
								(float) Math.sqrt(
										Math.pow(
												centerPoint.x - radiusPoint.x, 2)
												+ Math.pow(
														centerPoint.y - radiusPoint.y, 2));

						canvas.drawCircle(centerPoint.x, centerPoint.y,
								radiusInPixels, paint);
					}
				}
			}
		};

	}

	private void setupGoogleOverlay() {
		mGoogleLocationOverlay = new com.google.android.maps.Overlay() {

			@Override
			public boolean onTap(final GeoPoint p,
					final com.google.android.maps.MapView mapView) {
				LOG.debug("onTap: {}", p);
				float[] results = new float[1];
				if (!mRadiusMode) {
					mCenterGeoPoint =
							new org.osmdroid.google.wrapper.GeoPoint(p);
					mRadiusGeoPoint =
							new org.osmdroid.google.wrapper.GeoPoint(p);
					mRadiusMode = true;
				} else {
					mRadiusGeoPoint =
							new org.osmdroid.google.wrapper.GeoPoint(p);
					Location.distanceBetween(
							mCenterGeoPoint.getLatitudeE6() / POWER_OF_SIX,
							mCenterGeoPoint.getLongitudeE6() / POWER_OF_SIX,
							mRadiusGeoPoint.getLatitudeE6() / POWER_OF_SIX,
							mRadiusGeoPoint.getLongitudeE6() / POWER_OF_SIX,
							results);

					mRadiusInMeters = results[0];
					mRadiusMode = false;

				}
				updateStatusText();
				return super.onTap(p, mapView);
			}

			@Override
			public void draw(final Canvas canvas,
					final com.google.android.maps.MapView mapView,
					final boolean shadow) {
				if (mCenterGeoPoint != null) {
					Paint paint = new Paint();
					paint.setStyle(Paint.Style.FILL);
					paint.setAlpha(RADIUS_ALPHA);

					com.google.android.maps.Projection projection =
							mapView.getProjection();
					Point centerPoint = new Point();
					Point radiusPoint = new Point();
					// Have to unwrap...
					GeoPoint center = new GeoPoint(
							mCenterGeoPoint.getLatitudeE6(),
							mCenterGeoPoint.getLongitudeE6());
					projection.toPixels(center, centerPoint);

					drawMarker(canvas, centerPoint);

					if (mRadiusGeoPoint != null) {

						GeoPoint radius = new GeoPoint(
								mRadiusGeoPoint.getLatitudeE6(),
								mRadiusGeoPoint.getLongitudeE6());
						projection.toPixels(radius, radiusPoint);

						float radiusInPixels;
						radiusInPixels =
								(float) Math.sqrt(
										Math.pow(
												centerPoint.x - radiusPoint.x, 2)
												+ Math.pow(
														centerPoint.y - radiusPoint.y, 2));

						canvas.drawCircle(centerPoint.x, centerPoint.y,
								radiusInPixels, paint);
					}
				}
			}
		};
	}

	/** Updates the status text shown to the user. */
	private void updateStatusText() {
		TextView statusTextView = (TextView) findViewById(R.id.StatusTextView);
		if (mRadiusMode) {
			statusTextView.setText("Tap to set radius");
		} else {
			statusTextView.setText("Tap to (re)set center");
		}
		if (mOsmView != null) {
			mOsmView.invalidate();
		}
		if (mGoogleView != null) {
			mGoogleView.invalidate();
		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

}

