package lib.widget1.calendar.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lib.widget1.calendar.bizs.calendars.DPCManager;
import lib.widget1.calendar.bizs.decors.DPDecor;
import lib.widget1.calendar.bizs.themes.DPTManager;
import lib.widget1.calendar.cons.DPMode;
import lib.widget1.calendar.entities.DPInfo;


public class WeekView extends View {

	private final Region[][] monthRegionsFour = new Region[4][7];
	private final Region[][] monthRegionsFive = new Region[5][7];
	private final Region[][] monthRegionsSix = new Region[6][7];

	private final DPInfo[][] infoFour = new DPInfo[4][7];
	private final DPInfo[][] infoFive = new DPInfo[5][7];
	private final DPInfo[][] infoSix = new DPInfo[6][7];

	private final Map<String, List<Region>> regionSelected = new HashMap<>();

	private DPCManager mCManager = DPCManager.getInstance();
	private DPTManager mTManager = DPTManager.getInstance();

	protected Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG
			| Paint.DITHER_FLAG | Paint.LINEAR_TEXT_FLAG);
	protected Paint todayPaint = new Paint(Paint.ANTI_ALIAS_FLAG
			| Paint.DITHER_FLAG | Paint.LINEAR_TEXT_FLAG);
	private Scroller mScroller;
	private DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();
	private AccelerateInterpolator accelerateInterpolator = new AccelerateInterpolator();
//	private OnDateChangeListener onDateChangeListener;
	private OnWeekViewChangeListener onWeekViewChangeListener;
	private MonthView.OnDatePickedListener onDatePickedListener;
	private OnWeekDateClick onWeekClick;
	private ScaleAnimationListener scaleAnimationListener;

	private DPMode mDPMode = DPMode.MULTIPLE;
	private SlideMode mSlideMode;
	private DPDecor mDPDecor;

	private int circleRadius;
	private int indexYear, indexMonth;
	private int centerYear, centerMonth;
	private int leftYear, leftMonth;
	private int rightYear, rightMonth;
	private int width, height;
	private int sizeDecor, sizeDecor2x, sizeDecor3x;
	private int lastPointX, lastPointY;
	private int lastMoveX, lastMoveY;
	private int criticalWidth, criticalHeight;
	private int animZoomOut1, animZoomIn1, animZoomOut2;

	private float sizeTextGregorian, sizeTextFestival;
	private float offsetYFestival1, offsetYFestival2;
	private int num = 5;
	private int count = 5;

	private boolean isNewEvent, isFestivalDisplay = true,
			isHolidayDisplay = true, isTodayDisplay = true,
			isDeferredDisplay = true;

	private Map<String, BGCircle> cirApr = new HashMap<>();
	private Map<String, BGCircle> cirDpr = new HashMap<>();

	private List<String> dateSelected = new ArrayList<>();

	public WeekView(Context context) {
		this(context, null);
	}

	public WeekView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public WeekView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			scaleAnimationListener = new ScaleAnimationListener();
		}
		mScroller = new Scroller(context);
		mPaint.setTextAlign(Paint.Align.CENTER);
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		return super.onSaveInstanceState();
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		super.onRestoreInstanceState(state);
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			invalidate();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mScroller.forceFinished(true);
			mSlideMode = null;
			isNewEvent = true;
			lastPointX = (int) event.getX();
			lastPointY = (int) event.getY();
			return true;
		case MotionEvent.ACTION_MOVE:
			if (isNewEvent) {
				if (Math.abs(lastPointX - event.getX()) > 100) {
					mSlideMode = SlideMode.HOR;
					isNewEvent = false;
				} else if (Math.abs(lastPointY - event.getY()) > 50) {
					// mSlideMode = SlideMode.VER;
					// isNewEvent = false;
				}
			}
			if (mSlideMode == SlideMode.HOR) {
				int totalMoveX = (int) (lastPointX - event.getX()) + lastMoveX;
				smoothScrollTo(totalMoveX, indexYear * height);
			} else if (mSlideMode == SlideMode.VER) {
				// int totalMoveY = (int) (lastPointY - event.getY()) +
				// lastMoveY;
				// smoothScrollTo(width * indexMonth, totalMoveY);
			}
			break;
		case MotionEvent.ACTION_UP:
			if (mSlideMode == SlideMode.VER) {
				// if (Math.abs(lastPointY - event.getY()) > 25) {
				// if (lastPointY < event.getY()) {
				// if (Math.abs(lastPointY - event.getY()) >= criticalHeight) {
				// indexYear--;
				// centerYear = centerYear - 1;
				// }
				// } else if (lastPointY > event.getY()) {
				// if (Math.abs(lastPointY - event.getY()) >= criticalHeight) {
				// indexYear++;
				// centerYear = centerYear + 1;
				// }
				// }
				// buildRegion();
				// computeDate();
				// smoothScrollTo(width * indexMonth, height * indexYear);
				// lastMoveY = height * indexYear;
				// } else {
				// defineRegion((int) event.getX(), (int) event.getY());
				// }
			} else if (mSlideMode == SlideMode.HOR) {
				if (Math.abs(lastPointX - event.getX()) > 25) {
					if (lastPointX > event.getX()
							&& Math.abs(lastPointX - event.getX()) >= criticalWidth) {
						indexMonth++;
						centerMonth = (centerMonth + 1) % 13;
						if (centerMonth == 0) {
							centerMonth = 1;
							centerYear++;
						}
						if(null != onWeekViewChangeListener){
							onWeekViewChangeListener.onWeekViewChange(true);
						}
					} else if (lastPointX < event.getX()
							&& Math.abs(lastPointX - event.getX()) >= criticalWidth) {
						indexMonth--;
						centerMonth = (centerMonth - 1) % 12;
						if (centerMonth == 0) {
							centerMonth = 12;
							centerYear--;
						}
						if(null != onWeekViewChangeListener){
							onWeekViewChangeListener.onWeekViewChange(false);
						}
					}
					buildRegion();
					computeDate();
					smoothScrollTo(width * indexMonth, indexYear * height);
					lastMoveX = width * indexMonth;
				} else {
					defineRegion((int) event.getX(), (int) event.getY());
				}
			} else {
				defineRegion((int) event.getX(), (int) event.getY());
			}
			break;
		}
		return super.onTouchEvent(event);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int measureWidth = MeasureSpec.getSize(widthMeasureSpec);
		setMeasuredDimension(measureWidth, (int) (measureWidth * 6F / 7F) / count);
	}
	
	  public void moveForwad(){
	        indexMonth++;
	        centerMonth = (centerMonth + 1) % 13;
	        if (centerMonth == 0) {
	            centerMonth = 1;
	            centerYear++;
	        }
	        buildRegion();
	        computeDate();
	        smoothScrollTo(width * indexMonth, indexYear * height);
	        lastMoveX = width * indexMonth;
		    requestLayout();
	    }
	    
	    //滑动back
	    public void moveBack(){
	        indexMonth--;
	        centerMonth = (centerMonth - 1) % 12;
	        if (centerMonth == 0) {
	            centerMonth = 12;
	            centerYear--;
	        }
	        buildRegion();
	        computeDate();
	        smoothScrollTo(width * indexMonth, indexYear * height);
	        lastMoveX = width * indexMonth;
			requestLayout();
	    }

	@Override
	protected void onSizeChanged(int w, int h, int oldW, int oldH) {
		width = w;
		height = h;

		criticalWidth = (int) (1F / 5F * width);
		criticalHeight = (int) (1F / 5F * height);

		int cellW = (int) (w / 7F);
		int cellH4 = (int) (h / 4F);
		int cellH5 = (int) (h / 5F);
		int cellH6 = (int) (h / 6F);

		circleRadius = cellW;

		animZoomOut1 = (int) (cellW * 1.2F);
		animZoomIn1 = (int) (cellW * 0.8F);
		animZoomOut2 = (int) (cellW * 1.1F);

		sizeDecor = (int) (cellW / 3F);
		sizeDecor2x = sizeDecor * 2;
		sizeDecor3x = sizeDecor * 3;

		sizeTextGregorian = width / 23F;
		mPaint.setTextSize(sizeTextGregorian);

		float heightGregorian = mPaint.getFontMetrics().bottom
				- mPaint.getFontMetrics().top;
		sizeTextFestival = width / 40F;
		mPaint.setTextSize(sizeTextFestival);

		float heightFestival = mPaint.getFontMetrics().bottom
				- mPaint.getFontMetrics().top;
		offsetYFestival1 = (((Math.abs(mPaint.ascent() + mPaint.descent())) / 2F)
				+ heightFestival / 2F + heightGregorian / 2F) / 2F;
		offsetYFestival2 = offsetYFestival1 * 2F;

		for (int i = 0; i < monthRegionsFour.length; i++) {
			for (int j = 0; j < monthRegionsFour[i].length; j++) {
				Region region = new Region();
				region.set(j * cellW, i * cellH4, cellW + (j * cellW),
						cellW + (i * cellH4));
				monthRegionsFour[i][j] = region;
			}
		}
		for (int i = 0; i < monthRegionsFive.length; i++) {
			for (int j = 0; j < monthRegionsFive[i].length; j++) {
				Region region = new Region();
				region.set(j * cellW, i * cellH5, cellW + (j * cellW),
						cellW + (i * cellH5));
				monthRegionsFive[i][j] = region;
			}
		}
		for (int i = 0; i < monthRegionsSix.length; i++) {
			for (int j = 0; j < monthRegionsSix[i].length; j++) {
				Region region = new Region();
				region.set(j * cellW, i * cellH6, cellW + (j * cellW),
						cellW + (i * cellH6));
				monthRegionsSix[i][j] = region;
			}
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawColor(mTManager.colorBG());

		draw(canvas, width * (indexMonth - 1), height * indexYear, leftYear,
				leftMonth);
		draw(canvas, width * indexMonth, indexYear * height, centerYear,
				centerMonth);
		draw(canvas, width * (indexMonth + 1), height * indexYear, rightYear,
				rightMonth);

		drawBGCircle(canvas);
	}

	private void drawBGCircle(Canvas canvas) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			for (String s : cirDpr.keySet()) {
				BGCircle circle = cirDpr.get(s);
				drawBGCircle(canvas, circle);
			}
		}
		for (String s : cirApr.keySet()) {
			BGCircle circle = cirApr.get(s);
			drawBGCircle(canvas, circle);
		}
	}

	private void drawBGCircle(Canvas canvas, BGCircle circle) {
		canvas.save();
		canvas.translate(circle.getX() - circle.getRadius() / 2, circle.getY()
				- circle.getRadius() / 2);
		circle.getShape().getShape()
				.resize(circle.getRadius(), circle.getRadius());
		circle.getShape().draw(canvas);
		canvas.restore();
	}

	private void draw(Canvas canvas, int x, int y, int year, int month) {
		canvas.save();
		canvas.translate(x, y);
		DPInfo[][] info = mCManager.obtainDPInfo(year, month);
		DPInfo[][] result;
		Region[][] tmp;
		if (TextUtils.isEmpty(info[4][0].strG)) {
			tmp = monthRegionsFour;
			arrayClear(infoFour);
			result = arrayCopy(info, infoFour);
		} else if (TextUtils.isEmpty(info[5][0].strG)) {
			tmp = monthRegionsFive;
			arrayClear(infoFive);
			result = arrayCopy(info, infoFive);
		} else {
			tmp = monthRegionsSix;
			arrayClear(infoSix);
			result = arrayCopy(info, infoSix);
		}
		// for (int i = 0; i < result.length; i++) {
		if(num >= result.length){
			//5行与6行之间切换导致数组下标越界
			for (int j = 0; j < result[result.length-1].length; j++) {
				draw(canvas, tmp[0][j].getBounds(), info[result.length-1][j]);
			}
		}else{
			for (int j = 0; j < result[num].length; j++) {
				draw(canvas, tmp[0][j].getBounds(), info[num][j]);
			}
		}
	
		// }
		canvas.restore();
	}

	public void setLine(int num) {
		this.num = num;
	}

	public void setCount(int count){
		this.count = count;
		requestLayout();
	}

	private void draw(Canvas canvas, Rect rect, DPInfo info) {
		drawBG(canvas, rect, info);
		drawGregorian(canvas, rect, info.strG, info.isWeekend,info.isToday);
		if (isFestivalDisplay)drawFestival(canvas, rect, info.strF, info.isFestival);
		drawDecor(canvas, rect, info);
	}

	private void drawBG(Canvas canvas, Rect rect, DPInfo info) {
		if (null != mDPDecor && info.isDecorBG) {
			mDPDecor.drawDecorBG(canvas, rect, mPaint, centerYear + "-"
					+ centerMonth + "-" + info.strG);
		}
		if (info.isToday && isTodayDisplay) {
			drawBGToday(canvas, rect);
		} else {
			if (isHolidayDisplay)
				drawBGHoliday(canvas, rect, info.isHoliday);
			if (isDeferredDisplay)
				drawBGDeferred(canvas, rect, info.isDeferred);
		}
	}

	// 因为今天的样式需要自定义所以 重新换了Paint
		private void drawBGToday(Canvas canvas, Rect rect) {
			// mPaint.setColor(mTManager.colorToday());
			todayPaint.setColor(mTManager.colorToday());
			todayPaint.setStyle(Paint.Style.STROKE);
			canvas.drawCircle(rect.centerX(), rect.centerY(), circleRadius / 2F,
					todayPaint);
		}

	private void drawBGHoliday(Canvas canvas, Rect rect, boolean isHoliday) {
		mPaint.setColor(mTManager.colorHoliday());
		if (isHoliday)
			canvas.drawCircle(rect.centerX(), rect.centerY(),
					circleRadius / 2F, mPaint);
	}

	private void drawBGDeferred(Canvas canvas, Rect rect, boolean isDeferred) {
		mPaint.setColor(mTManager.colorDeferred());
		if (isDeferred)
			canvas.drawCircle(rect.centerX(), rect.centerY(),
					circleRadius / 2F, mPaint);
	}

	private void drawGregorian(Canvas canvas, Rect rect, String str,
			boolean isWeekend,boolean isToday) {
		mPaint.setTextSize(sizeTextGregorian);
		if (isWeekend) {
			mPaint.setColor(mTManager.colorWeekend());
		}else if (isToday && isTodayDisplay) {
			mPaint.setColor(mTManager.colorTodayText());
		} else {
			mPaint.setColor(mTManager.colorG());
		}
		float y = rect.centerY();
		if (!isFestivalDisplay)
			y = rect.centerY() + Math.abs(mPaint.ascent())
					- (mPaint.descent() - mPaint.ascent()) / 2F;
		canvas.drawText(str, rect.centerX(), y, mPaint);
	}

	private void drawFestival(Canvas canvas, Rect rect, String str,
			boolean isFestival) {
		mPaint.setTextSize(sizeTextFestival);
		if (isFestival) {
			mPaint.setColor(mTManager.colorF());
		} else {
			mPaint.setColor(mTManager.colorL());
		}
		if (str.contains("&")) {
			String[] s = str.split("&");
			String str1 = s[0];
			if (mPaint.measureText(str1) > rect.width()) {
				float ch = mPaint.measureText(str1, 0, 1);
				int length = (int) (rect.width() / ch);
				canvas.drawText(str1.substring(0, length), rect.centerX(),
						rect.centerY() + offsetYFestival1, mPaint);
				canvas.drawText(str1.substring(length), rect.centerX(),
						rect.centerY() + offsetYFestival2, mPaint);
			} else {
				canvas.drawText(str1, rect.centerX(), rect.centerY()
						+ offsetYFestival1, mPaint);
				String str2 = s[1];
				if (mPaint.measureText(str2) < rect.width()) {
					canvas.drawText(str2, rect.centerX(), rect.centerY()
							+ offsetYFestival2, mPaint);
				}
			}
		} else {
			if (mPaint.measureText(str) > rect.width()) {
				float ch = 0.0F;
				for (char c : str.toCharArray()) {
					float tmp = mPaint.measureText(String.valueOf(c));
					if (tmp > ch) {
						ch = tmp;
					}
				}
				int length = (int) (rect.width() / ch);
				canvas.drawText(str.substring(0, length), rect.centerX(),
						rect.centerY() + offsetYFestival1, mPaint);
				canvas.drawText(str.substring(length), rect.centerX(),
						rect.centerY() + offsetYFestival2, mPaint);
			} else {
				canvas.drawText(str, rect.centerX(), rect.centerY()
						+ offsetYFestival1, mPaint);
			}
		}
	}

	private void drawDecor(Canvas canvas, Rect rect, DPInfo info) {
		if (!TextUtils.isEmpty(info.strG)) {
			String data = centerYear + "-" + centerMonth + "-" + info.strG;
			if (null != mDPDecor && info.isDecorTL) {
				canvas.save();
				canvas.clipRect(rect.left, rect.top, rect.left + sizeDecor,
						rect.top + sizeDecor);
				mDPDecor.drawDecorTL(canvas, canvas.getClipBounds(), mPaint,
						data);
				canvas.restore();
			}
			if (null != mDPDecor && info.isDecorT) {
				canvas.save();
				canvas.clipRect(rect.left + sizeDecor, rect.top, rect.left
						+ sizeDecor2x, rect.top + sizeDecor);
				mDPDecor.drawDecorT(canvas, canvas.getClipBounds(), mPaint,
						data);
				canvas.restore();
			}
			if (null != mDPDecor && info.isDecorTR) {
				canvas.save();
				canvas.clipRect(rect.left + sizeDecor2x, rect.top, rect.left
						+ sizeDecor3x, rect.top + sizeDecor);
				mDPDecor.drawDecorTR(canvas, canvas.getClipBounds(), mPaint,
						data);
				canvas.restore();
			}
			if (null != mDPDecor && info.isDecorL) {
				canvas.save();
				canvas.clipRect(rect.left, rect.top + sizeDecor, rect.left
						+ sizeDecor, rect.top + sizeDecor2x);
				mDPDecor.drawDecorL(canvas, canvas.getClipBounds(), mPaint,
						data);
				canvas.restore();
			}
			if (null != mDPDecor && info.isDecorR) {
				canvas.save();
				canvas.clipRect(rect.left + sizeDecor2x, rect.top + sizeDecor,
						rect.left + sizeDecor3x, rect.top + sizeDecor2x);
				mDPDecor.drawDecorR(canvas, canvas.getClipBounds(), mPaint,
						data);
				canvas.restore();
			}
		}
	}

	List<String> getDateSelected() {
		return dateSelected;
	}

//	public void setOnDateChangeListener(
//			OnDateChangeListener onDateChangeListener) {
//		this.onDateChangeListener = onDateChangeListener;
//	}
	
	public void setOnWeekViewChangeListener(OnWeekViewChangeListener onWeekViewChangeListener){
		this.onWeekViewChangeListener = onWeekViewChangeListener;
	}
	
	public void setOnWeekClickListener(OnWeekDateClick onWeekClick){
		this.onWeekClick = onWeekClick;
	}

	
	public void setOnDatePickedListener(
			MonthView.OnDatePickedListener onDatePickedListener) {
		this.onDatePickedListener = onDatePickedListener;
	}

	public void setDPMode(DPMode mode) {
		this.mDPMode = mode;
	}

	public void setDPDecor(DPDecor decor) {
		this.mDPDecor = decor;
	}

	public DPMode getDPMode() {
		return mDPMode;
	}

	public void setDate(int year, int month) {
		centerYear = year;
		centerMonth = month;
		indexYear = 0;
		indexMonth = 0;
		buildRegion();
		computeDate();
		requestLayout();
		invalidate();
	}

	public void setFestivalDisplay(boolean isFestivalDisplay) {
		this.isFestivalDisplay = isFestivalDisplay;
	}

	public void setTodayDisplay(boolean isTodayDisplay) {
		this.isTodayDisplay = isTodayDisplay;
	}

	public void setHolidayDisplay(boolean isHolidayDisplay) {
		this.isHolidayDisplay = isHolidayDisplay;
	}

	public void setDeferredDisplay(boolean isDeferredDisplay) {
		this.isDeferredDisplay = isDeferredDisplay;
	}

	private void smoothScrollTo(int fx, int fy) {
		int dx = fx - mScroller.getFinalX();
		int dy = fy - mScroller.getFinalY();
		smoothScrollBy(dx, dy);
	}

	private void smoothScrollBy(int dx, int dy) {
		mScroller.startScroll(mScroller.getFinalX(), mScroller.getFinalY(), dx,
				dy, 500);
		invalidate();
	}

	private BGCircle createCircle(float x, float y) {
		OvalShape circle = new OvalShape();
		circle.resize(0, 0);
		ShapeDrawable drawable = new ShapeDrawable(circle);
		BGCircle circle1 = new BGCircle(drawable);
		circle1.setX(x);
		circle1.setY(y);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			circle1.setRadius(circleRadius);
		}
		drawable.getPaint().setColor(mTManager.colorBGCircle());
		return circle1;
	}

	private void buildRegion() {
		String key = indexYear + ":" + indexMonth;
		if (!regionSelected.containsKey(key)) {
			regionSelected.put(key, new ArrayList<Region>());
		}
	}

	private void arrayClear(DPInfo[][] info) {
		for (DPInfo[] anInfo : info) {
			Arrays.fill(anInfo, null);
		}
	}

	private DPInfo[][] arrayCopy(DPInfo[][] src, DPInfo[][] dst) {
		for (int i = 0; i < dst.length; i++) {
			System.arraycopy(src[i], 0, dst[i], 0, dst[i].length);
		}
		return dst;
	}

	@SuppressLint("NewApi")
	public void defineRegion(final int x, final int y) {
        DPInfo[][] info = mCManager.obtainDPInfo(centerYear, centerMonth);
        Region[][] tmp;
        if (TextUtils.isEmpty(info[4][0].strG)) {
            tmp = monthRegionsFour;
        } else if (TextUtils.isEmpty(info[5][0].strG)) {
            tmp = monthRegionsFive;
        } else {
            tmp = monthRegionsSix;
        }
//        for (int i = 0; i < tmp.length; i++) {
		//5，6行切换导致的下标越界
		if(num >= tmp.length){
			num = tmp.length-1;
		}

            for (int j = 0; j < tmp[num].length; j++) {
                Region region = tmp[0][j];
                if (TextUtils.isEmpty(mCManager.obtainDPInfo(centerYear, centerMonth)[num][j].strG)) {
                    continue;
                }
                if (region.contains(x, y)) {
                    List<Region> regions = regionSelected.get(indexYear + ":" + indexMonth);
                    if (mDPMode == DPMode.SINGLE) {
                        cirApr.clear();
                        regions.add(region);
                        final String date = centerYear + "." + centerMonth + "." +
                                mCManager.obtainDPInfo(centerYear, centerMonth)[num][j].strG;
                        BGCircle circle = createCircle(
                                region.getBounds().centerX() + indexMonth * width,
                                region.getBounds().centerY() + indexYear * height);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            ValueAnimator animScale1 =
                                    ObjectAnimator.ofInt(circle, "radius", 0, circleRadius);
                            animScale1.setDuration(10);
                            animScale1.setInterpolator(decelerateInterpolator);
                            animScale1.addUpdateListener(scaleAnimationListener);
                            AnimatorSet animSet = new AnimatorSet();
                            animSet.playSequentially(animScale1);
                            animSet.addListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    if (null != onDatePickedListener) {
                                        onDatePickedListener.onDatePicked(date);
                                    }
                                    if(null != onWeekClick){
                                    	onWeekClick.onWeekDateClick(x,y);
                                    }
                                }
                            });
                            animSet.start();
                        }
                        cirApr.put(date, circle);
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                            invalidate();
                            if (null != onDatePickedListener) {
                                onDatePickedListener.onDatePicked(date);
                            }
                            if(null != onWeekClick){
                            	onWeekClick.onWeekDateClick(x,y);
                            }
                        }
                    } else if (mDPMode == DPMode.MULTIPLE) {
                        if (regions.contains(region)) {
                            regions.remove(region);
                        } else {
                            regions.add(region);
                        }
                        final String date = centerYear + "-" + centerMonth + "-" +
                                mCManager.obtainDPInfo(centerYear, centerMonth)[num][j].strG;
                        if (dateSelected.contains(date)) {
                            dateSelected.remove(date);
                            BGCircle circle = cirApr.get(date);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                ValueAnimator animScale = ObjectAnimator.ofInt(circle, "radius", circleRadius, 0);
                                animScale.setDuration(250);
                                animScale.setInterpolator(accelerateInterpolator);
                                animScale.addUpdateListener(scaleAnimationListener);
                                animScale.addListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        cirDpr.remove(date);
                                    }
                                });
                                animScale.start();
                                cirDpr.put(date, circle);
                            }
                            cirApr.remove(date);
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                                invalidate();
                            }
                        } else {
                            dateSelected.add(date);
                            BGCircle circle = createCircle(
                                    region.getBounds().centerX() + indexMonth * width,
                                    region.getBounds().centerY() + indexYear * height);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                ValueAnimator animScale1 =
                                        ObjectAnimator.ofInt(circle, "radius", 0, animZoomOut1);
                                animScale1.setDuration(250);
                                animScale1.setInterpolator(decelerateInterpolator);
                                animScale1.addUpdateListener(scaleAnimationListener);

                                ValueAnimator animScale2 =
                                        ObjectAnimator.ofInt(circle, "radius", animZoomOut1, animZoomIn1);
                                animScale2.setDuration(100);
                                animScale2.setInterpolator(accelerateInterpolator);
                                animScale2.addUpdateListener(scaleAnimationListener);

                                ValueAnimator animScale3 =
                                        ObjectAnimator.ofInt(circle, "radius", animZoomIn1, animZoomOut2);
                                animScale3.setDuration(150);
                                animScale3.setInterpolator(decelerateInterpolator);
                                animScale3.addUpdateListener(scaleAnimationListener);

                                ValueAnimator animScale4 =
                                        ObjectAnimator.ofInt(circle, "radius", animZoomOut2, circleRadius);
                                animScale4.setDuration(50);
                                animScale4.setInterpolator(accelerateInterpolator);
                                animScale4.addUpdateListener(scaleAnimationListener);

                                AnimatorSet animSet = new AnimatorSet();
                                animSet.playSequentially(animScale1, animScale2, animScale3, animScale4);
                                animSet.start();
                            }
                            cirApr.put(date, circle);
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                                invalidate();
                            }
                        }
                    } else if (mDPMode == DPMode.NONE) {
                        if (regions.contains(region)) {
                            regions.remove(region);
                        } else {
                            regions.add(region);
                        }
                        final String date = centerYear + "-" + centerMonth + "-" +
                                mCManager.obtainDPInfo(centerYear, centerMonth)[num][j].strG;
                        if (dateSelected.contains(date)) {
                            dateSelected.remove(date);
                        } else {
                            dateSelected.add(date);
                        }
                    }
                }
            }
//        }
    }
	
	@SuppressLint("NewApi")
	public void changeChooseDate(int x , int y){
        DPInfo[][] info = mCManager.obtainDPInfo(centerYear, centerMonth);
        Region[][] tmp;
        if (TextUtils.isEmpty(info[4][0].strG)) {
            tmp = monthRegionsFour;
        } else if (TextUtils.isEmpty(info[5][0].strG)) {
            tmp = monthRegionsFive;
        } else {
            tmp = monthRegionsSix;
        }
//        for (int i = 0; i < tmp.length; i++) {
            for (int j = 0; j < tmp[num].length; j++) {
                Region region = tmp[0][j];
                if (TextUtils.isEmpty(mCManager.obtainDPInfo(centerYear, centerMonth)[num][j].strG)) {
                    continue;
                }
                if (region.contains(x, y)) {
                    List<Region> regions = regionSelected.get(indexYear + ":" + indexMonth);
                    if (mDPMode == DPMode.SINGLE) {
                        cirApr.clear();
                        regions.add(region);
                        final String date = centerYear + "." + centerMonth + "." +
                                mCManager.obtainDPInfo(centerYear, centerMonth)[num][j].strG;
                        BGCircle circle = createCircle(
                                region.getBounds().centerX() + indexMonth * width,
                                region.getBounds().centerY() + indexYear * height);
                        WeekView.this.invalidate();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            ValueAnimator animScale1 =
                                    ObjectAnimator.ofInt(circle, "radius", 0, circleRadius);
                            animScale1.setDuration(10);
                            animScale1.setInterpolator(decelerateInterpolator);
                            animScale1.addUpdateListener(scaleAnimationListener);
                            animScale1.start();
                        }
                        cirApr.put(date, circle);
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                            invalidate();
                        }
                    }
                }
            }
	}

	private void computeDate() {
		rightYear = leftYear = centerYear;

		rightMonth = centerMonth + 1;
		leftMonth = centerMonth - 1;

		if (centerMonth == 12) {
			rightYear++;
			rightMonth = 1;
		}
		if (centerMonth == 1) {
			leftYear--;
			leftMonth = 12;
		}
		
//		if (null != onDateChangeListener) {
//			onDateChangeListener.onDateChange(centerYear, centerMonth);
//		}
	}


	public interface OnWeekViewChangeListener {
		void onWeekViewChange(boolean isForward);
	}
	
	public interface OnWeekDateClick{
		void onWeekDateClick(int x, int y);
	}

	private enum SlideMode {
		VER, HOR
	}

	private class BGCircle {
		private float x, y;
		private int radius;

		private ShapeDrawable shape;

		public BGCircle(ShapeDrawable shape) {
			this.shape = shape;
		}

		public float getX() {
			return x;
		}

		public void setX(float x) {
			this.x = x;
		}

		public float getY() {
			return y;
		}

		public void setY(float y) {
			this.y = y;
		}

		public int getRadius() {
			return radius;
		}

		public void setRadius(int radius) {
			this.radius = radius;
		}

		public ShapeDrawable getShape() {
			return shape;
		}

		public void setShape(ShapeDrawable shape) {
			this.shape = shape;
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private class ScaleAnimationListener implements
			ValueAnimator.AnimatorUpdateListener {
		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			WeekView.this.invalidate();
		}
	}

}
