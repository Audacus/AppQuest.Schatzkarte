package ch.fenceposts.appquest.schatzkarte.overlay;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;

class CustomOverlay extends Overlay {
	

	public CustomOverlay(Context context) {
		super(context);
		
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void draw(Canvas canvas, MapView mapView, boolean shadow) {

//		if (!shadow) {
//			Point point = new Point();
//			mapView.getProjection().toMapPixels(mapCenter, point);
//
//			Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.marker_default);
//
//			int x = point.x - bmp.getWidth() / 2;
//			int y = point.y - bmp.getHeight();
//
//			canvas.drawBitmap(bmp, x, y, null);
//		}

	}

}