// http://madushankaperera.wordpress.com/2014/03/20/google-maps-android-api-v2-current-location-example-with-gps/
package ch.fenceposts.appquest.schatzkarte;

import java.io.File;
import java.util.List;

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
import org.osmdroid.views.overlay.TilesOverlay;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
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

public class MainActivity extends Activity implements LocationListener, OnClickListener {

	private static final double LATITUDE_HSR = 47.223319;
	private static final double LONGITUDE_HSR = 8.817275;
	private static final String DEBUG_TAG = "mydebug";
	private GeoPoint positionHsr = new GeoPoint(LATITUDE_HSR, LONGITUDE_HSR);
	private GeoPoint currentPosition;
    private LocationManager locationManager;
	private IGeoPoint mapCenter;
    private IMapController controllerMapView;
	private MapView mapViewMap;
	private TextView textViewLatitudeValue;
	private TextView textViewLongitudeValue;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
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

		// Die TileSource beschreibt die Eigenschaften der Kacheln die wir
		// anzeigen
		XYTileSource treasureMapTileSource = new XYTileSource("mbtiles", ResourceProxy.string.offline_mode, 1, 20, 256, ".png", "http://example.org/");

		File file = new File(Environment.getExternalStorageDirectory() /* entspricht sdcard */, "hsr.mbtiles");

		/*
		 * Das verwenden von mbtiles ist leider ein wenig aufwändig, wir müssen
		 * unsere XYTileSource in verschiedene Klassen 'verpacken' um sie dann
		 * als TilesOverlay über der Grundkarte anzuzeigen.
		 */
		MapTileModuleProviderBase treasureMapModuleProvider = new MapTileFileArchiveProvider(new SimpleRegisterReceiver(this), treasureMapTileSource, 
				new IArchiveFile[] {
					MBTilesFileArchive.getDatabaseFileArchive(file)
				});

		MapTileProviderBase treasureMapProvider = new MapTileProviderArray(treasureMapTileSource, null, new MapTileModuleProviderBase[] { treasureMapModuleProvider });

		TilesOverlay treasureMapTilesOverlay = new TilesOverlay(treasureMapProvider, getBaseContext());
		treasureMapTilesOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);

		// Jetzt können wir den Overlay zu unserer Karte hinzufügen:
		mapViewMap.getOverlays().add(treasureMapTilesOverlay);
		mapViewMap.invalidate();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 13337, 0, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 13337, 10, this);
		
		// TODO: check getting location ---------------------------------------
//		Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//		if (lastLocation != null) {
//			position = new GeoPoint(lastLocation.getLatitude(), lastLocation.getLongitude());
//			controllerMapView.setCenter(position);
//		}
		// ---------------------------------------------------------------------
		
        // set the current position to position of hsr
        currentPosition = positionHsr;
        
		// set position to hsr geo point
        centerCurrentPosition(null);
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
		
		currentPosition = new GeoPoint(location.getLatitude(), location.getLongitude());

		Log.d(DEBUG_TAG, "Longitude: " + Double.toString(currentPosition.getLongitude()) + " / Latitude: " + Double.toString(currentPosition.getLatitude()));

		writePosition(currentPosition);
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
	
	public void writePosition(GeoPoint position) {
		textViewLatitudeValue.setText(Double.toString(position.getLatitude()));
		textViewLongitudeValue.setText(Double.toString(position.getLongitude()));
	}
	
	// aktuelle Position zentrieren
	public void centerCurrentPosition(View view) {
		controllerMapView.setCenter(currentPosition);
	}
	
	// ein Marker in der aktuellen Mitte setzen (Fadenkreuz)
	public void setMarkerCenter(View view) { 
		mapCenter = mapViewMap.getMapCenter();
		List<Overlay> overlays = mapViewMap.getOverlays();
		overlays.clear();
		overlays.add(new CustomOverlay(this));
	}
	
	private class CustomOverlay extends Overlay {
		
		public CustomOverlay(Context context) {
			super(context);
			
			// TODO Auto-generated constructor stub
		}
	
		@Override
		protected void draw(Canvas canvas, MapView mapView, boolean shadow) {
	
			if (!shadow) {
				Point point = new Point();
				mapView.getProjection().toMapPixels(mapCenter, point);
	
				Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.marker_default);
	
				int x = point.x - bmp.getWidth() / 2;
				int y = point.y - bmp.getHeight();
	
				canvas.drawBitmap(bmp, x, y, null);
			}
	
		}
	}
}
