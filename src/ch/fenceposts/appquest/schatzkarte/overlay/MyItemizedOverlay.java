package ch.fenceposts.appquest.schatzkarte.overlay;

import java.util.ArrayList;

import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapView;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;

public abstract class MyItemizedOverlay extends ItemizedOverlay {

	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private Context mContext;

	public MyItemizedOverlay(Drawable pDefaultMarker,
			ResourceProxy pResourceProxy) {
		super(pDefaultMarker, pResourceProxy);
		// TODO Auto-generated constructor stub
	}

	public MyItemizedOverlay(Drawable pDefaultMarker,
			ResourceProxy pResourceProxy, Context pContext) {
		super(pDefaultMarker, pResourceProxy);

		mContext = pContext;
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
}
