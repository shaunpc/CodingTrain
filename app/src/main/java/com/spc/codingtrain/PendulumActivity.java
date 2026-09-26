package com.spc.codingtrain;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;

public class PendulumActivity extends AppCompatActivity {

    private static final String TAG = "PENDULUM";
    private String action = "START";
    Button btnAction;
    MyCanvasView myCanvasView;
    private static final int GRAVITY = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        RelativeLayout rLayout = new RelativeLayout(this);
        RelativeLayout.LayoutParams rlParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        setContentView(rLayout, rlParams);
        rLayout.setBackgroundColor(Color.DKGRAY);

        btnAction = new Button(this);
        btnAction.setId(R.id.action_button_id);
        btnAction.setText(R.string.action_button_start);
        btnAction.setOnClickListener(v -> actionButton());
        RelativeLayout.LayoutParams btnParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btnParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        btnParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        rLayout.addView(btnAction, btnParams);

        myCanvasView = new MyCanvasView(this);
        myCanvasView.setId(R.id.canvas_view_id);
        myCanvasView.setBackgroundColor(Color.BLUE);
        RelativeLayout.LayoutParams cParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        cParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        cParams.addRule(RelativeLayout.ABOVE, btnAction.getId());
        rLayout.addView(myCanvasView, cParams);

        setContentView(rLayout);
        Log.i(TAG, "OnCreate completed");
    }

    void actionButton() {
        if ("START".equals(action)) {
            btnAction.setText(R.string.action_button_done);
            action = "DONE";
            myCanvasView.startFrameUpdates();
        } else {
            myCanvasView.stopFrameUpdates();
            finish();
        }
    }

    class MyCanvasView extends View {
        Paint paintCanvas, paintText, paintPendulum, paintPendulumLine, paintAnchor, paintPath;
        boolean started = false;
        private Handler handler;
        private static final int FRAME_RATE = 20;

        float[] path = new float[1000];
        float anchorX, anchorY;
        float tmpX, tmpY;
        float m1, x1, y1;
        double a1, a1_vel, a1_acc, r1;
        float m2, x2, y2;
        double a2, a2_vel, a2_acc, r2;
        int movingPendulum = 0;

        MyCanvasView(Context context) {
            super(context);
            paintCanvas = new Paint();
            paintCanvas.setStyle(Paint.Style.FILL);
            paintCanvas.setColor(Color.WHITE);
            paintAnchor = new Paint();
            paintAnchor.setStyle(Paint.Style.FILL);
            paintAnchor.setColor(Color.LTGRAY);
            paintPendulum = new Paint();
            paintPendulum.setStyle(Paint.Style.FILL);
            paintPendulum.setColor(Color.BLUE);
            paintPendulumLine = new Paint();
            paintPendulumLine.setStyle(Paint.Style.STROKE);
            paintPendulumLine.setStrokeWidth(5);
            paintPendulumLine.setColor(Color.DKGRAY);
            paintText = new Paint();
            paintText.setTextSize(25);
            paintText.setColor(Color.WHITE);
            paintPath = new Paint();
            paintPath.setStyle(Paint.Style.STROKE);
            paintPath.setStrokeWidth(5);
            paintPath.setAlpha(150);
            paintPath.setStrokeCap(Paint.Cap.ROUND);
            paintPath.setColor(Color.RED);
        }

        private final Runnable updateFrame = new Runnable() {
            @Override
            public void run() {
                if (handler != null) {
                    handler.removeCallbacks(updateFrame);
                    if (myCanvasView != null && myCanvasView.getWidth() != 0) {
                        updateMyCanvas();
                        myCanvasView.invalidate();
                    }
                    handler.postDelayed(updateFrame, FRAME_RATE);
                }
            }
        };

        void startFrameUpdates() {
            handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(updateFrame, 500);
        }

        void stopFrameUpdates() {
            if (handler != null) {
                handler.removeCallbacks(updateFrame);
            }
        }

        private void updateMyCanvas() {
            if (!started) {
                int smaller = Math.min(myCanvasView.getWidth(), myCanvasView.getHeight());
                anchorX = (float) (myCanvasView.getWidth() / 2.0);
                anchorY = (float) (myCanvasView.getHeight() / 18.0);
                m1 = 30;
                r1 = smaller / 3.0;
                a1 = Math.PI / 4;
                a1_vel = 0;
                m2 = 30;
                r2 = smaller / 4.0;
                a2 = Math.PI / 8;
                a2_vel = 0;
                started = true;
            }

            if (movingPendulum == 0) {
                double num1 = -GRAVITY * (2 * m1 + m2) * Math.sin(a1);
                double num2 = -m2 * GRAVITY * Math.sin(a1 - 2 * a2);
                double num3 = -2 * Math.sin(a1 - a2) * m2;
                double num4 = a2_vel * a2_vel * r2 + a1_vel * a1_vel * r1 * Math.cos(a1 - a2);
                double denom = r1 * (2 * m1 + m2 - m2 * Math.cos(2 * a1 - 2 * a2));
                a1_acc = (num1 + num2 + num3 * num4) / denom;

                num1 = 2 * Math.sin(a1 - a2);
                num2 = (a1_vel * a1_vel * r1 * (m1 + m2));
                num3 = GRAVITY * (m1 + m2) * Math.cos(a1);
                num4 = a2_vel * a2_vel * r2 * m2 * Math.cos(a1 - a2);
                denom = r2 * (2 * m1 + m2 - m2 * (float) Math.cos(2 * a1 - 2 * a2));
                a2_acc = (num1 * (num2 + num3 + num4)) / denom;

                a1_vel += a1_acc;
                a2_vel += a2_acc;
                a1 += a1_vel;
                a2 += a2_vel;
                a1_vel *= 0.998;
                a2_vel *= 0.998;

                x1 = anchorX + (float) (r1 * Math.sin(a1));
                y1 = anchorY + (float) (r1 * Math.cos(a1));

                x2 = x1 + (float) (r2 * Math.sin(a2));
                y2 = y1 + (float) (r2 * Math.cos(a2));

                for (int i = path.length - 4; i >= 0; i--) {
                    path[i + 3] = path[i + 1];
                    path[i + 2] = path[i];
                }
                path[0] = x2;
                path[1] = y2;
            }
        }


        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            super.onDraw(canvas);

            if (started) {
                canvas.drawPaint(paintCanvas);
                canvas.drawRect(anchorX - 10, anchorY - 10, anchorX + 10, anchorY + 10, paintAnchor);
                canvas.drawLine(anchorX, anchorY, x1, y1, paintPendulumLine);
                canvas.drawCircle(x1, y1, m1, paintPendulum);
                canvas.drawLine(x1, y1, x2, y2, paintPendulumLine);
                canvas.drawCircle(x2, y2, m2, paintPendulum);

                if (movingPendulum != 0) {
                    paintPendulum.setAlpha(120);
                    paintPendulumLine.setAlpha(120);
                    if (movingPendulum == 1) {
                        canvas.drawCircle(tmpX, tmpY, m1, paintPendulum);
                        canvas.drawLine(anchorX, anchorY, tmpX, tmpY, paintPendulumLine);
                        canvas.drawLine(tmpX, tmpY, x2, y2, paintPendulumLine);
                    } else {
                        canvas.drawCircle(tmpX, tmpY, m2, paintPendulum);
                        canvas.drawLine(x1, y1, tmpX, tmpY, paintPendulumLine);
                    }
                    paintPendulum.setAlpha(255);
                    paintPendulumLine.setAlpha(255);
                }

                if (movingPendulum == 0) {
                    canvas.drawPoints(path, paintPath);
                }
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int desiredWidth = 500;
            int desiredHeight = 500;

            int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);

            int width, height;

            if (widthMode == MeasureSpec.EXACTLY) {
                width = widthSize;
            } else if (widthMode == MeasureSpec.AT_MOST) {
                width = Math.min(desiredWidth, widthSize);
            } else {
                width = desiredWidth;
            }

            if (heightMode == MeasureSpec.EXACTLY) {
                height = heightSize;
            } else if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(desiredHeight, heightSize);
            } else {
                height = desiredHeight;
            }

            setMeasuredDimension(width, height);
        }

        @Override
        public boolean performClick() {
            return super.performClick();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                performClick();
                if (handler == null) {
                    actionButton();
                }
                if (Math.abs((event.getX() - x1)) <= (m1 * 2) && Math.abs((event.getY() - y1)) <= (m1 * 2)) {
                    Log.i(TAG, "TOUCH_DOWN on Pendulum#1");
                    movingPendulum = 1;
                }

                if (Math.abs((event.getX() - x2)) <= (m2 * 2) && Math.abs((event.getY() - y2)) <= (m2 * 2)) {
                    Log.i(TAG, "TOUCH_DOWN on Pendulum#2");
                    movingPendulum = 2;
                }
            }

            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                if (movingPendulum != 0) {
                    tmpX = event.getX();
                    tmpY = event.getY();
                }
            }

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (movingPendulum == 1) {
                    x1 = event.getX();
                    y1 = event.getY();
                    a1 = Math.atan2(x1 - anchorX, y1 - anchorY);
                    a2 = Math.atan2(x2 - x1, y2 - y1);
                    r1 = Math.sqrt(Math.pow(x1 - anchorX, 2) + Math.pow(y1 - anchorY, 2));
                    r2 = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
                }
                if (movingPendulum == 2) {
                    x2 = event.getX();
                    y2 = event.getY();
                    a2 = Math.atan2(x2 - x1, y2 - y1);
                    r2 = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
                }
                if (movingPendulum > 0) {
                    path = new float[1000];
                    movingPendulum = 0;
                }
            }

            return true;
        }
    }
}
