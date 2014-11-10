// http://madushankaperera.wordpress.com/2014/03/20/google-maps-android-api-v2-current-location-example-with-gps/
// http://stackoverflow.com/questions/9417378/osmdroid-overlays-multiple-static-and-one-dynamic
// http://stackoverflow.com/questions/10879447/android-itemizedoverlay-place-and-remove-markers-on-map
package ch.fenceposts.appquest.schatzkarte;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapController;
import org.osmdroid.api.IMapView;
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
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.TilesOverlay;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
//import org.osmdroid.views.overlay.OverlayItem;

public class MainActivity extends Activity implements LocationListener, OnClickListener {

	private static final double LATITUDE_HSR = 47.223319;
	private static final double LONGITUDE_HSR = 8.817275;
	private static final String DEBUG_TAG = "mydebug";
	private DefaultResourceProxyImpl resourceProxy;
	private GeoPoint positionHsr = new GeoPoint(LATITUDE_HSR, LONGITUDE_HSR);
	private GeoPoint positionCurrent;
	private LocationManager locationManager;
	private IMapController controllerMapView;
	private ArrayList<OverlayItem> overlayItems;
	private ItemizedOverlay<OverlayItem> locationOverlay;
	private MapView mapViewMap;
	private TextView textViewLatitudeValue;
	private TextView textViewLongitudeValue;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		resourceProxy = new DefaultResourceProxyImpl(getApplicationContext());

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			Log.d(DEBUG_TAG, "GPS_PROVIDER is not enabled");
		} else {
			Log.d(DEBUG_TAG, "GPS_PROVIDER is enabled");
		}

		textViewLatitudeValue = (TextView) findViewById(R.id.textViewLatitudeValue);
		textViewLongitudeValue = (TextView) findViewById(R.id.textViewLongitudeValue);

		mapViewMap = (MapView) findViewById(R.id.mapViewMap /* eure ID der Map View */);
		mapViewMap.setTileSource(TileSourceFactory.MAPQUESTOSM);

		mapViewMap.setMultiTouchControls(true);
		mapViewMap.setBuiltInZoomControls(true);

		controllerMapView = mapViewMap.getController();
		controllerMapView.setZoom(18);

		// Die TileSource beschreibt die Eigenschaften der Kacheln die wir anzeigen
		
		XYTileSource  treasureMapTileSource = new XYTileSource("mbtiles", ResourceProxy.string.offline_mode, 1, 20, 256, ".png", new String[]{"http://example.org/"});

		File file = new File(Environment.getExternalStorageDirectory() /* entspricht sdcard */, "hsr.mbtiles");

		/*
		 * Das verwenden von mbtiles ist leider ein wenig aufwändig, wir müssen
		 * unsere XYTileSource in verschiedene Klassen 'verpacken' um sie dann
		 * als TilesOverlay über der Grundkarte anzuzeigen.
		 */
		MapTileModuleProviderBase treasureMapModuleProvider = new MapTileFileArchiveProvider(new SimpleRegisterReceiver(this), treasureMapTileSource, new IArchiveFile[] { MBTilesFileArchive.getDatabaseFileArchive(file) });

		MapTileProviderBase treasureMapProvider = new MapTileProviderArray(treasureMapTileSource, null, new MapTileModuleProviderBase[] { treasureMapModuleProvider });
		TilesOverlay treasureMapTilesOverlay = new TilesOverlay(treasureMapProvider, getBaseContext());
		treasureMapTilesOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);

		// Jetzt können wir den Overlay zu unserer Karte hinzufügen:
		mapViewMap.getOverlays().add(treasureMapTilesOverlay);

		overlayItems = new ArrayList<OverlayItem>();
		overlayItems.add(new OverlayItem("HSR", "Hochschule für Technik Rapperswil", positionHsr));

		locationOverlay = new ItemizedIconOverlay<OverlayItem>(overlayItems, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
			@Override
			public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
				Toast.makeText(MainActivity.this, item.getTitle(), Toast.LENGTH_SHORT).show();				
				return true;
			}

			@Override
			public boolean onItemLongPress(final int index, final OverlayItem item) {
//				Toast.makeText(MainActivity.this, item.getTitle(), Toast.LENGTH_SHORT).show();
				mapViewMap.getOverlays().clear();
				return false;
			}
		}, resourceProxy);
		mapViewMap.getOverlays().add(locationOverlay);

		centerPositionHsr(null);
		mapViewMap.invalidate();
	}

	@Override
	protected void onResume() {
		super.onResume();

		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 13337, 1, this);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 13337, 1, this);

		// TODO: check getting location ---------------------------------------
		// Location lastLocation =
		// locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		// if (lastLocation != null) {
		// position = new GeoPoint(lastLocation.getLatitude(),
		// lastLocation.getLongitude());
		// controllerMapView.setCenter(position);
		// }
		// ---------------------------------------------------------------------


		// set position to hsr geo point
		centerPositionHsr(null);
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
//		 controllerMapView.setCenter(currentPosition);
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

	// aktuelle Position zentrieren
	public void centerPositionCurrent(View view) {
		controllerMapView.setCenter(positionCurrent);
	}
	
	// HSR zentrieren
	public void centerPositionHsr(View view) {
		controllerMapView.setCenter(positionHsr);
	}

	// ein Marker in der aktuellen Mitte setzen (Fadenkreuz)
	public void setMarkerCenter(View view) {
		// Put overlay icon a little way from map centre
		overlayItems.add(new OverlayItem("Here", "current position", positionCurrent));
	}
	
	public class MarkerItemizedOverlay extends ItemizedOverlay<OverlayItem> {

	    public MarkerItemizedOverlay(Drawable pDefaultMarker,
				ResourceProxy pResourceProxy) {
			super(pDefaultMarker, pResourceProxy);
			// TODO Auto-generated constructor stub
		}

		private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	    MapView mView;

	    // Rest of the code ...

	    @Override
	    protected boolean onTap(int index) {
	        List<Overlay> mOverlays = mView.getOverlays();
	        return mOverlays.remove(this); 
	    }

		@Override
		public boolean onSnapToItem(int x, int y, Point snapPoint,
				IMapView mapView) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		protected OverlayItem createItem(int i) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int size() {
			// TODO Auto-generated method stub
			return 0;
		}
	}
}
