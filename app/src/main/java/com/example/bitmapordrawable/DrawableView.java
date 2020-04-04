package com.example.bitmapordrawable;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class DrawableView extends View {

    private Drawable drawable = new MeshDrawable();

    public DrawableView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        /**
         * 绘制之前，先要设定边界
         */
        drawable.setBounds(50, 50, getWidth() - 50, getHeight() - 50);
        drawable.draw(canvas);
    }
}
