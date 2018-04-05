package com.spc.codingtrain;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**

 * Created by shaun on 26/03/18- ass part of basic Canvas setup...
 */


public class MyCanvasView extends View {

    Paint paintCanvas, paintCircle, paintRect, paintText;
    int initX = 200, initY = 200, radius=100, rectWidth =500, rectHeight=400;
    public MyCanvasView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init () {
        paintCanvas = new Paint();
        paintCanvas.setStyle(Paint.Style.FILL);
        paintCanvas.setColor(Color.WHITE);

        paintCircle = new Paint();
        paintCircle.setAntiAlias(true);
        paintCircle.setColor(Color.BLUE);

        paintRect = new Paint();
        paintRect.setAntiAlias(true);
        paintRect.setColor(Color.RED);

        paintText = new Paint();
        paintText.setStyle(Paint.Style.FILL);
        paintText.setColor(Color.GREEN);
        paintText.setTextSize(40);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.drawPaint(paintCanvas);

        canvas.drawCircle(initX, initY, radius, paintCircle);

        canvas.drawRect(initX,initY+300,rectWidth+radius,initY+rectHeight,paintRect);

        canvas.rotate(-45);
        canvas.drawText("Well, hello there!", initX, initY+600, paintText );
       // canvas.restore();

    }
}
