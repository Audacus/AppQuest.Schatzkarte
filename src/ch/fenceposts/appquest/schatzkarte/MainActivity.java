// http://madushankaperera.wordpress.com/2014/03/20/google-maps-android-api-v2-current-location-example-with-gps/
// https://developers.google.com/maps/documentation/android/v1/hello-mapview !!!!!!
// http://stackoverflow.com/questions/9417378/osmdroid-overlays-multiple-static-and-one-dynamic
// http://stackoverflow.com/questions/10879447/android-itemizedoverlay-place-and-remove-markers-on-map
package ch.fenceposts.appquest.schatzkarte;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.TilesOverlay;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.graphics.Color;
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
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import ch.fenceposts.appquest.schatzkarte.database.LocationDbHelper;
import ch.fenceposts.appquest.schatzkarte.overlay.MyItemizedOverlay;

public class MainActivity extends Activity implements LocationListener, OnClickListener {

	public static DisplayMetrics displayMetrics = new DisplayMetrics();

	private static final double LATITUDE_HSR = 47.223252;
	private static final double LONGITUDE_HSR = 8.817011;
	private static final String DEBUG_TAG = "mydebug";
	private CheckBox checkBoxTrace;
	private DefaultResourceProxyImpl resourceProxy;
	private GeoPoint positionHsr = new GeoPoint(LATITUDE_HSR, LONGITUDE_HSR);
	private GeoPoint positionCurrent = positionHsr;
	private IMapController controllerMapView;
	private LocationDbHelper locationDbHelper;
	private LocationManager locationManager;
	private List<Overlay> mapOverlays;
	private MapView mapViewMap;
	private MyItemizedOverlay itemizedOverlay;
	private TextView textViewLatitudeValue;
	private TextView textViewLongitudeValue;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		locationDbHelper = new LocationDbHelper(getBaseContext());
		locationDbHelper.onUpgrade(locationDbHelper.getWritableDatabase(), LocationDbHelper.DATABASE_VERSION, LocationDbHelper.DATABASE_VERSION);

		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

		resourceProxy = new DefaultResourceProxyImpl(getApplicationContext());
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			Log.d(DEBUG_TAG, "GPS_PROVIDER is not enabled");
			alertGpsProvider();
		} else {
			Log.d(DEBUG_TAG, "GPS_PROVIDER is enabled");
		}
		if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			Log.d(DEBUG_TAG, "NETWORK_PROVIDER is not enabled");
		} else {
			Log.d(DEBUG_TAG, "NETWORK_PROVIDER is enabled");
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

		try {
			File file = new File(Environment.getExternalStorageDirectory() /*
																			 * entspricht
																			 * sdcard
																			 */, "hsr.mbtiles");

			/*
			 * Das verwenden von mbtiles ist leider ein wenig aufwändig, wir
			 * müssen unsere XYTileSource in verschiedene Klassen 'verpacken' um
			 * sie dann als TilesOverlay über der Grundkarte anzuzeigen.
			 */
			if (file != null) {
				MapTileModuleProviderBase treasureMapModuleProvider = new MapTileFileArchiveProvider(new SimpleRegisterReceiver(this), treasureMapTileSource,
						new IArchiveFile[] { MBTilesFileArchive.getDatabaseFileArchive(file) });

				MapTileProviderBase treasureMapProvider = new MapTileProviderArray(treasureMapTileSource, null, new MapTileModuleProviderBase[] { treasureMapModuleProvider });
				TilesOverlay treasureMapTilesOverlay = new TilesOverlay(treasureMapProvider, getBaseContext());
				treasureMapTilesOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);

				// Jetzt können wir den Overlay zu unserer Karte hinzufügen:
				mapViewMap.getOverlays().add(treasureMapTilesOverlay);
			}
		} catch (SQLiteCantOpenDatabaseException scode) {
			Log.d(DEBUG_TAG, "SQLiteCantOpenDatabaseException thrown!");
			alertHsrMbtiles();
		}
		Log.d(DEBUG_TAG, "After error");

		mapOverlays = mapViewMap.getOverlays();

		OverlayItem overlayItemHsr = new OverlayItem("HSR", "Hochschule für Technik Rapperswil", positionHsr);
		Drawable drawableMarkerHsr = this.getResources().getDrawable(R.drawable.marker_hsr);
		MyItemizedOverlay itemizedOverlayHsr = new MyItemizedOverlay(drawableMarkerHsr, resourceProxy, this);
		itemizedOverlayHsr.addOverlay(overlayItemHsr);
		mapOverlays.add(itemizedOverlayHsr);

		Drawable drawableMarkerDefault = this.getResources().getDrawable(R.drawable.marker_default);

		itemizedOverlay = new MyItemizedOverlay(drawableMarkerDefault, resourceProxy, this) {

			@Override
			public boolean onLongPress(MotionEvent e, MapView mapView) {
				Log.d(DEBUG_TAG, "MyItemizedOverlay.onLongPress called");

				IGeoPoint iGeoPoint = mapView.getProjection().fromPixels((int) e.getX(), (int) e.getY());
				addItem((GeoPoint) iGeoPoint);
				populate();
				mapView.invalidate();

				return super.onLongPress(e, mapView);
			}

			@Override
			public boolean onDoubleTap(MotionEvent e, MapView mapView) {
				Log.d(DEBUG_TAG, "MyItemizedOverlay.onDoubleTap called");

				removeItem(e, mapView);

				return super.onLongPress(e, mapView);
			}
		};

		mapOverlays.add(itemizedOverlay);
		mapViewMap.invalidate();
		// writePosition(positionCurrent);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(DEBUG_TAG, "onResume called");

		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, this);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, this);

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
	protected void onDestroy() {
		super.onDestroy();
		mapViewMap.getOverlays().remove(itemizedOverlay);
		deleteDatabase(LocationDbHelper.DATABASE_NAME);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem menuItem = menu.add("Log");
		menuItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				log();
				return false;
			}
		});

		return super.onCreateOptionsMenu(menu);
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

		Log.d(DEBUG_TAG, "Longitude: " + Integer.toString(positionCurrent.getLongitudeE6()) + " / Latitude: " + Integer.toString(positionCurrent.getLatitudeE6()));

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

	private void alertHsrMbtiles() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String text = getResources().getString(R.string.no_hsr_mbtiles);
		builder.setMessage(text);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
				// finish();
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	private void alertGpsProvider() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String text = getResources().getString(R.string.no_gps);
		builder.setMessage(text);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
				// finish();
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	public void writePosition(GeoPoint position) {
		textViewLatitudeValue.setText(String.valueOf(position.getLatitudeE6()));
		textViewLongitudeValue.setText(String.valueOf(position.getLongitudeE6()));
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
		mapViewMap.invalidate();
	}

	// write all locations in json format to log
	private void log() {
		Intent intent = new Intent("ch.appquest.intent.LOG");

		if (getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isEmpty()) {
			Toast.makeText(this, "Logbook App not Installed", Toast.LENGTH_LONG).show();
			return;
		}

		intent.putExtra("ch.appquest.taskname", "Schatzkarte");

		List<GeoPoint> locations = new ArrayList<GeoPoint>(locationDbHelper.getAllLocations());

		JSONArray jsonArray = new JSONArray();
		CharSequence jsonString = "FATAL_FENCEPOSTS_FAILURE";
		try {
			for (GeoPoint geoPoint : locations) {
				JSONObject jsonObjectGeoPoint = new JSONObject();

				jsonObjectGeoPoint.put("lon", geoPoint.getLongitudeE6());
				jsonObjectGeoPoint.put("lat", geoPoint.getLatitudeE6());

				jsonArray.put(jsonObjectGeoPoint);
			}
			Log.d(DEBUG_TAG, "jsonArray:\n" + jsonArray.toString());

			jsonString = jsonArray.toString();
		} catch (JSONException jsone) {
			jsone.printStackTrace();
		}

		// Achtung, je nach App wird etwas anderes eingetragen (siehe Tabelle
		// ganz unten):
		intent.putExtra("ch.appquest.logmessage", jsonString);

		startActivity(intent);
	}
}
