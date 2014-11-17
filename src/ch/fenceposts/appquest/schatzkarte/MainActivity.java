// http://madushankaperera.wordpress.com/2014/03/20/google-maps-android-api-v2-current-location-example-with-gps/
// https://developers.google.com/maps/documentation/android/v1/hello-mapview !!!!!!
// http://stackoverflow.com/questions/9417378/osmdroid-overlays-multiple-static-and-one-dynamic
// http://stackoverflow.com/questions/10879447/android-itemizedoverlay-place-and-remove-markers-on-map
package ch.fenceposts.appquest.schatzkarte;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.modules.MBTilesFileArchive;
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.TilesOverlay;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import ch.fenceposts.appquest.schatzkarte.overlay.MyItemizedOverlay;

public class MainActivity extends Activity implements LocationListener, OnClickListener {

	private static final double LATITUDE_HSR = 47.223319;
	private static final double LONGITUDE_HSR = 8.817275;
	private static final String DEBUG_TAG = "mydebug";
	private CheckBox checkBoxTrace;
	private DefaultResourceProxyImpl resourceProxy;
	private DisplayMetrics displayMetrics;
	private GeoPoint positionHsr = new GeoPoint(LATITUDE_HSR, LONGITUDE_HSR);
	private GeoPoint positionCurrent = positionHsr;
	private IMapController controllerMapView;
	private LocationManager locationManager;
	private List<Overlay> mapOverlays;
	private MapView mapViewMap;
	private MyItemizedOverlay itemizedOverlay;
	private OverlayItem overlayItemHsr;
	private TextView textViewLatitudeValue;
	private TextView textViewLongitudeValue;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

		resourceProxy = new DefaultResourceProxyImpl(getApplicationContext());
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			Log.d(DEBUG_TAG, "GPS_PROVIDER is not enabled");
		} else {
			Log.d(DEBUG_TAG, "GPS_PROVIDER is enabled");
		}

		textViewLatitudeValue = (TextView) findViewById(R.id.textViewLatitudeValue);
		textViewLongitudeValue = (TextView) findViewById(R.id.textViewLongitudeValue);
		checkBoxTrace = (CheckBox) findViewById(R.id.checkBoxTrace);

		mapViewMap = (MapView) findViewById(R.id.mapViewMap /*
															 * eure ID der Map
															 * View
															 */);
		mapViewMap.setTileSource(TileSourceFactory.MAPQUESTOSM);

		mapViewMap.setMultiTouchControls(true);
		mapViewMap.setBuiltInZoomControls(true);

		controllerMapView = mapViewMap.getController();
		controllerMapView.setZoom(18);

		// Die TileSource beschreibt die Eigenschaften der Kacheln die wir
		// anzeigen
		XYTileSource treasureMapTileSource = new XYTileSource("mbtiles", ResourceProxy.string.offline_mode, 1, 20, 256, ".png", new String[] { "http://example.org/" });

		File file = new File(Environment.getExternalStorageDirectory() /*
																		 * entspricht
																		 * sdcard
																		 */, "hsr.mbtiles");

		/*
		 * Das verwenden von mbtiles ist leider ein wenig aufwändig, wir müssen
		 * unsere XYTileSource in verschiedene Klassen 'verpacken' um sie dann
		 * als TilesOverlay über der Grundkarte anzuzeigen.
		 */
		MapTileModuleProviderBase treasureMapModuleProvider = new MapTileFileArchiveProvider(new SimpleRegisterReceiver(this), treasureMapTileSource,
				new IArchiveFile[] { MBTilesFileArchive.getDatabaseFileArchive(file) });

		MapTileProviderBase treasureMapProvider = new MapTileProviderArray(treasureMapTileSource, null, new MapTileModuleProviderBase[] { treasureMapModuleProvider });
		TilesOverlay treasureMapTilesOverlay = new TilesOverlay(treasureMapProvider, getBaseContext());
		treasureMapTilesOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);

		// Jetzt können wir den Overlay zu unserer Karte hinzufügen:
		mapViewMap.getOverlays().add(treasureMapTilesOverlay);

		mapOverlays = mapViewMap.getOverlays();
		overlayItemHsr = new OverlayItem("HSR", "Hochschule für Technik Rapperswil", positionHsr);
		Drawable drawableMarkerDefault = this.getResources().getDrawable(R.drawable.androidmarker);

		itemizedOverlay = new MyItemizedOverlay(drawableMarkerDefault, resourceProxy, this) {

			@Override
			public boolean onLongPress(MotionEvent e, MapView mapView) {
				IGeoPoint iGeoPoint = mapViewMap.getProjection().fromPixels((int) e.getX(), (int) e.getY());
				addItem((GeoPoint) iGeoPoint);
				mapOverlays.add(this);
				populate();
				mapViewMap.invalidate();

				return super.onLongPress(e, mapView);
			}

			@Override
			public boolean onDoubleTap(MotionEvent e, MapView mapView) {

				List<OverlayItem> tempMOverlays = new ArrayList<OverlayItem>(this.mOverlays);

				for (OverlayItem overlayItem : tempMOverlays) {

					Projection projection = mapViewMap.getProjection();
					GeoPoint geoPointItem = overlayItem.getPoint();

					Rect screenRect = mapView.getScreenRect(new Rect());
					Point pointItem = projection.toPixels(geoPointItem, null);

					float pixelDistanceX = pointItem.x - screenRect.left;
					float pixelDistanceY = pointItem.y - screenRect.top;

					double distance = Math.sqrt((e.getX() - pixelDistanceX) * (e.getX() - pixelDistanceX) + (e.getY() - pixelDistanceY) * (e.getY() - pixelDistanceY));

					if (distance < (Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels) / 9)) {
						this.mOverlays.remove(overlayItem);
						populate();
						mapViewMap.invalidate();
					}
				}

				return super.onLongPress(e, mapView);
			}
		};

		itemizedOverlay.addOverlay(overlayItemHsr);
		mapOverlays.add(itemizedOverlay);
		mapViewMap.invalidate();
		writePosition(positionCurrent);
	}

	@Override
	protected void onResume() {
		super.onResume();

		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 8484, 1, this);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 8484, 1, this);

		if (checkBoxTrace.isChecked()) {
			controllerMapView.animateTo(positionCurrent);
		} else {
			controllerMapView.animateTo(positionHsr);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		locationManager.removeUpdates(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onLocationChanged(Location location) {
		Log.d(DEBUG_TAG, "onLocationChanged called");

		positionCurrent = new GeoPoint(location.getLatitude(), location.getLongitude());

		Log.d(DEBUG_TAG, "Longitude: " + Double.toString(positionCurrent.getLongitude()) + " / Latitude: " + Double.toString(positionCurrent.getLatitude()));

		writePosition(positionCurrent);

		if (checkBoxTrace.isChecked()) {
			controllerMapView.animateTo(positionCurrent);
		}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub

	}

	public void writePosition(GeoPoint positionCurrent2) {
		textViewLatitudeValue.setText(Double.toString(positionCurrent2.getLatitude()));
		textViewLongitudeValue.setText(Double.toString(positionCurrent2.getLongitude()));
	}

	public void centerCurrentPosition(View view) {
		controllerMapView.animateTo(positionCurrent);
	}

	public void centerHsr(View view) {
		controllerMapView.animateTo(positionHsr);
	}

	// ein Marker in der aktuellen Mitte setzen (Fadenkreuz)
	public void setMarkerCenter(View view) {
		// Put overlay icon a little way from map centre
		itemizedOverlay.addItem((GeoPoint) mapViewMap.getMapCenter());

//		mapOverlays.add(itemizedOverlay);
		mapViewMap.invalidate();
	}
}
