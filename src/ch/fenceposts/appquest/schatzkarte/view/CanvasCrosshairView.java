package ch.fenceposts.appquest.schatzkarte.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

//View zum zeichnen der grünen Linie für über dem CameraPreview (SurfaceView)
public class CanvasCrosshairView extends View {
	public CanvasCrosshairView(Context context, AttributeSet attrs) {
		super(context, attrs);		
	}
	
	@Override
	// zeichne grüne Linie in der Mitte der View
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Paint paint = new Paint();
		int middleHeight = canvas.getHeight() / 2;
		int middleWidth = canvas.getWidth() / 2;
		
		paint.setColor(Color.BLACK);
		paint.setStrokeWidth(2.0f);
		canvas.drawLine(0, middleHeight, canvas.getWidth(), middleHeight, paint); // left to right
		canvas.drawLine(middleWidth, 0, middleWidth, canvas.getHeight(), paint); // bottom to top
	}
}
