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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class CirclePackingActivity extends AppCompatActivity {

    private static final String TAG = "CIRCLEPACK";
    private String action = "START";
    Button btnAction;
    MyCanvasView myCanvasView;

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
        Log.i(TAG, "OnCreate completed");
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
        Paint paintCanvas, paintText;
        boolean started = false;
        private Handler handler;
        private static final int FRAME_RATE = 20; // 50 frames per second
        List<Circle> circles = new ArrayList<>();
        String msg;

        MyCanvasView(Context context) {
            super(context);
            paintCanvas = new Paint();
            paintCanvas.setStyle(Paint.Style.STROKE);
            paintCanvas.setColor(Color.BLACK);
            paintText = new Paint();
            paintText.setTextSize(25);
            paintText.setColor(Color.WHITE);
        }

        private Runnable updateFrame = new Runnable() {
            @Override
            public void run() {
                handler.removeCallbacks(updateFrame);
                // KEY FUNCTION CALL - but only when window is ready!
                if (myCanvasView != null) {
                    if (myCanvasView.getWidth() != 0) {
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
        private void updateMyCanvas() {

            Random r = new Random();
            int maxX = getWidth();
            int maxY = getHeight();

            if (!started) {
                started = true;
            } else {

                // add a new random circle every frame updates
                createNewCircle(r.nextInt(maxX), r.nextInt(maxY));

                // perform updates on all circles
                for (Circle c : circles) {
                    // Log.i(TAG, "Circle "+c.x+","+c.y+" "+c.r);
                    if (c.growing) {
                        // Log.i(TAG,"   Growing...");
                        c.grow();
                        if (c.edges()) {  // Check if reached edges
                            // Log.i(TAG,"   Reached edges...");
                            c.growing = false;
                        }
                        // Log.i(TAG,"   Checking overlap...");
                        if (!checkNoOverlap(c)) {
                                c.growing = false;
                            }
                    }
                }
            }
        }


        void createNewCircle (int x, int y) {
            Circle newCircle = new Circle(x,y,1);
            if (checkNoOverlap(newCircle)) {
                circles.add(newCircle);
            }
        }

        boolean checkNoOverlap (Circle checkCircle) {
            boolean noOverlap = true;
            for (Circle other : circles) {
                if (checkCircle.overlapping(other)) {
                    noOverlap = false;
                    break;
                }
            }
            return noOverlap;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            // Draw the two lines and the pendulums themselves...
            if (started) {
                // now clear background
                canvas.drawPaint(paintCanvas);

                // show any circles
                // int cCount = 2;
                for (Circle c : circles) {
                    c.show(canvas);
                    // msg = "  Circle:("+c.x+","+c.y+") radius:" + c.r + " Growing:"+c.growing;
                    // canvas.drawText(msg, 20, cCount * 25, paintText);
                    //cCount++;
                }

                // display the info
                msg = "Circles:" + circles.size();
                canvas.drawText(msg, 20, 25, paintText);

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

                // See if we can create a new circle where pressed...
                createNewCircle((int) event.getX(),(int) event.getY());

            } // DOWN

            return true;
        }
    }

    class Circle {
        float x;
        float y;
        float r;
        boolean growing;
        MyColor colour;
        Paint paintCircle;

        Circle (float x, float y, float r) {
            this.x = x;
            this.y = y;
            this.r = r;
            this.growing = true;
            this.colour = new MyColor();  // get a random colour
            this.paintCircle = new Paint();
            this.paintCircle.setStyle(Paint.Style.STROKE);
            this.paintCircle.setStrokeWidth(5);
            this.paintCircle.setARGB(255, colour.r, colour.g, colour.b);
        }

        void show(Canvas canvas) {
            canvas.drawCircle(this.x, this.y, this.r, this.paintCircle);
        }

        void grow() {
            if (this.growing) {
                this.r++;
            }
        }

        boolean edges() {  // return true if circle touches sides
            return (this.x-this.r < 0 || this.y-this.r < 0 ||
                    this.x+this.r > myCanvasView.getWidth() ||
                    this.y+this.r > myCanvasView.getHeight());
        }

        boolean overlapping(Circle other) {  // return true if circle overlaps with other circle passed
            if (this.equals(other)) {  // if compared against itself, then not overlapping...
                return false;
            }
            // otherwise find the distance between the (x,y)'s and check against the two radius
            float distance =  (float) Math.sqrt(Math.pow(this.x - other.x, 2)+Math.pow(this.y - other.y, 2));
            float result = distance - this.r - other.r;
            // Log.i(TAG,"Overlapping result is "+result+ " returning"+(distance - this.r - other.r < 4));
            return (result < 4);
        }


    }
}
