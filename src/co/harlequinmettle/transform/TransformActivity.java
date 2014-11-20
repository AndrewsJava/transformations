package co.harlequinmettle.transform;

import java.lang.reflect.Field;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class TransformActivity extends Activity implements OnTouchListener {
	Bitmap ggrid;
	InnerView mIV;
	Matrix wingAnim = new Matrix();
	float lastX, lastY;
	Paint textPaint = new Paint();
	Paint textPaint2 = new Paint();
	Paint linePaint = new Paint();
	Paint buttonPaint = new Paint();
	Paint highlightPaint = new Paint();
	Paint centerRotate = new Paint();
	Paint originalLocation = new Paint();
	int chooseAction = 2, orderOfOp = 0;
	private DisplayMetrics metrics = new DisplayMetrics();
	float sw, sh;
	float bmw, bmh;
	float textSize = 20;
	float tX = 200, tY = 150, scaleFactorX = 1, scaleFactorY = 1, skewFactorX = 0, skewFactorY = 0, rotation = 0;
	float scale;
	float minButtonWidth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		buttonPaint.setARGB(60, 200, 90, 90);
		highlightPaint.setARGB(100, 120, 40, 90);
		// always set metrics for screen width and height
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		sw = metrics.widthPixels;
		sh = metrics.heightPixels;
		float minD = sw < sh ? sw : sh;
		if (minD < 760)
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		tX = sw / 2;
		tY = sh / 4;
		scale = getResources().getDisplayMetrics().density;
		textSize *= scale;
		textPaint.setTextSize(textSize - 4 * scale);
		linePaint.setTextSize(textSize - 8 * scale);
		textPaint.setARGB(155, 255, 0, 100);
		centerRotate.setARGB(100, 200, 200, 200);
		originalLocation.setARGB(100, 130, 130, 130);
		mIV = new InnerView(this);
		textPaint2.setTextSize(textSize);
		minButtonWidth = textPaint2.measureText("TRANSLATE");
		ggrid = getBitmapFromImageName("transimage");

		bmw = ggrid.getWidth();
		bmh = ggrid.getHeight();
		ggrid = getResizedBitmap(ggrid, (int) (bmw * scale), (int) (bmh * scale));
		setContentView(mIV);
		// register canvas to receive events as defined in this
		mIV.setOnTouchListener(this);

		// ///////////////////////////////////////

	}

	public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
		int width = bm.getWidth();
		int height = bm.getHeight();
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// CREATE A MATRIX FOR THE MANIPULATION
		Matrix matrix = new Matrix();
		// RESIZE THE BIT MAP
		matrix.postScale(scaleWidth, scaleHeight);

		// "RECREATE" THE NEW BITMAP
		Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
		return resizedBitmap;
	}

	public float sigFig(float orig, int sigfigs) {
		float easytoread = orig;
		easytoread *= Math.pow(10, sigfigs);
		int round = (int) (easytoread);
		return (float) ((float) round * Math.pow(10, -sigfigs));

	}

	public class InnerView extends View {
		Rect[] choiceSel = new Rect[4];
		Rect[] orderChoice = new Rect[6];
		RectF center = new RectF((int) sw / 2 - 10, (int) sh / 2 - 10, (int) sw / 2 + 10, (int) sh / 2 + 10);

		int topMargin = (int) (70 * scale);
		int leftMargin = (int) (130 * scale);
		int[][] description = { { 2, 0, 1 }, { 2, 1, 0 }, { 0, 1, 2 }, { 0, 2, 1 }, { 1, 2, 0 }, { 1, 0, 2 } };
		final String[] transforms = { "SKEW", "TRANSLATE", "SCALE", "ROTATE" };
		final String[] transformsAbbrev = { "Skw", "Trn", "Scl" };
		final String[] orders = { "SclSkwTrn", "SclTrnSkw", "SkwTrnScl", "SkwSclTrn", "TrnSclSkw", "TrnSkwScl" };

		int bw;
		int cw;

		public InnerView(Context context) {
			super(context);
			bw = (int) (sw - 4 * (choiceSel.length)) / choiceSel.length;
			for (int i = 0; i < choiceSel.length; i++) {
				choiceSel[i] = new Rect(7 + 4 * i + bw * i, 7, 2 + 4 * i + bw * i + bw, (int) textSize + 7);

			}

			cw = (int) ((sh - topMargin - 2 * orderChoice.length) / orderChoice.length);
			for (int i = 0; i < orderChoice.length; i++) {
				orderChoice[i] = new Rect(7, 2 * i + topMargin + i * cw, leftMargin, 2 * i + topMargin + i * cw + cw);

			}
			// TODO Auto-generated constructor stub
		}

		public void onDraw(Canvas c) {
			c.drawARGB(255, 200, 160, 250);
			int count = 0;
			for (Rect r : choiceSel) {
				c.drawRect(r, buttonPaint);
				if (chooseAction - 1 == count)
					c.drawRect(r, highlightPaint);
				count++;
			}
			count = 0;
			for (Rect r : orderChoice) {
				c.drawRect(r, buttonPaint);
				if (orderOfOp == count)
					c.drawRect(r, highlightPaint);
				count++;
			}
			for (int i = leftMargin; i < sw; i += 25) {
				c.drawLine(i, topMargin, i, sh, linePaint);
				if (i % 100 == 0)
					c.drawLine(i + 1, topMargin, i + 1, sh, linePaint);
			}

			for (int i = topMargin; i < sh; i += 25) {
				c.drawLine(leftMargin, i, sw, i, linePaint);
				if (i % 100 == 0)
					c.drawLine(leftMargin, i + 1, sw, i + 1, linePaint);
			}

			c.drawRect(tX, tY, tX + bmw * scale, tY + bmh * scale, originalLocation);
			// c.drawLine(0, 0, tX, tY, linePaint);

			wingAnim.reset();
			wingAnim.postTranslate(-ggrid.getWidth() / 2, -ggrid.getHeight() / 2);
			wingAnim.postRotate(rotation);
			wingAnim.postTranslate(ggrid.getWidth() / 2, ggrid.getHeight() / 2);

			switch (orderOfOp) {
			case 0:
				wingAnim.postScale(scaleFactorX, scaleFactorY);
				wingAnim.postSkew(skewFactorX, skewFactorY);
				wingAnim.postTranslate(tX, tY);
				break;

			case 1:
				wingAnim.postScale(scaleFactorX, scaleFactorY);
				wingAnim.postTranslate(tX, tY);
				wingAnim.postSkew(skewFactorX, skewFactorY);
				break;

			case 2:
				wingAnim.postSkew(skewFactorX, skewFactorY);
				wingAnim.postTranslate(tX, tY);
				wingAnim.postScale(scaleFactorX, scaleFactorY);
				break;

			case 3:
				wingAnim.postSkew(skewFactorX, skewFactorY);
				wingAnim.postScale(1, scaleFactorY);
				wingAnim.postTranslate(tX, tY);

				break;

			case 4:
				wingAnim.postTranslate(tX, tY);
				wingAnim.postScale(scaleFactorX, scaleFactorY);
				wingAnim.postSkew(skewFactorX, skewFactorY);
				break;

			case 5:
				wingAnim.postTranslate(tX, tY);
				wingAnim.postSkew(skewFactorX, skewFactorY);
				wingAnim.postScale(scaleFactorX, scaleFactorY);
				break;

			default:
				break;

			}

			c.drawBitmap(ggrid, wingAnim, null);

			if (chooseAction == 4) {
				c.drawArc(center, 0, 360, true, centerRotate);
				for (int i = 1; i < 3; i++) {
					RectF decor = new RectF(sw / 2 - 100 * i, sh / 2 - 100 * i, sw / 2 + 100 * i, sh / 2 + 100 * i);

					c.drawArc(decor, (float) (0), (float) (360), false, centerRotate);
				}
			}
			for (int i = 0; i < choiceSel.length; i++) {
				c.drawText(transforms[i], 7 + 4 * i + bw * i, 3 + textSize, linePaint);

			}

			if (sw > sh)
				for (int i = 0; i < orderChoice.length; i++) {
					c.drawText(orders[i], 9, i * cw + topMargin + textSize + 10, textPaint2);

				}
			else {
				int i = 0;
				for (int[] sequence : description) {
					int j = 0;
					for (int ordr : sequence) {

						c.drawText(transformsAbbrev[description[i][j]], 9, i * cw + topMargin + (1 + j) * (2 + textSize) + 10, textPaint2);
						j++;
					}
					i++;
				}
			}

			if (true) {

				c.drawText("(" + sigFig(skewFactorX, 2) + "," + sigFig(skewFactorY, 2) + ")", 7 + (4 + bw) * 0, 2 * textSize, textPaint);
				c.drawText("(" + (int) (tX) + "," + (int) (tY) + ")", 7 + (4 + bw) * 1, 2 * textSize, textPaint);
				c.drawText("(" + sigFig(scaleFactorX, 2) + "," + sigFig(scaleFactorY, 2) + ")", 7 + (4 + bw) * 2, 2 * textSize, textPaint);
				c.drawText("(" + sigFig(rotation, 0) + ")", 7 + (4 + bw) * 3, 2 * textSize, textPaint);

			}
			for (int i = 0; i < 3; i++) {
				c.drawText(transforms[description[orderOfOp][i]], sw / 2, sh - (2 - i) * textSize - 65, textPaint2);
			}
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
			lastX = event.getX();
			lastY = event.getY();
			break;
		case MotionEvent.ACTION_UP:

			/*
			 * File file = new File(Environment.getExternalStorageDirectory() +
			 * "/screenshottest.png"); mIV.setDrawingCacheEnabled(true);
			 * 
			 * // this is the important code :) // Without it the view will have
			 * a dimension of 0,0 and the bitmap // will be null mIV.measure(
			 * MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
			 * MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
			 * mIV.layout(0, 0, mIV.getMeasuredWidth(),
			 * mIV.getMeasuredHeight());
			 * 
			 * mIV.buildDrawingCache(true);
			 * System.out.println("WHY IS DRAWINGCACHE NULL?   " +
			 * mIV.isDrawingCacheEnabled()); // Bitmap b =
			 * Bitmap.createBitmap(mIV.getDrawingCache());
			 * mIV.setDrawingCacheEnabled(false); // clear drawing cache //
			 * ///////////////////////////////
			 * 
			 * try { file.createNewFile(); FileOutputStream ostream = new
			 * FileOutputStream(file); ggrid.compress(CompressFormat.PNG, 100,
			 * ostream); ostream.close(); } catch (Exception e) {
			 * e.printStackTrace(); }
			 */
			if (event.getY() < 2 * textSize) {
				chooseAction = (int) (1 + event.getX() / mIV.bw);
			}
			if (event.getX() < mIV.leftMargin && event.getY() > mIV.topMargin) {
				orderOfOp = (int) ((event.getY() - mIV.topMargin) / mIV.cw);
			}
			break;
		case MotionEvent.ACTION_MOVE:
			float x = event.getX();
			float y = event.getY();
			float changeXBy = -(lastX - x) / 100;
			float changeYBy = -(lastY - y) / 100;
			if (x > mIV.leftMargin && y > mIV.topMargin)
				switch (chooseAction) {
				case 1:

					skewFactorX += changeXBy;
					skewFactorY += changeYBy;
					// wingAnim.postSkew(skewFactorX, skewFactorY);
					break;
				case 2:

					tX += 60 * changeXBy;
					tY += 60 * changeYBy;
					// wingAnim.postTranslate(20*changeXBy, 20*changeYBy);
					break;
				case 3:

					scaleFactorX += changeXBy;
					scaleFactorY += changeYBy;
					// wingAnim.postScale(scaleFactorX, scaleFactorY);
					break;
				case 4:
					if (y > sh / 2)
						rotation -= 15 * changeXBy;
					else
						rotation += 15 * changeXBy;

					if (x > sw / 2)
						rotation += 15 * changeYBy;
					else
						rotation -= 15 * changeYBy;

					// wingAnim.postRotate(rotation/15);
					break;

				}
			lastX = x;
			lastY = y;
			break;
		}
		mIV.invalidate();
		return true;
	}

	// takes in string and calculates the R file int from res
	private Bitmap getBitmapFromImageName(String resName) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inScaled = false;

		try {
			@SuppressWarnings("rawtypes")
			Class res = R.drawable.class;
			Field field = res.getField(resName);
			int drawableId = field.getInt(null);

			return BitmapFactory.decodeResource(getResources(), drawableId, options);

		} catch (Exception e) {
			e.printStackTrace();
		}
		// should return a default image r.id
		return null;// possible to cause a problem
	}

}
