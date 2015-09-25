package com.randomcorp.sujay.extend.Utils;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;

public class RoundedCharacterDrawable extends ColorDrawable {

    private final char character;
    private final Paint textPaint;
    private final Paint circlePaint;
    private static final float SHADE_FACTOR = 0.9f;

    public RoundedCharacterDrawable(char character, int color) {
        super(Color.TRANSPARENT);
        this.character = character;
        this.textPaint = new Paint();
        this.circlePaint = new Paint();

        // text paint settings
        textPaint.setColor(Color.WHITE);
        textPaint.setAntiAlias(true);
        textPaint.setFakeBoldText(true);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextAlign(Paint.Align.CENTER);

        circlePaint.setColor(color);
        circlePaint.setStyle(Paint.Style.FILL);
        circlePaint.setStrokeWidth(1);
        circlePaint.setAntiAlias(true);
    }

    private int getDarkerShade(int color) {
        return Color.rgb((int)(SHADE_FACTOR * Color.red(color)),
                (int)(SHADE_FACTOR * Color.green(color)),
                (int)(SHADE_FACTOR * Color.blue(color)));
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        RectF rect = new RectF(getBounds());
        canvas.drawRoundRect(rect,rect.width()/2,rect.height()/2,circlePaint);


        // draw text
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        textPaint.setTextSize(height / 2);
        canvas.drawText(String.valueOf(character), width/2, height/2 - ((textPaint.descent() + textPaint.ascent()) / 2) , textPaint);
    }

    @Override
    public void setAlpha(int alpha) {
        textPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        textPaint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}