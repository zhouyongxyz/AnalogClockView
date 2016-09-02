package com.example.jerry.libanalogclockview;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.RectF;
import android.os.Handler;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.AttributeSet;
import android.view.View;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * This widget display an analogic clock with two hands for hours and
 * minutes.
 */
public class AnalogClockView extends View {

    private final static String TAG = "AnalogClockView";

    private final static int DEFAULT_STYLE_COLOR = Color.WHITE;
    private final static int DEFAULT_SECOND_COLOR = Color.argb(255,210,105,30);
    private final static int VIEW_MODE_1 = 1;
    private final static int VIEW_MODE_2 = 2;
    private Time mCalendar;
    private final int mAmPmDayColor;
    private Paint mTextPaint;

    private boolean mAttached;

    private final Handler mHandler = new Handler();
    private float mSeconds;
    private float mMinutes;
    private float mHour;
    private boolean mChanged;
    private final Context mContext;
    private String mTimeZoneId;
    private boolean mNoSeconds = false;

    private final int mStyleColor;
    private final int mSecondColor;
    private int mViewMode = VIEW_MODE_1;
    private IViewMode mCurrentView;
    private Map<Integer,IViewMode> mViewList;


    public AnalogClockView(Context context) {
        this(context, null);
    }

    public AnalogClockView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnalogClockView(Context context, AttributeSet attrs,
                       int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        Resources r = mContext.getResources();

        mAmPmDayColor = r.getColor(R.color.ts_day_analog_ampm_color);
        mTextPaint = new Paint(Paint.LINEAR_TEXT_FLAG);
        mTextPaint.setColor(mAmPmDayColor);
        mTextPaint.setTextSize(40.0F);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AnalogClockView);
        mStyleColor = a.getColor(R.styleable.AnalogClockView_acv_style_color, DEFAULT_STYLE_COLOR);
        mSecondColor = a.getColor(R.styleable.AnalogClockView_acv_second_color, DEFAULT_SECOND_COLOR);

        mCalendar = new Time();
        mViewList = new HashMap<Integer, IViewMode>();
        mCurrentView = getCurrentView();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (!mAttached) {
            mAttached = true;
            IntentFilter filter = new IntentFilter();

            filter.addAction(Intent.ACTION_TIME_TICK);
            filter.addAction(Intent.ACTION_TIME_CHANGED);
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);

            getContext().registerReceiver(mIntentReceiver, filter, null, mHandler);
        }

        // NOTE: It's safe to do these after registering the receiver since the receiver always runs
        // in the main thread, therefore the receiver can't run before this method returns.

        // The time zone may have changed while the receiver wasn't registered, so update the Time
        mCalendar = new Time();

        // Make sure we update to the current time
        onTimeChanged();

        // tick the seconds
        post(mClockTick);

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAttached) {
            getContext().unregisterReceiver(mIntentReceiver);
            removeCallbacks(mClockTick);
            mAttached = false;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mChanged = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        boolean changed = mChanged;
        if (changed) {
            mChanged = false;
        }

        int availableWidth = getWidth();
        int availableHeight = getHeight();

        int x = availableWidth / 2;
        int y = availableHeight / 2;

        int w = availableWidth;
        int h = availableHeight;

        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG));

        if(mCurrentView == null) {
            return;
        }

        mCurrentView.drawDialPlate(canvas, x, y, w, h);
        mCurrentView.setTime(mHour,mMinutes,mSeconds);
        mCurrentView.drawHour(canvas, x, y, w, h);
        mCurrentView.drawMinute(canvas, x, y, w, h);
        mCurrentView.drawSecond(canvas, x, y, w, h);
        //drawDialPlate(canvas, x, y, w-30, h-30);
        /*if (mDotRadius > 0f && mDotPaint != null) {
            canvas.drawCircle(x, y - (h / 2) + mDotOffset, mDotRadius, mDotPaint);
        }

        if(getIsEnglish()){
            canvas.drawText(getAmPmString(), 0.9f * x, 1.67f * y, mTextPaint);
        }else if(IsChinese()){
            canvas.drawText(getAmPmString(), 0.88f * x, 1.67f * y, mTextPaint);
        }else{
            canvas.drawText(getAmPmString(), 0.76f * x, 1.67f * y, mTextPaint);
        }

        drawDialElements(canvas, x, y,w-30,h-30, mHour / 12.0f * 360.0f, 1);
        drawDialElements(canvas, x, y,w-30,h-30, mMinutes / 60.0f * 360.0f, 2);
        if (!mNoSeconds) {
            drawDialElements(canvas,x,y,w-30,h-30,mSeconds / 60.0f *2*(float)Math.PI,3);
        }
        */
    }

    private void onTimeChanged() {
        mCalendar.setToNow();

        if (mTimeZoneId != null) {
            mCalendar.switchTimezone(mTimeZoneId);
        }

        int hour = mCalendar.hour;
        int minute = mCalendar.minute;
        int second = mCalendar.second;
        //      long millis = System.currentTimeMillis() % 1000;

        mSeconds = second;//(float) ((second * 1000 + millis) / 166.666);
        mMinutes = minute + second / 60.0f;
        mHour = hour + mMinutes / 60.0f;
        mChanged = true;

        updateContentDescription(mCalendar);
    }

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                String tz = intent.getStringExtra("time-zone");
                mCalendar = new Time(TimeZone.getTimeZone(tz).getID());
            }
            onTimeChanged();
            invalidate();
        }
    };

    private final Runnable mClockTick = new Runnable () {

        @Override
        public void run() {
            onTimeChanged();
            invalidate();
            AnalogClockView.this.postDelayed(mClockTick, 1000);
        }
    };

    private void updateContentDescription(Time time) {
        final int flags = DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_24HOUR;
        String contentDescription = DateUtils.formatDateTime(mContext,
                time.toMillis(false), flags);
        setContentDescription(contentDescription);
    }

    public void setTimeZone(String id) {
        mTimeZoneId = id;
        onTimeChanged();
    }

    public void enableSeconds(boolean enable) {
        mNoSeconds = !enable;
    }

    private boolean getIsEnglish()
    {
        return Locale.getDefault().getLanguage().equals(Locale.ENGLISH.getLanguage());
    }
    private String getAmPmString(){
        String[] ampms = new DateFormatSymbols().getAmPmStrings();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int ampm = calendar.get(Calendar.AM_PM);
        return (ampm == 0)?ampms[0]:ampms[1];
    }

    private boolean IsChinese()
    {
        return Locale.getDefault().getLanguage().equals(Locale.CHINESE.getLanguage());
    }

    //draw the Dial Plate
    private void drawDialPlate(Canvas canvas,int x,int y,int w,int h)
    {
        float radius = 10.0f;
        Paint paint = new Paint();
        paint.setARGB(255,255, 255,255);

        int N = 60;
        double delta = 2*Math.PI/N;
        float cx,cy;
        // draw points
        for(int i = 0; i < N; i++)
        {
            cx = w/2*(float)Math.cos(delta*i)+x;
            cy = w/2*(float)Math.sin(delta*i)+y;
            if(i%5==0)
            {
                paint.setARGB(255,255, 255,255);
            }
            else
            {
                paint.setARGB(40,255, 255,255);
            }
            canvas.drawCircle(cx, cy, radius,paint);
        }
        paint.setARGB(255,255, 255,255);
        canvas.drawCircle(x,y, radius, paint);
        // draw text
        int offset = w/2 - 50;
        x = x-15;
        y = y+15;
        canvas.drawText("12", x, y-offset, mTextPaint);
        canvas.drawText("3", x+offset, y, mTextPaint);
        canvas.drawText("6", x, y+offset, mTextPaint);
        canvas.drawText("9", x-offset, y, mTextPaint);
    }

    private void drawDialElements(Canvas canvas,int x,int y,int w,int h,float angle ,int flag)
    {
        float radius = 12.0f;
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        float cx,cy;
        if(flag == 3)
        {
            paint.setARGB(255,210,105,30);
            cx = w/2*(float)Math.cos(angle)+x;
            cy = w/2*(float)Math.sin(angle)+y;
            canvas.drawCircle(cx, cy, radius,paint);
        }
        else if(flag == 2)
        {
            canvas.save();
            canvas.rotate(angle-90, x, y);
            paint.setARGB(255, 255, 255, 255);
            RectF rect = new RectF(x+20, y - 5, x + 4*w/10, y + 5);
            canvas.drawRoundRect(rect, 10, 10, paint);
            canvas.restore();
        }
        else if(flag == 1)
        {
            canvas.save();
            canvas.rotate(angle-90, x, y);
            paint.setARGB(255, 255, 255, 255);
            RectF rect = new RectF(x+20, y - 5, x + 3*w/10, y + 5);
            canvas.drawRoundRect(rect, 10, 10, paint);
            canvas.restore();
        }
    }

    public void setViewMode(int mode) {
        if(mode < 1 || mode > 2) {
            return;
        }
        mViewMode = mode;
        mCurrentView = getCurrentView();
    }

    private IViewMode getCurrentView() {
        IViewMode viewMode = mViewList.get(mViewMode);
        if(viewMode == null) {
            switch (mViewMode) {
                case 1:viewMode = new ClockView1(mStyleColor,mSecondColor);break;
                case 2:viewMode = new ClockView2(mStyleColor,mSecondColor);break;
            }
            mViewList.put(mViewMode,viewMode);
        }
        return viewMode;
    }
}
