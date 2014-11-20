package ch.fenceposts.appquest.schatzkarte;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import ch.fenceposts.appquest.schatzkarte.database.LocationContract.LocationEntry;
import ch.fenceposts.appquest.schatzkarte.database.LocationDbHelper;

public class MyItemizedOverlay extends ItemizedOverlay<OverlayItem> {

	private static final String DEBUG_TAG = "mydebug";
	public List<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private Context mContext;
	private LocationDbHelper mLocationDbHelper;
	private SQLiteDatabase mDbLocation;

	public MyItemizedOverlay(Drawable pDefaultMarker, ResourceProxy pResourceProxy, Context pContext) {
		super(pDefaultMarker, pResourceProxy);
		mContext = pContext;

		mLocationDbHelper = new LocationDbHelper(mContext);
		// Gets the data repository in write mode
		mDbLocation = mLocationDbHelper.getWritableDatabase();
	}

	@Override
	protected OverlayItem createItem(int i) {
		return mOverlays.get(i);
	}

	@Override
	public int size() {
		return mOverlays.size();
	}

	@Override
	protected boolean onTap(int index) {
		OverlayItem item = mOverlays.get(index);
		AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
		dialog.setTitle(item.getTitle());
		dialog.setMessage(item.getSnippet());
		dialog.show();
		return true;
	}

	public void addOverlay(OverlayItem overlay) {
		mOverlays.add(overlay);
		populate();
	}

	@Override
	public boolean onSnapToItem(int x, int y, Point snapPoint, IMapView mapView) {
		// TODO Auto-generated method stub
		return false;
	}

	public void addItem(GeoPoint pGeoPoint) {
		long locationId = addDbLocation(pGeoPoint);
		addOverlay(new OverlayItem(String.valueOf(locationId), "long: " + Double.toString(pGeoPoint.getLongitude()) + "\nlati: " + Double.toString(pGeoPoint.getLatitude()),
				pGeoPoint));
		Log.d(DEBUG_TAG, "mOverlays.size = " + String.valueOf(mOverlays.size()));
	}

	public void removeItem(MotionEvent e, MapView mapView) {

		List<OverlayItem> tempMOverlays = new ArrayList<OverlayItem>(this.mOverlays);

		for (OverlayItem overlayItem : tempMOverlays) {

			Projection projection = mapView.getProjection();
			GeoPoint geoPointItem = overlayItem.getPoint();

			Rect screenRect = mapView.getScreenRect(new Rect());
			Point pointItem = projection.toPixels(geoPointItem, null);

			float pixelDistanceX = pointItem.x - screenRect.left;
			float pixelDistanceY = pointItem.y - screenRect.top;

			double distance = Math.sqrt((e.getX() - pixelDistanceX) * (e.getX() - pixelDistanceX) + (e.getY() - pixelDistanceY) * (e.getY() - pixelDistanceY));

			if (distance < (Math.min(MainActivity.displayMetrics.widthPixels, MainActivity.displayMetrics.heightPixels) / 9)) {
				removeDbLocation(Long.parseLong(overlayItem.getTitle()));
				this.mOverlays.remove(overlayItem);
			}
		}
		populate();
		mapView.invalidate();
		Log.d(DEBUG_TAG, "mOverlays.size = " + String.valueOf(mOverlays.size()));
	}

	private long addDbLocation(GeoPoint geoPoint) {
		// Create a new map of values, where column names are the keys
		ContentValues valuesDb = new ContentValues();
		valuesDb.put(LocationEntry.COLUMN_NAME_LATITUDE, geoPoint.getLatitudeE6());
		valuesDb.put(LocationEntry.COLUMN_NAME_LONGITUDE, geoPoint.getLongitudeE6());

		// Insert the new row, returning the primary key value of the new row
		long newRowId;
		newRowId = mDbLocation.insert(LocationEntry.TABLE_NAME, LocationEntry.COLUMN_NAME_NULLABLE, valuesDb);
		
		Log.d(DEBUG_TAG, "new row inserted to the location db. newRowId => " + String.valueOf(newRowId));
		Log.d(DEBUG_TAG, "location db row count: " + String.valueOf(mLocationDbHelper.getDbLocationRowCount()));

		return newRowId;
	}

	private void removeDbLocation(long locationId) {
		// Define 'where' part of query.
		String selection = LocationEntry._ID + " LIKE ?";

		// Specify arguments in placeholder order.
		String[] selectionArgs = { String.valueOf(locationId) };

		// Issue SQL statement.
		mDbLocation.delete(LocationEntry.TABLE_NAME, selection, selectionArgs);
		
		Log.d(DEBUG_TAG, "row deleted from the location db. locationId => " + String.valueOf(locationId));
		Log.d(DEBUG_TAG, "location db row count: " + String.valueOf(mLocationDbHelper.getDbLocationRowCount()));
	}
}
