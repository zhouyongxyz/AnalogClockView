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
import android.os.Handler;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.AttributeSet;
import android.view.View;

import java.util.HashMap;
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
    }

    private void onTimeChanged() {
        mCalendar.setToNow();

        int hour = mCalendar.hour;
        int minute = mCalendar.minute;
        int second = mCalendar.second;
        mSeconds = second;
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
