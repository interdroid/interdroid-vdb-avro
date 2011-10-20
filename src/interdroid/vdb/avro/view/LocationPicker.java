package interdroid.vdb.avro.view;

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

	/** The alpha value for the radius circle. */
	private static final int	RADIUS_ALPHA	= 30;

	/** The quality passed to compress the image. */
	protected static final int	MAP_IMAGE_QUALITY	= 25;

	/** The action that triggers this activity. */
	public static final String ACTION_PICK_LOCATION =
			Intent.ACTION_PICK + "_LOCATION";

	/** The radius given in meters. */
	public static final String RADIUSINMETERS = "RadiusInMeters";
	/** The longitude key. */
	public static final String RADIUS_LONGITUDE = "Longitude";
	/** The latitude key. */
	public static final String RADIUS_LATITUDE = "Latitude";
	/** The radius longitude key. */
	public static final String LONGITUDE = "RadiusLongitude";
	/** The radius latitude key. */
	public static final String LATITUDE = "RadiusLatitude";
	/** The image of the map picked. */
	public static final String MAP_IMAGE = "MapImage";

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

	@Override
	public final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();

		if (intent.getData() == null) {
			if (intent.hasExtra(LATITUDE) && intent.hasExtra(LONGITUDE)) {
				int latitudeE6 = intent.getIntExtra(LATITUDE, 0);
				int longitudeE6 = intent.getIntExtra(LONGITUDE, 0);
				mCenterGeoPoint = new GeoPoint(latitudeE6, longitudeE6);
			}
			if (intent.hasExtra(RADIUS_LATITUDE)
					&& intent.hasExtra(RADIUS_LONGITUDE)) {
				int latitudeE6 = intent.getIntExtra(RADIUS_LATITUDE, 0);
				int longitudeE6 = intent.getIntExtra(RADIUS_LONGITUDE, 0);
				mRadiusGeoPoint = new GeoPoint(latitudeE6, longitudeE6);
			}
		} else {
			Cursor c = null;
			try {
				c = getContentResolver().query(intent.getData(),
						new String[]
								{LATITUDE, LONGITUDE, RADIUS_LATITUDE,
					RADIUS_LONGITUDE}, null, null, null);
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
		mMapView = (MapView) findViewById(R.id.MapView);
		mMapView.setBuiltInZoomControls(true);
		mMapView.displayZoomControls(true);
		mMapView.setDrawingCacheEnabled(true);

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
				if (getIntent().getData() == null) {
					Intent intent = new Intent();
					intent.putExtra(LONGITUDE,
							mCenterGeoPoint.getLongitudeE6());
					intent.putExtra(LATITUDE, mCenterGeoPoint.getLatitudeE6());
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					mMapView.getDrawingCache().compress(
							CompressFormat.JPEG, MAP_IMAGE_QUALITY, bos);
					System.gc();
					intent.putExtra(MAP_IMAGE, bos.toByteArray());
					bos = null;
					if (mRadiusMode) {
						intent.putExtra(RADIUS_LONGITUDE,
								mRadiusGeoPoint.getLongitudeE6());
						intent.putExtra(RADIUS_LATITUDE,
								mRadiusGeoPoint.getLatitudeE6());
						intent.putExtra(RADIUSINMETERS, mRadiusInMeters);
					}
					setResult(RESULT_OK, intent);
					finish();
				} else {
					ContentValues values = new ContentValues();
					values.put(LONGITUDE, mCenterGeoPoint.getLongitudeE6());
					values.put(LATITUDE, mCenterGeoPoint.getLatitudeE6());
					values.put(RADIUS_LONGITUDE,
							mRadiusGeoPoint.getLongitudeE6());
					values.put(RADIUS_LATITUDE,
							mRadiusGeoPoint.getLatitudeE6());
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					mMapView.getDrawingCache().compress(
							CompressFormat.JPEG, MAP_IMAGE_QUALITY, bos);
					values.put(MAP_IMAGE, bos.toByteArray());
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

