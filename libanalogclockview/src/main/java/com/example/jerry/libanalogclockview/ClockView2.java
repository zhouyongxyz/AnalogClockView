package com.example.jerry.libanalogclockview;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Created by zhouyong on 9/1/16.
 */
public class ClockView2 implements IViewMode {
    private int mStyleColor;
    private int mSecondColor;
    float mHour;
    float mMinutes;
    float mSeconds;

    public ClockView2(int styleColor,int secondColor) {
        mStyleColor = styleColor;
        mSecondColor = secondColor;
    }

    @Override
    public void drawDialPlate(Canvas canvas, int x, int y, int width, int height) {
        width -= 10;
        height -= 10;
        Paint degreePaint = new Paint();
        degreePaint.setAntiAlias(true);
        Paint paintCircle = new Paint();
        paintCircle.setStyle(Paint.Style.STROKE);
        paintCircle.setAntiAlias(true);
        paintCircle.setStrokeWidth(width * 0.005f);
        canvas.drawCircle(x, y, width/2, paintCircle);
        float lineLength = 0;
        for (int i = 0; i < 60; i++) {
            if (i % 5 == 0) {
                degreePaint.setStrokeWidth(width/120);
                lineLength = width/15;
            } else {
                degreePaint.setStrokeWidth(width*0.005f);
                lineLength = width/30;
            }
            //每旋转60度进行一次刻画
            canvas.drawLine(x, width * 0.005f, y, lineLength, degreePaint);
            canvas.rotate(360 / 60, x, y);
        }
        degreePaint.setTextSize(width*0.05f);
        for (int i = 1; i <= 12; i++) {
            float angle = i / 12.0f * 360.0f;
            canvas.save();
            canvas.rotate(angle, x, y);
            canvas.drawText(Integer.toString(i), x -15, y - height*0.35f, degreePaint);
            canvas.restore();
        }
        //draw point
        canvas.drawCircle(x, y, width/120, paintCircle);
    }

    @Override
    public void setTime(float hour,float minutes,float seconds) {
        mHour = hour;
        mMinutes = minutes;
        mSeconds = seconds;
    }

    @Override
    public void drawHour(Canvas canvas, int x, int y, int width, int height) {
        Paint paint = new Paint();
        paint.setStrokeWidth(width * 0.005f);
        float angle = mHour / 12.0f * 360.0f;
        canvas.save();
        canvas.rotate(angle, x, y);
        canvas.drawLine(x, y, x, y-height*0.25f, paint);
        canvas.restore();
    }

    @Override
    public void drawMinute(Canvas canvas, int x, int y, int width, int height) {
        Paint paint = new Paint();
        paint.setStrokeWidth(width * 0.005f);
        float angle = mMinutes / 60.0f * 360.0f;
        canvas.save();
        canvas.rotate(angle, x, y);
        canvas.drawLine(x, y, x, y-height*0.35f, paint);
        canvas.restore();
    }

    @Override
    public void drawSecond(Canvas canvas, int x, int y, int width, int height) {
        Paint paint = new Paint();
        paint.setStrokeWidth(width * 0.005f);
        paint.setColor(Color.RED);
        float angle = mSeconds / 60.0f * 360.0f;
        canvas.save();
        canvas.rotate(angle, x, y);
        canvas.drawLine(x, y, x, y-height*0.45f, paint);
        canvas.restore();
    }
}
