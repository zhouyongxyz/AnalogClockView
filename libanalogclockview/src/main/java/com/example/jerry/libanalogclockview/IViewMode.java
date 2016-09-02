package com.example.jerry.libanalogclockview;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by zhouyong on 9/1/16.
 */
public interface IViewMode {
    void drawDialPlate(Canvas canvas,int x,int y,int width,int height);
    void setTime(float hour,float minutes,float seconds);
    void drawHour(Canvas canvas,int x,int y,int width,int height);
    void drawMinute(Canvas canvas,int x,int y,int width,int height);
    void drawSecond(Canvas canvas,int x,int y,int width,int height);
}
