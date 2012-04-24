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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

/**
 * A picker for a location (with radius).
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
public class LocationPicker extends MapActivity {
	/** Access to logging. */
	private static final Logger LOG = LoggerFactory
			.getLogger(LocationPicker.class);

	/** Power of six factor for dealing with lat and long values. */
	private static final double	POWER_OF_SIX	= 1E6;

	private static final String API_KEY =
			"0OomkHbNcEYGHtSAw6TIIuX6q1a3iJIT0qSEZwA";

	/** The alpha value for the radius circle. */
	private static final int	RADIUS_ALPHA	= 30;

	/** The quality passed to compress the image. */
	protected static final int	MAP_IMAGE_QUALITY	= 25;

	/** The action that triggers this activity. */
	public static final String ACTION_PICK_LOCATION =
			Intent.ACTION_PICK + "_LOCATION";

	/** The map view displaying the map. */
	private MapView mMapView;
	/** The overlay with our picked point. */
	private Overlay mLocationsOverlay;

	/** The centered point. */
	private GeoPoint mCenterGeoPoint;
	/** The radius point. */
	private GeoPoint mRadiusGeoPoint;
	/** Are we showing a radius currently. */
	private boolean mRadiusMode = false;

	/** The radius calculated into meters. */
	private float mRadiusInMeters;

	private String mField;

	@Override
	public final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();

		mField = intent.getStringExtra("field");

		String latField =
				getField(AvroSchemaProperties.LATITUDE);
		String lonField =
				getField(AvroSchemaProperties.LONGITUDE);
		String radiusLatField =
				getField(AvroSchemaProperties.RADIUS_LATITUDE);
		String radiusLonField =
				getField(AvroSchemaProperties.RADIUS_LONGITUDE);

		if (intent.getData() == null) {


			if (intent.hasExtra(latField)
					&& intent.hasExtra(lonField)) {
				int latitudeE6 = intent.getIntExtra(latField, 0);
				int longitudeE6 = intent.getIntExtra(lonField, 0);
				mCenterGeoPoint = new GeoPoint(latitudeE6, longitudeE6);
			}
			if (intent.hasExtra(radiusLatField)
					&& intent.hasExtra(radiusLonField)) {
				int latitudeE6 = intent.getIntExtra(radiusLatField, 0);
				int longitudeE6 = intent.getIntExtra(radiusLonField, 0);
				mRadiusGeoPoint = new GeoPoint(latitudeE6, longitudeE6);
			}
		} else {
			Cursor c = null;
			try {
				c = getContentResolver().query(intent.getData(),
						getProjection(mField), null, null, null);
				if (c != null && c.moveToFirst()) {
					mCenterGeoPoint = new GeoPoint(c.getInt(0), c.getInt(1));
					// CHECKSTYLE:OFF 3 is not so magic since it is just an
					// index into the array above I think it is clear enough.
					mRadiusGeoPoint = new GeoPoint(c.getInt(2), c.getInt(3));
					// CHECKSTYLE:ON
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

		setContentView(R.layout.locationpicker);
		mMapView = new MapView(this, API_KEY);

		LinearLayout container = (LinearLayout) findViewById(R.id.MapContainer);
		container.addView(mMapView);
		container.requestLayout();

		mMapView.setBuiltInZoomControls(true);
		mMapView.setDrawingCacheEnabled(true);
		mMapView.setClickable(true);
		mMapView.getController().setZoom(2);

		MapController mapController = mMapView.getController();

		if (mCenterGeoPoint != null) {
			mapController.setCenter(mCenterGeoPoint);
			if (mRadiusGeoPoint != null) {
				mapController.zoomToSpan(mRadiusGeoPoint.getLatitudeE6(),
						mRadiusGeoPoint.getLongitudeE6());
			}
		}

		setupOverlay();
		setupButtons();

		updateStatusText();
	}

	private String getField(String extension) {
		return mField + extension;
	}

	private String[] getProjection(String mField) {
		return new String[]
				{
				getField(AvroSchemaProperties.LATITUDE),
				getField(AvroSchemaProperties.LONGITUDE),
				getField(AvroSchemaProperties.RADIUS_LATITUDE),
				getField(AvroSchemaProperties.RADIUS_LONGITUDE)};
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
				mMapView.invalidate();
			}
		});

		Button doneButton = (Button) findViewById(R.id.DoneButton);
		doneButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				String latField =
						getField(AvroSchemaProperties.LATITUDE);
				String lonField =
						getField(AvroSchemaProperties.LONGITUDE);
				String radiusLatField =
						getField(AvroSchemaProperties.RADIUS_LATITUDE);
				String radiusLonField =
						getField(AvroSchemaProperties.RADIUS_LONGITUDE);
				String radius =
						getField(AvroSchemaProperties.RADIUS_IN_METERS);

				if (getIntent().getData() == null) {
					Intent intent = new Intent();
					intent.putExtra(latField,
							mCenterGeoPoint.getLongitudeE6());
					intent.putExtra(latField, mCenterGeoPoint.getLatitudeE6());
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					mMapView.getDrawingCache().compress(
							CompressFormat.JPEG, MAP_IMAGE_QUALITY, bos);
					intent.putExtra(mField, bos.toByteArray());
					bos = null;
					if (mRadiusMode) {
						intent.putExtra(radiusLatField,
								mRadiusGeoPoint.getLongitudeE6());
						intent.putExtra(radiusLonField,
								mRadiusGeoPoint.getLatitudeE6());
						intent.putExtra(radius, mRadiusInMeters);
					}
					setResult(RESULT_OK, intent);
					finish();
				} else {
					ContentValues values = new ContentValues();
					values.put(lonField, mCenterGeoPoint.getLongitudeE6());
					values.put(latField, mCenterGeoPoint.getLatitudeE6());
					values.put(radiusLonField,
							mRadiusGeoPoint.getLongitudeE6());
					values.put(radiusLatField,
							mRadiusGeoPoint.getLatitudeE6());
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					mMapView.getDrawingCache().compress(
							CompressFormat.JPEG, MAP_IMAGE_QUALITY, bos);
					values.put(mField, bos.toByteArray());
					// Let GC collect this since it is kind of big.
					bos = null;
					getContentResolver().update(
							getIntent().getData(), values, null, null);
					setResult(RESULT_OK);
					finish();
				}
			}
		});
	}

	/**
	 * Sets up the radius overlay.
	 */
	private void setupOverlay() {
		mLocationsOverlay = new Overlay() {

			@Override
			public boolean onTap(final GeoPoint p, final MapView mapView) {
				float[] results = new float[1];
				if (!mRadiusMode) {
					mCenterGeoPoint = p;
					mRadiusGeoPoint = p;
					mRadiusMode = true;
				} else {
					mRadiusGeoPoint = p;
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
					final MapView mapView, final boolean shadow) {
				if (mCenterGeoPoint != null) {
					Paint paint = new Paint();
					paint.setStyle(Paint.Style.FILL);
					paint.setAlpha(RADIUS_ALPHA);

					Projection projection = mapView.getProjection();
					Point centerPoint = new Point();
					Point radiusPoint = new Point();
					projection.toPixels(mCenterGeoPoint, centerPoint);
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
		};

		mMapView.getOverlays().add(mLocationsOverlay);
	}

	/** Updates the status text shown to the user. */
	private void updateStatusText() {
		TextView statusTextView = (TextView) findViewById(R.id.StatusTextView);
		if (mRadiusMode) {
			statusTextView.setText("Tap to set radius");
		} else {
			statusTextView.setText("Tap to (re)set center");
		}

	}

	@Override
	protected final boolean isRouteDisplayed() {
		return false;
	}

}

