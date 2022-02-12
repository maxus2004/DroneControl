package com.example.dronecontrol;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class StreamView extends View {
    IPConnection ipConnection;

    public StreamView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void setIpConnection(IPConnection ipConnection) {
        this.ipConnection = ipConnection;
    }

    Paint paint = new Paint();
    Rect src = new Rect();
    Rect dst = new Rect();

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (ipConnection != null) {
            Bitmap bitmap = ipConnection.getLatestFrame();
            if (bitmap != null) {
                int dstHeight = getHeight();
                int dstWidth = (int) (getHeight() * ((float) bitmap.getWidth() / bitmap.getHeight()));
                src.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
                dst.set((getWidth()-dstWidth)/2, 0, dstWidth+(getWidth()-dstWidth)/2, dstHeight);
                canvas.drawBitmap(bitmap, src, dst, paint);
            }
        }
        invalidate();
    }
}
