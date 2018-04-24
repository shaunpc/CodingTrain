package com.spc.codingtrain;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
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
        // First, create the overall RelativeLayout view group
        RelativeLayout rLayout = new RelativeLayout(this);
        RelativeLayout.LayoutParams rlParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        setContentView(rLayout, rlParams);
        rLayout.setBackgroundColor(Color.DKGRAY);

        // Next, add the Button at the bottom of the screen
        btnAction = new Button(this);
        btnAction.setId(R.id.action_button_id);
        btnAction.setText(R.string.action_button_start);
        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionButton();
            }
        });
        RelativeLayout.LayoutParams btnParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btnParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        btnParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        rLayout.addView(btnAction, btnParams);

        // Then, fill the rest with the MyCanvasView class
        myCanvasView = new MyCanvasView(this);
        myCanvasView.setId(R.id.canvas_view_id);
        myCanvasView.setBackgroundColor(Color.BLUE);
        RelativeLayout.LayoutParams cParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        cParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        cParams.addRule(RelativeLayout.ABOVE, btnAction.getId());
        rLayout.addView(myCanvasView, cParams);

        setContentView(rLayout);
        Log.i(TAG,"OnCreate completed");
    }

    void actionButton() {
        if (action.equals("START")) {
            btnAction.setText(R.string.action_button_done);
            action = "DONE";
            myCanvasView.startFrameUpdates();
        } else {
            myCanvasView.stopFrameUpdates();
            finish();
        }
    }

    class MyCanvasView extends View {
        private static final String TAG = "CANVASVIEW";
        Paint paintCanvas, paintText, paintPendulum, paintPendulumLine, paintAnchor, paintPath;
        boolean started = false;
        private Handler handler;
        private static final int FRAME_RATE = 20 ; // 50 frames per second

        float path[] = new float[1000];    // Holds the pendulum#2 path history
        float anchorX, anchorY;       //Anchor point
        float tmpX, tmpY;       //Temporary point as user moves the pendulum
        float m1, x1, y1;   // Pendulum #1 mass, coords
        double a1, a1_vel, a1_acc, r1;   // (angle, velocity, acceleration, length)
        float m2, x2, y2;   // Pendulum #2 mass, coords
        double a2, a2_vel, a2_acc, r2;   // (angle, velocity, acceleration, length)
        int movingPendulum = 0; // which pendulum is being moved by user (0=neither)

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

        private Runnable updateFrame = new Runnable() {
            @Override
            public void run() {
                handler.removeCallbacks(updateFrame);
                // KEY FUNCTION CALL - but only when window is ready!
                if (myCanvasView != null) {
                    if (myCanvasView.getWidth()!=0) {
                        updateMyCanvas();
                        myCanvasView.invalidate();
                    }
                }
                handler.postDelayed(updateFrame, FRAME_RATE);
            }
        };

        void startFrameUpdates() {
            handler = new Handler();
            handler.postDelayed(updateFrame, 500);
        }

        void stopFrameUpdates() {
            handler.removeCallbacks(updateFrame);
        }

        //TEMPLATE - KEY FUNCTION - apply any changes to each construct
        private void updateMyCanvas () {

            if (!started) {
                int smaller = Math.min(myCanvasView.getWidth(),myCanvasView.getHeight());
                anchorX = myCanvasView.getWidth()/2; // in middle
                anchorY = myCanvasView.getHeight()/18; // near top
                m1 = 30;    // Pendulum #1 mass
                r1 = smaller/3;    // Pendulum #1 length of string
                a1 = Math.PI/4;     // Pendulum #1 angle
                a1_vel = 0;
                m2 = 30;    // Pendulum #2 mass
                r2 = smaller/4;    // Pendulum #2 length of string
                a2 = Math.PI/8;     // Pendulum #2 angle
                a2_vel = 0;
                started=true;
            }

            //As long as the user isn't moving the pendulums around, then update the calcs...
            if (movingPendulum==0) {
                // STEP 1 = use real physics calcs to get angular acceleration
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

                // STEP 2 = Update the angle for each, based on velocity and acceleration of each
                a1_vel += a1_acc;
                a2_vel += a2_acc;
                a1 += a1_vel;
                a2 += a2_vel;
                a1_vel *= 0.998;  // slow things down a bit over time
                a2_vel *= 0.998;  // slow things down a bit over time

                // Calculate the (x,y) of pendulum#1 given current angle (adjust for anchor point)
                x1 = anchorX + (float) (r1 * Math.sin(a1));
                y1 = anchorY + (float) (r1 * Math.cos(a1));

                // Calculate the (x,y) of pendulum#2 given current angle (adjust for P#1 position)
                x2 = x1 + (float) (r2 * Math.sin(a2));
                y2 = y1 + (float) (r2 * Math.cos(a2));

                // Add to the historical path array [keeping most recent at the front]
                for (int i=path.length-4; i >= 0; i--) {
                    //if (path[i+3]+path[i+2]+path[i+1]+path[i]==0) {break;}
                    path[i+3]=path[i+1];
                    path[i+2]=path[i];
                }
                path[0]=x2;
                path[1]=y2;
            }
        }


        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            // Draw the two lines and the pendulums themselves...
            if (started) {
                // now clear background, and paint the pendulums
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
                    canvas.drawPoints(path,paintPath);
                }
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            int desiredWidth = 500;
            int desiredHeight = 500;

            int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);

            int width, height;

            // Measure Width
            if (widthMode == MeasureSpec.EXACTLY) {
                width = widthSize;
            } else if (widthMode == MeasureSpec.AT_MOST) {
                width = Math.min(desiredWidth, widthSize);
            } else {
                width = desiredWidth;
            }

            // Measure Height
            if (heightMode == MeasureSpec.EXACTLY) {
                height = heightSize;
            } else if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(desiredHeight, heightSize);
            } else {
                height = desiredHeight;
            }

            // MUST CALL THIS
            setMeasuredDimension(width, height);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (handler == null) {  // touched screen before "START" button
                    actionButton();     // then mimic the action button being pressed...
                }
                // Check if pressed on pendulum1 (check for double the radius to give chance to grab it!)
                if (Math.abs((event.getX() - x1)) <= (m1*2) && Math.abs((event.getY() - y1)) <= (m1*2)) {
                    Log.i(TAG, "TOUCH_DOWN on Pendulum#1");
                    movingPendulum = 1;
                }

                if (Math.abs((event.getX() - x2)) <= (m2*2) && Math.abs((event.getY() - y2)) <= (m2*2)) {
                    Log.i(TAG, "TOUCH_DOWN on Pendulum#2");
                    movingPendulum = 2;
                }
            } // DOWN

            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                if (movingPendulum != 0) {
                    tmpX = event.getX();
                    tmpY = event.getY();
                }
            } // MOVE

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (movingPendulum == 1) {
                    x1 = event.getX();
                    y1 = event.getY();
                    //reset the new angles and lengths formed - from the anchor point
                    a1 = Math.atan2(x1-anchorX, y1-anchorY);
                    a2 = Math.atan2(x2-x1, y2-y1);
                    r1 = Math.sqrt(Math.pow(x1-anchorX,2) + Math.pow(y1-anchorY,2));
                    r2 = Math.sqrt(Math.pow(x2-x1,2) + Math.pow(y2-y1,2));
                }
                if (movingPendulum == 2) {
                    x2 = event.getX();
                    y2 = event.getY();
                    //reset the new angle formed - from the first pendulum point
                    a2 = Math.atan2(x2-x1, y2-y1);
                    r2 = Math.sqrt(Math.pow(x2-x1,2) + Math.pow(y2-y1,2));
                }
                if (movingPendulum > 0) {
                    path = new float[1000];
                    movingPendulum = 0;
                };

            } // UP

            return true;
        }
    }
}