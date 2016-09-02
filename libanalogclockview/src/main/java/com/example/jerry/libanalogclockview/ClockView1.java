package com.example.jerry.libanalogclockview;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * Created by zhouyong on 9/1/16.
 */
public class ClockView1 implements IViewMode {
    private int mStyleColor;
    private int mSecondColor;
    float mHour;
    float mMinutes;
    float mSeconds;

    public ClockView1(int styleColor,int secondColor) {
        mStyleColor = styleColor;
        mSecondColor = secondColor;
    }

    @Override
    public void drawDialPlate(Canvas canvas, int x, int y, int width, int height) {
        float radius = 10.0f;
        Paint paint = new Paint();
        paint.setARGB(255,255, 255,255);
        width -= 30;
        int N = 60;
        double delta = 2*Math.PI/N;
        float cx,cy;
        // draw points
        for(int i = 0; i < N; i++)
        {
            cx = width/2*(float)Math.cos(delta*i)+x;
            cy = width/2*(float)Math.sin(delta*i)+y;
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
        int offset = width/2 - 50;
        x = x-15;
        y = y+15;

        Paint mTextPaint = new Paint(Paint.LINEAR_TEXT_FLAG);
        mTextPaint.setColor(mStyleColor);
        mTextPaint.setTextSize(40.0F);
        canvas.drawText("12", x, y-offset, mTextPaint);
        canvas.drawText("3", x+offset, y, mTextPaint);
        canvas.drawText("6", x, y+offset, mTextPaint);
        canvas.drawText("9", x-offset, y, mTextPaint);
    }

    @Override
    public void setTime(float hour,float minutes,float seconds) {
        mHour = hour;
        mMinutes = minutes;
        mSeconds = seconds;
    }

    @Override
    public void drawHour(Canvas canvas, int x, int y, int width, int height) {
        float angle = mHour / 12.0f * 360.0f;
        width -= 30;
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        canvas.save();
        canvas.rotate(angle-90, x, y);
        paint.setARGB(255, 255, 255, 255);
        RectF rect = new RectF(x+20, y - 5, x + 3*width/10, y + 5);
        canvas.drawRoundRect(rect, 10, 10, paint);
        canvas.restore();
    }

    @Override
    public void drawMinute(Canvas canvas, int x, int y, int width, int height) {
        width -= 30;
        float angle = mMinutes / 60.0f * 360.0f;
        Paint paint = new Paint();
        canvas.save();
        canvas.rotate(angle-90, x, y);
        paint.setARGB(255, 255, 255, 255);
        RectF rect = new RectF(x+20, y - 5, x + 4*width/10, y + 5);
        canvas.drawRoundRect(rect, 10, 10, paint);
        canvas.restore();
    }

    @Override
    public void drawSecond(Canvas canvas, int x, int y, int width, int height) {
        width -= 30;
        Paint paint = new Paint();
        float cx,cy;
        float radius = 12.0f;
        //float angle = mSeconds / 60.0f *2*(float)Math.PI;
        paint.setARGB(255,210,105,30);
        //cx = width/2*(float)Math.cos(angle)+x;
        //cy = width/2*(float)Math.sin(angle)+y;
        //canvas.drawCircle(cx, cy, radius,paint);
        float angle = mSeconds / 60.0f * 360.0f;
        canvas.save();
        canvas.rotate(angle, x, y);
        canvas.drawCircle(x, y - width/2, radius,paint);
        canvas.restore();
    }
}
