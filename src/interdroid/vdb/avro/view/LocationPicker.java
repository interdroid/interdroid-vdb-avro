package interdroid.vdb.avro.view;

import java.io.ByteArrayOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import interdroid.vdb.avro.R;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

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
import android.widget.TextView;

public class LocationPicker extends MapActivity {
	private static final Logger logger = LoggerFactory
			.getLogger(LocationPicker.class);

	public static final String ACTION_PICK_LOCATION = Intent.ACTION_PICK + "_LOCATION";

	public static final String RADIUSINMETERS = "RadiusInMeters";
	public static final String RADIUS_LONGITUDE = "Longitude";
	public static final String RADIUS_LATITUDE = "Latitude";
	public static final String LONGITUDE = "RadiusLongitude";
	public static final String LATITUDE = "RadiusLatitude";
	public static final String MAP_IMAGE = "MapImage";

	private MapView mMapView;
	private Overlay mLocationsOverlay;

	private GeoPoint mCenterGeoPoint;
	private GeoPoint mRadiusGeoPoint;
	private boolean mRadiusMode = false;

	private float mRadiusInMeters;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();

		if (intent.getData() == null) {
			if (intent.hasExtra(LATITUDE) && intent.hasExtra(LONGITUDE)) {
				int latitudeE6 = intent.getIntExtra(LATITUDE, 0);
				int longitudeE6 = intent.getIntExtra(LONGITUDE, 0);
				mCenterGeoPoint = new GeoPoint(latitudeE6, longitudeE6);
			}
			if (intent.hasExtra(RADIUS_LATITUDE) && intent.hasExtra(RADIUS_LONGITUDE)) {
				int latitudeE6 = intent.getIntExtra(RADIUS_LATITUDE, 0);
				int longitudeE6 = intent.getIntExtra(RADIUS_LONGITUDE, 0);
				mRadiusGeoPoint = new GeoPoint(latitudeE6, longitudeE6);
			}
		} else {
			Cursor c = null;
			try {
				c = getContentResolver().query(intent.getData(),
						new String[] {LATITUDE, LONGITUDE, RADIUS_LATITUDE, RADIUS_LONGITUDE}, null, null, null);
				if (c != null && c.moveToFirst()) {
					mCenterGeoPoint = new GeoPoint(c.getInt(0), c.getInt(1));
					mRadiusGeoPoint = new GeoPoint(c.getInt(2), c.getInt(3));
				}
			} catch (Exception e) {
				logger.error("Got error fetching data.", e);
			} finally {
				if (c != null) {
					try {
						c.close();
					} catch (Exception e2) {
						logger.error("Error closing cursor.", e2);
					}
				}
			}
		}

		setContentView(R.layout.locationpicker);
		mMapView = (MapView) findViewById(R.id.MapView);
		mMapView.setBuiltInZoomControls(true);
		mMapView.displayZoomControls(true);
		mMapView.setDrawingCacheEnabled(true);

		MapController mapController = mMapView.getController();

		if (mCenterGeoPoint != null) {
			mapController.setCenter(mCenterGeoPoint);
			if (mRadiusGeoPoint != null) {
				mapController.zoomToSpan(mRadiusGeoPoint.getLatitudeE6(), mRadiusGeoPoint.getLongitudeE6());
			}
		}

		mLocationsOverlay = new Overlay() {

			@Override
			public boolean onTap(GeoPoint p, MapView mapView) {
				float[] results = new float[1];
				if (!mRadiusMode) {
					mCenterGeoPoint = p;
					mRadiusGeoPoint = p;
					mRadiusMode = true;
				} else {
					mRadiusGeoPoint = p;
					Location.distanceBetween(mCenterGeoPoint.getLatitudeE6() / 1E6,
							mCenterGeoPoint.getLongitudeE6() / 1E6,
							mRadiusGeoPoint.getLatitudeE6() / 1E6,
							mRadiusGeoPoint.getLongitudeE6() / 1E6, results);

					mRadiusInMeters = results[0];
					mRadiusMode = false;

				}
				updateStatusText();
				return super.onTap(p, mapView);
			}

			@Override
			public void draw(Canvas canvas, MapView mapView, boolean shadow) {
				if (mCenterGeoPoint != null ) {
					Paint paint = new Paint();
					paint.setStyle(Paint.Style.FILL);
					paint.setAlpha(30);

					Projection projection = mapView.getProjection();
					Point centerPoint = new Point();
					Point radiusPoint = new Point();
					projection.toPixels(mCenterGeoPoint, centerPoint);
					projection.toPixels(mRadiusGeoPoint, radiusPoint);

					float radiusInPixels;
					radiusInPixels = (float) Math.sqrt(Math.pow(centerPoint.x - radiusPoint.x, 2) +
							Math.pow(centerPoint.y - radiusPoint.y, 2));

					canvas.drawCircle(centerPoint.x, centerPoint.y, radiusInPixels, paint);
				}
			}
		};


		Button resetButton = (Button) findViewById(R.id.ResetButton);
		resetButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mCenterGeoPoint = null;
				mRadiusMode = false;
				updateStatusText();
				mMapView.invalidate();
			}
		});

		Button doneButton = (Button) findViewById(R.id.DoneButton);
		doneButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (getIntent().getData() == null) {
					Intent intent = new Intent();
					intent.putExtra(LONGITUDE, mCenterGeoPoint.getLongitudeE6());
					intent.putExtra(LATITUDE, mCenterGeoPoint.getLatitudeE6());
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					mMapView.getDrawingCache().compress(CompressFormat.PNG, 100, bos);
					System.gc();
					intent.putExtra(MAP_IMAGE, bos.toByteArray());
					bos = null;
					if (mRadiusMode) {
						intent.putExtra(RADIUS_LONGITUDE, mRadiusGeoPoint.getLongitudeE6());
						intent.putExtra(RADIUS_LATITUDE, mRadiusGeoPoint.getLatitudeE6());
						intent.putExtra(RADIUSINMETERS, mRadiusInMeters);
					}
					setResult(RESULT_OK, intent);
					finish();
				} else {
					ContentValues values = new ContentValues();
					values.put(LONGITUDE, mCenterGeoPoint.getLongitudeE6());
					values.put(LATITUDE, mCenterGeoPoint.getLatitudeE6());
					values.put(RADIUS_LONGITUDE, mRadiusGeoPoint.getLongitudeE6());
					values.put(RADIUS_LATITUDE, mRadiusGeoPoint.getLatitudeE6());
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					mMapView.getDrawingCache().compress(CompressFormat.PNG, 100, bos);
					values.put(MAP_IMAGE, bos.toByteArray());
					// Let GC collect this since it is kind of big.
					bos = null;
					getContentResolver().update(getIntent().getData(), values, null, null);
					setResult(RESULT_OK);
					finish();
				}
			}
		});
		mMapView.getOverlays().add(mLocationsOverlay);
		updateStatusText();
	}

	private void updateStatusText() {
		TextView statusTextView = (TextView) findViewById(R.id.StatusTextView);
		if (mRadiusMode) {
			statusTextView.setText("Tap to set radius");
		} else {
			statusTextView.setText("Tap to (re)set center");
		}

	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

}

